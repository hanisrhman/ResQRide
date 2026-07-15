package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    // UI
    CircleImageView imgAvatar;
    TextView txtName, txtEmail, txtPhone,
            txtSOSCount, txtEmergencyContact, txtGuestNotice;

    Button btnEditProfile, btnAddContact, btnLogout;

    // Firebase
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind UI
        imgAvatar = findViewById(R.id.imgAvatar);
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtSOSCount = findViewById(R.id.txtSOSCount);
        txtEmergencyContact = findViewById(R.id.txtEmergencyContact);
        txtGuestNotice = findViewById(R.id.txtGuestNotice);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnLogout = findViewById(R.id.btnLogout);

        loadProfile();
        loadSOSCount();
        loadEmergencyContact();

        // Edit profile
        btnEditProfile.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null || user.isAnonymous()) {
                toast("Please register to edit profile");
                return;
            }
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        // Add emergency contact
        btnAddContact.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null || user.isAnonymous()) {
                toast("Please register to add emergency contact");
                return;
            }
            startActivity(new Intent(this, AddEmergencyContactActivity.class));
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        setupBottomNav();
    }

    private void loadProfile() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        // Guest
        if (user.isAnonymous()) {
            txtName.setText("Guest User");
            txtEmail.setText("Sign up to unlock features");
            txtPhone.setText("Phone: Not available");
            txtEmergencyContact.setText("Emergency Contact: Not available");
            txtSOSCount.setText("0");

            txtGuestNotice.setVisibility(View.VISIBLE);

            btnEditProfile.setEnabled(false);
            btnEditProfile.setAlpha(0.5f);

            btnAddContact.setEnabled(false);
            btnAddContact.setAlpha(0.5f);
            return;
        }

        txtGuestNotice.setVisibility(View.GONE);
        txtEmail.setText(user.getEmail());

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    txtName.setText(
                            doc.getString("name") != null
                                    ? doc.getString("name")
                                    : "User"
                    );

                    String phone = doc.getString("phone");
                    txtPhone.setText(
                            phone != null && !phone.isEmpty()
                                    ? "Phone: " + phone
                                    : "Phone: Not set"
                    );

                    String avatar = doc.getString("avatarUrl");
                    if (avatar != null && !avatar.isEmpty()) {
                        Glide.with(this)
                                .load(avatar)
                                .placeholder(R.drawable.ic_user)
                                .into(imgAvatar);
                    }
                });
    }

    // ================= LOAD SOS COUNT =================
    private void loadSOSCount() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.isAnonymous()) {
            txtSOSCount.setText("0");
            return;
        }

        db.collection("sos_requests")
                .whereEqualTo("riderId", user.getUid())
                .get()
                .addOnSuccessListener(snapshot ->
                        txtSOSCount.setText(
                                String.valueOf(snapshot.size())
                        ));
    }

    // ================= LOAD EMERGENCY CONTACT =================
    private void loadEmergencyContact() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.isAnonymous()) {
            txtEmergencyContact.setText("Emergency Contact: Not available");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("emergency_contacts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot == null || snapshot.isEmpty()) {
                        txtEmergencyContact.setText("Emergency Contact: Not set");
                        return;
                    }

                    var doc = snapshot.getDocuments().get(0);

                    String name = doc.getString("name");
                    String phone = doc.getString("phone");

                    txtEmergencyContact.setText(
                            "Emergency Contact: " +
                                    (name != null ? name : "-") +
                                    " (" + (phone != null ? phone : "-") + ")"
                    );
                })
                .addOnFailureListener(e ->
                        txtEmergencyContact.setText("Emergency Contact: Error")
                );
    }

    // ================= BOTTOM NAV =================
    private void setupBottomNav() {

        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_profile);

        nav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }
            if (item.getItemId() == R.id.nav_garage) {
                startActivity(new Intent(this, GarageActivity.class));
                finish();
                return true;
            }
            if (item.getItemId() == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                finish();
                return true;
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadSOSCount();
        loadEmergencyContact(); // 🔥 IMPORTANT
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}