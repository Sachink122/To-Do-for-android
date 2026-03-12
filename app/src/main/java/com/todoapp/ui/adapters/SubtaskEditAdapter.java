package com.todoapp.ui.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.todoapp.data.model.Subtask;
import com.todoapp.databinding.ItemSubtaskEditBinding;

/**
 * RecyclerView adapter for editing subtasks in add/edit task view.
 * Supports title editing, completion toggle, deletion, and drag-to-reorder.
 */
public class SubtaskEditAdapter extends ListAdapter<Subtask, SubtaskEditAdapter.SubtaskEditViewHolder> {

    private final SubtaskEditListener listener;

    public interface SubtaskEditListener {
        void onSubtaskTitleChanged(Subtask subtask, String newTitle);
        void onSubtaskCheckedChanged(Subtask subtask, boolean isChecked);
        void onSubtaskDelete(Subtask subtask);
        void onSubtaskMove(int fromPosition, int toPosition);
    }

    public SubtaskEditAdapter(SubtaskEditListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Subtask> DIFF_CALLBACK = new DiffUtil.ItemCallback<Subtask>() {
        @Override
        public boolean areItemsTheSame(@NonNull Subtask oldItem, @NonNull Subtask newItem) {
            // Use position for temporary subtasks (id == 0)
            if (oldItem.getId() == 0 && newItem.getId() == 0) {
                return oldItem.getPosition() == newItem.getPosition();
            }
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Subtask oldItem, @NonNull Subtask newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.isCompleted() == newItem.isCompleted() &&
                   oldItem.getPosition() == newItem.getPosition();
        }
    };

    @NonNull
    @Override
    public SubtaskEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubtaskEditBinding binding = ItemSubtaskEditBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SubtaskEditViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskEditViewHolder holder, int position) {
        Subtask subtask = getItem(position);
        holder.bind(subtask);
    }

    class SubtaskEditViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubtaskEditBinding binding;
        private TextWatcher textWatcher;

        public SubtaskEditViewHolder(ItemSubtaskEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Subtask subtask) {
            // Remove old text watcher
            if (textWatcher != null) {
                binding.editSubtaskTitle.removeTextChangedListener(textWatcher);
            }

            // Set title
            binding.editSubtaskTitle.setText(subtask.getTitle());

            // Create new text watcher
            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (listener != null) {
                        listener.onSubtaskTitleChanged(subtask, s.toString());
                    }
                }
            };
            binding.editSubtaskTitle.addTextChangedListener(textWatcher);

            // Checkbox
            binding.checkboxSubtask.setOnCheckedChangeListener(null);
            binding.checkboxSubtask.setChecked(subtask.isCompleted());
            binding.checkboxSubtask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onSubtaskCheckedChanged(subtask, isChecked);
                }
            });

            // Delete button
            binding.btnDeleteSubtask.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSubtaskDelete(subtask);
                }
            });

            // Drag handle - would be used with ItemTouchHelper for drag and drop
            // The actual drag implementation would be in the fragment
        }
    }

    /**
     * Called when an item is moved during drag and drop
     */
    public void onItemMove(int fromPosition, int toPosition) {
        if (listener != null) {
            listener.onSubtaskMove(fromPosition, toPosition);
        }
    }
}
