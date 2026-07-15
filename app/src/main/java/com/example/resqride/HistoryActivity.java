package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.SosHistoryAdapter;
import com.example.resqride.models.SosRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    // UI
    RecyclerView recycler;
    Button btnActive, btnPast;
    EditText edtSearch;
    BottomNavigationView bottomNav;

    // Firebase
    FirebaseFirestore db;
    String userId;

    // State
    boolean showActive = false;

    // Data
    List<SosRequest> list = new ArrayList<>();      // shown list
    List<SosRequest> fullList = new ArrayList<>();  // original list
    SosHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // ===== Bind UI =====
        recycler = findViewById(R.id.recyclerHistory);
        btnActive = findViewById(R.id.btnActive);
        btnPast = findViewById(R.id.btnPast);
        edtSearch = findViewById(R.id.edtSearch);
        bottomNav = findViewById(R.id.bottomNavigation);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SosHistoryAdapter(this, list);
        recycler.setAdapter(adapter);

        // ===== Firebase =====
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        // ===== Tabs =====
        btnActive.setOnClickListener(v -> {
            showActive = true;
            edtSearch.setText("");
            loadHistory();
            updateTabs();
        });

        btnPast.setOnClickListener(v -> {
            showActive = false;
            edtSearch.setText("");
            loadHistory();
            updateTabs();
        });

        // ===== Search =====
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterHistory(s.toString());
            }
        });

        loadHistory();
        updateTabs();
        setupBottomNav();
    }

    // ================= TAB UI =================
    private void updateTabs() {
        if (showActive) {
            btnActive.setBackgroundTintList(getColorStateList(R.color.blue));
            btnActive.setTextColor(getColor(R.color.white));

            btnPast.setBackgroundTintList(getColorStateList(R.color.gray));
            btnPast.setTextColor(getColor(R.color.black));
        } else {
            btnPast.setBackgroundTintList(getColorStateList(R.color.blue));
            btnPast.setTextColor(getColor(R.color.white));

            btnActive.setBackgroundTintList(getColorStateList(R.color.gray));
            btnActive.setTextColor(getColor(R.color.black));
        }
    }

    // ================= LOAD HISTORY =================
    private void loadHistory() {

        if (userId == null) return;

        Query query;

        if (showActive) {
            // 🔥 ACTIVE SOS (Firestore SAFE)
            query = db.collection("sos_requests")
                    .whereEqualTo("riderId", userId)
                    .whereIn("status", List.of(
                            "selecting",
                            "selecting_workshop",
                            "waiting_workshop",
                            "accepted",
                            "on_the_way",
                            "arrived",
                            "in_progress"
                    ))
                    .orderBy("time", Query.Direction.DESCENDING);
        } else {
            // ✅ COMPLETED
            query = db.collection("sos_requests")
                    .whereEqualTo("riderId", userId)
                    .whereEqualTo("status", "completed")
                    .orderBy("time", Query.Direction.DESCENDING);
        }

        query.get().addOnSuccessListener(snapshot -> {

            list.clear();
            fullList.clear();

            for (QueryDocumentSnapshot d : snapshot) {
                SosRequest s = d.toObject(SosRequest.class);
                s.id = d.getId();
                fullList.add(s);
            }

            list.addAll(fullList);
            adapter.notifyDataSetChanged();
        });
    }

    // ================= SEARCH FILTER =================
    private void filterHistory(String keyword) {

        list.clear();

        if (keyword.isEmpty()) {
            list.addAll(fullList);
        } else {
            keyword = keyword.toLowerCase();

            for (SosRequest s : fullList) {

                boolean match =
                        (s.problem != null &&
                                s.problem.toLowerCase().contains(keyword)) ||

                                (s.assignedWorkshopName != null &&
                                        s.assignedWorkshopName.toLowerCase().contains(keyword)) ||

                                (s.vehicleModel != null &&
                                        s.vehicleModel.toLowerCase().contains(keyword));

                if (match) {
                    list.add(s);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // ================= BOTTOM NAV =================
    private void setupBottomNav() {

        bottomNav.setSelectedItemId(R.id.nav_history);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_garage) {
                startActivity(new Intent(this, GarageActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return true;
        });
    }
}