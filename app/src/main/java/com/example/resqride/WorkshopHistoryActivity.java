package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.WorkshopHistoryAdapter;
import com.example.resqride.models.SosRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class WorkshopHistoryActivity extends AppCompatActivity {

    RecyclerView recycler;
    WorkshopHistoryAdapter adapter;
    List<SosRequest> list = new ArrayList<>();

    FirebaseFirestore db;
    String workshopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_history);

        recycler = findViewById(R.id.recyclerHistory);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WorkshopHistoryAdapter(this, list);
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        workshopId = FirebaseAuth.getInstance().getUid();

        loadHistory();
        setupBottomNav();
    }

    // ================= LOAD HISTORY =================
    private void loadHistory() {

        db.collection("sos_requests")
                .whereEqualTo("assignedWorkshopId", workshopId)
                .whereEqualTo("status", "completed")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {

                    if (e != null) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snap == null) return;

                    list.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        SosRequest s = d.toObject(SosRequest.class);
                        if (s != null) {
                            s.id = d.getId();
                            list.add(s);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    // ================= BOTTOM NAV =================
    private void setupBottomNav() {

        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_history);

        nav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, WorkshopHomeActivity.class));
                finish();
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, WorkshopProfileActivity.class));
                finish();
            }
            return true;
        });
    }
}