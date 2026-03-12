package com.todoapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.todoapp.R;
import com.todoapp.ui.MainActivity;

/**
 * Home screen widget provider for displaying pending tasks.
 * Updates periodically and responds to task changes.
 */
public class TaskWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "com.todoapp.widget.ACTION_REFRESH";
    public static final String ACTION_TASK_CLICK = "com.todoapp.widget.ACTION_TASK_CLICK";
    public static final String EXTRA_TASK_ID = "extra_task_id";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Create RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task_list);

        // Set up the intent for the list view service
        Intent serviceIntent = new Intent(context, TaskWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.list_tasks, serviceIntent);

        // Set empty view
        views.setEmptyView(R.id.list_tasks, R.id.layout_empty);

        // Set up add task button
        Intent addTaskIntent = new Intent(context, MainActivity.class);
        addTaskIntent.setAction("ACTION_ADD_TASK");
        addTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent addTaskPendingIntent = PendingIntent.getActivity(
                context, 0, addTaskIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btn_add_task, addTaskPendingIntent);

        // Set up header click to open app
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                context, 1, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_header, openAppPendingIntent);

        // Set up task item click template
        Intent taskClickIntent = new Intent(context, TaskWidgetProvider.class);
        taskClickIntent.setAction(ACTION_TASK_CLICK);
        taskClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent taskClickPendingIntent = PendingIntent.getBroadcast(
                context, 2, taskClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        views.setPendingIntentTemplate(R.id.list_tasks, taskClickPendingIntent);

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_tasks);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        
        if (ACTION_REFRESH.equals(action)) {
            // Refresh all widgets
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName widgetComponent = new ComponentName(context, TaskWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);
            
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_tasks);
            }
        } else if (ACTION_TASK_CLICK.equals(action)) {
            // Handle task click
            long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
            if (taskId != -1) {
                Intent openTaskIntent = new Intent(context, MainActivity.class);
                openTaskIntent.setAction("ACTION_VIEW_TASK");
                openTaskIntent.putExtra(EXTRA_TASK_ID, taskId);
                openTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(openTaskIntent);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Widget added for the first time
    }

    @Override
    public void onDisabled(Context context) {
        // Last widget removed
    }

    /**
     * Trigger a widget update from anywhere in the app
     */
    public static void refreshWidgets(Context context) {
        Intent intent = new Intent(context, TaskWidgetProvider.class);
        intent.setAction(ACTION_REFRESH);
        context.sendBroadcast(intent);
    }
}
