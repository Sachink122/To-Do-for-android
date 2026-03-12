package com.todoapp.ui.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.todoapp.data.model.Subtask;
import com.todoapp.databinding.ItemSubtaskEditBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for editing subtasks in add/edit task screen.
 */
public class EditSubtaskAdapter extends RecyclerView.Adapter<EditSubtaskAdapter.EditSubtaskViewHolder> {

    private List<Subtask> subtasks = new ArrayList<>();
    private final EditSubtaskListener listener;

    public interface EditSubtaskListener {
        void onSubtaskTitleChanged(int position, String newTitle);
        void onSubtaskCheckChanged(int position, boolean isChecked);
        void onSubtaskDelete(int position);
    }

    public EditSubtaskAdapter(EditSubtaskListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Subtask> newSubtasks) {
        this.subtasks = newSubtasks != null ? new ArrayList<>(newSubtasks) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EditSubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubtaskEditBinding binding = ItemSubtaskEditBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EditSubtaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EditSubtaskViewHolder holder, int position) {
        Subtask subtask = subtasks.get(position);
        holder.bind(subtask, position);
    }

    @Override
    public int getItemCount() {
        return subtasks.size();
    }

    class EditSubtaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubtaskEditBinding binding;
        private TextWatcher textWatcher;

        public EditSubtaskViewHolder(@NonNull ItemSubtaskEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Subtask subtask, int position) {
            // Remove old text watcher to avoid multiple callbacks
            if (textWatcher != null) {
                binding.editSubtaskTitle.removeTextChangedListener(textWatcher);
            }

            // Set values
            binding.editSubtaskTitle.setText(subtask.getTitle());
            binding.checkboxSubtask.setOnCheckedChangeListener(null);
            binding.checkboxSubtask.setChecked(subtask.isCompleted());

            // Checkbox listener
            binding.checkboxSubtask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onSubtaskCheckChanged(position, isChecked);
                }
            });

            // Title change listener
            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (listener != null) {
                        listener.onSubtaskTitleChanged(position, s.toString());
                    }
                }
            };
            binding.editSubtaskTitle.addTextChangedListener(textWatcher);

            // Delete button
            binding.btnDeleteSubtask.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSubtaskDelete(position);
                }
            });
        }
    }
}
