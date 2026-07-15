package com.example.resqride;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PickLocationActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    GoogleMap mMap;
    Marker marker;

    EditText edtSearch;
    Button btnSearch, btnConfirm;

    double selectedLat = 0;
    double selectedLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnConfirm = findViewById(R.id.btnConfirm);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnSearch.setOnClickListener(v -> searchLocation());
        btnConfirm.setOnClickListener(v -> confirmLocation());
    }

    // ================= MAP =================
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng shahAlam = new LatLng(3.0738, 101.5183);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shahAlam, 14f));

        mMap.setOnMapClickListener(this::placeMarker);
    }

    // ================= SEARCH =================
    private void searchLocation() {

        String location = edtSearch.getText().toString().trim();
        if (location.isEmpty()) {
            Toast.makeText(this,
                    "Enter a location",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses =
                    geocoder.getFromLocationName(location, 1);

            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(this,
                        "Location not found",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = addresses.get(0);
            LatLng latLng = new LatLng(
                    address.getLatitude(),
                    address.getLongitude()
            );

            placeMarker(latLng);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Search failed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ================= PIN =================
    private void placeMarker(LatLng latLng) {

        selectedLat = latLng.latitude;
        selectedLng = latLng.longitude;

        if (marker != null) marker.remove();

        marker = mMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title("Selected Location")
                        .draggable(true)
        );

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 16f)
        );

        mMap.setOnMarkerDragListener(
                new GoogleMap.OnMarkerDragListener() {
                    @Override public void onMarkerDragStart(@NonNull Marker m) {}
                    @Override public void onMarkerDrag(@NonNull Marker m) {}

                    @Override
                    public void onMarkerDragEnd(@NonNull Marker m) {
                        selectedLat = m.getPosition().latitude;
                        selectedLng = m.getPosition().longitude;
                    }
                }
        );
    }

    // ================= CONFIRM =================
    private void confirmLocation() {

        if (selectedLat == 0) {
            Toast.makeText(this,
                    "Please select a location",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent();
        i.putExtra("lat", selectedLat);
        i.putExtra("lng", selectedLng);
        setResult(RESULT_OK, i);
        finish();
    }
}