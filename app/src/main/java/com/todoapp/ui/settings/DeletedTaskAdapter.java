package com.todoapp.ui.settings;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.todoapp.R;
import com.todoapp.data.model.Task;
import com.todoapp.databinding.ItemDeletedTaskBinding;
import com.todoapp.util.DateUtils;

/**
 * Adapter for displaying deleted tasks in the trash.
 */
public class DeletedTaskAdapter extends ListAdapter<Task, DeletedTaskAdapter.DeletedTaskViewHolder> {

    private final DeletedTaskClickListener listener;

    public interface DeletedTaskClickListener {
        void onRestoreClick(Task task);
        void onDeletePermanentlyClick(Task task);
    }

    public DeletedTaskAdapter(DeletedTaskClickListener listener) {
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
                    oldItem.getDeletedAt() != null && newItem.getDeletedAt() != null &&
                    oldItem.getDeletedAt().equals(newItem.getDeletedAt());
        }
    };

    @NonNull
    @Override
    public DeletedTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeletedTaskBinding binding = ItemDeletedTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DeletedTaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeletedTaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task, listener);
    }

    static class DeletedTaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemDeletedTaskBinding binding;

        public DeletedTaskViewHolder(@NonNull ItemDeletedTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Task task, DeletedTaskClickListener listener) {
            binding.textTaskTitle.setText(task.getTitle());
            
            // Show deleted date
            if (task.getDeletedAt() != null) {
                String deletedDate = DateUtils.formatDate(task.getDeletedAt());
                binding.textDeletedDate.setText(
                        binding.getRoot().getContext().getString(R.string.deleted_on, deletedDate));
            } else {
                binding.textDeletedDate.setText("");
            }

            // Set priority indicator color
            int priorityColor;
            switch (task.getPriority()) {
                case 3:
                    priorityColor = binding.getRoot().getContext().getColor(R.color.priority_high);
                    break;
                case 2:
                    priorityColor = binding.getRoot().getContext().getColor(R.color.priority_medium);
                    break;
                default:
                    priorityColor = binding.getRoot().getContext().getColor(R.color.priority_low);
                    break;
            }
            binding.viewPriorityIndicator.setBackgroundColor(priorityColor);

            // Button clicks
            binding.buttonRestore.setOnClickListener(v -> listener.onRestoreClick(task));
            binding.buttonDeletePermanently.setOnClickListener(v -> listener.onDeletePermanentlyClick(task));
        }
    }
}
