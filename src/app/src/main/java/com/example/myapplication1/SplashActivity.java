package com.example.myapplication1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIMEOUT = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply the saved theme at the very beginning
        SettingsActivity.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Optional: Hide ActionBar if your theme shows it by default on activities
        // and you want a truly fullscreen splash.
        // if (getSupportActionBar() != null) {
        //     getSupportActionBar().hide();
        // }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close SplashActivity so user can't navigate back to it
        }, SPLASH_TIMEOUT);
    }
}
