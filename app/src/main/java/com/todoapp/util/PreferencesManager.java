package com.todoapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * PreferencesManager - Manages shared preferences for app settings.
 * 
 * Provides type-safe access to all app preferences with default values.
 */
@Singleton
public class PreferencesManager {

    // Theme mode constants (mirror from Constants for convenience)
    public static final int THEME_SYSTEM = Constants.THEME_SYSTEM;
    public static final int THEME_LIGHT = Constants.THEME_LIGHT;
    public static final int THEME_DARK = Constants.THEME_DARK;

    private final SharedPreferences sharedPreferences;

    @Inject
    public PreferencesManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(
            Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ==================== THEME ====================

    public int getThemeMode() {
        return sharedPreferences.getInt(Constants.PREF_THEME_MODE, Constants.THEME_SYSTEM);
    }

    public void setThemeMode(int themeMode) {
        sharedPreferences.edit().putInt(Constants.PREF_THEME_MODE, themeMode).apply();
    }

    // ==================== REMINDERS ====================

    public boolean getRemindersEnabled() {
        return sharedPreferences.getBoolean("reminders_enabled", true);
    }

    public void setRemindersEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean("reminders_enabled", enabled).apply();
    }

    // ==================== DEFAULT VALUES ====================

    public int getDefaultPriority() {
        return sharedPreferences.getInt(Constants.PREF_DEFAULT_PRIORITY, 0);
    }

    public void setDefaultPriority(int priority) {
        sharedPreferences.edit().putInt(Constants.PREF_DEFAULT_PRIORITY, priority).apply();
    }

    public Long getDefaultCategoryId() {
        long categoryId = sharedPreferences.getLong("default_category_id", -1);
        return categoryId == -1 ? null : categoryId;
    }

    public void setDefaultCategoryId(Long categoryId) {
        if (categoryId == null) {
            sharedPreferences.edit().putLong("default_category_id", -1).apply();
        } else {
            sharedPreferences.edit().putLong("default_category_id", categoryId).apply();
        }
    }

    public long getDefaultReminderTime() {
        return sharedPreferences.getLong(Constants.PREF_DEFAULT_REMINDER_TIME, 
            Constants.REMINDER_BEFORE_30_MIN);
    }

    public void setDefaultReminderTime(long reminderTime) {
        sharedPreferences.edit().putLong(Constants.PREF_DEFAULT_REMINDER_TIME, reminderTime).apply();
    }

    // ==================== DISPLAY SETTINGS ====================

    public boolean getShowCompleted() {
        return sharedPreferences.getBoolean(Constants.PREF_SHOW_COMPLETED, true);
    }

    public void setShowCompleted(boolean showCompleted) {
        sharedPreferences.edit().putBoolean(Constants.PREF_SHOW_COMPLETED, showCompleted).apply();
    }

    public int getSortOrder() {
        return sharedPreferences.getInt(Constants.PREF_SORT_ORDER, 0);
    }

    public void setSortOrder(int sortOrder) {
        sharedPreferences.edit().putInt(Constants.PREF_SORT_ORDER, sortOrder).apply();
    }

    // ==================== NOTIFICATION SETTINGS ====================

    public boolean getNotificationSoundEnabled() {
        return sharedPreferences.getBoolean(Constants.PREF_NOTIFICATION_SOUND, true);
    }

    public void setNotificationSoundEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(Constants.PREF_NOTIFICATION_SOUND, enabled).apply();
    }

    public boolean getNotificationVibrateEnabled() {
        return sharedPreferences.getBoolean(Constants.PREF_NOTIFICATION_VIBRATE, true);
    }

    public void setNotificationVibrateEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(Constants.PREF_NOTIFICATION_VIBRATE, enabled).apply();
    }

    public boolean getDailySummaryEnabled() {
        return sharedPreferences.getBoolean(Constants.PREF_DAILY_SUMMARY_ENABLED, false);
    }

    public void setDailySummaryEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(Constants.PREF_DAILY_SUMMARY_ENABLED, enabled).apply();
    }

    public long getDailySummaryTime() {
        // Default: 8:00 AM
        return sharedPreferences.getLong(Constants.PREF_DAILY_SUMMARY_TIME, 8 * 60 * 60 * 1000);
    }

    public void setDailySummaryTime(long time) {
        sharedPreferences.edit().putLong(Constants.PREF_DAILY_SUMMARY_TIME, time).apply();
    }

    // ==================== BACKUP SETTINGS ====================

    public long getLastBackupDate() {
        return sharedPreferences.getLong(Constants.PREF_LAST_BACKUP_DATE, 0);
    }

    public void setLastBackupDate(long date) {
        sharedPreferences.edit().putLong(Constants.PREF_LAST_BACKUP_DATE, date).apply();
    }

    // ==================== PROFILE SETTINGS ====================

    public boolean isUserSignedIn() {
        return sharedPreferences.getBoolean("user_signed_in", false);
    }

    public void setUserSignedIn(boolean signedIn) {
        sharedPreferences.edit().putBoolean("user_signed_in", signedIn).apply();
    }

    public String getUserName() {
        return sharedPreferences.getString("user_name", "");
    }

    public void setUserName(String name) {
        sharedPreferences.edit().putString("user_name", name).apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString("user_email", "");
    }

    public void setUserEmail(String email) {
        sharedPreferences.edit().putString("user_email", email).apply();
    }

    public String getUserPhotoUrl() {
        return sharedPreferences.getString("user_photo_url", "");
    }

    public void setUserPhotoUrl(String photoUrl) {
        sharedPreferences.edit().putString("user_photo_url", photoUrl).apply();
    }

    public String getLocalProfileImagePath() {
        return sharedPreferences.getString("local_profile_image_path", "");
    }

    public void setLocalProfileImagePath(String path) {
        sharedPreferences.edit().putString("local_profile_image_path", path).apply();
    }

    public void clearUserProfile() {
        sharedPreferences.edit()
            .remove("user_signed_in")
            .remove("user_name")
            .remove("user_email")
            .remove("user_photo_url")
            .apply();
    }

    // ==================== APP STATE ====================

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(Constants.PREF_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean isFirst) {
        sharedPreferences.edit().putBoolean(Constants.PREF_FIRST_LAUNCH, isFirst).apply();
    }

    // ==================== GENERIC METHODS ====================

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void putLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}
