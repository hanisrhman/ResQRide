package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.VehicleAdapter;
import com.example.resqride.models.Vehicle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class GarageActivity extends AppCompatActivity {

    RecyclerView recycler;
    FloatingActionButton fabAdd;
    BottomNavigationView bottomNav;

    List<Vehicle> vehicleList = new ArrayList<>();
    VehicleAdapter adapter;

    FirebaseFirestore db;
    FirebaseUser user;

    ListenerRegistration vehicleListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage);

        recycler = findViewById(R.id.recyclerVehicles);
        fabAdd = findViewById(R.id.btnAddVehicle);
        bottomNav = findViewById(R.id.bottomNavigation);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new VehicleAdapter(this, vehicleList);
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        setupAddButton();
        setupBottomNav();
    }

    // START realtime listener
    @Override
    protected void onStart() {
        super.onStart();
        loadVehiclesRealtime();
    }

    // STOP realtime listener
    @Override
    protected void onStop() {
        super.onStop();

        if (vehicleListener != null) {
            vehicleListener.remove();
            vehicleListener = null;
        }
    }

    // REALTIME LOAD
    private void loadVehiclesRealtime() {

        if (user == null || user.isAnonymous()) {

            vehicleList.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        vehicleListener = db.collection("users")
                .document(user.getUid())
                .collection("vehicles")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null) return;

                    vehicleList.clear();

                    for (DocumentSnapshot d : snapshot.getDocuments()) {

                        Vehicle v = d.toObject(Vehicle.class);

                        if (v != null) {
                            v.id = d.getId();
                            vehicleList.add(v);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    // ADD VEHICLE BUTTON
    private void setupAddButton() {

        if (user == null || user.isAnonymous()) {

            fabAdd.setAlpha(0.4f);

            fabAdd.setOnClickListener(v ->
                    Toast.makeText(this,
                            "Please sign up to add vehicles",
                            Toast.LENGTH_LONG).show());

        } else {

            fabAdd.setOnClickListener(v ->
                    startActivity(
                            new Intent(this, AddVehicleActivity.class)
                    ));
        }
    }

    // BOTTOM NAVIGATION
    private void setupBottomNav() {

        bottomNav.setSelectedItemId(R.id.nav_garage);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                startActivity(new Intent(this, HomeActivity.class));
                finish();

            } else if (id == R.id.nav_history) {

                startActivity(new Intent(this, HistoryActivity.class));
                finish();

            } else if (id == R.id.nav_profile) {

                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            }

            return true;
        });
    }
}