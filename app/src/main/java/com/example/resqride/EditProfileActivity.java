package com.example.resqride;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 101;

    ImageView imgProfile;
    EditText edtName, edtPhone;
    Button btnSave;

    Uri imageUri;

    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 🔥 Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // 🔗 UI
        imgProfile = findViewById(R.id.imgProfile);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);

        // 🚫 Block guest
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.isAnonymous()) {
            Toast.makeText(this,
                    "Please register to edit profile",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadProfile();

        imgProfile.setOnClickListener(v -> pickImage());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    // ================= LOAD PROFILE =================
    private void loadProfile() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    edtName.setText(doc.getString("name"));
                    edtPhone.setText(doc.getString("phone"));

                    String avatarUrl = doc.getString("avatarUrl");
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.ic_user)
                                .into(imgProfile);
                    }
                });
    }

    // ================= PICK IMAGE =================
    private void pickImage() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE &&
                resultCode == RESULT_OK &&
                data != null) {

            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
        }
    }

    // ================= SAVE PROFILE =================
    private void saveProfile() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Name is required");
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        if (imageUri != null) {
            uploadImageAndSave(user.getUid(), name, phone);
        } else {
            saveToFirestore(name, phone, null);
        }
    }

    // ================= UPLOAD IMAGE =================
    private void uploadImageAndSave(
            String uid,
            String name,
            String phone) {

        StorageReference ref =
                storage.getReference("profile_images/" + uid + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        saveToFirestore(
                                                name,
                                                phone,
                                                uri.toString()
                                        )))
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    resetButton();
                });
    }

    // ================= SAVE FIRESTORE =================
    private void saveToFirestore(
            String name,
            String phone,
            String avatarUrl) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);

        if (avatarUrl != null) {
            data.put("avatarUrl", avatarUrl);
        }

        db.collection("users")
                .document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(v -> {
                    Toast.makeText(
                            this,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    resetButton();
                });
    }

    private void resetButton() {
        btnSave.setEnabled(true);
        btnSave.setText("Save Changes");
    }
}