package com.todoapp.ui.tasks;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.todoapp.R;
import com.todoapp.data.model.Subtask;
import com.todoapp.data.model.Task;
import com.todoapp.databinding.FragmentTaskDetailBinding;
import com.todoapp.ui.adapters.SubtaskAdapter;
import com.todoapp.ui.viewmodel.TaskDetailViewModel;
import com.todoapp.util.DateUtils;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment displaying task details with subtasks and actions.
 */
@AndroidEntryPoint
public class TaskDetailFragment extends Fragment implements SubtaskAdapter.SubtaskClickListener {

    private FragmentTaskDetailBinding binding;
    private TaskDetailViewModel viewModel;
    private SubtaskAdapter subtaskAdapter;
    private long taskId = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TaskDetailViewModel.class);

        if (getArguments() != null) {
            taskId = getArguments().getLong("taskId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        setupMenu();
        loadTask();
        observeViewModel();
    }

    private void setupViews() {
        // Subtasks RecyclerView
        subtaskAdapter = new SubtaskAdapter(this);
        binding.recyclerSubtasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSubtasks.setAdapter(subtaskAdapter);

        // Completion checkbox
        binding.checkboxComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleTaskCompleted();
        });

        // Add subtask
        binding.btnAddSubtask.setOnClickListener(v -> addSubtask());
        binding.editNewSubtask.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addSubtask();
                return true;
            }
            return false;
        });

        // Edit button
        binding.btnEdit.setOnClickListener(v -> navigateToEdit());

        // Delete button
        binding.btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_task_detail, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.action_edit) {
                    navigateToEdit();
                    return true;
                } else if (id == R.id.action_delete) {
                    showDeleteConfirmation();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void loadTask() {
        if (taskId != -1) {
            viewModel.loadTask(taskId);
        }
    }

    private void observeViewModel() {
        viewModel.getTask().observe(getViewLifecycleOwner(), this::displayTask);

        viewModel.getSubtasks().observe(getViewLifecycleOwner(), this::displaySubtasks);

        viewModel.getTaskDeleted().observe(getViewLifecycleOwner(), deleted -> {
            if (deleted != null && deleted) {
                navigateBack();
            }
        });
    }

    private void displayTask(Task task) {
        if (task == null) return;

        // Title and completion
        binding.textTitle.setText(task.getTitle());
        binding.checkboxComplete.setOnCheckedChangeListener(null);
        binding.checkboxComplete.setChecked(task.isCompleted());
        binding.checkboxComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleTaskCompleted();
        });

        // Description
        if (!TextUtils.isEmpty(task.getDescription())) {
            binding.cardDescription.setVisibility(View.VISIBLE);
            binding.textDescription.setText(task.getDescription());
        } else {
            binding.cardDescription.setVisibility(View.GONE);
        }

        // Priority
        if (task.getPriority() != Task.Priority.NONE) {
            binding.chipPriority.setVisibility(View.VISIBLE);
            binding.chipPriority.setText(getPriorityText(task.getPriority()));
        } else {
            binding.chipPriority.setVisibility(View.GONE);
        }

        // Due date
        if (task.getDueDate() != null) {
            binding.cardDueDate.setVisibility(View.VISIBLE);
            binding.textDueDate.setText(DateUtils.formatDateTime(task.getDueDate()));

            // Show overdue chip
            if (DateUtils.isOverdue(task.getDueDate()) && !task.isCompleted()) {
                binding.chipOverdue.setVisibility(View.VISIBLE);
            } else {
                binding.chipOverdue.setVisibility(View.GONE);
            }
        } else {
            binding.cardDueDate.setVisibility(View.GONE);
        }

        // Reminder
        if (task.isHasReminder() && task.getReminderTime() != null) {
            binding.cardReminder.setVisibility(View.VISIBLE);
            binding.textReminder.setText(DateUtils.formatDateTime(task.getReminderTime()));
        } else {
            binding.cardReminder.setVisibility(View.GONE);
        }

        // Repeat
        if (task.isRepeating() && task.getRepeatInterval() != Task.RepeatInterval.NONE) {
            binding.cardRepeat.setVisibility(View.VISIBLE);
            binding.textRepeat.setText(getRepeatText(task.getRepeatInterval()));
        } else {
            binding.cardRepeat.setVisibility(View.GONE);
        }

        // Notes
        if (!TextUtils.isEmpty(task.getNotes())) {
            binding.cardNotes.setVisibility(View.VISIBLE);
            binding.textNotes.setText(task.getNotes());
        } else {
            binding.cardNotes.setVisibility(View.GONE);
        }

        // Timestamps
        binding.textCreatedAt.setText(getString(R.string.created_at, 
                DateUtils.formatDateTime(task.getCreatedAt())));
        binding.textUpdatedAt.setText(getString(R.string.updated_at, 
                DateUtils.formatDateTime(task.getUpdatedAt())));
    }

    private void displaySubtasks(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            binding.progressSubtasks.setVisibility(View.GONE);
            binding.textSubtaskProgress.setVisibility(View.GONE);
            subtaskAdapter.submitList(null);
        } else {
            binding.progressSubtasks.setVisibility(View.VISIBLE);
            binding.textSubtaskProgress.setVisibility(View.VISIBLE);

            int completed = 0;
            for (Subtask subtask : subtasks) {
                if (subtask.isCompleted()) completed++;
            }

            binding.progressSubtasks.setMax(subtasks.size());
            binding.progressSubtasks.setProgress(completed);
            binding.textSubtaskProgress.setText(completed + "/" + subtasks.size());
            // Create a new ArrayList to ensure DiffUtil detects changes
            subtaskAdapter.submitList(new java.util.ArrayList<>(subtasks));
        }
    }

    private String getPriorityText(int priority) {
        switch (priority) {
            case Task.Priority.HIGH:
                return getString(R.string.priority_high);
            case Task.Priority.MEDIUM:
                return getString(R.string.priority_medium);
            case Task.Priority.LOW:
                return getString(R.string.priority_low);
            default:
                return getString(R.string.priority_none);
        }
    }

    private String getRepeatText(int interval) {
        switch (interval) {
            case Task.RepeatInterval.DAILY:
                return getString(R.string.repeat_daily);
            case Task.RepeatInterval.WEEKLY:
                return getString(R.string.repeat_weekly);
            case Task.RepeatInterval.MONTHLY:
                return getString(R.string.repeat_monthly);
            case Task.RepeatInterval.YEARLY:
                return getString(R.string.repeat_yearly);
            default:
                return getString(R.string.repeat_none);
        }
    }

    private void addSubtask() {
        String title = binding.editNewSubtask.getText().toString().trim();
        android.util.Log.d("TaskDetailFragment", "addSubtask called with title: '" + title + "'");
        if (!TextUtils.isEmpty(title)) {
            viewModel.addSubtask(title);
            binding.editNewSubtask.setText("");
            android.util.Log.d("TaskDetailFragment", "Subtask sent to ViewModel");
        } else {
            android.util.Log.d("TaskDetailFragment", "Empty title, not adding subtask");
        }
    }

    private void navigateToEdit() {
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putLong("taskId", taskId);
        navController.navigate(R.id.action_taskDetail_to_addEditTask, args);
    }

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_task)
                .setMessage(R.string.delete_task_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteTask();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void navigateBack() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigateUp();
    }

    @Override
    public void onSubtaskCheckChanged(Subtask subtask, boolean isCompleted) {
        viewModel.toggleSubtaskCompleted(subtask);
    }

    @Override
    public void onSubtaskDelete(Subtask subtask) {
        viewModel.deleteSubtask(subtask);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
