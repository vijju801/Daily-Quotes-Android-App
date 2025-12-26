package com.example.myapplication1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";
    private static final String CHANNEL_ID = "QUOTE_CHANNEL";
    private static final int NOTIFICATION_ID = 101;

    private static final String[] QUOTES = {
            "The only way to do great work is to love what you do. - Steve Jobs",
            "Strive not to be a success, but rather to be of value. - Albert Einstein",
            "The mind is everything. What you think you become. - Buddha"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received. Preparing to show notification.");

        createNotificationChannel(context);

        // Explicitly check for notification permission before trying to post.
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Cannot post notification. POST_NOTIFICATIONS permission not granted.");
            return; // Stop execution if permission is not granted.
        }

        String randomQuote = QUOTES[new Random().nextInt(QUOTES.length)];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name) // A safe, standard icon name.
                .setContentTitle("Quote of the Day")
                .setContentText(randomQuote)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(randomQuote))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Notification successfully posted.");
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while posting notification. This should not happen if permission check passes.", e);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Quote Notifications";
            String description = "Channel for daily and hourly quote notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created/ensured.");
            }
        }
    }
}
