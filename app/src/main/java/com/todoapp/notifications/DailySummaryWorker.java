package com.todoapp.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.todoapp.data.dao.TaskDao;
import com.todoapp.data.database.TodoDatabase;
import com.todoapp.data.model.Task;

import java.util.Calendar;
import java.util.List;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

/**
 * DailySummaryWorker - WorkManager worker for daily task summary notifications.
 * 
 * Runs once daily to show a summary of pending and due tasks.
 */
@HiltWorker
public class DailySummaryWorker extends Worker {

    private final Context context;

    @AssistedInject
    public DailySummaryWorker(
            @Assisted Context context,
            @Assisted WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            TodoDatabase database = TodoDatabase.getInstance(context);
            TaskDao taskDao = database.taskDao();

            // Get current time bounds
            long now = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfDay = cal.getTimeInMillis();
            long endOfDay = startOfDay + (24 * 60 * 60 * 1000);

            // Get task counts
            List<Task> allTasks = taskDao.getAllTasksSync();
            int pendingCount = 0;
            int overdueCount = 0;
            int dueTodayCount = 0;

            for (Task task : allTasks) {
                // Skip completed, archived, and deleted tasks
                if (!task.isCompleted() && !task.isArchived() && !task.isDeleted()) {
                    pendingCount++;
                    
                    if (task.getDueDate() != null) {
                        if (task.getDueDate() < now) {
                            overdueCount++;
                        } else if (task.getDueDate() >= startOfDay && task.getDueDate() < endOfDay) {
                            dueTodayCount++;
                        }
                    }
                }
            }

            // Show notification if there are tasks
            if (pendingCount > 0 || overdueCount > 0 || dueTodayCount > 0) {
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showDailySummary(pendingCount, overdueCount, dueTodayCount);
            }

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}
