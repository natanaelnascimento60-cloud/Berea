package com.example.model

import java.text.Normalizer
import java.util.Locale

data class MorphologyAttribute(
    val label: String,
    val value: String
)

object MorphologyDecoder {

    private val diacriticsRegex = Regex("[\\u0300-\\u036f\\u0591-\\u05C7]+")
    private val trimPunctuationRegex = Regex("^[\\s.,;··׃־:!?()\\[\\]\"'«»’‘]+|[\\s.,;··׃־:!?()\\[\\]\"'«»’‘]+$")

    internal val hebrewInseparablePrefixes = setOf('ו', 'ה', 'ב', 'ל', 'כ')

    fun stripPunctuation(word: String): String {
        return trimPunctuationRegex.replace(word.trim(), "")
    }

    fun normalizeBiblicalToken(word: String): String {
        val cleaned = stripPunctuation(word)
        val decomposed = Normalizer.normalize(cleaned, Normalizer.Form.NFD)
        return diacriticsRegex.replace(decomposed, "")
            .lowercase(Locale.ROOT)
            .replace("ς", "σ")
    }

    fun findMatchingToken(rawWord: String, tokens: List<InterlinearToken>): InterlinearToken? {
        if (tokens.isEmpty()) return null
        val clean = stripPunctuation(rawWord)
        if (clean.isBlank()) return null

        // 1. Exact match on originalWord
        tokens.firstOrNull { stripPunctuation(it.originalWord) == clean }?.let { return it }

        // 2. Normalized match computed in a single pass
        val norm = normalizeBiblicalToken(clean)
        val withoutPrefix = if (norm.length > 2 && norm.first() in hebrewInseparablePrefixes) {
            norm.drop(1)
        } else {
            null
        }

        var lemmaMatch: InterlinearToken? = null
        var prefixMatch: InterlinearToken? = null
        for (token in tokens) {
            val tokNorm = normalizeBiblicalToken(token.originalWord)
            if (tokNorm == norm) return token
            val lemNorm = if (token.lemma.isNotBlank()) normalizeBiblicalToken(token.lemma) else ""
            if (lemmaMatch == null && lemNorm == norm) {
                lemmaMatch = token
            }
            if (withoutPrefix != null && prefixMatch == null &&
                (tokNorm == withoutPrefix || lemNorm == withoutPrefix)
            ) {
                prefixMatch = token
            }
        }
        return lemmaMatch ?: prefixMatch
    }

    fun inferLexicalAttributes(entry: LexiconEntry): List<MorphologyAttribute> {
        val sampleForm = entry.sampleOccurrences.firstOrNull()?.grammarForm.orEmpty()
        val sampleCode = sampleForm.substringBefore(" (").trim()
        if (sampleCode.isNotBlank()) {
            val decoded = decode(sampleCode, entry.language)
            if (decoded.isNotEmpty()) return decoded
        }
        val gloss = entry.shortGloss.lowercase(Locale.ROOT)
        val word = entry.originalWord.trim()
        val inferredClass = when {
            entry.language == OriginalLanguage.GREEK && (word.endsWith("ω") || word.endsWith("μαι")) -> "Verbo (forma lexical)"
            gloss.startsWith("criar") || gloss.startsWith("pastorear") || gloss.startsWith("salvar") ||
                gloss.startsWith("guardar") || gloss.startsWith("armar") || gloss.startsWith("subsistir") -> "Verbo (forma lexical)"
            else -> "Substantivo / Termo Lexical"
        }
        return listOf(
            MorphologyAttribute("Classe", inferredClass),
            MorphologyAttribute("Idioma", entry.language.label),
            MorphologyAttribute("Lema", entry.originalWord)
        )
    }

    fun decode(code: String, language: OriginalLanguage): List<MorphologyAttribute> {
        val raw = code.trim()
        if (raw.isBlank()) return emptyList()

        return if (language == OriginalLanguage.HEBREW || raw.contains("Qal", ignoreCase = true) ||
            raw.contains("Piel", ignoreCase = true) || raw.contains("Hiphil", ignoreCase = true) ||
            raw.contains("Niphal", ignoreCase = true) || raw.contains("DirObj", ignoreCase = true) ||
            raw.endsWith("ms") || raw.endsWith("fs") || raw.endsWith("mp") || raw.endsWith("fp") ||
            isStepHebrewMorph(raw)
        ) {
            decodeHebrew(raw)
        } else {
            decodeGreek(raw)
        }
    }

