package com.suryakusuma.novelin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Ambil logo
        logo = findViewById(R.id.logo);

        // Load animasi
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.splash_anim);
        logo.startAnimation(anim);

        // Delay pindah activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);

            // Animasi pindah screen
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            finish();
        }, 3000);
    }
}