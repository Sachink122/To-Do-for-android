package com.todoapp.di;

import android.content.Context;

import com.todoapp.notifications.NotificationHelper;
import com.todoapp.notifications.ReminderScheduler;
import com.todoapp.util.PreferencesManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * AppModule - Hilt module for providing app-wide dependencies.
 * 
 * This module provides utility classes and helpers that are used
 * throughout the application.
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    /**
     * Provide the PreferencesManager for storing app settings
     */
    @Provides
    @Singleton
    public PreferencesManager providePreferencesManager(@ApplicationContext Context context) {
        return new PreferencesManager(context);
    }

    /**
     * Provide the NotificationHelper for showing notifications
     */
    @Provides
    @Singleton
    public NotificationHelper provideNotificationHelper(@ApplicationContext Context context) {
        return new NotificationHelper(context);
    }

    /**
     * Provide the ReminderScheduler for scheduling alarms
     */
    @Provides
    @Singleton
    public ReminderScheduler provideReminderScheduler(@ApplicationContext Context context) {
        return new ReminderScheduler(context);
    }
}
