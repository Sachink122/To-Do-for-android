package com.todoapp.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.todoapp.data.database.TodoDatabase;
import com.todoapp.data.model.Task;
import com.todoapp.util.Constants;

/**
 * AlarmReceiver - BroadcastReceiver for handling alarm triggers.
 * 
 * Receives alarm broadcasts and shows the appropriate notification.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received");

        if (intent == null) {
            return;
        }

        long taskId = intent.getLongExtra(Constants.EXTRA_TASK_ID, -1);
        String taskTitle = intent.getStringExtra(Constants.EXTRA_TASK_TITLE);

        if (taskId == -1) {
            Log.e(TAG, "No task ID in alarm intent");
            return;
        }

        // Load task from database and show notification
        TodoDatabase.databaseWriteExecutor.execute(() -> {
            TodoDatabase database = TodoDatabase.getInstance(context);
            Task task = database.taskDao().getTaskByIdSync(taskId);

            if (task != null && !task.isCompleted()) {
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showTaskReminder(task);
                Log.d(TAG, "Showed notification for task: " + task.getTitle());
            } else {
                Log.d(TAG, "Task not found or already completed: " + taskId);
            }
        });
    }
}
