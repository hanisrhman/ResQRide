package com.example.resqride;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.WorkshopSelectAdapter;
import com.example.resqride.models.Workshop;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class WorkshopSelectionActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    RecyclerView recycler;
    WorkshopSelectAdapter adapter;

    List<Workshop> workshopList = new ArrayList<>();

    FirebaseFirestore db;
    GoogleMap mMap;

    double riderLat, riderLng;
    String sosId;

    BottomSheetBehavior<?> bottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_selection);

        riderLat = getIntent().getDoubleExtra("lat",0);
        riderLng = getIntent().getDoubleExtra("lng",0);
        sosId = getIntent().getStringExtra("sosId");

        db = FirebaseFirestore.getInstance();

        recycler = findViewById(R.id.recyclerWorkshops);

        recycler.setLayoutManager(
                new LinearLayoutManager(this));

        adapter = new WorkshopSelectAdapter(
                this,
                workshopList,
                sosId,
                riderLat,
                riderLng
        );

        recycler.setAdapter(adapter);

        bottomSheet =
                BottomSheetBehavior.from(
                        findViewById(R.id.bottomSheet));

        bottomSheet.setPeekHeight(300);

        SupportMapFragment mapFragment =
                (SupportMapFragment)getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        loadWorkshops();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        LatLng rider =
                new LatLng(riderLat, riderLng);

        mMap.addMarker(
                new MarkerOptions()
                        .position(rider)
                        .title("You")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(
                                        BitmapDescriptorFactory.HUE_BLUE)));

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(rider,14f));
    }

    private void loadWorkshops(){

        db.collection("workshops")
                .whereEqualTo("approved", true)
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    workshopList.clear();

                    if(mMap != null)
                        mMap.clear();

                    LatLngBounds.Builder bounds =
                            new LatLngBounds.Builder();

                    LatLng rider =
                            new LatLng(riderLat,riderLng);

                    mMap.addMarker(
                            new MarkerOptions()
                                    .position(rider)
                                    .title("You")
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(
                                                    BitmapDescriptorFactory.HUE_BLUE)));

                    bounds.include(rider);

                    for(QueryDocumentSnapshot d : snapshot){

                        Workshop w =
                                d.toObject(Workshop.class);

                        w.id = d.getId();

                        workshopList.add(w);

                        LatLng pos =
                                new LatLng(w.lat,w.lng);

                        mMap.addMarker(
                                new MarkerOptions()
                                        .position(pos)
                                        .title(w.name));

                        bounds.include(pos);
                    }

                    Collections.sort(workshopList,
                            Comparator.comparingDouble(
                                    w -> distance(w.lat,w.lng)));

                    adapter.notifyDataSetChanged();

                    if(workshopList.size()>0){

                        mMap.animateCamera(
                                CameraUpdateFactory
                                        .newLatLngBounds(
                                                bounds.build(),120));
                    }

                });
    }

    private double distance(double lat,double lng){

        float[] r = new float[1];

        Location.distanceBetween(
                riderLat,riderLng,
                lat,lng,
                r);

        return r[0];
    }
}