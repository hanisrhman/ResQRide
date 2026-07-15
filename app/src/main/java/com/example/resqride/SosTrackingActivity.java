package com.example.resqride;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SosTrackingActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String sosId;

    private Marker riderMarker;
    private Marker mechanicMarker;
    private Polyline routePolyline;

    private LatLng riderPos;
    private LatLng mechanicPos;
    private LatLng lastRoutePosition;

    private TextView txtStatus, txtDistance;
    private boolean paymentOpened = false;

    private static final String API_KEY = "YOUR_API_KEY";

    // ===================================================
    // ON CREATE
    // ===================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_tracking);

        sosId = getIntent().getStringExtra("sosId");
        if (sosId == null) finish();

        db = FirebaseFirestore.getInstance();

        txtStatus = findViewById(R.id.txtStatus);
        txtDistance = findViewById(R.id.txtDistance);

        findViewById(R.id.btnChat).setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("sosId", sosId);
            startActivity(i);
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    // ===================================================
    // MAP READY
    // ===================================================

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        listenSOS();
    }

    // ===================================================
    // REALTIME LISTENER
    // ===================================================

    private void listenSOS() {

        db.collection("sos_requests")
                .document(sosId)
                .addSnapshotListener((doc, error) -> {

                    if (doc == null || !doc.exists()) return;

                    Double rLat = doc.getDouble("lat");
                    Double rLng = doc.getDouble("lng");

                    Double mLat = doc.getDouble("mechanicLat");
                    Double mLng = doc.getDouble("mechanicLng");

                    if (rLat == null || rLng == null) return;

                    riderPos = new LatLng(rLat, rLng);
                    updateRiderMarker();

                    if (mLat != null && mLng != null) {

                        mechanicPos = new LatLng(mLat, mLng);
                        updateMechanicMarker();

                        // Only redraw route if position changed significantly
                        if (lastRoutePosition == null ||
                                distanceBetween(lastRoutePosition, mechanicPos) > 20) {

                            lastRoutePosition = mechanicPos;
                            drawRoute();
                        }
                    }

                    updateStatus(doc.getString("status"));
                });
    }

    // ===================================================
    // MARKERS
    // ===================================================

    private void updateRiderMarker() {

        if (riderMarker == null) {
            riderMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(riderPos)
                            .title("You")
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(
                                            BitmapDescriptorFactory.HUE_AZURE))
            );
        } else {
            riderMarker.setPosition(riderPos);
        }

        moveCameraFit();
    }

    private void updateMechanicMarker() {

        if (mechanicMarker == null) {

            mechanicMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(mechanicPos)
                            .icon(getMechanicIcon())
                            .flat(true)
            );

        } else {

            animateMarker(mechanicMarker, mechanicPos);
        }

        moveCameraFit();
    }

    // ===================================================
    // CAMERA FIT BOTH MARKERS
    // ===================================================

    private void moveCameraFit() {

        if (riderPos == null) return;

        if (mechanicPos == null) {
            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(riderPos, 15));
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(riderPos);
        builder.include(mechanicPos);

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                        builder.build(), 200));
    }

    // ===================================================
    // DRAW GOOGLE ROUTE
    // ===================================================

    private void drawRoute() {

        String url =
                "https://maps.googleapis.com/maps/api/directions/json?"
                        + "origin=" + mechanicPos.latitude + "," + mechanicPos.longitude
                        + "&destination=" + riderPos.latitude + "," + riderPos.longitude
                        + "&key=" + API_KEY;

        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        response -> {

                            try {

                                JSONArray routes =
                                        response.getJSONArray("routes");

                                if (routes.length() == 0) return;

                                JSONObject route =
                                        routes.getJSONObject(0);

                                String encoded =
                                        route.getJSONObject("overview_polyline")
                                                .getString("points");

                                List<LatLng> points =
                                        decodePolyline(encoded);

                                if (routePolyline != null)
                                    routePolyline.remove();

                                routePolyline =
                                        mMap.addPolyline(
                                                new PolylineOptions()
                                                        .addAll(points)
                                                        .width(12)
                                                        .color(Color.BLUE)
                                        );

                                JSONObject leg =
                                        route.getJSONArray("legs")
                                                .getJSONObject(0);

                                String distance =
                                        leg.getJSONObject("distance")
                                                .getString("text");

                                String duration =
                                        leg.getJSONObject("duration")
                                                .getString("text");

                                txtDistance.setText(
                                        distance + " • ETA " + duration);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
                        error -> error.printStackTrace()
                );

        Volley.newRequestQueue(this).add(request);
    }

    // ===================================================
    // SMOOTH ANIMATION
    // ===================================================

    private void animateMarker(Marker marker, LatLng to) {

        LatLng start = marker.getPosition();

        long startTime = System.currentTimeMillis();
        long duration = 800;

        Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {

                float t =
                        (System.currentTimeMillis() - startTime)
                                / (float) duration;

                if (t > 1) t = 1;

                double lat =
                        t * to.latitude + (1 - t) * start.latitude;

                double lng =
                        t * to.longitude + (1 - t) * start.longitude;

                marker.setPosition(new LatLng(lat, lng));

                if (t < 1)
                    handler.postDelayed(this, 16);
            }
        });
    }

    // ===================================================
    // STATUS
    // ===================================================

    private void updateStatus(String status) {

        if (status == null) return;

        switch (status) {

            case "accepted":
                txtStatus.setText("🚗 Mechanic on the way");
                break;

            case "arrived":
                txtStatus.setText("📍 Mechanic arrived");
                break;

            case "completed":

                txtStatus.setText("✅ Repair completed");

                if (!paymentOpened) {

                    paymentOpened = true;

                    Intent i =
                            new Intent(this, PaymentActivity.class);

                    i.putExtra("sosId", sosId);

                    startActivity(i);
                    finish();
                }
                break;
        }
    }

    // ===================================================
    // ICON
    // ===================================================

    private BitmapDescriptor getMechanicIcon() {

        Bitmap bitmap =
                BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.ic_mechanic);

        Bitmap small =
                Bitmap.createScaledBitmap(
                        bitmap, 90, 90, false);

        return BitmapDescriptorFactory.fromBitmap(small);
    }

    // ===================================================
    // UTIL
    // ===================================================

    private float distanceBetween(LatLng a, LatLng b) {

        float[] result = new float[1];

        Location.distanceBetween(
                a.latitude, a.longitude,
                b.latitude, b.longitude,
                result);

        return result[0];
    }

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

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }

        return poly;
    }
}