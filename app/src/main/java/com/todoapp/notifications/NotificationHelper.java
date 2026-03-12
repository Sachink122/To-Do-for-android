package com.todoapp.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.todoapp.R;
import com.todoapp.data.model.Task;
import com.todoapp.ui.MainActivity;
import com.todoapp.util.Constants;
import com.todoapp.util.DateUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * NotificationHelper - Helper class for creating and showing notifications.
 * 
 * Handles all notification creation, styling, and actions for task reminders.
 */
@Singleton
public class NotificationHelper {

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    @Inject
    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(context);
    }

    /**
     * Show a task reminder notification
     */
    public void showTaskReminder(Task task) {
        if (task == null) return;

        int notificationId = (int) (Constants.NOTIFICATION_ID_BASE + task.getId());

        // Create intent to open task detail
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setAction(Constants.ACTION_OPEN_TASK);
        openIntent.putExtra(Constants.EXTRA_TASK_ID, task.getId());
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create complete action
        Intent completeIntent = new Intent(context, NotificationActionReceiver.class);
        completeIntent.setAction(Constants.ACTION_COMPLETE_TASK);
        completeIntent.putExtra(Constants.EXTRA_TASK_ID, task.getId());
        completeIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create snooze action
        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction(Constants.ACTION_SNOOZE_REMINDER);
        snoozeIntent.putExtra(Constants.EXTRA_TASK_ID, task.getId());
        snoozeIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification content
        String contentText = buildContentText(task);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, Constants.NOTIFICATION_CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.getTitle())
            .setContentText(contentText)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(getPriorityLevel(task.getPriority()))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_check, "Complete", completePendingIntent)
            .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)
            .setDefaults(Notification.DEFAULT_ALL)
            .setColor(getNotificationColor(task.getPriority()));

        // Show notification
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // Handle permission not granted
            e.printStackTrace();
        }
    }

    /**
     * Show a daily summary notification
     */
    public void showDailySummary(int pendingCount, int overdueCount, int dueToday) {
        int notificationId = Constants.NOTIFICATION_ID_DAILY_SUMMARY;

        // Create intent to open app
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build content
        StringBuilder content = new StringBuilder();
        if (dueToday > 0) {
            content.append(dueToday).append(" task").append(dueToday > 1 ? "s" : "")
                   .append(" due today. ");
        }
        if (overdueCount > 0) {
            content.append(overdueCount).append(" overdue. ");
        }
        if (pendingCount > 0) {
            content.append(pendingCount).append(" total pending.");
        }
        if (content.length() == 0) {
            content.append("No pending tasks. Great job!");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, Constants.NOTIFICATION_CHANNEL_SUMMARY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Task Summary")
            .setContentText(content.toString())
            .setStyle(new NotificationCompat.BigTextStyle().bigText(content.toString()))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent);

        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancel a notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    /**
     * Cancel task reminder notification
     */
    public void cancelTaskReminder(long taskId) {
        int notificationId = (int) (Constants.NOTIFICATION_ID_BASE + taskId);
        notificationManager.cancel(notificationId);
    }

    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    // ==================== HELPER METHODS ====================

    private String buildContentText(Task task) {
        StringBuilder text = new StringBuilder();

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            text.append(task.getDescription());
        }

        if (task.getDueDate() != null) {
            if (text.length() > 0) text.append("\n");
            text.append("Due: ").append(DateUtils.getDueDateText(task.getDueDate(), task.getDueTime()));
        }

        if (text.length() == 0) {
            text.append("Tap to view task details");
        }

        return text.toString();
    }

    private int getPriorityLevel(int taskPriority) {
        switch (taskPriority) {
            case Task.Priority.HIGH:
                return NotificationCompat.PRIORITY_HIGH;
            case Task.Priority.MEDIUM:
                return NotificationCompat.PRIORITY_DEFAULT;
            case Task.Priority.LOW:
                return NotificationCompat.PRIORITY_LOW;
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
        }
    }

    private int getNotificationColor(int taskPriority) {
        switch (taskPriority) {
            case Task.Priority.HIGH:
                return 0xFFF44336; // Red
            case Task.Priority.MEDIUM:
                return 0xFFFF9800; // Orange
            case Task.Priority.LOW:
                return 0xFF4CAF50; // Green
            default:
                return 0xFF2196F3; // Blue
        }
    }
}
