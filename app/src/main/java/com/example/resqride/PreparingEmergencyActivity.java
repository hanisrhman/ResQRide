package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class PreparingEmergencyActivity extends AppCompatActivity {

    private static final long TIMEOUT_MS = 30_000; // 30 seconds

    private FirebaseFirestore db;
    private ListenerRegistration sosListener;

    private String sosId;
    private boolean navigated = false;

    private TextView txtPreparingStatus;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preparing_emergency);

        txtPreparingStatus = findViewById(R.id.txtPreparingStatus);
        db = FirebaseFirestore.getInstance();

        sosId = getIntent().getStringExtra("sosId");

        if (sosId == null) {
            Toast.makeText(this, "Invalid SOS request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listenToSOSStatus();
        startTimeout();
    }

    // ================= FIRESTORE LISTENER =================
    private void listenToSOSStatus() {

        sosListener = db.collection("sos_requests")
                .document(sosId)
                .addSnapshotListener((doc, e) -> {

                    if (doc == null || !doc.exists() || navigated) return;

                    String status = doc.getString("status");
                    if (status == null) return;

                    switch (status) {

                        case "accepted":
                            navigated = true;
                            goToStatus();
                            break;

                        case "selecting":
                            txtPreparingStatus.setText(
                                    "Finding nearby workshops..."
                            );
                            break;

                        case "waiting_workshop":
                            txtPreparingStatus.setText(
                                    "Waiting for workshop response..."
                            );
                            break;
                    }
                });
    }

    // ================= TIMEOUT =================
    private void startTimeout() {

        handler.postDelayed(() -> {

            if (navigated) return;
            navigated = true;

            db.collection("sos_requests")
                    .document(sosId)
                    .get()
                    .addOnSuccessListener(doc -> {

                        if (!doc.exists()) return;

                        double lat = doc.getDouble("lat") != null ? doc.getDouble("lat") : 0;
                        double lng = doc.getDouble("lng") != null ? doc.getDouble("lng") : 0;

                        Intent i = new Intent(
                                PreparingEmergencyActivity.this,
                                WorkshopSelectionActivity.class
                        );
                        i.putExtra("sosId", sosId);
                        i.putExtra("lat", lat);
                        i.putExtra("lng", lng);
                        startActivity(i);
                        finish();
                    });
        }, TIMEOUT_MS);
    }

    // ================= NAVIGATION =================
    private void goToStatus() {

        Intent i = new Intent(
                PreparingEmergencyActivity.this,
                SosStatusActivity.class
        );
        i.putExtra("sosId", sosId);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (sosListener != null) sosListener.remove();
        handler.removeCallbacksAndMessages(null);
    }
}