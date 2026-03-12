package com.todoapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.todoapp.R;
import com.todoapp.data.database.TodoDatabase;
import com.todoapp.data.model.Task;
import com.todoapp.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating remote views for widget items.
 */
public class TaskWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private final int appWidgetId;
    private List<Task> tasks;

    public TaskWidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        this.tasks = new ArrayList<>();
    }

    @Override
    public void onCreate() {
        // Initial data loading happens in onDataSetChanged
    }

    @Override
    public void onDataSetChanged() {
        // Load incomplete tasks from database
        // Note: This runs on a binder thread, so we can do synchronous database access
        try {
            TodoDatabase database = TodoDatabase.getInstance(context);
            tasks = database.taskDao().getIncompleteTasks();
            
            // Limit to 10 tasks for widget
            if (tasks.size() > 10) {
                tasks = tasks.subList(0, 10);
            }
        } catch (Exception e) {
            tasks = new ArrayList<>();
        }
    }

    @Override
    public void onDestroy() {
        tasks.clear();
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position < 0 || position >= tasks.size()) {
            return null;
        }

        Task task = tasks.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task_item);

        // Set task title
        views.setTextViewText(R.id.text_title, task.getTitle());

        // Priority indicator
        int priorityColor = getPriorityColor(task.getPriority());
        views.setInt(R.id.priority_indicator, "setBackgroundColor", 
                context.getResources().getColor(priorityColor, context.getTheme()));

        // Due date
        if (task.getDueDate() != null) {
            views.setViewVisibility(R.id.text_due_date, View.VISIBLE);
            views.setTextViewText(R.id.text_due_date, DateUtils.formatRelativeDate(task.getDueDate()));
            
            // Highlight overdue
            if (DateUtils.isOverdue(task.getDueDate())) {
                views.setTextColor(R.id.text_due_date, 
                        context.getResources().getColor(R.color.red_500, context.getTheme()));
            }
        } else {
            views.setViewVisibility(R.id.text_due_date, View.GONE);
        }

        // Set fill-in intent for click handling
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(TaskWidgetProvider.EXTRA_TASK_ID, task.getId());
        views.setOnClickFillInIntent(R.id.text_title, fillInIntent);

        return views;
    }

    private int getPriorityColor(int priority) {
        switch (priority) {
            case Task.Priority.HIGH:
                return R.color.priority_high;
            case Task.Priority.MEDIUM:
                return R.color.priority_medium;
            case Task.Priority.LOW:
                return R.color.priority_low;
            default:
                return R.color.priority_none;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        // Return null to use default loading view
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= tasks.size()) {
            return -1;
        }
        return tasks.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
