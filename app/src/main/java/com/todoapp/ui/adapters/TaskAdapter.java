package com.todoapp.ui.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.todoapp.R;
import com.todoapp.data.model.Task;
import com.todoapp.databinding.ItemTaskBinding;
import com.todoapp.util.DateUtils;

import java.util.List;

/**
 * RecyclerView adapter for displaying tasks with swipe actions and click listeners.
 */
public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    private final TaskClickListener listener;

    public interface TaskClickListener {
        void onTaskClick(Task task);
        void onTaskCheckChanged(Task task, boolean isCompleted);
        void onStarClick(Task task);
    }

    public TaskAdapter(TaskClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.isCompleted() == newItem.isCompleted() &&
                   oldItem.getPriority() == newItem.getPriority() &&
                   oldItem.isImportant() == newItem.isImportant() &&
                   java.util.Objects.equals(oldItem.getDueDate(), newItem.getDueDate()) &&
                   java.util.Objects.equals(oldItem.getDescription(), newItem.getDescription());
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task);
    }

    @Nullable
    public Task getTaskAt(int position) {
        if (position >= 0 && position < getItemCount()) {
            return getItem(position);
        }
        return null;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemTaskBinding binding;

        public TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Task task) {
            // Title with strikethrough if completed
            binding.textTitle.setText(task.getTitle());
            if (task.isCompleted()) {
                binding.textTitle.setPaintFlags(binding.textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.textTitle.setAlpha(0.6f);
            } else {
                binding.textTitle.setPaintFlags(binding.textTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.textTitle.setAlpha(1.0f);
            }

            // Description
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                binding.textDescription.setVisibility(View.VISIBLE);
                binding.textDescription.setText(task.getDescription());
            } else {
                binding.textDescription.setVisibility(View.GONE);
            }

            // Checkbox
            binding.checkboxComplete.setOnCheckedChangeListener(null);
            binding.checkboxComplete.setChecked(task.isCompleted());
            binding.checkboxComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskCheckChanged(task, isChecked);
                }
            });

            // Priority indicator
            if (task.getPriority() != Task.Priority.NONE) {
                binding.priorityIndicator.setVisibility(View.VISIBLE);
                int color = getPriorityColor(task.getPriority());
                binding.priorityIndicator.setBackgroundColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), color));
            } else {
                binding.priorityIndicator.setVisibility(View.GONE);
            }

            // Due date
            if (task.getDueDate() != null) {
                binding.layoutDueDate.setVisibility(View.VISIBLE);
                String dateText = DateUtils.formatRelativeDate(task.getDueDate());
                binding.textDueDate.setText(dateText);
                
                // Highlight overdue dates
                if (DateUtils.isOverdue(task.getDueDate()) && !task.isCompleted()) {
                    binding.textDueDate.setTextColor(
                            ContextCompat.getColor(binding.getRoot().getContext(), R.color.red_500));
                } else {
                    binding.textDueDate.setTextColor(
                            ContextCompat.getColor(binding.getRoot().getContext(), R.color.grey_600));
                }
            } else {
                binding.layoutDueDate.setVisibility(View.GONE);
            }

            // Reminder icon
            binding.iconReminder.setVisibility(task.isHasReminder() ? View.VISIBLE : View.GONE);

            // Repeat icon
            binding.iconRepeat.setVisibility(task.isRepeating() ? View.VISIBLE : View.GONE);

            // Star/Important button
            binding.btnStar.setImageResource(
                    task.isImportant() ? R.drawable.ic_star : R.drawable.ic_star_outline);
            binding.btnStar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStarClick(task);
                }
            });

            // Card click
            binding.cardTask.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            // Hide subtask layout for now (would need TaskWithSubtasks)
            binding.layoutSubtasks.setVisibility(View.GONE);
            
            // Category chip - would need category info
            binding.chipCategory.setVisibility(View.GONE);
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
    }
}
