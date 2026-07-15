package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class WorkshopLoginActivity extends AppCompatActivity {

    // UI
    EditText edtEmail, edtPassword;
    Button btnLogin, btnGuest;
    TextView txtRegister, txtForgot;

    // Firebase
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_login);

        // 🔥 Firebase init
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔗 Bind views (MUST match XML IDs)
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGuest = findViewById(R.id.btnGuest);
        txtRegister = findViewById(R.id.txtRegister);
        txtForgot = findViewById(R.id.txtForgot);

        // 🔐 LOGIN
        btnLogin.setOnClickListener(v -> loginWorkshop());

        // 📝 REGISTER WORKSHOP
        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, WorkshopRegisterActivity.class));
        });

        // ❓ FORGOT PASSWORD
        txtForgot.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        // 🚫 GUEST NOT ALLOWED
        btnGuest.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Guest access is not allowed for workshops",
                    Toast.LENGTH_SHORT).show();
        });
    }

    // ============================
    // 🔐 LOGIN LOGIC
    // ============================
    private void loginWorkshop() {

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = auth.getCurrentUser().getUid();

                    // 🔎 CHECK ADMIN APPROVAL
                    db.collection("workshops")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(doc -> {

                                if (doc.exists() && Boolean.TRUE.equals(doc.getBoolean("approved"))) {

                                    // ✅ APPROVED → GO TO WORKSHOP HOME
                                    startActivity(new Intent(this, WorkshopHomeActivity.class));
                                    finish();

                                } else {
                                    // ❌ NOT APPROVED
                                    Toast.makeText(this,
                                            "Your workshop account is pending admin approval",
                                            Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Error checking approval",
                                            Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
