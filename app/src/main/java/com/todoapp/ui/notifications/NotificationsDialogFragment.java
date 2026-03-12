package com.todoapp.ui.notifications;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.todoapp.R;
import com.todoapp.data.model.Task;
import com.todoapp.data.repository.TaskRepository;
import com.todoapp.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * NotificationsDialogFragment - Dialog for displaying upcoming task reminders.
 * 
 * Features:
 * - Display list of tasks with upcoming reminders
 * - Shows reminder time for each task
 * - Empty state when no reminders are scheduled
 */
@AndroidEntryPoint
public class NotificationsDialogFragment extends DialogFragment {

    @Inject
    TaskRepository taskRepository;

    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private NotificationAdapter adapter;

    public static NotificationsDialogFragment newInstance() {
        return new NotificationsDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_notifications, null);
        
        initViews(view);
        setupRecyclerView();
        
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.upcoming_reminders)
                .setView(view)
                .setPositiveButton(R.string.close, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadNotifications();
    }

    private void initViews(View view) {
        rvNotifications = view.findViewById(R.id.rv_notifications);
        layoutEmpty = view.findViewById(R.id.layout_empty);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        // Show all tasks that have reminders enabled
        taskRepository.getTasksWithRemindersEnabled().observe(this, tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                adapter.setTasks(tasks);
                rvNotifications.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            } else {
                rvNotifications.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Adapter for displaying notification items
     */
    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        
        private List<Task> tasks = new ArrayList<>();

        public void setTasks(List<Task> tasks) {
            this.tasks = tasks;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Task task = tasks.get(position);
            holder.bind(task);
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvTaskTitle;
            private final TextView tvReminderTime;
            private final View priorityIndicator;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
                tvReminderTime = itemView.findViewById(R.id.tv_reminder_time);
                priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            }

            void bind(Task task) {
                tvTaskTitle.setText(task.getTitle());
                
                // Format reminder time
                if (task.getReminderTime() != null) {
                    String formattedTime = DateUtils.formatDateTime(task.getReminderTime());
                    tvReminderTime.setText(formattedTime);
                } else {
                    tvReminderTime.setText("");
                }
                
                // Set priority indicator color
                int priority = task.getPriority();
                if (priority > 0) {
                    priorityIndicator.setVisibility(View.VISIBLE);
                    int colorRes;
                    switch (priority) {
                        case 3: // High
                            colorRes = R.color.red_500;
                            break;
                        case 2: // Medium
                            colorRes = R.color.orange_500;
                            break;
                        case 1: // Low
                            colorRes = R.color.blue_500;
                            break;
                        default:
                            colorRes = R.color.grey_500;
                            break;
                    }
                    priorityIndicator.setBackgroundTintList(
                            itemView.getContext().getColorStateList(colorRes));
                } else {
                    priorityIndicator.setVisibility(View.GONE);
                }
            }
        }
    }
}
