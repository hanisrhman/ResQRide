package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.resqride.adapters.WorkshopReviewAdapter;
import com.example.resqride.models.WorkshopReview;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WorkshopProfileActivity extends AppCompatActivity {

    // ================= UI =================

    private SwitchMaterial switchAvailable;

    private Button btnEditProfile, btnLogout;

    private ImageView imgProfile;

    private TextView txtName, txtRating, txtJobs,
            txtAddress, txtServices, txtPhone, txtTime;

    private TextView txtTotalJobs, txtThisWeek, txtThisMonth;

    private RecyclerView recyclerReviews;

    // ================= FIREBASE =================

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String workshopId;

    // ================= ADAPTER =================

    private WorkshopReviewAdapter reviewAdapter;
    private List<WorkshopReview> reviewList = new ArrayList<>();

    // ================= LISTENERS =================

    private ListenerRegistration profileListener;
    private ListenerRegistration reviewListener;

    private boolean ignoreSwitch = false;

    // ================= ON CREATE =================

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            goToLogin();
            return;
        }

        workshopId = auth.getUid();

        bindViews();

        setupRecycler();

        setupActions();

        setupBottomNav();

        loadProfile();

        loadJobStatistics();

        loadReviews();
    }

    // ================= BIND =================

    private void bindViews() {

        switchAvailable = findViewById(R.id.switchAvailable);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        imgProfile = findViewById(R.id.imgProfile);

        txtName = findViewById(R.id.txtName);
        txtRating = findViewById(R.id.txtRating);
        txtJobs = findViewById(R.id.txtJobs);

        txtAddress = findViewById(R.id.txtAddress);
        txtServices = findViewById(R.id.txtServices);
        txtPhone = findViewById(R.id.txtPhone);
        txtTime = findViewById(R.id.txtTime);

        txtTotalJobs = findViewById(R.id.txtTotalJobs);
        txtThisWeek = findViewById(R.id.txtThisWeek);
        txtThisMonth = findViewById(R.id.txtThisMonth);

        recyclerReviews = findViewById(R.id.recyclerReviews);
    }

    // ================= RECYCLER =================

    private void setupRecycler() {

        recyclerReviews.setLayoutManager(
                new LinearLayoutManager(this));

        reviewAdapter =
                new WorkshopReviewAdapter(this, reviewList);

        recyclerReviews.setAdapter(reviewAdapter);
    }

    // ================= LOAD PROFILE =================

    private void loadProfile() {

        profileListener =
                db.collection("workshops")
                        .document(workshopId)
                        .addSnapshotListener(this, (doc, e) -> {

                            if (e != null) return;

                            if (doc == null || !doc.exists())
                                return;

                            txtName.setText(
                                    safe(doc.getString("name"), "Workshop"));

                            txtAddress.setText("📍 " +
                                    safe(doc.getString("address"), "-"));

                            txtPhone.setText("📞 " +
                                    safe(doc.getString("phone"), "-"));

                            String open = safe(doc.getString("openTime"), "-");
                            String close = safe(doc.getString("closeTime"), "-");

                            txtTime.setText("⏰ " + open + " - " + close);

                            Double rating = doc.getDouble("rating");

                            txtRating.setText("⭐ " +
                                    String.format("%.1f",
                                            rating != null ? rating : 0.0));

                            Long jobs = doc.getLong("totalJobs");

                            txtJobs.setText(
                                    (jobs != null ? jobs : 0)
                                            + " jobs completed");

                            List<String> services =
                                    (List<String>) doc.get("services");

                            txtServices.setText(
                                    services != null && !services.isEmpty()
                                            ? "🛠 " + String.join(", ", services)
                                            : "🛠 -");

                            Boolean available =
                                    doc.getBoolean("available");

                            if (available != null) {

                                ignoreSwitch = true;
                                switchAvailable.setChecked(available);
                                ignoreSwitch = false;
                            }

                            String imageUrl =
                                    doc.getString("imageUrl");

                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_user)
                                    .into(imgProfile);
                        });
    }

    // ================= LOAD JOB STATS =================

    private void loadJobStatistics() {

        Calendar weekCal = Calendar.getInstance();
        weekCal.set(Calendar.DAY_OF_WEEK,
                weekCal.getFirstDayOfWeek());
        weekCal.set(Calendar.HOUR_OF_DAY, 0);
        weekCal.set(Calendar.MINUTE, 0);
        weekCal.set(Calendar.SECOND, 0);

        long weekStart = weekCal.getTimeInMillis();

        Calendar monthCal = Calendar.getInstance();
        monthCal.set(Calendar.DAY_OF_MONTH, 1);
        monthCal.set(Calendar.HOUR_OF_DAY, 0);
        monthCal.set(Calendar.MINUTE, 0);
        monthCal.set(Calendar.SECOND, 0);

        long monthStart = monthCal.getTimeInMillis();

        // TOTAL
        db.collection("sos_requests")
                .whereEqualTo("assignedWorkshopId", workshopId)
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(snap ->
                        txtTotalJobs.setText(
                                String.valueOf(snap.size())));

        // WEEK
        db.collection("sos_requests")
                .whereEqualTo("assignedWorkshopId", workshopId)
                .whereEqualTo("status", "completed")
                .whereGreaterThanOrEqualTo("completedAt", weekStart)
                .get()
                .addOnSuccessListener(snap ->
                        txtThisWeek.setText(
                                String.valueOf(snap.size())));

        // MONTH
        db.collection("sos_requests")
                .whereEqualTo("assignedWorkshopId", workshopId)
                .whereEqualTo("status", "completed")
                .whereGreaterThanOrEqualTo("completedAt", monthStart)
                .get()
                .addOnSuccessListener(snap ->
                        txtThisMonth.setText(
                                String.valueOf(snap.size())));
    }

    // ================= LOAD REVIEWS =================

    private void loadReviews() {

        reviewListener =
                db.collection("sos_requests")
                        .whereEqualTo("assignedWorkshopId", workshopId)
                        .whereEqualTo("status", "completed")
                        .addSnapshotListener(this, (snap, err) -> {

                            if (err != null || snap == null)
                                return;

                            reviewList.clear();

                            for (DocumentSnapshot d : snap.getDocuments()) {

                                Double rating =
                                        d.getDouble("rating");

                                String review =
                                        d.getString("review");

                                if (rating == null || rating == 0)
                                    continue;

                                WorkshopReview r =
                                        new WorkshopReview();

                                r.sosId = d.getId();
                                r.riderId = d.getString("riderId");
                                r.review = review != null ? review : "";
                                r.rating = rating;

                                reviewList.add(r);
                            }

                            reviewAdapter.notifyDataSetChanged();
                        });
    }

    // ================= ACTIONS =================

    private void setupActions() {

        switchAvailable.setOnCheckedChangeListener((btn, checked) -> {

            if (ignoreSwitch) return;

            db.collection("workshops")
                    .document(workshopId)
                    .update("available", checked);
        });

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this,
                        WorkshopEditProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {

            cleanupListeners();

            auth.signOut();

            goToLogin();
        });
    }

    // ================= NAV =================

    private void setupBottomNav() {

        BottomNavigationView nav =
                findViewById(R.id.bottomNavigation);

        nav.setSelectedItemId(R.id.nav_profile);

        nav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {

                startActivity(new Intent(this,
                        WorkshopHomeActivity.class));

                finish();
                return true;
            }

            if (item.getItemId() == R.id.nav_history) {

                startActivity(new Intent(this,
                        WorkshopHistoryActivity.class));

                finish();
                return true;
            }

            return true;
        });
    }

    // ================= HELPERS =================

    private String safe(String value, String def) {
        return value != null ? value : def;
    }

    private void goToLogin() {

        Intent intent = new Intent(this,
                WorkshopLoginActivity.class);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    private void cleanupListeners() {

        if (profileListener != null)
            profileListener.remove();

        if (reviewListener != null)
            reviewListener.remove();
    }

    // ================= DESTROY =================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        cleanupListeners();
    }
}