package com.example.resqride;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddVehicleActivity extends AppCompatActivity {

    EditText edtModel, edtPlate, edtYear, edtService;
    Button btnSave;

    FirebaseFirestore db;
    String userId, vehicleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        edtModel = findViewById(R.id.edtModel);
        edtPlate = findViewById(R.id.edtPlate);
        edtYear = findViewById(R.id.edtYear);
        edtService = findViewById(R.id.edtService);
        btnSave = findViewById(R.id.btnSaveVehicle);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        vehicleId = getIntent().getStringExtra("vehicleId");
        if (vehicleId != null) loadVehicle();


        btnSave.setOnClickListener(v -> save());
    }

    private void loadVehicle() {
        db.collection("users")
                .document(userId)
                .collection("vehicles")
                .document(vehicleId)
                .get()
                .addOnSuccessListener(d -> {
                    edtModel.setText(d.getString("model"));
                    edtPlate.setText(d.getString("plateNumber"));
                    edtYear.setText(d.getString("year"));
                    edtService.setText(d.getString("lastService"));
                });
    }

    private void save() {
        Map<String, Object> data = new HashMap<>();
        data.put("model", edtModel.getText().toString());
        data.put("plateNumber", edtPlate.getText().toString());
        data.put("year", edtYear.getText().toString());
        data.put("lastService", edtService.getText().toString());
        data.put("createdAt", com.google.firebase.Timestamp.now());

        if (vehicleId == null) {
            db.collection("users")
                    .document(userId)
                    .collection("vehicles")
                    .add(data);
        } else {
            db.collection("users")
                    .document(userId)
                    .collection("vehicles")
                    .document(vehicleId)
                    .update(data);
        }

        finish();
    }
}