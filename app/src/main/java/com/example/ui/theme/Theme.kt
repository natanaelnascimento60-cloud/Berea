package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class BereaExtendedColors(
    val folioHighlightBg: Color,
    val originalScriptAccent: Color,
    val morphologyBg: Color,
    val brassAccent: Color,
    val successGreen: Color,
    val subtleBorder: Color,
    val isDark: Boolean
)

val LocalBereaColors = staticCompositionLocalOf {
    BereaExtendedColors(
        folioHighlightBg = Color(0xFFF5F1E6),
        originalScriptAccent = AegeanNavy,
        morphologyBg = Color(0xFFEDE8DB),
        brassAccent = BrassAccent,
        successGreen = OliveSuccess,
        subtleBorder = ArchivalBorder,
        isDark = false
    )
}

private val BereaLightColorScheme = lightColorScheme(
    primary = BurgundyPrimary,
    onPrimary = Color.White,
    primaryContainer = BurgundySoft,
    onPrimaryContainer = BurgundyPrimary,
    secondary = AegeanNavy,
    onSecondary = Color.White,
    secondaryContainer = AegeanSoft,
    onSecondaryContainer = AegeanNavy,
    tertiary = BrassAccent,
    onTertiary = Color.White,
    tertiaryContainer = BrassSoft,
    onTertiaryContainer = Color(0xFF6B4710),
    background = ArchivalCreamBg,
    onBackground = SlateInk,
    surface = FolioWhiteSurface,
    onSurface = SlateInk,
    surfaceVariant = LinenVariant,
    onSurfaceVariant = MutedGraphite,
    outline = ArchivalBorder,
    outlineVariant = Color(0xFFE6E1D5),
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val BereaDarkColorScheme = darkColorScheme(
    primary = RubricRoseDark,
    onPrimary = Color(0xFF2B0B0B),
    primaryContainer = Color(0xFF472020),
    onPrimaryContainer = Color(0xFFFAD8D8),
    secondary = AegeanLightDark,
    onSecondary = Color(0xFF0A2433),
    secondaryContainer = Color(0xFF1A3344),
    onSecondaryContainer = Color(0xFFD2ECFA),
    tertiary = BrassWarmDark,
    onTertiary = Color(0xFF261A05),
    tertiaryContainer = Color(0xFF3A2B12),
    onTertiaryContainer = Color(0xFFF5DEAE),
    background = NightDeskBg,
    onBackground = ParchmentIvory,
    surface = NightFolioSurface,
    onSurface = ParchmentIvory,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = MutedSlateText,
    outline = NightBorder,
    outlineVariant = Color(0xFF272C36),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@Composable
fun VerboTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BereaDarkColorScheme else BereaLightColorScheme
    val extendedColors = if (darkTheme) {
        BereaExtendedColors(
            folioHighlightBg = Color(0xFF1C2029),
            originalScriptAccent = AegeanLightDark,
            morphologyBg = Color(0xFF252A36),
            brassAccent = BrassWarmDark,
            successGreen = Color(0xFF63C291),
            subtleBorder = NightBorder,
            isDark = true
        )
    } else {
        BereaExtendedColors(
            folioHighlightBg = Color(0xFFF5F1E6),
            originalScriptAccent = AegeanNavy,
            morphologyBg = Color(0xFFEDE8DB),
            brassAccent = BrassAccent,
            successGreen = OliveSuccess,
            subtleBorder = ArchivalBorder,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalBereaColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VerboTypography,
            content = content
        )
    }
}
