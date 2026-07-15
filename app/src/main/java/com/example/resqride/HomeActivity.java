package com.example.resqride;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.RecentServiceAdapter;
import com.example.resqride.models.SosRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 100;

    TextView txtLocation, txtWelcome, txtWelcomeSub, txtNoService;
    Button btnSOS, btnWorkshop, btnGarage, btnSignUp;
    LinearLayout layoutNotice;
    RecyclerView recyclerRecent;

    FirebaseAuth auth;
    FirebaseFirestore db;
    FusedLocationProviderClient locationClient;

    List<SosRequest> recentList = new ArrayList<>();
    RecentServiceAdapter recentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtLocation = findViewById(R.id.txtLocation);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtWelcomeSub = findViewById(R.id.txtWelcomeSub);
        txtNoService = findViewById(R.id.txtNoService);

        btnSOS = findViewById(R.id.btnSOS);
        btnWorkshop = findViewById(R.id.btnWorkshop);
        btnGarage = findViewById(R.id.btnGarage);
        btnSignUp = findViewById(R.id.btnSignUp);

        layoutNotice = findViewById(R.id.layoutNotice);
        recyclerRecent = findViewById(R.id.recyclerRecent);

        recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        recentAdapter = new RecentServiceAdapter(this, recentList);
        recyclerRecent.setAdapter(recentAdapter);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        loadWelcomeText();
        checkLocationPermission();
        setupButtons();
        loadRecentServices();
        setupBottomNav();
    }

    // ================= WELCOME =================
    private void loadWelcomeText() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null || user.isAnonymous()) {
            txtWelcome.setText("Welcome");
            txtWelcomeSub.setText("Stay safe on your journey");
            layoutNotice.setVisibility(View.VISIBLE);
            return;
        }

        layoutNotice.setVisibility(View.GONE);

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    txtWelcome.setText(
                            name != null && !name.isEmpty()
                                    ? "Welcome, " + name
                                    : "Welcome"
                    );
                });
    }

    // ================= LOCATION =================
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) showAddress(location);
                    else txtLocation.setText("Location unavailable");
                });
    }

    private void showAddress(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list =
                    geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1
                    );

            txtLocation.setText(
                    !list.isEmpty() && list.get(0).getLocality() != null
                            ? list.get(0).getLocality()
                            : "Your location"
            );
        } catch (Exception e) {
            txtLocation.setText("Location error");
        }
    }

    // ================= BUTTONS =================
    private void setupButtons() {

        btnSOS.setOnClickListener(v ->
                startActivity(new Intent(this, SosActivity.class)));

        btnWorkshop.setOnClickListener(v ->
                startActivity(new Intent(this, WorkshopFinderActivity.class)));

        btnGarage.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null || user.isAnonymous()) {
                Toast.makeText(this,
                        "Please sign up to use Garage feature",
                        Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(this, GarageActivity.class));
        });

        btnSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
    private void loadRecentServices() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null || user.isAnonymous()) {
            recyclerRecent.setVisibility(View.GONE);
            txtNoService.setVisibility(View.VISIBLE);
            txtNoService.setText("Sign up to view recent services");
            return;
        }

        recyclerRecent.setVisibility(View.VISIBLE);

        db.collection("sos_requests")
                .whereEqualTo("riderId", user.getUid())
                .whereEqualTo("status", "completed")
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(snapshot -> {

                    recentList.clear();

                    for (QueryDocumentSnapshot d : snapshot) {
                        SosRequest s = d.toObject(SosRequest.class);
                        if (s != null) {
                            s.id = d.getId();
                            recentList.add(s);
                        }
                    }

                    recentAdapter.notifyDataSetChanged();

                    txtNoService.setVisibility(
                            recentList.isEmpty() ? View.VISIBLE : View.GONE
                    );
                })
                .addOnFailureListener(e -> {
                    txtNoService.setVisibility(View.VISIBLE);
                    txtNoService.setText("Unable to load recent services");
                });
    }

    // ================= BOTTOM NAV =================
    private void setupBottomNav() {

        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_home);

        nav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            if (id == R.id.nav_garage) {
                startActivity(new Intent(this, GarageActivity.class));
                overridePendingTransition(
                        R.anim.fade_in,
                        R.anim.fade_out
                );
                finish();
                return true;
            }

            if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                overridePendingTransition(
                        R.anim.fade_in,
                        R.anim.fade_out
                );
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(
                        R.anim.fade_in,
                        R.anim.fade_out
                );
                finish();
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWelcomeText();
    }
}