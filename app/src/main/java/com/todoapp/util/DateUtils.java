package com.todoapp.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * DateUtils - Utility class for date and time operations.
 * 
 * Provides helper methods for formatting, parsing, and calculating dates.
 */
public final class DateUtils {

    private DateUtils() {
        // Prevent instantiation
    }

    // ==================== FORMATTERS ====================

    private static final SimpleDateFormat DATE_DISPLAY = 
        new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
    private static final SimpleDateFormat DATE_SHORT = 
        new SimpleDateFormat(Constants.DATE_FORMAT_SHORT, Locale.getDefault());
    private static final SimpleDateFormat DATE_FULL = 
        new SimpleDateFormat(Constants.DATE_FORMAT_FULL, Locale.getDefault());
    private static final SimpleDateFormat TIME_12H = 
        new SimpleDateFormat(Constants.TIME_FORMAT_12H, Locale.getDefault());
    private static final SimpleDateFormat TIME_24H = 
        new SimpleDateFormat(Constants.TIME_FORMAT_24H, Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME = 
        new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
    private static final SimpleDateFormat BACKUP_FORMAT = 
        new SimpleDateFormat(Constants.BACKUP_DATE_FORMAT, Locale.getDefault());

    // ==================== FORMAT METHODS ====================

    /**
     * Format timestamp to display date (e.g., "Jan 15, 2024")
     */
    public static String formatDate(long timestamp) {
        return DATE_DISPLAY.format(new Date(timestamp));
    }

    /**
     * Format timestamp to short date (e.g., "Jan 15")
     */
    public static String formatDateShort(long timestamp) {
        return DATE_SHORT.format(new Date(timestamp));
    }

    /**
     * Format timestamp to full date (e.g., "Monday, January 15, 2024")
     */
    public static String formatDateFull(long timestamp) {
        return DATE_FULL.format(new Date(timestamp));
    }

    /**
     * Format timestamp to time (e.g., "02:30 PM")
     */
    public static String formatTime(long timestamp) {
        return TIME_12H.format(new Date(timestamp));
    }

    /**
     * Format timestamp to 24h time (e.g., "14:30")
     */
    public static String formatTime24h(long timestamp) {
        return TIME_24H.format(new Date(timestamp));
    }

    /**
     * Format timestamp to date and time (e.g., "Jan 15, 2024 02:30 PM")
     */
    public static String formatDateTime(long timestamp) {
        return DATE_TIME.format(new Date(timestamp));
    }

    /**
     * Format timestamp to relative date (e.g., "Today", "Tomorrow", "Jan 15")
     */
    public static String formatRelativeDate(Long timestamp) {
        if (timestamp == null) {
            return "";
        }
        
        if (isToday(timestamp)) {
            return "Today";
        } else if (isTomorrow(timestamp)) {
            return "Tomorrow";
        } else if (isYesterday(timestamp)) {
            return "Yesterday";
        } else {
            return formatDateShort(timestamp);
        }
    }

    /**
     * Format timestamp for backup filename
     */
    public static String formatForBackup(long timestamp) {
        return BACKUP_FORMAT.format(new Date(timestamp));
    }

    // ==================== RELATIVE TIME ====================

    /**
     * Get human-readable relative time (e.g., "2 hours ago", "in 3 days")
     */
    public static String getRelativeTimeSpan(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = timestamp - now;
        boolean isPast = diff < 0;
        long absDiff = Math.abs(diff);

        if (absDiff < TimeUnit.MINUTES.toMillis(1)) {
            return "just now";
        } else if (absDiff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(absDiff);
            String unit = minutes == 1 ? "minute" : "minutes";
            return isPast ? minutes + " " + unit + " ago" : "in " + minutes + " " + unit;
        } else if (absDiff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(absDiff);
            String unit = hours == 1 ? "hour" : "hours";
            return isPast ? hours + " " + unit + " ago" : "in " + hours + " " + unit;
        } else if (absDiff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(absDiff);
            String unit = days == 1 ? "day" : "days";
            return isPast ? days + " " + unit + " ago" : "in " + days + " " + unit;
        } else if (absDiff < TimeUnit.DAYS.toMillis(30)) {
            long weeks = TimeUnit.MILLISECONDS.toDays(absDiff) / 7;
            String unit = weeks == 1 ? "week" : "weeks";
            return isPast ? weeks + " " + unit + " ago" : "in " + weeks + " " + unit;
        } else {
            return formatDate(timestamp);
        }
    }

    /**
     * Get friendly due date text
     */
    public static String getDueDateText(Long dueDate, Long dueTime) {
        if (dueDate == null) {
            return "No due date";
        }

        long now = System.currentTimeMillis();
        
        if (isToday(dueDate)) {
            if (dueTime != null) {
                return "Today at " + formatTime(dueTime);
            }
            return "Today";
        } else if (isTomorrow(dueDate)) {
            if (dueTime != null) {
                return "Tomorrow at " + formatTime(dueTime);
            }
            return "Tomorrow";
        } else if (isYesterday(dueDate)) {
            return "Yesterday";
        } else if (isThisWeek(dueDate) && dueDate > now) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dueDate);
            String dayName = new SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.getTime());
            if (dueTime != null) {
                return dayName + " at " + formatTime(dueTime);
            }
            return dayName;
        } else {
            if (dueTime != null) {
                return formatDateShort(dueDate) + " at " + formatTime(dueTime);
            }
            return formatDateShort(dueDate);
        }
    }

    // ==================== DATE CHECKS ====================

    /**
     * Check if timestamp is today
     */
    public static boolean isToday(long timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        
        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if timestamp is tomorrow
     */
    public static boolean isTomorrow(long timestamp) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        
        return tomorrow.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
               tomorrow.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if timestamp is yesterday
     */
    public static boolean isYesterday(long timestamp) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        
        return yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
               yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if timestamp is this week
     */
    public static boolean isThisWeek(long timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        
        return now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
               now.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Check if timestamp is overdue
     */
    public static boolean isOverdue(long timestamp) {
        return timestamp < System.currentTimeMillis();
    }

    // ==================== DATE CALCULATION ====================

    /**
     * Get start of day for timestamp
     */
    public static long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Get end of day for timestamp
     */
    public static long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    /**
     * Get start of week
     */
    public static long getStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        return getStartOfDay(cal.getTimeInMillis());
    }

    /**
     * Get end of week
     */
    public static long getEndOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        cal.add(Calendar.MILLISECOND, -1);
        return cal.getTimeInMillis();
    }

    /**
     * Get start of month
     */
    public static long getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(cal.getTimeInMillis());
    }

    /**
     * Add days to timestamp
     */
    public static long addDays(long timestamp, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTimeInMillis();
    }

    /**
     * Add weeks to timestamp
     */
    public static long addWeeks(long timestamp, int weeks) {
        return addDays(timestamp, weeks * 7);
    }

    /**
     * Add months to timestamp
     */
    public static long addMonths(long timestamp, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.add(Calendar.MONTH, months);
        return cal.getTimeInMillis();
    }

    /**
     * Get next occurrence for repeating task
     */
    public static long getNextRepeatDate(long currentDate, int repeatInterval) {
        switch (repeatInterval) {
            case 1: // Daily
                return addDays(currentDate, 1);
            case 2: // Weekly
                return addWeeks(currentDate, 1);
            case 3: // Monthly
                return addMonths(currentDate, 1);
            case 4: // Yearly
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(currentDate);
                cal.add(Calendar.YEAR, 1);
                return cal.getTimeInMillis();
            default:
                return currentDate;
        }
    }

    /**
     * Combine date and time into single timestamp
     */
    public static long combineDateTime(long date, long time) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTimeInMillis(date);
        
        Calendar timeCal = Calendar.getInstance();
        timeCal.setTimeInMillis(time);
        
        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);
        
        return dateCal.getTimeInMillis();
    }
}