    private fun isStepHebrewMorph(raw: String): Boolean {
        val firstSeg = raw.substringBefore("/")
        return firstSeg.length >= 2 && (firstSeg[0] == 'H' || firstSeg[0] == 'A') &&
            firstSeg[1] in setOf('V', 'N', 'A', 'P', 'R', 'C', 'T', 'D', 'S', 'c')
    }

    private fun decodeGreek(code: String): List<MorphologyAttribute> {
        val parts = code.split("-")
        val pos = parts.getOrNull(0).orEmpty()
        val detail1 = parts.getOrNull(1).orEmpty()
        val detail2 = parts.getOrNull(2).orEmpty()

        val attributes = mutableListOf<MorphologyAttribute>()

        val posLabel = when (pos.uppercase(Locale.ROOT)) {
            "V" -> "Verbo"
            "N" -> "Substantivo"
            "ADJ", "A" -> "Adjetivo"
            "ART", "T" -> "Artigo Definido"
            "PREP" -> "Preposição"
            "CONJ" -> "Conjunção"
            "ADV" -> "Advérbio"
            "PRON", "P" -> "Pronome Pessoal"
            "R" -> "Pronome Relativo"
            "D" -> "Pronome Demonstrativo"
            "I" -> "Pronome Interrogativo"
            "X" -> "Pronome Indefinido"
            "F" -> "Pronome Reflexivo"
            "C" -> "Pronome Recíproco"
            "PART" -> "Partícula"
            else -> pos
        }
        attributes.add(MorphologyAttribute("Classe", posLabel))

        if (pos.equals("V", ignoreCase = true) && detail1.length >= 3) {
            val tense = when (detail1[0].uppercaseChar()) {
                'P' -> "Presente (ação contínua)"
                'I' -> "Imperfeito (contínuo no passado)"
                'A' -> "Aoristo (ação pontual/global)"
                'F' -> "Futuro"
                'R' -> "Perfeito (ação concluída c/ efeito presente)"
                'L' -> "Mais-que-perfeito"
                else -> detail1[0].toString()
            }
            val voice = when (detail1[1].uppercaseChar()) {
                'A' -> "Ativa"
                'M' -> "Média"
                'P' -> "Passiva"
                'D' -> "Deponente Média"
                'O' -> "Deponente Passiva"
                'E' -> "Média/Passiva"
                'N' -> "Ativa/Passiva (sem distinção)"
                else -> detail1[1].toString()
            }
            val mood = when (detail1[2].uppercaseChar()) {
                'I' -> "Indicativo"
                'P' -> "Particípio"
                'S' -> "Subjuntivo"
                'M' -> "Imperativo"
                'N' -> "Infinitivo"
                'O' -> "Optativo"
                else -> detail1[2].toString()
            }
            attributes.add(MorphologyAttribute("Tempo/Aspecto", tense))
            attributes.add(MorphologyAttribute("Voz", voice))
            attributes.add(MorphologyAttribute("Modo", mood))
        }

        val nominalTag = when {
            detail2.isNotBlank() -> detail2
            !pos.equals("V", ignoreCase = true) && detail1.isNotBlank() -> detail1
            else -> ""
        }

        if (nominalTag.length >= 2 && nominalTag[0].isDigit()) {
            val person = "${nominalTag[0]}ª Pessoa"
            val number = when (nominalTag[1].uppercaseChar()) {
                'S' -> "Singular"
                'P' -> "Plural"
                else -> nominalTag[1].toString()
            }
            attributes.add(MorphologyAttribute("Pessoa", person))
            attributes.add(MorphologyAttribute("Número", number))
        } else if (nominalTag.length >= 3) {
            val kase = when (nominalTag[0].uppercaseChar()) {
                'N' -> "Nominativo (sujeito/predicativo)"
                'G' -> "Genitivo (posse/origem)"
                'D' -> "Dativo (meio/beneficiário)"
                'A' -> "Acusativo (objeto direto)"
                'V' -> "Vocativo (interpelação)"
                else -> nominalTag[0].toString()
            }
            val gender = when (nominalTag[1].uppercaseChar()) {
                'M' -> "Masculino"
                'F' -> "Feminino"
                'N' -> "Neutro"
                else -> nominalTag[1].toString()
            }
            val number = when (nominalTag[2].uppercaseChar()) {
                'S' -> "Singular"
                'P' -> "Plural"
                else -> nominalTag[2].toString()
            }
            attributes.add(MorphologyAttribute("Caso", kase))
            attributes.add(MorphologyAttribute("Gênero", gender))
            attributes.add(MorphologyAttribute("Número", number))
        }

        return attributes
    }

