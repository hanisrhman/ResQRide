package com.example.resqride;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEmergencyContactActivity extends AppCompatActivity {

    EditText edtName, edtPhone, edtRelation;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emergency_contact);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtRelation = findViewById(R.id.edtRelation);
        btnSave = findViewById(R.id.btnSaveContact);

        btnSave.setOnClickListener(v -> saveContact());
    }

    private void saveContact() {

        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String relation = edtRelation.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this,
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("relationship", relation);
        data.put("createdAt", System.currentTimeMillis()); // 🔥 IMPORTANT FIX

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("emergency_contacts")
                .add(data)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,
                            "Emergency contact saved",
                            Toast.LENGTH_SHORT).show();
                    finish(); // go back to Profile
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}