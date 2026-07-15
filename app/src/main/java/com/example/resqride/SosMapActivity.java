package com.example.resqride;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqride.adapters.WorkshopAdapter;
import com.example.resqride.models.WorkshopModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SosMapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    GoogleMap mMap;
    RecyclerView recyclerView;
    WorkshopAdapter adapter;
    ArrayList<WorkshopModel> workshopList;

    FirebaseFirestore db;
    String sosId = "TEMP_SOS_ID"; // replace with real sosId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_map);

        db = FirebaseFirestore.getInstance();

        // 🔹 MAP
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 🔹 RECYCLER VIEW
        recyclerView = findViewById(R.id.recyclerWorkshops);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        workshopList = new ArrayList<>();

        adapter = new WorkshopAdapter(workshopList, workshop -> {
            sendSosRequest(workshop);
        });

        recyclerView.setAdapter(adapter);

        loadNearbyWorkshops();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng riderLoc = new LatLng(3.0738, 101.5183);
        mMap.addMarker(new MarkerOptions()
                .position(riderLoc)
                .title("You"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(riderLoc, 14f));
    }

    private void loadNearbyWorkshops() {

        db.collection("workshops")
                .whereEqualTo("approved", true)
                .whereEqualTo("online", true)
                .get()
                .addOnSuccessListener(query -> {

                    workshopList.clear();

                    for (DocumentSnapshot doc : query) {
                        WorkshopModel w = doc.toObject(WorkshopModel.class);
                        if (w != null) {
                            w.id = doc.getId();
                            workshopList.add(w);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void sendSosRequest(WorkshopModel workshop) {

        Map<String, Object> data = new HashMap<>();
        data.put("workshopId", workshop.id);
        data.put("status", "pending");

        db.collection("sos_requests")
                .document(sosId)
                .update(data)
                .addOnSuccessListener(v ->
                        Toast.makeText(this,
                                "Request sent to " + workshop.name,
                                Toast.LENGTH_LONG).show()
                );
    }
}