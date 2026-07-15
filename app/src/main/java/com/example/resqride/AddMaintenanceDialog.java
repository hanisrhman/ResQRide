package com.example.resqride;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.resqride.utils.VehicleUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddMaintenanceDialog {

    public static void show(Context context, String vehicleId) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_maintenance, null);

        EditText edtServiceType = view.findViewById(R.id.edtServiceType);
        EditText edtWorkshop = view.findViewById(R.id.edtWorkshop);
        EditText edtDate = view.findViewById(R.id.edtDate);
        EditText edtNotes = view.findViewById(R.id.edtNotes);


        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();

        // 🔙 Back & Cancel
        view.findViewById(R.id.btnBack)
                .setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btnCancel)
                .setOnClickListener(v -> dialog.dismiss());

        // 📅 Date Picker
        final Calendar selectedDate = Calendar.getInstance();

        edtDate.setFocusable(false); // prevent keyboard
        edtDate.setOnClickListener(v -> {

            DatePickerDialog picker = new DatePickerDialog(
                    context,
                    (dp, year, month, day) -> {
                        selectedDate.set(year, month, day);
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
            );
            picker.show();
        });

        // 💾 SAVE
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {

            String service = edtServiceType.getText().toString().trim();
            String workshop = edtWorkshop.getText().toString().trim();
            String notes = edtNotes.getText().toString().trim();

            if (service.isEmpty() || workshop.isEmpty()
                    || edtDate.getText().toString().isEmpty()) {

                Toast.makeText(context,
                        "Please fill all required fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null) {
                Toast.makeText(context,
                        "User not logged in",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("serviceType", service);
            data.put("workshop", workshop);
            data.put("notes", notes);
            data.put("date", new Timestamp(selectedDate.getTime()));
            data.put("createdAt", Timestamp.now());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("vehicles")
                    .document(vehicleId)
                    .collection("maintenance")
                    .add(data)
                    .addOnSuccessListener(r -> {

                        // ✅ AUTO UPDATE LAST SERVICE
                        VehicleUtils.updateLastService(userId, vehicleId);

                        Toast.makeText(context,
                                "Maintenance saved",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context,
                                    e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        });

        dialog.show();
    }
}