    private fun decodeHebrew(code: String): List<MorphologyAttribute> {
        if (isStepHebrewMorph(code)) {
            return decodeStepHebrew(code)
        }

        val attributes = mutableListOf<MorphologyAttribute>()
        val segments = code.split("+")

        val prefixes = mutableListOf<String>()
        var core = code
        if (segments.size > 1) {
            segments.dropLast(1).forEach { pref ->
                prefixes.add(
                    when (pref.uppercase(Locale.ROOT)) {
                        "CONJ" -> "Conjunção (וְ 'e')"
                        "PREP" -> "Preposição inseparável"
                        "ART" -> "Artigo Definido (הַ)"
                        else -> pref
                    }
                )
            }
            core = segments.last()
        }

        if (prefixes.isNotEmpty()) {
            attributes.add(MorphologyAttribute("Prefixo", prefixes.joinToString(" + ")))
        }

        val parts = core.split("-")
        val head = parts.getOrNull(0).orEmpty()
        when (head.uppercase(Locale.ROOT)) {
            "V" -> {
                attributes.add(MorphologyAttribute("Classe", "Verbo"))
                parts.getOrNull(1)?.let { binyan ->
                    val binyanLabel = when (binyan.lowercase(Locale.ROOT)) {
                        "qal" -> "Qal (simples ativo)"
                        "piel" -> "Piel (intensivo ativo)"
                        "hiphil" -> "Hiphil (causativo ativo)"
                        "niphal" -> "Niphal (simples passivo/reflexivo)"
                        "hithpael" -> "Hithpael (reflexivo)"
                        else -> binyan
                    }
                    attributes.add(MorphologyAttribute("Tronco (Binyan)", binyanLabel))
                }
                parts.getOrNull(2)?.let { conj ->
                    val conjLabel = when (conj.lowercase(Locale.ROOT)) {
                        "perf" -> "Perfeito (ação completa)"
                        "impf" -> "Imperfeito (ação contínua/futura)"
                        "part" -> "Particípio"
                        "impv" -> "Imperativo"
                        else -> conj
                    }
                    attributes.add(MorphologyAttribute("Conjugação", conjLabel))
                }
                parts.getOrNull(3)?.let { pgn ->
                    decodeHebrewPgn(pgn, attributes)
                }
            }
            "N" -> {
                attributes.add(MorphologyAttribute("Classe", "Substantivo"))
                parts.getOrNull(1)?.let { mod ->
                    if (mod.equals("Proper", ignoreCase = true)) {
                        attributes.add(MorphologyAttribute("Tipo", "Nome Próprio / Teônimo"))
                    } else {
                        decodeHebrewPgn(mod, attributes)
                    }
                }
            }
            "DIROBJ" -> attributes.add(MorphologyAttribute("Função", "Marcador de Objeto Direto Definido"))
            "NEG" -> attributes.add(MorphologyAttribute("Classe", "Partícula Negativa"))
            else -> attributes.add(MorphologyAttribute("Análise", core))
        }

        if (segments.size > 1 && segments.last().contains("1cs", ignoreCase = true)) {
            attributes.add(MorphologyAttribute("Sufixo Pron.", "1ª Pessoa Comum Sing. ('meu/me')"))
        }

        return attributes
    }

