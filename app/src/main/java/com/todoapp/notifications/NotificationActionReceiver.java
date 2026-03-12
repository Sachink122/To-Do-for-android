package com.todoapp.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.todoapp.data.database.TodoDatabase;
import com.todoapp.data.model.Task;
import com.todoapp.util.Constants;

/**
 * NotificationActionReceiver - Handles notification action button clicks.
 * 
 * Processes actions like "Complete" and "Snooze" from notification buttons.
 */
public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        long taskId = intent.getLongExtra(Constants.EXTRA_TASK_ID, -1);
        int notificationId = intent.getIntExtra(Constants.EXTRA_NOTIFICATION_ID, -1);

        Log.d(TAG, "Received action: " + action + " for task: " + taskId);

        if (taskId == -1) {
            Log.e(TAG, "No task ID in intent");
            return;
        }

        NotificationHelper notificationHelper = new NotificationHelper(context);

        if (Constants.ACTION_COMPLETE_TASK.equals(action)) {
            handleCompleteTask(context, taskId, notificationId, notificationHelper);
        } else if (Constants.ACTION_SNOOZE_REMINDER.equals(action)) {
            handleSnoozeReminder(context, taskId, notificationId, notificationHelper);
        }
    }

    /**
     * Handle task completion from notification
     */
    private void handleCompleteTask(Context context, long taskId, int notificationId,
                                   NotificationHelper notificationHelper) {
        // Dismiss notification
        notificationHelper.cancelNotification(notificationId);

        // Mark task as completed in database
        TodoDatabase.databaseWriteExecutor.execute(() -> {
            try {
                TodoDatabase database = TodoDatabase.getInstance(context);
                long now = System.currentTimeMillis();
                database.taskDao().setTaskCompleted(taskId, true, now, now);
                Log.d(TAG, "Task " + taskId + " marked as completed");

                // Cancel any scheduled reminders
                ReminderScheduler scheduler = new ReminderScheduler(context);
                scheduler.cancelReminder(taskId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to complete task: " + e.getMessage());
            }
        });
    }

    /**
     * Handle snooze action from notification
     */
    private void handleSnoozeReminder(Context context, long taskId, int notificationId,
                                     NotificationHelper notificationHelper) {
        // Dismiss current notification
        notificationHelper.cancelNotification(notificationId);

        // Get task title for rescheduling
        TodoDatabase.databaseWriteExecutor.execute(() -> {
            try {
                TodoDatabase database = TodoDatabase.getInstance(context);
                Task task = database.taskDao().getTaskByIdSync(taskId);
                
                if (task != null) {
                    // Schedule snooze for 15 minutes (default)
                    ReminderScheduler scheduler = new ReminderScheduler(context);
                    scheduler.scheduleSnooze(taskId, task.getTitle(), Constants.SNOOZE_15_MINUTES);
                    Log.d(TAG, "Snoozed reminder for task " + taskId + " for 15 minutes");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to snooze reminder: " + e.getMessage());
            }
        });
    }
}
