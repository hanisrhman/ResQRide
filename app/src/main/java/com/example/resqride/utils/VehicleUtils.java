package com.example.resqride.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class VehicleUtils {

    /**
     * Updates vehicle.lastService using
     * the latest maintenance record
     */
    public static void updateLastService(
            String userId,
            String vehicleId
    ) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .collection("vehicles")
                .document(vehicleId)
                .collection("maintenance")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        // ❌ No maintenance → clear lastService
                        db.collection("users")
                                .document(userId)
                                .collection("vehicles")
                                .document(vehicleId)
                                .update("lastService", "");
                        return;
                    }

                    String lastService =
                            snapshot.getDocuments()
                                    .get(0)
                                    .getString("serviceType");

                    if (lastService == null)
                        lastService = "";

                    // ✅ Update vehicle
                    db.collection("users")
                            .document(userId)
                            .collection("vehicles")
                            .document(vehicleId)
                            .update("lastService", lastService);
                });
    }
}