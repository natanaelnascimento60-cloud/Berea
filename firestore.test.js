const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated access is denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("readingPlans").get());
});

test("UserProfile: owner can create and partially update profile; cross-user and ghost fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Natanael",
      streakDays: 7,
      lastStudyDate: "2026-11-25",
      preferredTranslation: "NVI",
      notificationsEnabled: true,
      notificationHour: 7,
      notificationMinute: 30,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update: streak
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      streakDays: 8,
      lastStudyDate: "2026-11-26",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Partial update: notifications
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      notificationsEnabled: false,
      notificationHour: 8,
      notificationMinute: 0,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field / shadow update
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      streakDays: 9,
      lastStudyDate: "2026-11-27",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject cross-user read/write
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      preferredTranslation: "ARA",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("StudyNotes: owner CRUD succeeds, @DocumentId id key rejected, cross-user denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  // Empty subcollection list query succeeds for owner
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("notes").get());

  // Create note without `id` field
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").set({
      userId: ALICE_UID,
      verseRef: "1 Pedro 1:3",
      title: "Viva Esperança",
      content: "O termo ἀναγεννάω destaca o novo nascimento operado por Deus.",
      tag: "Exegese",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").update({
      verseRef: "1 Pedro 1:3-4",
      title: "Viva Esperança e Herança",
      content: "Texto atualizado com comentário de herança incorruptível.",
      tag: "Grego",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject extra `id` field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_2").set({
      id: "note_2",
      userId: ALICE_UID,
      verseRef: "João 1:1",
      title: "Logos",
      content: "No princípio era o Verbo.",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cross-user access denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").delete());

  // Owner delete succeeds
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").delete());
});

test("ReadingPlans: owner create, day completion update, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("readingPlans").get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("readingPlans").doc("plan_1").set({
      userId: ALICE_UID,
      title: "Evangelho de João e Epístolas",
      description: "Leitura devocional e exegética de 7 dias.",
      totalDays: 7,
      completedDays: [1],
      readings: [
        "Dia 1: João 1 — O Verbo Eterno",
        "Dia 2: João 3 — O Novo Nascimento",
        "Dia 3: João 15 — A Videira Verdadeira",
        "Dia 4: Romanos 8 — Vida no Espírito",
        "Dia 5: Efésios 2 — Salvos pela Graça",
        "Dia 6: Filipenses 2 — A Humilhação de Cristo",
        "Dia 7: 1 Pedro 1 — Viva Esperança",
      ],
      active: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("readingPlans").doc("plan_1").update({
      completedDays: [1, 2],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("readingPlans").get());
});
