package com.example.myapplication1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";
    public static final int HOURLY_ALARM_REQUEST_CODE = 1;

    /**
     * Cancels all scheduled alarms and reschedules them based on current SharedPreferences.
     * This is the single source of truth for updating the alarm schedule.
     */
    public static void rescheduleAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BootReceiver.PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean(BootReceiver.KEY_NOTIFICATIONS_ENABLED, false);

        // Always cancel everything first to prevent orphan alarms from a previous state.
        cancelAll(context);

        if (notificationsEnabled) {
            Log.d(TAG, "Notifications are enabled. Proceeding to schedule alarms.");

            // Schedule hourly notifications if enabled.
            if (prefs.getBoolean("hourlyNotificationsEnabled", false)) {
                scheduleHourlyAlarm(context);
            }

            // Schedule all custom time notifications.
            Set<String> customTimes = prefs.getStringSet("customNotificationTimes", new HashSet<>());
            for (String time : customTimes) {
                scheduleCustomAlarm(context, time);
            }
        } else {
            Log.d(TAG, "Notifications are disabled. All alarms will remain cancelled.");
        }
    }

    private static void scheduleHourlyAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, HOURLY_ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerAtMillis = System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR;
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_HOUR, pendingIntent);
        Log.d(TAG, "Scheduled hourly alarm.");
    }

    private static void scheduleCustomAlarm(Context context, String time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null || time == null || !time.matches("\\d{2}:\\d{2}")) {
            Log.e(TAG, "Invalid input to scheduleCustomAlarm.");
            return;
        }

        try {
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            int requestCode = hour * 100 + minute; // Deterministic, unique request code.

            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // If the calculated time is in the past, schedule it for the next day.
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    Log.d(TAG, "Scheduled custom alarm for " + time + " with request code " + requestCode);
                } else {
                    Log.w(TAG, "Cannot schedule exact alarm, permission not granted.");
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.d(TAG, "Scheduled custom alarm for " + time + " with request code " + requestCode);
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing time in scheduleCustomAlarm: " + time, e);
        }
    }

    /**
     * Robustly cancels all possible scheduled alarms for this app.
     */
    public static void cancelAll(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Cancel hourly alarm
        Intent hourlyIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent hourlyPendingIntent = PendingIntent.getBroadcast(context, HOURLY_ALARM_REQUEST_CODE, hourlyIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (hourlyPendingIntent != null) {
            alarmManager.cancel(hourlyPendingIntent);
            hourlyPendingIntent.cancel();
            Log.d(TAG, "Cancelled hourly alarm.");
        }

        // Cancel ALL potential custom alarms by iterating through previously saved times.
        // This ensures that even if a time was deleted, its alarm is found and cancelled.
        SharedPreferences prefs = context.getSharedPreferences(BootReceiver.PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> customTimes = prefs.getStringSet("customNotificationTimes", new HashSet<>());
        for (String time : customTimes) {
            cancelCustomAlarm(context, time);
        }
        Log.d(TAG, "Finished cancelling all known custom alarms.");
    }
    
    // Helper to cancel a single custom alarm
    private static void cancelCustomAlarm(Context context, String time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null || time == null || !time.matches("\\d{2}:\\d{2}")) return;

        try {
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            int requestCode = hour * 100 + minute;

            Intent customIntent = new Intent(context, NotificationReceiver.class);
            PendingIntent customPendingIntent = PendingIntent.getBroadcast(context, requestCode, customIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (customPendingIntent != null) {
                alarmManager.cancel(customPendingIntent);
                customPendingIntent.cancel();
                Log.d(TAG, "Cancelled custom alarm for " + time);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing time in cancelCustomAlarm: " + time, e);
        }
    }
}
