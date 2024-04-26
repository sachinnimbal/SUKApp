package com.example.sukappusers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "DeviceIdChangeChannel";
    private static final String CHANNEL_NAME = "Device ID Change";
    private static final int NOTIFICATION_ID = 1;

    public static void showDeviceIdChangedNotification(Context context) {
        // Create an explicit intent for the ResetPasswordActivity
        Intent intent = new Intent(context, ResetPasswordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Create the notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logout)
                .setContentTitle("Logged in on Another Device")
                .setContentText("You have been logged out because you logged in on another device. Please reset your password since you are not logged in.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        // Show the notification
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
