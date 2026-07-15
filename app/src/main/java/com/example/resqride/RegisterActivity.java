package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    Button btnRegister;
    TextView txtLogin;

    // Password rules
    TextView ruleLength, ruleUpper, ruleNumber, ruleSpecial;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        ruleLength = findViewById(R.id.ruleLength);
        ruleUpper = findViewById(R.id.ruleUpper);
        ruleNumber = findViewById(R.id.ruleNumber);
        ruleSpecial = findViewById(R.id.ruleSpecial);

        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);

        edtPassword.addTextChangedListener(passwordWatcher);

        btnRegister.setOnClickListener(v -> registerUser());

        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // ================= PASSWORD WATCHER =================
    private final TextWatcher passwordWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String pwd = s.toString();

            updateRule(ruleLength, pwd.length() >= 8);
            updateRule(ruleUpper, pwd.matches(".*[A-Z].*"));
            updateRule(ruleNumber, pwd.matches(".*[0-9].*"));
            updateRule(ruleSpecial, pwd.matches(".*[!@#$%^&*+=?-].*"));
        }
    };

    private void updateRule(TextView tv, boolean ok) {
        tv.setText(ok ? "✔ " + tv.getTag() : "✘ " + tv.getTag());
        tv.setTextColor(getColor(ok ? R.color.green : R.color.red));
    }

    // ================= REGISTER =================
    private void registerUser() {

        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirm = edtConfirmPassword.getText().toString();

        if (name.isEmpty()) {
            edtName.setError("Name required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Invalid email");
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this,
                    "Password does not meet all requirements",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirm)) {
            edtConfirmPassword.setError("Passwords do not match");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    String uid = result.getUser().getUid();

                    // ✅ SAVE USER TO FIRESTORE
                    Map<String, Object> user = new HashMap<>();
                    user.put("name", name);
                    user.put("email", email);
                    user.put("phone", "");
                    user.put("createdAt", System.currentTimeMillis());

                    db.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(v -> {

                                Toast.makeText(this,
                                        "Account created successfully 🎉",
                                        Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private boolean isValidPassword(String p) {
        return p.length() >= 8 &&
                p.matches(".*[A-Z].*") &&
                p.matches(".*[0-9].*") &&
                p.matches(".*[!@#$%^&*+=?-].*");
    }
}