package com.todoapp.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.todoapp.data.database.TodoDatabase;
import com.todoapp.data.model.Task;

import java.util.List;

/**
 * BootReceiver - BroadcastReceiver for rescheduling alarms after device boot.
 * 
 * When the device restarts, all scheduled alarms are lost.
 * This receiver reschedules all pending task reminders.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            Log.d(TAG, "Device boot completed, rescheduling reminders");
            rescheduleAllReminders(context);
        }
    }

    /**
     * Reschedule all pending task reminders
     */
    private void rescheduleAllReminders(Context context) {
        TodoDatabase.databaseWriteExecutor.execute(() -> {
            try {
                TodoDatabase database = TodoDatabase.getInstance(context);
                ReminderScheduler scheduler = new ReminderScheduler(context);

                // Get all tasks with pending reminders
                List<Task> tasksWithReminders = database.taskDao()
                    .getTasksWithRemindersSync(System.currentTimeMillis());

                for (Task task : tasksWithReminders) {
                    if (task.isHasReminder() && task.getReminderTime() != null) {
                        scheduler.scheduleReminder(task);
                        Log.d(TAG, "Rescheduled reminder for task: " + task.getId());
                    }
                }

                Log.d(TAG, "Rescheduled " + tasksWithReminders.size() + " reminders");
            } catch (Exception e) {
                Log.e(TAG, "Failed to reschedule reminders: " + e.getMessage());
            }
        });
    }
}
