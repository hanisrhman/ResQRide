package com.example.resqride;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ImageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        ImageView img = new ImageView(this);

        img.setBackgroundColor(0xFF000000);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);

        setContentView(img);

        String url = getIntent().getStringExtra("imageUrl");

        Glide.with(this)
                .load(url)
                .into(img);

        img.setOnClickListener(v->finish());
    }
}