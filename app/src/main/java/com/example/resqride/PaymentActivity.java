package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentActivity extends AppCompatActivity {

    private String sosId;
    private RadioGroup paymentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        sosId = getIntent().getStringExtra("sosId");

        TextView txtAmount = findViewById(R.id.txtAmount);
        paymentGroup = findViewById(R.id.paymentGroup);
        Button btnPay = findViewById(R.id.btnPay);

        txtAmount.setText("RM 25.00");

        btnPay.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {

        int selectedId = paymentGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this,
                    "Please select payment method",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selected = findViewById(selectedId);

        FirebaseFirestore.getInstance()
                .collection("sos_requests")
                .document(sosId)
                .update(
                        "paymentMethod", selected.getText().toString(),
                        "paymentStatus", "paid",
                        "status", "completed",
                        "completedAt", System.currentTimeMillis()
                )
                .addOnSuccessListener(v -> {

                    Intent i = new Intent(
                            PaymentActivity.this,
                            RatingActivity.class
                    );
                    i.putExtra("sosId", sosId);
                    startActivity(i);
                    finish();
                });
    }
}