package com.example.resqride;

import static android.content.Intent.getIntent;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NavigationActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final int LOCATION_REQ = 100;
    private static final String API_KEY = "AIzaSyDelRrYRYfDAsdIhNxWXh7CtrolWt-O-j4";

    private GoogleMap mMap;
    private FirebaseFirestore db;

    private String sosId;
    private double riderLat, riderLng;

    private Marker riderMarker;
    private Marker mechanicMarker;
    private Polyline routePolyline;

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    private LinearLayout infoCard;
    private ImageView btnToggleCard;
    private TextView txtRiderName, txtProblem, txtAddress,
            txtPlate, txtDistance;
    private ImageView imgProblem;
    private Button btnNavigate, btnArrived,
            btnComplete, btnChat;

    private boolean cardVisible = true;
    private LatLng previousMechanicPos = null;

    // ===================================================
    // ON CREATE
    // ===================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        sosId = getIntent().getStringExtra("sosId");
        if (sosId == null) finish();

        db = FirebaseFirestore.getInstance();
        locationClient =
                LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        setupToggle();
        setupButtons();
        loadSOS();

        SupportMapFragment mapFragment =
                (SupportMapFragment)
                        getSupportFragmentManager()
                                .findFragmentById(R.id.map);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    // ===================================================
    // BIND UI
    // ===================================================

    private void bindViews() {
        infoCard = findViewById(R.id.infoCard);
        btnToggleCard = findViewById(R.id.btnToggleCard);

        txtRiderName = findViewById(R.id.txtRiderName);
        txtProblem = findViewById(R.id.txtProblem);
        txtAddress = findViewById(R.id.txtAddress);
        txtPlate = findViewById(R.id.txtPlate);
        txtDistance = findViewById(R.id.txtDistance);

        imgProblem = findViewById(R.id.imgProblem);

        btnNavigate = findViewById(R.id.btnNavigate);
        btnArrived = findViewById(R.id.btnArrived);
        btnComplete = findViewById(R.id.btnComplete);
        btnChat = findViewById(R.id.btnChat);
    }

    // ===================================================
    // CARD TOGGLE
    // ===================================================

    private void setupToggle() {
        btnToggleCard.setOnClickListener(v -> {
            if (cardVisible) {
                infoCard.animate()
                        .translationY(infoCard.getHeight())
                        .setDuration(300);
            } else {
                infoCard.animate()
                        .translationY(0)
                        .setDuration(300);
            }
            cardVisible = !cardVisible;
        });
    }

    // ===================================================
    // LOAD SOS
    // ===================================================

    private void loadSOS() {
        db.collection("sos_requests")
                .document(sosId)
                .addSnapshotListener((doc, error) -> {

                    if (doc == null || !doc.exists()) return;

                    riderLat = doc.getDouble("lat");
                    riderLng = doc.getDouble("lng");

                    txtRiderName.setText(doc.getString("riderName"));
                    txtProblem.setText(doc.getString("problem"));
                    txtAddress.setText("📍 " + doc.getString("address"));
                    txtPlate.setText("Plate: " + doc.getString("vehiclePlate"));

                    String status = doc.getString("status");
                    updateButtons(status);

                    String img = doc.getString("mediaUrl");

                    if (img != null && !img.isEmpty()) {
                        imgProblem.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(img)
                                .into(imgProblem);
                    } else {
                        imgProblem.setVisibility(View.GONE);
                    }

                    showRiderMarker();
                });
    }

    // ===================================================
    // BUTTON FLOW
    // ===================================================

    private void updateButtons(String status) {

        btnNavigate.setVisibility(View.GONE);
        btnArrived.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnChat.setVisibility(View.GONE);

        if ("accepted".equals(status)) {
            btnNavigate.setVisibility(View.VISIBLE);
            btnChat.setVisibility(View.VISIBLE);
        }

        else if ("navigating".equals(status)) {
            btnArrived.setVisibility(View.VISIBLE);
            btnChat.setVisibility(View.VISIBLE);
        }

        else if ("arrived".equals(status)) {
            btnComplete.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtons() {

        btnNavigate.setOnClickListener(v ->
                db.collection("sos_requests")
                        .document(sosId)
                        .update("status", "navigating")
        );

        btnArrived.setOnClickListener(v ->
                db.collection("sos_requests")
                        .document(sosId)
                        .update("status", "arrived")
        );

        btnComplete.setOnClickListener(v -> {
            db.collection("sos_requests")
                    .document(sosId)
                    .update("status", "completed");

            startActivity(new Intent(this,
                    WorkshopHistoryActivity.class));
            finish();
        });

        btnChat.setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("sosId", sosId);
            startActivity(i);
        });
    }

    // ===================================================
    // MAP READY
    // ===================================================

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        startTracking();
    }

    private void showRiderMarker() {

        if (mMap == null) return;

        LatLng rider = new LatLng(riderLat, riderLng);

        if (riderMarker == null) {
            riderMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(rider)
                            .title("Rider")
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(
                                            BitmapDescriptorFactory.HUE_RED))
            );
        } else {
            riderMarker.setPosition(rider);
        }

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(rider, 15f)
        );
    }

    // ===================================================
    // LIVE TRACKING

    private void startTracking() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQ);
            return;
        }

        LocationRequest request = LocationRequest.create()
                .setInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(
                    @NonNull LocationResult result) {

                Location loc = result.getLastLocation();
                if (loc == null) return;

                LatLng newPos = new LatLng(
                        loc.getLatitude(),
                        loc.getLongitude()
                );

                animateMarker(newPos);
                updateDistance(loc);
                drawDirection(newPos);
            }
        };

        locationClient.requestLocationUpdates(
                request,
                locationCallback,
                getMainLooper());
    }

    // ===================================================
    // SMOOTH MARKER ANIMATION
    // ===================================================

    private void animateMarker(LatLng newPos) {

        if (mechanicMarker == null) {
            mechanicMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(newPos)
                            .title("You")
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(
                                            BitmapDescriptorFactory.HUE_BLUE))
            );
            previousMechanicPos = newPos;
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(800);
        animator.setInterpolator(new LinearInterpolator());

        LatLng start = previousMechanicPos;

        animator.addUpdateListener(animation -> {
            float v = animation.getAnimatedFraction();

            double lat =
                    v * newPos.latitude + (1 - v) * start.latitude;
            double lng =
                    v * newPos.longitude + (1 - v) * start.longitude;

            mechanicMarker.setPosition(new LatLng(lat, lng));
        });

        animator.start();

        previousMechanicPos = newPos;

        mMap.animateCamera(
                CameraUpdateFactory.newLatLng(newPos));
    }

    // ===================================================
    // DISTANCE + FIRESTORE SYNC
    // ===================================================

    private void updateDistance(Location mechanicLoc) {

        float[] result = new float[1];

        Location.distanceBetween(
                mechanicLoc.getLatitude(),
                mechanicLoc.getLongitude(),
                riderLat,
                riderLng,
                result);

        double km = result[0] / 1000;

        txtDistance.setText(
                String.format("%.2f km away", km));

        db.collection("sos_requests")
                .document(sosId)
                .update("liveDistanceKm", km);
    }

    // ===================================================
    // GOOGLE DIRECTIONS API ROUTE
    // ===================================================

    private void drawDirection(LatLng mechanicPos) {

        if (riderMarker == null) return;

        new Thread(() -> {
            try {

                String url =
                        "https://maps.googleapis.com/maps/api/directions/json?"
                                + "origin=" + mechanicPos.latitude + "," + mechanicPos.longitude
                                + "&destination=" + riderLat + "," + riderLng
                                + "&key=" + API_KEY;

                Scanner sc =
                        new Scanner(new URL(url).openStream())
                                .useDelimiter("\\A");

                String json = sc.hasNext() ? sc.next() : "";

                JSONObject obj = new JSONObject(json);

                JSONArray routes =
                        obj.getJSONArray("routes");

                if (routes.length() == 0) return;

                String points =
                        routes.getJSONObject(0)
                                .getJSONObject("overview_polyline")
                                .getString("points");

                List<LatLng> decoded =
                        decodePolyline(points);

                runOnUiThread(() -> {

                    if (routePolyline != null)
                        routePolyline.remove();

                    routePolyline =
                            mMap.addPolyline(
                                    new PolylineOptions()
                                            .addAll(decoded)
                                            .width(10f)
                                            .color(Color.BLUE)
                            );
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ===================================================
    // POLYLINE DECODER
    // ===================================================

    private List<LatLng> decodePolyline(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, lat = 0, lng = 0;

        while (index < encoded.length()) {

            int b, shift = 0, result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            lat += ((result & 1) != 0
                    ? ~(result >> 1)
                    : (result >> 1));

            shift = 0;
            result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            lng += ((result & 1) != 0
                    ? ~(result >> 1)
                    : (result >> 1));

            poly.add(new LatLng(
                    lat / 1E5,
                    lng / 1E5));
        }

        return poly;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationClient != null && locationCallback != null)
            locationClient.removeLocationUpdates(locationCallback);
    }
}