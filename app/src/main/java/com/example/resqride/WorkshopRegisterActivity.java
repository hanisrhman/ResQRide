package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WorkshopRegisterActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword, edtPhone, edtAddress;
    Button btnRegister, btnPickLocation;
    TextView txtBack, txtLocationStatus;

    FirebaseAuth auth;
    FirebaseFirestore db;

    double workshopLat = 0;
    double workshopLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);

        btnRegister = findViewById(R.id.btnRegister);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        txtBack = findViewById(R.id.txtBack);
        txtLocationStatus = findViewById(R.id.txtLocationStatus);

        btnPickLocation.setOnClickListener(v -> {
            Intent i = new Intent(this, PickLocationActivity.class);
            startActivityForResult(i, 200);
        });

        btnRegister.setOnClickListener(v -> registerWorkshop());

        txtBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            workshopLat = data.getDoubleExtra("lat", 0);
            workshopLng = data.getDoubleExtra("lng", 0);

            txtLocationStatus.setText("Location selected ✔");
            txtLocationStatus.setTextColor(0xFF2E7D32);
        }
    }

    private void registerWorkshop() {

        if (workshopLat == 0) {
            Toast.makeText(this,
                    "Please pin workshop location",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(
                        edtEmail.getText().toString(),
                        edtPassword.getText().toString())
                .addOnSuccessListener(result -> {

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", edtName.getText().toString());
                    data.put("email", edtEmail.getText().toString());
                    data.put("phone", edtPhone.getText().toString());
                    data.put("address", edtAddress.getText().toString());

                    data.put("lat", workshopLat);
                    data.put("lng", workshopLng);

                    data.put("approved", false);
                    data.put("online", false);
                    data.put("available", false);
                    data.put("rating", 0);

                    db.collection("workshops")
                            .document(result.getUser().getUid())
                            .set(data)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this,
                                        "Submitted! Waiting for admin approval",
                                        Toast.LENGTH_LONG).show();
                                finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}