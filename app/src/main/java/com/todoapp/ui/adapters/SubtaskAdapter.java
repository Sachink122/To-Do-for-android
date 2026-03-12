package com.todoapp.ui.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.todoapp.data.model.Subtask;
import com.todoapp.databinding.ItemSubtaskBinding;

/**
 * RecyclerView adapter for displaying subtasks in task detail view.
 */
public class SubtaskAdapter extends ListAdapter<Subtask, SubtaskAdapter.SubtaskViewHolder> {

    private final SubtaskClickListener listener;

    public interface SubtaskClickListener {
        void onSubtaskCheckChanged(Subtask subtask, boolean isCompleted);
        void onSubtaskDelete(Subtask subtask);
    }

    public SubtaskAdapter(SubtaskClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Subtask> DIFF_CALLBACK = new DiffUtil.ItemCallback<Subtask>() {
        @Override
        public boolean areItemsTheSame(@NonNull Subtask oldItem, @NonNull Subtask newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Subtask oldItem, @NonNull Subtask newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.isCompleted() == newItem.isCompleted();
        }
    };

    @NonNull
    @Override
    public SubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubtaskBinding binding = ItemSubtaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SubtaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskViewHolder holder, int position) {
        Subtask subtask = getItem(position);
        holder.bind(subtask);
    }

    class SubtaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubtaskBinding binding;

        public SubtaskViewHolder(ItemSubtaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Subtask subtask) {
            binding.textSubtaskTitle.setText(subtask.getTitle());
            
            // Strikethrough if completed
            if (subtask.isCompleted()) {
                binding.textSubtaskTitle.setPaintFlags(
                        binding.textSubtaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.textSubtaskTitle.setAlpha(0.6f);
            } else {
                binding.textSubtaskTitle.setPaintFlags(
                        binding.textSubtaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.textSubtaskTitle.setAlpha(1.0f);
            }

            // Checkbox
            binding.checkboxSubtask.setOnCheckedChangeListener(null);
            binding.checkboxSubtask.setChecked(subtask.isCompleted());
            binding.checkboxSubtask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onSubtaskCheckChanged(subtask, isChecked);
                }
            });
        }
    }
}
