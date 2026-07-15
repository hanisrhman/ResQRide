package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        Button btnRider = findViewById(R.id.btnRider);
        Button btnWorkshop = findViewById(R.id.btnWorkshop);

        btnWorkshop.setOnClickListener(v -> {
            startActivity(new Intent(this, WorkshopLoginActivity.class));
        });

        btnRider.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}
