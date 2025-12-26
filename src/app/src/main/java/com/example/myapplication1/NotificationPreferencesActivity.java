package com.example.myapplication1;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationPreferencesActivity extends AppCompatActivity implements CustomTimesAdapter.OnTimeDeleteListener {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1003;
    private SharedPreferences notificationPrefs;
    private MaterialSwitch notificationsEnabledSwitch;
    private MaterialSwitch hourlyNotificationsSwitch;
    private CustomTimesAdapter customTimesAdapter;
    private List<String> customTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_preferences);

        notificationPrefs = getSharedPreferences(BootReceiver.PREFS_NAME, MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_notification_prefs);
        toolbar.setNavigationOnClickListener(v -> finish());

        notificationsEnabledSwitch = findViewById(R.id.switch_notifications_enabled);
        hourlyNotificationsSwitch = findViewById(R.id.switch_hourly_notifications);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_custom_times);
        Button addTimeButton = findViewById(R.id.btn_add_time);

        loadPreferences();

        customTimesAdapter = new CustomTimesAdapter(customTimes, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(customTimesAdapter);

        addTimeButton.setOnClickListener(v -> openTimePicker());

        setupListeners();
    }

    private void loadPreferences() {
        boolean notificationsEnabled = notificationPrefs.getBoolean(BootReceiver.KEY_NOTIFICATIONS_ENABLED, false);
        boolean hourlyEnabled = notificationPrefs.getBoolean("hourlyNotificationsEnabled", false);
        Set<String> timesSet = notificationPrefs.getStringSet("customNotificationTimes", new HashSet<>());
        customTimes = new ArrayList<>(timesSet);
        Collections.sort(customTimes);

        notificationsEnabledSwitch.setChecked(notificationsEnabled);
        hourlyNotificationsSwitch.setChecked(hourlyEnabled);
        updateUIState(notificationsEnabled);
    }

    private void setupListeners() {
        notificationsEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestNotificationPermission();
            } else {
                saveGlobalNotificationPreference(false);
            }
        });

        hourlyNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = notificationPrefs.edit();
            editor.putBoolean("hourlyNotificationsEnabled", isChecked);
            editor.apply();
            AlarmScheduler.rescheduleAll(this);
        });
    }

    private void openTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute);
            if (!customTimes.contains(time)) {
                customTimes.add(time);
                Collections.sort(customTimes);
                saveCustomTimes();
                customTimesAdapter.setCustomTimes(customTimes);
                AlarmScheduler.rescheduleAll(this);
            }
        }, 12, 0, true);
        timePickerDialog.show();
    }

    @Override
    public void onTimeDeleted(String time) {
        customTimes.remove(time);
        saveCustomTimes();
        customTimesAdapter.setCustomTimes(customTimes);
        AlarmScheduler.rescheduleAll(this);
    }

    private void saveCustomTimes() {
        SharedPreferences.Editor editor = notificationPrefs.edit();
        editor.putStringSet("customNotificationTimes", new HashSet<>(customTimes));
        editor.apply();
    }

    private void saveGlobalNotificationPreference(boolean enabled) {
        SharedPreferences.Editor editor = notificationPrefs.edit();
        editor.putBoolean(BootReceiver.KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();

        updateUIState(enabled);

        if (enabled) {
            AlarmScheduler.rescheduleAll(this);
            Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
        } else {
            AlarmScheduler.cancelAll(this);
            Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                saveGlobalNotificationPreference(true);
            }
        } else {
            saveGlobalNotificationPreference(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveGlobalNotificationPreference(true);
            } else {
                notificationsEnabledSwitch.setChecked(false);
                Toast.makeText(this, "Notification permission is required to enable notifications.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateUIState(boolean notificationsEnabled) {
        hourlyNotificationsSwitch.setEnabled(notificationsEnabled);
        findViewById(R.id.btn_add_time).setEnabled(notificationsEnabled);
        findViewById(R.id.recycler_view_custom_times).setAlpha(notificationsEnabled ? 1.0f : 0.5f);
    }
}