    private fun decodeStepHebrew(code: String): List<MorphologyAttribute> {
        val attributes = mutableListOf<MorphologyAttribute>()
        val segments = code.split("/").filter { it.isNotBlank() }
        val prefixes = mutableListOf<String>()
        var coreSegment: String? = null

        segments.forEachIndexed { index, rawSeg ->
            val seg = if (index == 0 && (rawSeg.startsWith("H") || rawSeg.startsWith("A"))) {
                rawSeg.drop(1)
            } else {
                rawSeg
            }
            val isLast = index == segments.lastIndex
            if (!isLast && seg in setOf("C", "c", "R", "Rd", "Td", "T", "Ti")) {
                prefixes.add(
                    when (seg) {
                        "C", "c" -> "Conjunção (וְ 'e')"
                        "R" -> "Preposição inseparável"
                        "Rd" -> "Preposição + Artigo Definido"
                        "Td", "T" -> "Artigo Definido (הַ)"
                        "Ti" -> "Partícula interrogativa"
                        else -> seg
                    }
                )
            } else if (!isLast && seg.startsWith("S")) {
                attributes.add(MorphologyAttribute("Sufixo", "Sufixo pronominal ($seg)"))
            } else if (coreSegment == null) {
                coreSegment = seg
            } else if (seg.startsWith("S")) {
                attributes.add(MorphologyAttribute("Sufixo", "Sufixo pronominal (${seg.drop(1)})"))
            }
        }

        if (prefixes.isNotEmpty()) {
            attributes.add(MorphologyAttribute("Prefixo", prefixes.joinToString(" + ")))
        }

        val core = coreSegment ?: return attributes
        when (core.firstOrNull()) {
            'V' -> {
                attributes.add(MorphologyAttribute("Classe", "Verbo"))
                core.getOrNull(1)?.let { stem ->
                    val binyan = when (stem) {
                        'q' -> "Qal (simples ativo)"
                        'N' -> "Niphal (passivo/reflexivo)"
                        'p' -> "Piel (intensivo ativo)"
                        'P' -> "Pual (intensivo passivo)"
                        'h' -> "Hiphil (causativo ativo)"
                        'H' -> "Hophal (causativo passivo)"
                        't' -> "Hithpael (reflexivo)"
                        else -> stem.toString()
                    }
                    attributes.add(MorphologyAttribute("Tronco (Binyan)", binyan))
                }
                core.getOrNull(2)?.let { conjChar ->
                    val conj = when (conjChar) {
                        'p' -> "Perfeito (ação completa)"
                        'i' -> "Imperfeito (contínuo/futuro)"
                        'w' -> "Wayyiqtol (imperfeito consecutivo)"
                        'v' -> "Imperativo"
                        'a' -> "Infinitivo Absoluto"
                        'c' -> "Infinitivo Construto"
                        'r' -> "Particípio Ativo"
                        's' -> "Particípio Passivo"
                        else -> conjChar.toString()
                    }
                    attributes.add(MorphologyAttribute("Conjugação", conj))
                }
                val rem = core.drop(3)
                if (rem.isNotEmpty()) decodeHebrewPgn(rem, attributes)
            }
            'N' -> {
                attributes.add(MorphologyAttribute("Classe", "Substantivo"))
                when (core.getOrNull(1)) {
                    'p' -> attributes.add(MorphologyAttribute("Tipo", "Nome Próprio"))
                    'g' -> attributes.add(MorphologyAttribute("Tipo", "Gentílico"))
                    'c' -> attributes.add(MorphologyAttribute("Tipo", "Comum"))
                }
                val pgnAndState = core.drop(2)
                if (pgnAndState.isNotEmpty()) {
                    decodeHebrewPgn(pgnAndState.take(2), attributes)
                    pgnAndState.getOrNull(2)?.let { state ->
                        val stateLabel = when (state) {
                            'a' -> "Absoluto"
                            'c' -> "Construto ('de')"
                            'd' -> "Determinado"
                            else -> null
                        }
                        if (stateLabel != null) attributes.add(MorphologyAttribute("Estado", stateLabel))
                    }
                }
            }
            'A' -> {
                attributes.add(MorphologyAttribute("Classe", "Adjetivo"))
                val pgnAndState = core.drop(2)
                if (pgnAndState.isNotEmpty()) decodeHebrewPgn(pgnAndState.take(2), attributes)
            }
            'R' -> attributes.add(MorphologyAttribute("Classe", "Preposição"))
            'C', 'c' -> attributes.add(MorphologyAttribute("Classe", "Conjunção"))
            'P' -> attributes.add(MorphologyAttribute("Classe", "Pronome"))
            'D' -> attributes.add(MorphologyAttribute("Classe", "Advérbio"))
            'T' -> {
                val func = if (core.getOrNull(1) == 'o') "Marcador de Objeto Direto" else "Partícula"
                attributes.add(MorphologyAttribute("Função", func))
            }
            else -> attributes.add(MorphologyAttribute("Análise", code))
        }
        return attributes
    }

    private fun decodeHebrewPgn(pgn: String, out: MutableList<MorphologyAttribute>) {
        var idx = 0
        if (pgn.isNotEmpty() && pgn[0].isDigit()) {
            out.add(MorphologyAttribute("Pessoa", "${pgn[0]}ª Pessoa"))
            idx = 1
        }
        val rem = pgn.drop(idx).lowercase(Locale.ROOT)
        if (rem.isNotEmpty()) {
            val gender = when (rem[0]) {
                'm' -> "Masculino"
                'f' -> "Feminino"
                'c' -> "Comum"
                else -> null
            }
            if (gender != null) out.add(MorphologyAttribute("Gênero", gender))
        }
        if (rem.length >= 2) {
            val number = when (rem[1]) {
                's' -> "Singular"
                'p' -> "Plural"
                else -> null
            }
            if (number != null) out.add(MorphologyAttribute("Número", number))
        }
    }
}
