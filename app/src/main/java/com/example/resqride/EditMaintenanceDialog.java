package com.example.resqride;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.resqride.models.Maintenance;
import com.example.resqride.utils.VehicleUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditMaintenanceDialog {

    public static void show(
            Context context,
            String vehicleId,
            String maintenanceId,
            Maintenance m
    ) {

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null || vehicleId == null || maintenanceId == null) {
            Toast.makeText(context,
                    "Unable to edit maintenance",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_maintenance, null);

        EditText edtService = view.findViewById(R.id.edtServiceType);
        EditText edtWorkshop = view.findViewById(R.id.edtWorkshop);
        EditText edtDate = view.findViewById(R.id.edtDate);
        EditText edtNotes = view.findViewById(R.id.edtNotes);

        edtService.setText(m.serviceType);
        edtWorkshop.setText(m.workshop);
        edtNotes.setText(m.notes);

        final Calendar selectedDate = Calendar.getInstance();
        if (m.date != null) {
            selectedDate.setTime(m.date.toDate());
            edtDate.setText(
                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(m.date.toDate())
            );
        }

        edtDate.setOnClickListener(v -> {
            new DatePickerDialog(
                    context,
                    (dp, y, mo, d) -> {
                        selectedDate.set(y, mo, d);
                        edtDate.setText(
                                new SimpleDateFormat(
                                        "dd MMM yyyy",
                                        Locale.getDefault()
                                ).format(selectedDate.getTime())
                        );
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {

            Map<String, Object> data = new HashMap<>();
            data.put("serviceType", edtService.getText().toString().trim());
            data.put("workshop", edtWorkshop.getText().toString().trim());
            data.put("notes", edtNotes.getText().toString().trim());
            data.put("date", new Timestamp(selectedDate.getTime()));

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("vehicles")
                    .document(vehicleId)
                    .collection("maintenance")
                    .document(maintenanceId)
                    .update(data)
                    .addOnSuccessListener(r -> {
                        VehicleUtils.updateLastService(userId, vehicleId);
                        Toast.makeText(context,
                                "Maintenance updated",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        view.findViewById(R.id.btnCancel)
                .setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}