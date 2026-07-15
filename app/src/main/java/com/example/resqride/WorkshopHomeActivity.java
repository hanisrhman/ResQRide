package com.example.resqride;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.WorkshopSOSAdapter;
import com.example.resqride.models.WorkshopSOS;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WorkshopHomeActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RecyclerView recyclerSOS;
    private TextView txtStatus;

    private FirebaseFirestore db;
    private String workshopId;

    private double workshopLat = 0;
    private double workshopLng = 0;

    private final List<WorkshopSOS> sosList = new ArrayList<>();
    private WorkshopSOSAdapter adapter;

    private ListenerRegistration sosListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_home);

        txtStatus = findViewById(R.id.txtStatus);
        recyclerSOS = findViewById(R.id.recyclerSOS);
        recyclerSOS.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        workshopId = FirebaseAuth.getInstance().getUid();

        if (workshopId == null) {
            finish();
            return;
        }

        adapter = new WorkshopSOSAdapter(this, sosList, workshopId);
        recyclerSOS.setAdapter(adapter);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 🔔 Android 13+ notification permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    201
            );
        }

        setupBottomNav();
        saveWorkshopFcmToken();
        loadWorkshopInfo();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void saveWorkshopFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {

                    db.collection("workshops")
                            .document(workshopId)
                            .set(
                                    new HashMap<String, Object>() {{
                                        put("fcmToken", token);
                                    }},
                                    SetOptions.merge()
                            );
                });
    }

    private void loadWorkshopInfo() {
        db.collection("workshops")
                .document(workshopId)
                .addSnapshotListener((doc, e) -> {

                    if (doc == null || !doc.exists()) return;

                    Boolean available = doc.getBoolean("available");

                    workshopLat = doc.getDouble("lat") != null ? doc.getDouble("lat") : 3.0738;
                    workshopLng = doc.getDouble("lng") != null ? doc.getDouble("lng") : 101.5183;

                    if (available == null || !available) {

                        txtStatus.setText("Offline");
                        txtStatus.setBackgroundColor(Color.RED);

                        sosList.clear();
                        adapter.notifyDataSetChanged();

                        if (mMap != null) mMap.clear();

                        if (sosListener != null) {
                            sosListener.remove();
                            sosListener = null;
                        }
                        return;
                    }

                    txtStatus.setText("Available");
                    txtStatus.setBackgroundColor(Color.parseColor("#16A34A"));

                    listenSOS();
                });
    }

    // ================= LISTEN SOS =================
    private void listenSOS() {

        if (sosListener != null) return;

        sosListener = db.collection("sos_requests")
                .whereEqualTo("assignedWorkshopId", workshopId)
                .whereIn("status", Arrays.asList("waiting_workshop", "accepted"))
                .addSnapshotListener((snap, err) -> {

                    if (snap == null || mMap == null) return;

                    sosList.clear();
                    mMap.clear();

                    LatLng workshopPos = new LatLng(workshopLat, workshopLng);
                    mMap.addMarker(new MarkerOptions()
                            .position(workshopPos)
                            .title("Your Workshop")
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_BLUE)));

                    mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(workshopPos, 13f));

                    for (DocumentSnapshot d : snap.getDocuments()) {

                        WorkshopSOS sos = d.toObject(WorkshopSOS.class);
                        if (sos == null) continue;

                        sos.id = d.getId();
                        sos.workshopLat = workshopLat;
                        sos.workshopLng = workshopLng;

                        sosList.add(sos);

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(sos.lat, sos.lng))
                                .title(sos.riderName)
                                .snippet(sos.problem)
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_RED)));
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void setupBottomNav() {

        BottomNavigationView nav =
                findViewById(R.id.bottomNavigation);

        nav.setSelectedItemId(R.id.nav_home);

        nav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            if (id == R.id.nav_history) {
                startActivity(
                        new Intent(this, WorkshopHistoryActivity.class)
                );
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                startActivity(
                        new Intent(this, WorkshopProfileActivity.class)
                );
                finish();
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sosListener != null) sosListener.remove();
    }
}