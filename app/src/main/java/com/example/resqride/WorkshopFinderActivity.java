package com.example.resqride;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.WorkshopCardAdapter;
import com.example.resqride.models.Workshop;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.*;

import java.util.*;

public class WorkshopFinderActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    // UI
    RecyclerView recyclerWorkshop;
    TextInputEditText edtSearch;
    Chip chipOpen, chipRating, chipNear;
    MaterialToolbar topBar;

    // Adapter
    WorkshopCardAdapter adapter;

    // Data
    List<Workshop> workshopList = new ArrayList<>();
    List<Workshop> filteredList = new ArrayList<>();

    // Firebase
    FirebaseFirestore db;

    // Location
    FusedLocationProviderClient locationClient;
    Location riderLocation;

    // Map
    GoogleMap mMap;

    // Markers
    HashMap<String, Marker> markerMap = new HashMap<>();

    Marker riderMarker;

    // Permission
    private static final int LOCATION_REQ = 1001;

    // ================= ON CREATE =================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_finder);

        // Toolbar
        topBar = findViewById(R.id.topBar);
        topBar.setNavigationOnClickListener(v -> finish());

        // UI
        recyclerWorkshop = findViewById(R.id.recyclerWorkshop);
        edtSearch = findViewById(R.id.edtSearch);
        chipOpen = findViewById(R.id.chipOpen);
        chipRating = findViewById(R.id.chipRating);
        chipNear = findViewById(R.id.chipNear);

        recyclerWorkshop.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false));

        adapter = new WorkshopCardAdapter(
                this,
                filteredList,
                workshop -> focusMarker(workshop.id)
        );

        recyclerWorkshop.setAdapter(adapter);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // Location
        locationClient =
                LocationServices.getFusedLocationProviderClient(this);

        // Map
        SupportMapFragment mapFragment =
                (SupportMapFragment)
                        getSupportFragmentManager()
                                .findFragmentById(R.id.map);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        setupSearch();
        setupFilters();

        requestLocation();
    }

    // ================= MAP READY =================

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Marker click listener
        mMap.setOnMarkerClickListener(marker -> {

            Object tag = marker.getTag();

            if (tag != null) {

                String id = tag.toString();

                highlightCard(id);

                focusMarker(id);
            }

            return false;
        });
    }

    // ================= LOCATION =================

    private void requestLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    LOCATION_REQ);

            return;
        }

        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location == null) {

                        Toast.makeText(
                                this,
                                "Cannot get location",
                                Toast.LENGTH_SHORT).show();

                        return;
                    }

                    riderLocation = location;

                    listenRealtimeWorkshops();
                });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (requestCode == LOCATION_REQ &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            requestLocation();
        }
    }

    // ================= REALTIME FIRESTORE =================

    private void listenRealtimeWorkshops() {

        db.collection("workshop_finder")
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null)
                        return;

                    workshopList.clear();
                    markerMap.clear();

                    if (mMap != null)
                        mMap.clear();

                    addRiderMarker();

                    for (DocumentSnapshot doc : snapshot) {

                        Workshop w =
                                doc.toObject(Workshop.class);

                        if (w == null) continue;

                        w.id = doc.getId();

                        calculateDistance(w);

                        workshopList.add(w);

                        addWorkshopMarker(w);
                    }

                    applyFilter();
                });
    }

    // ================= ADD RIDER MARKER =================

    private void addRiderMarker() {

        LatLng riderPos =
                new LatLng(
                        riderLocation.getLatitude(),
                        riderLocation.getLongitude());

        riderMarker =
                mMap.addMarker(
                        new MarkerOptions()
                                .position(riderPos)
                                .title("You")
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(
                                                BitmapDescriptorFactory.HUE_AZURE))
                );

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        riderPos,
                        14f
                )
        );
    }

    // ================= ADD WORKSHOP MARKER =================

    private void addWorkshopMarker(Workshop w) {

        LatLng pos = new LatLng(w.lat, w.lng);

        Marker marker =
                mMap.addMarker(
                        new MarkerOptions()
                                .position(pos)
                                .title(w.name)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(
                                                BitmapDescriptorFactory.HUE_RED))
                );

        if (marker != null) {

            marker.setTag(w.id);

            markerMap.put(w.id, marker);
        }
    }

    // ================= DISTANCE =================

    private void calculateDistance(Workshop w) {

        Location wl = new Location("");

        wl.setLatitude(w.lat);
        wl.setLongitude(w.lng);

        double km =
                riderLocation.distanceTo(wl) / 1000;

        w.setDistance(km);
    }

    // ================= MARKER FOCUS =================

    private void focusMarker(String id) {

        Marker marker = markerMap.get(id);

        if (marker == null) return;

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        marker.getPosition(),
                        16f
                )
        );

        marker.showInfoWindow();
    }

    // ================= HIGHLIGHT CARD =================

    private void highlightCard(String id) {

        for (int i = 0; i < filteredList.size(); i++) {

            if (filteredList.get(i).id.equals(id)) {

                recyclerWorkshop.smoothScrollToPosition(i);

                break;
            }
        }
    }

    // ================= SEARCH =================

    private void setupSearch() {

        edtSearch.addTextChangedListener(
                new TextWatcher() {

                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {}

                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {}

                    public void afterTextChanged(
                            Editable s) {

                        applyFilter();
                    }
                });
    }

    // ================= FILTER =================

    private void setupFilters() {

        chipOpen.setOnCheckedChangeListener(
                (v, b) -> applyFilter());

        chipRating.setOnCheckedChangeListener(
                (v, b) -> applyFilter());

        chipNear.setOnCheckedChangeListener(
                (v, b) -> applyFilter());
    }

    private void applyFilter() {

        filteredList.clear();

        String keyword =
                edtSearch.getText() == null ?
                        "" :
                        edtSearch.getText()
                                .toString()
                                .toLowerCase();

        for (Workshop w : workshopList) {

            if (!w.name.toLowerCase()
                    .contains(keyword))
                continue;

            if (chipOpen.isChecked()
                    && !w.isOpenNow())
                continue;

            if (chipRating.isChecked()
                    && w.rating < 4)
                continue;

            filteredList.add(w);
        }

        if (chipNear.isChecked()) {

            Collections.sort(
                    filteredList,
                    Comparator.comparingDouble(
                            a -> a.distanceKm));
        }

        adapter.notifyDataSetChanged();
    }
}