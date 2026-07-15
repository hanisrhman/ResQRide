package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SosDetailActivity extends AppCompatActivity {

    private String sosId;
    private FirebaseFirestore db;

    // UI
    private TextView txtProblem;
    private TextView txtWorkshop;
    private TextView txtPayment;
    private TextView txtStatus;

    private TextView txtRider;
    private TextView txtVehicleModel;
    private TextView txtPlate;
    private TextView txtCost;
    private TextView txtRating;
    private TextView txtReview;
    private TextView txtTime;

    private Button btnReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_detail);

        sosId = getIntent().getStringExtra("sosId");

        if (sosId == null) {
            Toast.makeText(this, "Invalid SOS ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        // Bind UI
        txtProblem = findViewById(R.id.txtProblem);
        txtWorkshop = findViewById(R.id.txtWorkshop);
        txtPayment = findViewById(R.id.txtPayment);
        txtStatus = findViewById(R.id.txtStatus);

        txtRider = findViewById(R.id.txtRider);
        txtVehicleModel = findViewById(R.id.txtVehicleModel);
        txtPlate = findViewById(R.id.txtPlate);
        txtCost = findViewById(R.id.txtCost);
        txtRating = findViewById(R.id.txtRating);
        txtReview = findViewById(R.id.txtReview);
        txtTime = findViewById(R.id.txtTime);

        btnReceipt = findViewById(R.id.btnReceipt);

        btnReceipt.setEnabled(false);

        loadDetail();
    }

    // ================= LOAD DETAIL =================

    private void loadDetail() {

        db.collection("sos_requests")
                .document(sosId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "SOS not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    showSosData(doc);

                    loadVehicle(doc);

                    loadRider(doc);

                    loadReview(doc);
                });
    }

    // ================= SHOW SOS =================

    private void showSosData(DocumentSnapshot doc) {

        String problem = doc.getString("problem");
        txtProblem.setText(problem != null ? problem : "-");

        String workshop = doc.getString("assignedWorkshopName");
        txtWorkshop.setText(workshop != null ? workshop : "-");

        String payment = doc.getString("paymentMethod");
        txtPayment.setText(payment != null ? payment : "-");

        Double cost = doc.getDouble("cost");
        txtCost.setText(cost != null ? "RM " + cost : "RM 25.00");

        Long time = doc.getLong("time");
        if (time != null) {
            String formatted = new SimpleDateFormat(
                    "dd MMM yyyy • hh:mm a",
                    Locale.getDefault()
            ).format(new Date(time));

            txtTime.setText(formatted);
        } else {
            txtTime.setText("-");
        }

        String status = doc.getString("status");
        txtStatus.setText(status != null ? status.toUpperCase() : "-");

        if ("completed".equals(status)) {
            btnReceipt.setEnabled(true);

            btnReceipt.setOnClickListener(v -> {

                Intent intent = new Intent(
                        SosDetailActivity.this,
                        ReceiptActivity.class
                );

                intent.putExtra("sosId", sosId);

                startActivity(intent);
            });
        }
    }

    // ================= LOAD VEHICLE =================

    private void loadVehicle(DocumentSnapshot doc) {

        String vehicleId = doc.getString("vehicleId");
        String riderId = doc.getString("riderId");

        if (vehicleId == null || riderId == null) return;

        db.collection("users")
                .document(riderId)
                .collection("vehicles")
                .document(vehicleId)
                .get()
                .addOnSuccessListener(vehicleDoc -> {

                    if (vehicleDoc.exists()) {

                        String model = vehicleDoc.getString("model");
                        String plate = vehicleDoc.getString("plateNumber");

                        txtVehicleModel.setText(
                                model != null ? model : "-"
                        );

                        txtPlate.setText(
                                plate != null ? plate : "-"
                        );
                    }
                });
    }

    // ================= LOAD RIDER =================

    private void loadRider(DocumentSnapshot doc) {

        String riderId = doc.getString("riderId");

        if (riderId == null) return;

        db.collection("users")
                .document(riderId)
                .get()
                .addOnSuccessListener(userDoc -> {

                    String name = userDoc.getString("name");

                    txtRider.setText(
                            name != null ? name : "-"
                    );
                });
    }

    // ================= LOAD REVIEW =================

    private void loadReview(DocumentSnapshot doc) {

        Double rating = doc.getDouble("rating");
        String review = doc.getString("review");

        txtRating.setText(
                rating != null ? String.valueOf(rating) + " ⭐" : "-"
        );

        txtReview.setText(
                review != null ? review : "-"
        );
    }
}