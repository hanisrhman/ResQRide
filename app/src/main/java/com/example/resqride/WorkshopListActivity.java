package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.WorkshopAdapter;
import com.example.resqride.models.WorkshopModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkshopListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkshopAdapter adapter;
    private ArrayList<WorkshopModel> workshopList;

    private FirebaseFirestore db;
    private String sosId; // passed from SosActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_list);

        // 🔹 Get SOS ID
        sosId = getIntent().getStringExtra("sosId");

        if (sosId == null) {
            Toast.makeText(this, "Invalid SOS request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerWorkshops);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        workshopList = new ArrayList<>();

        // ✅ Adapter with click callback
        adapter = new WorkshopAdapter(workshopList, workshop -> {
            sendRequestToWorkshop(workshop);
        });

        recyclerView.setAdapter(adapter);

        loadAvailableWorkshops();
    }

    // ================= LOAD WORKSHOPS =================
    private void loadAvailableWorkshops() {

        db.collection("workshops")
                .whereEqualTo("approved", true)
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    workshopList.clear();

                    for (DocumentSnapshot doc : querySnapshot) {

                        WorkshopModel workshop = doc.toObject(WorkshopModel.class);
                        if (workshop != null) {
                            workshop.id = doc.getId();
                            workshopList.add(workshop);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load workshops",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // ================= SEND REQUEST =================
    private void sendRequestToWorkshop(WorkshopModel workshop) {

        Map<String, Object> update = new HashMap<>();
        update.put("assignedWorkshopId", workshop.id);
        update.put("assignedWorkshopName", workshop.name);
        update.put("status", "waiting_workshop");

        db.collection("sos_requests")
                .document(sosId)
                .update(update)
                .addOnSuccessListener(v -> {

                    Toast.makeText(
                            this,
                            "Request sent to " + workshop.name,
                            Toast.LENGTH_LONG
                    ).show();

                    // 🔹 Go to waiting screen (optional but recommended)
                    Intent intent = new Intent(
                            WorkshopListActivity.this,
                            PreparingEmergencyActivity.class
                    );
                    intent.putExtra("sosId", sosId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to send request",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}