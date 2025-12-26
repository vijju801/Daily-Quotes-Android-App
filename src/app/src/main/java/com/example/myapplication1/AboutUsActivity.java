package com.example.myapplication1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window; // Added for Activity Transitions
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.transition.platform.MaterialSharedAxis;

public class AboutUsActivity extends AppCompatActivity {

    private TextView privacyPolicyLink;
    private Button rateUsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Activity Transitions - Must be called before super.onCreate()
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        // Apply theme first if you have a specific theme for AboutUsActivity
        // or rely on the application/SettingsActivity to have set the global theme.
        // SettingsActivity.applyTheme(this); 
        super.onCreate(savedInstanceState);

        // Set up Enter and Return Transitions
        MaterialSharedAxis enterTransition = new MaterialSharedAxis(MaterialSharedAxis.X, true); // Forward
        getWindow().setEnterTransition(enterTransition);

        MaterialSharedAxis returnTransition = new MaterialSharedAxis(MaterialSharedAxis.X, false); // Backward
        getWindow().setReturnTransition(returnTransition);

        setContentView(R.layout.activity_about_us);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("About Us");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        privacyPolicyLink = findViewById(R.id.about_privacy_policy);
        rateUsButton = findViewById(R.id.about_rate_us_button);

        // You can set the version name programmatically if desired
        // TextView appVersion = findViewById(R.id.about_app_version);
        // try {
        //     String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        //     appVersion.setText("Version " + versionName);
        // } catch (PackageManager.NameNotFoundException e) {
        //     e.printStackTrace();
        // }

        privacyPolicyLink.setOnClickListener(v -> {
            // In a real app, replace with an Intent to open your privacy policy URL
            Toast.makeText(AboutUsActivity.this, "Opening Privacy Policy (not implemented yet)", Toast.LENGTH_SHORT).show();
            // Example:
            // Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yourwebsite.com/privacy"));
            // startActivity(browserIntent);
        });

        rateUsButton.setOnClickListener(v -> {
            // In a real app, replace with an Intent to open your app's page on the Google Play Store
            Toast.makeText(AboutUsActivity.this, "Opening Google Play (not implemented yet)", Toast.LENGTH_SHORT).show();
            // Example:
            // final String appPackageName = getPackageName();
            // try {
            //     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            // } catch (android.content.ActivityNotFoundException anfe) {
            //     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            // }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Respond to the action bar's Up/Home button
            finish(); // Navigates back to the previous activity in the stack
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
