package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SosStatusActivity extends AppCompatActivity {

    private TextView txtStatus, txtWorkshop;

    private FirebaseFirestore db;
    private String sosId;

    private boolean hasNavigated = false;

    // ⏱ TIMEOUT (60 seconds)
    private static final long TIMEOUT_MS = 60 * 1000;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_status);

        txtStatus = findViewById(R.id.txtStatus);
        txtWorkshop = findViewById(R.id.txtWorkshop);

        db = FirebaseFirestore.getInstance();
        sosId = getIntent().getStringExtra("sosId");

        if (sosId == null) {
            finish();
            return;
        }

        listenSOSStatus();
        startTimeoutWatcher();
    }

    // ================= REAL-TIME LISTENER =================
    private void listenSOSStatus() {

        db.collection("sos_requests")
                .document(sosId)
                .addSnapshotListener((doc, e) -> {

                    if (doc == null || !doc.exists()) return;

                    String status = doc.getString("status");

                    // DEFAULT
                    txtStatus.setText("Waiting for workshop...");
                    txtWorkshop.setText("No workshop assigned");

                    // ⏳ WAITING
                    if ("waiting_workshop".equals(status)) {
                        txtStatus.setText("Waiting for workshop to accept...");
                        txtWorkshop.setText(
                                doc.getString("assignedWorkshopName") != null
                                        ? doc.getString("assignedWorkshopName")
                                        : "Workshop selected"
                        );
                    }

                    // ✅ ACCEPTED
                    if ("accepted".equals(status) && !hasNavigated) {
                        hasNavigated = true;

                        Intent i = new Intent(this, SosTrackingActivity.class);
                        i.putExtra("sosId", sosId);
                        startActivity(i);
                        finish();
                    }

                    // ❌ DECLINED
                    if ("declined".equals(status) && !hasNavigated) {
                        hasNavigated = true;
                        redirectToSelection(
                                "Workshop declined. Please choose another.",
                                doc.getDouble("lat"),
                                doc.getDouble("lng")
                        );
                    }
                });
    }

    // ================= TIMEOUT WATCHER =================
    private void startTimeoutWatcher() {

        handler.postDelayed(() -> {

            if (hasNavigated) return;

            db.collection("sos_requests")
                    .document(sosId)
                    .get()
                    .addOnSuccessListener(doc -> {

                        if (!doc.exists()) return;

                        String status = doc.getString("status");

                        if ("waiting_workshop".equals(status)) {

                            // 🔄 RESET WORKSHOP
                            Map<String, Object> reset = new HashMap<>();
                            reset.put("assignedWorkshopId", null);
                            reset.put("assignedWorkshopName", null);
                            reset.put("status", "selecting_workshop");

                            db.collection("sos_requests")
                                    .document(sosId)
                                    .update(reset)
                                    .addOnSuccessListener(v -> {

                                        hasNavigated = true;

                                        redirectToSelection(
                                                "No response from workshop. Please select another.",
                                                doc.getDouble("lat"),
                                                doc.getDouble("lng")
                                        );
                                    });
                        }
                    });

        }, TIMEOUT_MS);
    }

    // ================= REDIRECT =================
    private void redirectToSelection(String message, Double lat, Double lng) {

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        Intent i = new Intent(this, WorkshopSelectionActivity.class);
        i.putExtra("sosId", sosId);

        if (lat != null && lng != null) {
            i.putExtra("lat", lat);
            i.putExtra("lng", lng);
        }

        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}