/**
 * ResQRide Cloud Functions
 * Notify workshop when assignedWorkshopId is set
 */

const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

exports.notifyWorkshopOnAssigned =
onDocumentUpdated("sos_requests/{sosId}", async (event) => {

  const before = event.data.before.data();
  const after = event.data.after.data();

  if (!after) {
    console.log("No SOS data");
    return;
  }

  const assignedWorkshopId = after.assignedWorkshopId;

  // Only trigger when workshop newly assigned
  if (!assignedWorkshopId) {
    console.log("No workshop assigned");
    return;
  }

  // prevent duplicate notification
  if (before.assignedWorkshopId === assignedWorkshopId) {
    console.log("Workshop already assigned before");
    return;
  }

  console.log("Workshop assigned:", assignedWorkshopId);

  const db = admin.firestore();

  const workshopDoc =
    await db.collection("workshops")
    .doc(assignedWorkshopId)
    .get();

  if (!workshopDoc.exists) {
    console.log("Workshop not found");
    return;
  }

  const token = workshopDoc.data().fcmToken;

  if (!token) {
    console.log("No FCM token");
    return;
  }

  const riderName = after.riderName || "Rider";
  const problem = after.problem || "Needs help";

  const message = {

    notification: {
      title: "🚨 New SOS Request",
      body: riderName + " needs your help"
    },

    data: {
      sosId: event.params.sosId,
      type: "SOS"
    },

    token: token
  };

  try {

    await admin.messaging().send(message);

    console.log("✅ Notification sent");

  } catch (error) {

    console.error("❌ Error:", error);

  }

});