package com.example.resqride;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.resqride.models.SosRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WorkshopReceiptActivity extends AppCompatActivity {

    TextView txtRider, txtVehicle, txtProblem, txtWorkshop,
            txtTime, txtPayment, txtStatus,
            txtRating, txtReview;

    FirebaseFirestore db;
    String sosId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_receipt);

        sosId = getIntent().getStringExtra("sosId");
        db = FirebaseFirestore.getInstance();

        bindViews();
        loadReceipt();
    }

    private void bindViews() {

        txtRider = findViewById(R.id.txtRiderName);
        txtVehicle = findViewById(R.id.txtVehicle);
        txtProblem = findViewById(R.id.txtProblem);
        txtWorkshop = findViewById(R.id.txtWorkshop);
        txtTime = findViewById(R.id.txtTime);
        txtPayment = findViewById(R.id.txtPayment);
        txtStatus = findViewById(R.id.txtStatus);
        txtRating = findViewById(R.id.txtRating);
        txtReview = findViewById(R.id.txtReview);
    }

    // ================= LOAD RECEIPT =================
    private void loadReceipt() {

        db.collection("sos_requests")
                .document(sosId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    SosRequest s = doc.toObject(SosRequest.class);
                    if (s == null) return;

                    txtRider.setText(s.riderName);
                    txtVehicle.setText(
                            (s.vehicleModel != null ? s.vehicleModel : "-")
                                    + " • "
                                    + (s.vehiclePlate != null ? s.vehiclePlate : "-")
                    );

                    txtProblem.setText(s.problem);
                    txtWorkshop.setText(
                            s.assignedWorkshopName != null
                                    ? s.assignedWorkshopName
                                    : "-"
                    );

                    // Time
                    if (s.time > 0) {
                        String formatted = new SimpleDateFormat(
                                "dd MMM yyyy • hh:mm a",
                                Locale.getDefault()
                        ).format(new Date(s.time));
                        txtTime.setText(formatted);
                    }

                    txtStatus.setText(
                            s.status != null
                                    ? s.status.toUpperCase()
                                    : "-"
                    );

                    txtPayment.setText(
                            s.paymentMethod != null
                                    ? s.paymentMethod + " (" + s.paymentStatus + ")"
                                    : "Not paid"
                    );

                    // Rating
                    if (s.rating > 0) {
                        txtRating.setText("⭐ " + s.rating + " / 5");
                        txtReview.setText(
                                s.review != null && !s.review.isEmpty()
                                        ? s.review
                                        : "No review"
                        );
                    } else {
                        txtRating.setText("Not rated");
                        txtReview.setText("-");
                    }
                });
    }
}