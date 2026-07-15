package com.example.resqride;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkshopEditProfileActivity extends AppCompatActivity {

    private static final int REQ_IMAGE = 101;

    ImageView imgProfile;
    EditText edtName, edtPhone, edtAddress, edtOpenTime, edtCloseTime;
    CheckBox cbTire, cbOil, cbBattery;
    Button btnPickLocation, btnSave;

    FirebaseFirestore db;
    FirebaseStorage storage;
    String workshopId;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_edit_profile);

        // 🔹 Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        workshopId = FirebaseAuth.getInstance().getUid();

        // 🔹 Bind views
        imgProfile = findViewById(R.id.imgProfile);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtOpenTime = findViewById(R.id.edtOpenTime);
        edtCloseTime = findViewById(R.id.edtCloseTime);
        cbTire = findViewById(R.id.cbTire);
        cbOil = findViewById(R.id.cbOil);
        cbBattery = findViewById(R.id.cbBattery);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        btnSave = findViewById(R.id.btnSave);

        loadProfile();

        btnPickLocation.setOnClickListener(v -> {
            Intent i = new Intent(this, PickLocationActivity.class);
            startActivityForResult(i, 300);
        });

        imgProfile.setOnClickListener(v -> chooseImage());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    // ================= LOAD PROFILE =================
    private void loadProfile() {

        db.collection("workshops")
                .document(workshopId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    edtName.setText(doc.getString("name"));
                    edtPhone.setText(doc.getString("phone"));
                    edtAddress.setText(doc.getString("address"));
                    edtOpenTime.setText(doc.getString("openTime"));
                    edtCloseTime.setText(doc.getString("closeTime"));


                    // ✅ SERVICES AS LIST (IMPORTANT)
                    ArrayList<String> services =
                            (ArrayList<String>) doc.get("services");

                    if (services != null) {
                        cbTire.setChecked(services.contains("Tire Repair"));
                        cbOil.setChecked(services.contains("Oil Change"));
                        cbBattery.setChecked(services.contains("Battery Jumpstart"));
                    }

                    String imageUrl = doc.getString("imageUrl");
                    if (imageUrl != null) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_user)
                                .into(imgProfile);
                    }
                });
    }

    // ================= IMAGE PICKER =================
    private void chooseImage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Profile Picture");
        builder.setItems(new String[]{"Gallery"}, (d, i) -> {
            Intent pick = new Intent(Intent.ACTION_PICK);
            pick.setType("image/*");
            startActivityForResult(pick, REQ_IMAGE);
        });
        builder.show();
    }

    double selectedLat = 0;
    double selectedLng = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("lat", 0);
            double lng = data.getDoubleExtra("lng", 0);

            db.collection("workshops")
                    .document(workshopId)
                    .update("lat", lat, "lng", lng);
        }

        if (resultCode == RESULT_OK && data != null && requestCode == REQ_IMAGE) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
        }
    }

    // ================= SAVE PROFILE =================
    private void saveProfile() {

        ArrayList<String> services = new ArrayList<>();
        if (cbTire.isChecked()) services.add("Tire Repair");
        if (cbOil.isChecked()) services.add("Oil Change");
        if (cbBattery.isChecked()) services.add("Battery Jumpstart");

        Map<String, Object> data = new HashMap<>();
        data.put("name", edtName.getText().toString());
        data.put("phone", edtPhone.getText().toString());
        data.put("address", edtAddress.getText().toString());
        data.put("openTime", edtOpenTime.getText().toString());
        data.put("closeTime", edtCloseTime.getText().toString());
        data.put("services", services);

        if (imageUri != null) {
            uploadImageThenSave(data);
        } else {
            updateFirestore(data);
        }

        if (selectedLat != 0) {
            data.put("lat", selectedLat);
            data.put("lng", selectedLng);
        }
    }

    // ================= IMAGE UPLOAD =================
    private void uploadImageThenSave(Map<String, Object> data) {

        StorageReference ref = storage.getReference()
                .child("workshop_profiles/" + workshopId + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(t ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            data.put("imageUrl", uri.toString());
                            updateFirestore(data);
                        }));
    }

    // ================= FIRESTORE UPDATE =================
    private void updateFirestore(Map<String, Object> data) {

        db.collection("workshops")
                .document(workshopId)
                .update(data)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}