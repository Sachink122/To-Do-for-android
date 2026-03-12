package com.todoapp.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Service for providing data to the task widget.
 */
public class TaskWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TaskWidgetFactory(getApplicationContext(), intent);
    }
}
