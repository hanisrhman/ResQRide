package com.example.resqride;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.MaintenanceAdapter;
import com.example.resqride.models.Maintenance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class VehicleDetailActivity extends AppCompatActivity {

    // UI
    TextView txtModel, txtPlate, txtYear;
    RecyclerView recyclerMaintenance;
    FloatingActionButton btnAddMaintenance;
    ImageView btnBack;

    // Firebase
    FirebaseFirestore db;
    FirebaseAuth auth;
    String userId, vehicleId;

    // Recycler
    List<Maintenance> maintenanceList = new ArrayList<>();
    MaintenanceAdapter adapter;

    // Listener
    ListenerRegistration maintenanceListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        // ===== INIT FIREBASE =====
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();

        if (userId == null) {
            finish();
            return;
        }

        // ===== GET VEHICLE ID =====
        vehicleId = getIntent().getStringExtra("vehicleId");

        if (vehicleId == null) {
            Toast.makeText(this, "Vehicle not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ===== UI =====
        txtModel = findViewById(R.id.txtModel);
        txtPlate = findViewById(R.id.txtPlate);
        txtYear = findViewById(R.id.txtYear);
        recyclerMaintenance = findViewById(R.id.recyclerMaintenance);
        btnAddMaintenance = findViewById(R.id.btnAddMaintenance);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // ===== RECYCLER =====
        recyclerMaintenance.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MaintenanceAdapter(
                this,
                vehicleId,
                maintenanceList
        );

        recyclerMaintenance.setAdapter(adapter);

        // ===== LOAD DATA =====
        loadVehicleDetails();
        listenMaintenanceHistory();

        // ===== ADD MAINTENANCE =====
        btnAddMaintenance.setOnClickListener(v ->
                AddMaintenanceDialog.show(this, vehicleId)
        );
    }

    // ================= LOAD VEHICLE =================
    private void loadVehicleDetails() {

        db.collection("users")
                .document(userId)
                .collection("vehicles")
                .document(vehicleId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    txtModel.setText(doc.getString("model"));
                    txtPlate.setText(doc.getString("plateNumber"));

                    String year = doc.getString("year");
                    txtYear.setText("Manufacture Year: " + (year == null ? "-" : year));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ================= REAL-TIME MAINTENANCE =================
    private void listenMaintenanceHistory() {

        maintenanceListener = db.collection("users")
                .document(userId)
                .collection("vehicles")
                .document(vehicleId)
                .collection("maintenance")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null) return;

                    maintenanceList.clear();

                    for (QueryDocumentSnapshot d : snapshot) {
                        Maintenance m = d.toObject(Maintenance.class);
                        m.id = d.getId(); // 🔥 VERY IMPORTANT
                        maintenanceList.add(m);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    // ================= CLEANUP =================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (maintenanceListener != null) {
            maintenanceListener.remove();
        }
    }
}