package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // 🔁 If user already logged in → go Home
        if (user != null) {
            Intent intent = new Intent(this, HomeActivity.class);

            // Check if guest
            intent.putExtra("guest", user.isAnonymous());
            startActivity(intent);
        }
        // 🔐 Else → go Login
        else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        // Close MainActivity so user cannot go back to it
        finish();
    }
}