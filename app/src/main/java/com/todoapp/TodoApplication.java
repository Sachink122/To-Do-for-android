package com.todoapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;

import com.todoapp.util.Constants;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

/**
 * TodoApplication - Main Application class with Hilt integration.
 * 
 * This class initializes Hilt for dependency injection and sets up
 * notification channels for reminders.
 */
@HiltAndroidApp
public class TodoApplication extends Application implements Configuration.Provider {

    @Inject
    HiltWorkerFactory workerFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Create notification channels
        createNotificationChannels();
    }

    /**
     * Create notification channels for Android O and above.
     * Channels are required for showing notifications on API 26+.
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                getSystemService(NotificationManager.class);

            // Task Reminder Channel (high priority)
            NotificationChannel reminderChannel = new NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_REMINDERS,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setDescription("Notifications for task reminders and due dates");
            reminderChannel.enableVibration(true);
            reminderChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(reminderChannel);

            // General Notifications Channel (default priority)
            NotificationChannel generalChannel = new NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("General app notifications");
            notificationManager.createNotificationChannel(generalChannel);

            // Daily Summary Channel (low priority)
            NotificationChannel summaryChannel = new NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SUMMARY,
                "Daily Summary",
                NotificationManager.IMPORTANCE_LOW
            );
            summaryChannel.setDescription("Daily task summary notifications");
            notificationManager.createNotificationChannel(summaryChannel);
        }
    }

    /**
     * Provide WorkManager configuration with Hilt worker factory.
     * This allows Hilt to inject dependencies into Worker classes.
     */
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build();
    }
}
