package com.example.myapplication1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    public static final String PREFS_NAME = "DailyQuotePrefs";
    public static final String KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed. Rescheduling all alarms.");
            AlarmScheduler.rescheduleAll(context);
        }
    }
}
