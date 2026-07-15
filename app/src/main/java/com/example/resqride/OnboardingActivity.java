package com.example.resqride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.resqride.adapters.OnboardingAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private TextView txtTitle, txtDesc;

    private final String[] titles = {
            "Emergency SOS",
            "Nearby Workshops",
            "Fast & Safe Help"
    };

    private final String[] descriptions = {
            "Send SOS instantly with your live location.",
            "Find trusted motorcycle workshops nearby.",
            "Get assistance quickly and safely."
    };

    private final int[] images = {
            R.drawable.onboard_1,
            R.drawable.onboard_2,
            R.drawable.onboard_3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        txtTitle = findViewById(R.id.txtTitle);
        txtDesc = findViewById(R.id.txtDesc);

        // Adapter
        OnboardingAdapter adapter = new OnboardingAdapter(images);
        viewPager.setAdapter(adapter);


        // Initial text
        updateText(0);

        // Page change
        viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);

                        updateText(position);

                        if (position == images.length - 1) {
                            btnNext.setText("Get Started");
                        } else {
                            btnNext.setText("Next");
                        }
                    }
                });

        // Button action
        btnNext.setOnClickListener(v -> {

            int current = viewPager.getCurrentItem();

            if (current < images.length - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                startActivity(
                        new Intent(
                                OnboardingActivity.this,
                                RoleSelectionActivity.class
                        )
                );
                finish();
            }
        });
    }

    private void updateText(int position) {
        txtTitle.setText(titles[position]);
        txtDesc.setText(descriptions[position]);
    }
}