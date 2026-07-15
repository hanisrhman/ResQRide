package com.example.resqride;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import com.example.resqride.utils.VehicleUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class DeleteMaintenanceDialog {

    public static void show(
            Context context,
            String vehicleId,
            String maintenanceId
    ) {

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null || vehicleId == null || maintenanceId == null) {
            Toast.makeText(context,
                    "Unable to delete maintenance",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Delete Maintenance")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Delete", (d, w) -> {

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .collection("vehicles")
                            .document(vehicleId)
                            .collection("maintenance")
                            .document(maintenanceId)
                            .delete()
                            .addOnSuccessListener(r -> {
                                VehicleUtils.updateLastService(userId, vehicleId);
                                Toast.makeText(context,
                                        "Maintenance deleted",
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}