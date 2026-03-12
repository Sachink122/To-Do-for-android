package com.todoapp.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.todoapp.data.model.Task;
import com.todoapp.util.Constants;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ReminderScheduler - Schedules exact alarms for task reminders.
 * 
 * Uses AlarmManager for precise timing of notifications.
 * WorkManager is used for less time-critical background work.
 */
@Singleton
public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";
    private final Context context;
    private final AlarmManager alarmManager;

    @Inject
    public ReminderScheduler(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Schedule a reminder for a task
     */
    public void scheduleReminder(Task task) {
        if (task == null || !task.isHasReminder() || task.getReminderTime() == null) {
            Log.d(TAG, "Task has no reminder to schedule");
            return;
        }

        long reminderTime = task.getReminderTime();
        
        // Don't schedule if reminder time is in the past
        if (reminderTime <= System.currentTimeMillis()) {
            Log.d(TAG, "Reminder time is in the past, not scheduling");
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(Constants.ACTION_OPEN_TASK);
        intent.putExtra(Constants.EXTRA_TASK_ID, task.getId());
        intent.putExtra(Constants.EXTRA_TASK_TITLE, task.getTitle());

        int requestCode = (int) (Constants.REQUEST_CODE_ALARM + task.getId());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ requires checking for exact alarm permission
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    );
                    Log.d(TAG, "Scheduled exact alarm for task " + task.getId());
                } else {
                    // Fall back to inexact alarm
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    );
                    Log.d(TAG, "Scheduled inexact alarm for task " + task.getId());
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                );
                Log.d(TAG, "Scheduled exact alarm for task " + task.getId());
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                );
                Log.d(TAG, "Scheduled exact alarm for task " + task.getId());
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule exact alarm: " + e.getMessage());
            // Fall back to WorkManager for this reminder
        }
    }

    /**
     * Cancel a scheduled reminder
     */
    public void cancelReminder(long taskId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(Constants.ACTION_OPEN_TASK);

        int requestCode = (int) (Constants.REQUEST_CODE_ALARM + taskId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        Log.d(TAG, "Cancelled reminder for task " + taskId);
    }

    /**
     * Reschedule a reminder (cancel and create new)
     */
    public void rescheduleReminder(Task task) {
        cancelReminder(task.getId());
        scheduleReminder(task);
    }

    /**
     * Schedule snooze reminder
     */
    public void scheduleSnooze(long taskId, String taskTitle, int snoozeMinutes) {
        long snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(Constants.ACTION_OPEN_TASK);
        intent.putExtra(Constants.EXTRA_TASK_ID, taskId);
        intent.putExtra(Constants.EXTRA_TASK_TITLE, taskTitle);

        int requestCode = (int) (Constants.REQUEST_CODE_ALARM + taskId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                );
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                );
            }
            Log.d(TAG, "Scheduled snooze for task " + taskId + " in " + snoozeMinutes + " minutes");
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule snooze: " + e.getMessage());
        }
    }

    /**
     * Check if exact alarms can be scheduled
     */
    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
}
