package com.example.resqride;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.FirebaseFirestore;

public class TrackMechanicActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String sosId;

    private Marker riderMarker, mechanicMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_mechanic);

        sosId = getIntent().getStringExtra("sosId");

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        listenLiveLocation();
    }

    private void listenLiveLocation() {
        FirebaseFirestore.getInstance()
                .collection("sos_requests")
                .document(sosId)
                .addSnapshotListener((doc, e) -> {

                    if (doc == null || !doc.exists()) return;

                    Double rLat = doc.getDouble("lat");
                    Double rLng = doc.getDouble("lng");
                    Double mLat = doc.getDouble("mechanicLat");
                    Double mLng = doc.getDouble("mechanicLng");

                    if (rLat == null || mLat == null) return;

                    LatLng rider = new LatLng(rLat, rLng);
                    LatLng mechanic = new LatLng(mLat, mLng);

                    if (riderMarker == null) {
                        riderMarker = mMap.addMarker(
                                new MarkerOptions()
                                        .position(rider)
                                        .title("You"));
                    }

                    if (mechanicMarker == null) {
                        mechanicMarker = mMap.addMarker(
                                new MarkerOptions()
                                        .position(mechanic)
                                        .title("Mechanic")
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(
                                                        BitmapDescriptorFactory.HUE_BLUE)));
                    } else {
                        mechanicMarker.setPosition(mechanic);
                    }

                    mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(rider, 15));
                });
    }
}