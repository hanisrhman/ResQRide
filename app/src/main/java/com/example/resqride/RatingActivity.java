package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText edtReview;
    private Button btnSubmit, btnSkip;

    private FirebaseFirestore db;
    private String sosId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ratingBar = findViewById(R.id.ratingBar);
        edtReview = findViewById(R.id.edtReview);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSkip = findViewById(R.id.btnSkip);

        db = FirebaseFirestore.getInstance();
        sosId = getIntent().getStringExtra("sosId");

        if (sosId == null) {
            finish();
            return;
        }

        btnSubmit.setOnClickListener(v -> submitRating());
        btnSkip.setOnClickListener(v -> goHistory());
    }

    private void submitRating() {

        float rating = ratingBar.getRating();
        String review = edtReview.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this,
                    "Please give a rating or press Skip",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("rating", rating);
        ratingData.put("review", review);

        db.collection("sos_requests")
                .document(sosId)
                .update(ratingData)
                .addOnSuccessListener(v -> {
                    Toast.makeText(
                            this,
                            "Thank you for your feedback!",
                            Toast.LENGTH_LONG
                    ).show();
                    goHistory();
                });
    }

    // ================= GO HISTORY =================
    private void goHistory() {
        Intent i = new Intent(this, HistoryActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}