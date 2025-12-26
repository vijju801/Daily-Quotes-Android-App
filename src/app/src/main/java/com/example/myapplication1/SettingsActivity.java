package com.example.myapplication1;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.transition.platform.MaterialSharedAxis;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_SETTINGS_NAME = "AppSettingsPrefs";
    public static final String KEY_THEME = "theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        applyTheme(this);
        super.onCreate(savedInstanceState);

        MaterialSharedAxis enterTransition = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        getWindow().setEnterTransition(enterTransition);

        MaterialSharedAxis returnTransition = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        getWindow().setReturnTransition(returnTransition);

        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RadioGroup themeRadioGroup = findViewById(R.id.theme_radio_group);
        MaterialCardView notificationPreferencesCard = findViewById(R.id.notification_preferences_card);
        MaterialCardView aboutUsCard = findViewById(R.id.about_us_card);
        MaterialCardView contactUsCard = findViewById(R.id.contact_us_card);

        SharedPreferences settingsPrefs = getSharedPreferences(PREFS_SETTINGS_NAME, MODE_PRIVATE);
        String currentTheme = settingsPrefs.getString(KEY_THEME, "system");
        switch (currentTheme) {
            case "dark":
                ((RadioButton) findViewById(R.id.radio_dark)).setChecked(true);
                break;
            case "light":
                 ((RadioButton) findViewById(R.id.radio_light)).setChecked(true);
                 break;
            default:
                ((RadioButton) findViewById(R.id.radio_system)).setChecked(true);
                break;
        }

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedTheme;
            if (checkedId == R.id.radio_dark) {
                selectedTheme = "dark";
            } else if (checkedId == R.id.radio_light) {
                selectedTheme = "light";
            } else {
                selectedTheme = "system";
            }
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString(KEY_THEME, selectedTheme);
            editor.apply();
            recreate();
        });

        notificationPreferencesCard.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, NotificationPreferencesActivity.class);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        });

        aboutUsCard.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutUsActivity.class);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        });

        contactUsCard.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"vijayrk0513@gmail.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback/Support for Daily Quotes App");
            try {
                startActivity(Intent.createChooser(emailIntent, "Send email using..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(SettingsActivity.this, "No email client installed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void applyTheme(Context context) {
        SharedPreferences settingsPrefs = context.getSharedPreferences(PREFS_SETTINGS_NAME, MODE_PRIVATE);
        String theme = settingsPrefs.getString(KEY_THEME, "system");
        switch (theme) {
            case "dark":
                context.setTheme(R.style.Theme_MyApplication1_Futuristic);
                break;
            case "light":
                context.setTheme(R.style.Theme_MyApplication1_Zen);
                break;
            default:
                int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    context.setTheme(R.style.Theme_MyApplication1_Futuristic);
                } else {
                    context.setTheme(R.style.Theme_MyApplication1_Zen);
                }
                break;
        }
    }
}
