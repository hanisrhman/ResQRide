package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private GoogleSignInClient googleClient;

    private EditText edtEmail, edtPassword;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            try {
                                GoogleSignInAccount account =
                                        GoogleSignIn.getSignedInAccountFromIntent(
                                                        result.getData())
                                                .getResult(ApiException.class);

                                firebaseAuthWithGoogle(account.getIdToken());

                            } catch (ApiException e) {
                                toast("Google sign-in failed");
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        // Google Sign-In config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnLogin).setOnClickListener(v -> loginEmail());
        findViewById(R.id.btnGoogle).setOnClickListener(v -> loginGoogle());
        findViewById(R.id.btnGuest).setOnClickListener(v -> loginGuest());

        findViewById(R.id.txtForgot).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );

        findViewById(R.id.txtRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    // ================= EMAIL LOGIN =================
    private void loginEmail() {

        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            toast("Please fill in all fields");
            return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(r -> goHome())
                .addOnFailureListener(e ->
                        toast(e.getMessage()));
    }

    // ================= GOOGLE LOGIN =================
    private void loginGoogle() {
        Intent intent = googleClient.getSignInIntent();
        googleLauncher.launch(intent);
    }

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnSuccessListener(r -> goHome())
                .addOnFailureListener(e ->
                        toast("Google authentication failed"));
    }

    // ================= GUEST LOGIN =================
    private void loginGuest() {

        auth.signInAnonymously()
                .addOnSuccessListener(r -> goHome())
                .addOnFailureListener(e ->
                        toast("Guest login failed"));
    }

    // ================= NAVIGATION =================
    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}