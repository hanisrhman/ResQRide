package com.example.resqride;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class RejectReasonDialog extends Dialog {

    public RejectReasonDialog(Context context, String sosId) {
        super(context);
        setContentView(R.layout.dialog_reject_reason);

        EditText edtReason = findViewById(R.id.edtReason);
        Button btnSubmit = findViewById(R.id.btnSubmitReject);

        btnSubmit.setOnClickListener(v -> {
            String reason = edtReason.getText().toString().trim();

            if (reason.isEmpty()) {
                Toast.makeText(context,
                        "Please enter a reason",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore.getInstance()
                    .collection("sos_requests")
                    .document(sosId)
                    .update(
                            "status", "rejected",
                            "rejectReason", reason
                    );

            dismiss();
        });
    }
}
