package com.example.resqride;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    String sosId;
    FirebaseFirestore db;

    TextView txtReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        sosId = getIntent().getStringExtra("sosId");
        db = FirebaseFirestore.getInstance();

        txtReceipt = findViewById(R.id.txtReceipt);

        loadReceipt();
    }

    private void loadReceipt() {

        db.collection("sos_requests")
                .document(sosId)
                .get()
                .addOnSuccessListener(d -> {

                    if (!d.exists()) return;

                    String date = new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                    ).format(new Date(d.getLong("time")));

                    String receipt =
                            "Date: " + date + "\n\n" +
                                    "Problem:\n" + d.getString("problem") + "\n\n" +
                                    "Workshop:\n" + d.getString("assignedWorkshopName") + "\n\n" +
                                    "Payment Method:\n" + d.getString("paymentMethod") + "\n\n" +
                                    "Amount Paid:\nRM 25.00\n\n" +
                                    "Status:\nCOMPLETED";

                    txtReceipt.setText(receipt);
                });
    }
}