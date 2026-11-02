const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_admin_123";
const BOB_UID = "bob_admin_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

function pastDate() {
  return new Date(Date.now() - 5000);
}

test("Unauthenticated user cannot read or write enquiries", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("enquiries").get());
});

test("Authenticated user can create and query own enquiry with valid timestamp", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastDate();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("enquiries").doc("enq_1").set({
      userId: ALICE_UID,
      studentName: "Aarav Sharma",
      fatherName: "Rajesh Sharma",
      mobileNumber: "9876543210",
      classInterested: "Class 1",
      address: "12 Park Street",
      remarks: "Interested in morning batch",
      session: "2027-28",
      status: "Pending",
      formIssued: false,
      formPrice: 300,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("enquiries")
      .where("userId", "==", ALICE_UID)
      .get()
  );
});

test("Cross-user access is strictly rejected (PII isolation)", async () => {
  const ts = pastDate();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("enquiries").doc("enq_secret").set({
      userId: ALICE_UID,
      studentName: "Riya Das",
      fatherName: "Subhash Das",
      mobileNumber: "9123456780",
      classInterested: "Nursery",
      address: "Lake Gardens",
      remarks: "VIP enquiry",
      session: "2027-28",
      status: "Form Issued",
      formIssued: true,
      formPrice: 250,
      createdAt: ts,
      updatedAt: ts,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("enquiries").doc("enq_secret").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("enquiries")
      .where("userId", "==", ALICE_UID)
      .get()
  );
});

test("Shadow update with ghost field is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastDate();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("config").doc("main");

  await assertSucceeds(
    docRef.set({
      userId: ALICE_UID,
      schoolName: "EduPulse Academy",
      helplineNumber: "+91-9800011223",
      activeSession: "2027-28",
      defaultFormPrice: 250,
      classFormPrices: { Nursery: 200, "Class 1": 300 },
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertFails(
    docRef.update({
      defaultFormPrice: 350,
      isSuperAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Negative form price or invalid status enum is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastDate();

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("enquiries").doc("enq_bad").set({
      userId: ALICE_UID,
      studentName: "Bad Data",
      fatherName: "Parent",
      mobileNumber: "9999999999",
      classInterested: "Class 5",
      address: "Addr",
      remarks: "Rem",
      session: "2027-28",
      status: "InvalidStatus",
      formIssued: false,
      formPrice: -100,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});
