package com.todoapp.util;

/**
 * Constants - Application-wide constant values.
 * 
 * Central place for all constant values used throughout the app.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // ==================== APP INFO ====================
    public static final String APP_NAME = "TodoApp";
    public static final String APP_VERSION = "1.0.0";

    // ==================== NOTIFICATION CHANNELS ====================
    public static final String NOTIFICATION_CHANNEL_REMINDERS = "reminders";
    public static final String NOTIFICATION_CHANNEL_GENERAL = "general";
    public static final String NOTIFICATION_CHANNEL_SUMMARY = "summary";

    // ==================== NOTIFICATION IDS ====================
    public static final int NOTIFICATION_ID_BASE = 1000;
    public static final int NOTIFICATION_ID_DAILY_SUMMARY = 9000;

    // ==================== REQUEST CODES ====================
    public static final int REQUEST_CODE_NOTIFICATION = 100;
    public static final int REQUEST_CODE_ALARM = 200;
    public static final int REQUEST_CODE_PICK_DATE = 300;
    public static final int REQUEST_CODE_PICK_TIME = 301;
    public static final int REQUEST_CODE_PERMISSIONS = 400;

    // ==================== INTENT EXTRAS ====================
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    public static final String EXTRA_ACTION = "extra_action";
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";

    // ==================== INTENT ACTIONS ====================
    public static final String ACTION_COMPLETE_TASK = "com.todoapp.ACTION_COMPLETE_TASK";
    public static final String ACTION_SNOOZE_REMINDER = "com.todoapp.ACTION_SNOOZE_REMINDER";
    public static final String ACTION_OPEN_TASK = "com.todoapp.ACTION_OPEN_TASK";
    public static final String ACTION_ADD_TASK = "com.todoapp.ACTION_ADD_TASK";
    public static final String ACTION_WIDGET_UPDATE = "com.todoapp.ACTION_WIDGET_UPDATE";

    // ==================== SHARED PREFERENCES ====================
    public static final String PREFS_NAME = "todo_app_prefs";
    public static final String PREF_THEME_MODE = "theme_mode";
    public static final String PREF_DEFAULT_PRIORITY = "default_priority";
    public static final String PREF_DEFAULT_REMINDER_TIME = "default_reminder_time";
    public static final String PREF_SHOW_COMPLETED = "show_completed";
    public static final String PREF_SORT_ORDER = "sort_order";
    public static final String PREF_FIRST_LAUNCH = "first_launch";
    public static final String PREF_LAST_BACKUP_DATE = "last_backup_date";
    public static final String PREF_NOTIFICATION_SOUND = "notification_sound";
    public static final String PREF_NOTIFICATION_VIBRATE = "notification_vibrate";
    public static final String PREF_DAILY_SUMMARY_ENABLED = "daily_summary_enabled";
    public static final String PREF_DAILY_SUMMARY_TIME = "daily_summary_time";

    // ==================== THEME VALUES ====================
    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    // ==================== SNOOZE OPTIONS (in minutes) ====================
    public static final int SNOOZE_5_MINUTES = 5;
    public static final int SNOOZE_15_MINUTES = 15;
    public static final int SNOOZE_30_MINUTES = 30;
    public static final int SNOOZE_1_HOUR = 60;
    public static final int SNOOZE_3_HOURS = 180;
    public static final int SNOOZE_1_DAY = 1440;

    // ==================== REMINDER DEFAULTS (in milliseconds) ====================
    public static final long REMINDER_BEFORE_5_MIN = 5 * 60 * 1000;
    public static final long REMINDER_BEFORE_15_MIN = 15 * 60 * 1000;
    public static final long REMINDER_BEFORE_30_MIN = 30 * 60 * 1000;
    public static final long REMINDER_BEFORE_1_HOUR = 60 * 60 * 1000;
    public static final long REMINDER_BEFORE_1_DAY = 24 * 60 * 60 * 1000;

    // ==================== WORK MANAGER TAGS ====================
    public static final String WORK_TAG_REMINDER = "reminder_work";
    public static final String WORK_TAG_DAILY_SUMMARY = "daily_summary_work";
    public static final String WORK_TAG_REPEATING_TASK = "repeating_task_work";
    public static final String WORK_TAG_BACKUP = "backup_work";

    // ==================== FILE PATHS ====================
    public static final String BACKUP_FOLDER = "TodoApp_Backups";
    public static final String BACKUP_FILE_PREFIX = "todo_backup_";
    public static final String BACKUP_FILE_EXTENSION = ".json";

    // ==================== DATE/TIME FORMATS ====================
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String DATE_FORMAT_SHORT = "MMM dd";
    public static final String DATE_FORMAT_FULL = "EEEE, MMMM dd, yyyy";
    public static final String TIME_FORMAT_12H = "hh:mm a";
    public static final String TIME_FORMAT_24H = "HH:mm";
    public static final String DATE_TIME_FORMAT = "MMM dd, yyyy hh:mm a";
    public static final String BACKUP_DATE_FORMAT = "yyyyMMdd_HHmmss";

    // ==================== ANIMATION DURATIONS ====================
    public static final int ANIMATION_DURATION_SHORT = 150;
    public static final int ANIMATION_DURATION_MEDIUM = 300;
    public static final int ANIMATION_DURATION_LONG = 500;

    // ==================== LIMITS ====================
    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;
    public static final int MAX_SUBTASKS = 50;
    public static final int MAX_CATEGORIES = 20;
}
