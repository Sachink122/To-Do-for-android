package com.todoapp.ui.tasks;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.todoapp.R;
import com.todoapp.data.model.Category;
import com.todoapp.data.model.Subtask;
import com.todoapp.data.model.Task;
import com.todoapp.databinding.FragmentAddEditTaskBinding;
import com.todoapp.ui.adapters.EditSubtaskAdapter;
import com.todoapp.ui.viewmodel.AddEditTaskViewModel;
import com.todoapp.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment for adding or editing a task.
 */
@AndroidEntryPoint
public class AddEditTaskFragment extends Fragment implements EditSubtaskAdapter.EditSubtaskListener {

    private FragmentAddEditTaskBinding binding;
    private AddEditTaskViewModel viewModel;
    private EditSubtaskAdapter subtaskAdapter;
    private long taskId = -1;
    private Calendar selectedDueDate;
    private Calendar selectedDueTime;
    private Calendar selectedReminderTime;
    private boolean hasDueDate = false;
    private List<Category> categories = new ArrayList<>();
    private Category selectedCategory = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddEditTaskViewModel.class);

        // Get task ID from arguments if editing
        if (getArguments() != null) {
            taskId = getArguments().getLong("taskId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddEditTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        setupListeners();

        if (taskId != -1) {
            loadTask();
        }

        observeViewModel();
    }

    private void setupViews() {
        selectedDueDate = Calendar.getInstance();
        selectedDueTime = Calendar.getInstance();
        selectedReminderTime = Calendar.getInstance();

        // Setup subtasks RecyclerView
        subtaskAdapter = new EditSubtaskAdapter(this);
        binding.recyclerSubtasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSubtasks.setAdapter(subtaskAdapter);
    }

    private void setupListeners() {
        // Due date picker
        binding.btnDueDate.setOnClickListener(v -> showDatePicker());
        binding.btnDueTime.setOnClickListener(v -> showTimePicker());
        binding.btnClearDueDate.setOnClickListener(v -> clearDueDate());

        // Reminder
        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.btnReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        binding.btnReminderTime.setOnClickListener(v -> showReminderPicker());

        // Repeat toggle
        binding.switchRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.chipGroupRepeat.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Category selection
        binding.btnSelectCategory.setOnClickListener(v -> showCategoryDialog());

        // Priority chips
        binding.chipPriorityNone.setOnClickListener(v -> viewModel.setPriority(Task.Priority.NONE));
        binding.chipPriorityLow.setOnClickListener(v -> viewModel.setPriority(Task.Priority.LOW));
        binding.chipPriorityMedium.setOnClickListener(v -> viewModel.setPriority(Task.Priority.MEDIUM));
        binding.chipPriorityHigh.setOnClickListener(v -> viewModel.setPriority(Task.Priority.HIGH));

        // Repeat chips
        binding.chipRepeatDaily.setOnClickListener(v -> viewModel.setRepeatInterval(Task.RepeatInterval.DAILY));
        binding.chipRepeatWeekly.setOnClickListener(v -> viewModel.setRepeatInterval(Task.RepeatInterval.WEEKLY));
        binding.chipRepeatMonthly.setOnClickListener(v -> viewModel.setRepeatInterval(Task.RepeatInterval.MONTHLY));

        // Subtask add button
        binding.btnAddSubtask.setOnClickListener(v -> addSubtask());
        binding.editNewSubtask.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                addSubtask();
                return true;
            }
            return false;
        });

        // Save button
        binding.fabSave.setOnClickListener(v -> saveTask());
    }

    private void addSubtask() {
        String title = binding.editNewSubtask.getText().toString().trim();
        if (!title.isEmpty()) {
            viewModel.addSubtask(title);
            binding.editNewSubtask.setText("");
        }
    }

    private void loadTask() {
        viewModel.loadTask(taskId);
    }

    private void observeViewModel() {
        viewModel.getCurrentTask().observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                populateForm(task);
            }
        });

        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), R.string.task_saved, Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe categories
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categoryList -> {
            if (categoryList != null) {
                categories.clear();
                categories.addAll(categoryList);
            }
        });

        // Observe subtasks
        viewModel.getSubtasks().observe(getViewLifecycleOwner(), subtasks -> {
            subtaskAdapter.submitList(subtasks);
        });
    }

    // EditSubtaskListener implementation
    @Override
    public void onSubtaskTitleChanged(int position, String newTitle) {
        viewModel.updateSubtaskTitle(position, newTitle);
    }

    @Override
    public void onSubtaskCheckChanged(int position, boolean isChecked) {
        viewModel.toggleSubtaskCompleted(position);
    }

    @Override
    public void onSubtaskDelete(int position) {
        viewModel.removeSubtask(position);
    }

    private void populateForm(Task task) {
        binding.editTitle.setText(task.getTitle());
        binding.editDescription.setText(task.getDescription());
        
        // Notes field if exists
        if (binding.editNotes != null) {
            binding.editNotes.setText(task.getNotes());
        }

        // Due date
        if (task.getDueDate() != null) {
            hasDueDate = true;
            selectedDueDate.setTimeInMillis(task.getDueDate());
            updateDueDateDisplay();
        }

        // Due time
        if (task.getDueTime() != null) {
            selectedDueTime.setTimeInMillis(task.getDueTime());
            updateDueTimeDisplay();
        }

        // Priority
        setPriorityChip(task.getPriority());

        // Category
        if (task.getCategoryId() != null && task.getCategoryId() > 0) {
            for (Category category : categories) {
                if (category.getId() == task.getCategoryId()) {
                    selectedCategory = category;
                    binding.btnSelectCategory.setText(category.getName());
                    break;
                }
            }
        }

        // Reminder
        binding.switchReminder.setChecked(task.isHasReminder());
        if (task.getReminderTime() != null) {
            selectedReminderTime.setTimeInMillis(task.getReminderTime());
            updateReminderDisplay();
        }

        // Repeat
        binding.switchRepeat.setChecked(task.isRepeating());
        if (task.isRepeating()) {
            binding.chipGroupRepeat.setVisibility(View.VISIBLE);
            setRepeatChip(task.getRepeatInterval());
        }
    }

    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDueDate.set(Calendar.YEAR, year);
                    selectedDueDate.set(Calendar.MONTH, month);
                    selectedDueDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    hasDueDate = true;
                    updateDueDateDisplay();
                },
                selectedDueDate.get(Calendar.YEAR),
                selectedDueDate.get(Calendar.MONTH),
                selectedDueDate.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void showTimePicker() {
        TimePickerDialog picker = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDueTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDueTime.set(Calendar.MINUTE, minute);
                    updateDueTimeDisplay();
                },
                selectedDueTime.get(Calendar.HOUR_OF_DAY),
                selectedDueTime.get(Calendar.MINUTE),
                false
        );
        picker.show();
    }

    private void updateDueDateDisplay() {
        binding.btnDueDate.setText(DateUtils.formatDate(selectedDueDate.getTimeInMillis()));
        binding.btnClearDueDate.setVisibility(View.VISIBLE);
    }

    private void updateDueTimeDisplay() {
        binding.btnDueTime.setText(DateUtils.formatTime(selectedDueTime.getTimeInMillis()));
    }

    private void clearDueDate() {
        hasDueDate = false;
        selectedDueDate = Calendar.getInstance();
        binding.btnDueDate.setText(R.string.select_date);
        binding.btnDueTime.setText(R.string.select_time);
        binding.btnClearDueDate.setVisibility(View.GONE);
    }

    private void showReminderPicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedReminderTime.set(Calendar.YEAR, year);
                    selectedReminderTime.set(Calendar.MONTH, month);
                    selectedReminderTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(
                            requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                selectedReminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedReminderTime.set(Calendar.MINUTE, minute);
                                updateReminderDisplay();
                            },
                            selectedReminderTime.get(Calendar.HOUR_OF_DAY),
                            selectedReminderTime.get(Calendar.MINUTE),
                            false
                    );
                    timePicker.show();
                },
                selectedReminderTime.get(Calendar.YEAR),
                selectedReminderTime.get(Calendar.MONTH),
                selectedReminderTime.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void updateReminderDisplay() {
        binding.btnReminderTime.setText(DateUtils.formatDateTime(selectedReminderTime.getTimeInMillis()));
    }

    private void showCategoryDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_categories, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryNames = new String[categories.size() + 1];
        categoryNames[0] = getString(R.string.no_category);
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i + 1] = categories.get(i).getName();
        }

        int selectedIndex = 0;
        if (selectedCategory != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == selectedCategory.getId()) {
                    selectedIndex = i + 1;
                    break;
                }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_category)
                .setSingleChoiceItems(categoryNames, selectedIndex, (dialog, which) -> {
                    if (which == 0) {
                        selectedCategory = null;
                        viewModel.setCategoryId(null);
                        binding.btnSelectCategory.setText(R.string.select_category);
                    } else {
                        selectedCategory = categories.get(which - 1);
                        viewModel.setCategoryId(selectedCategory.getId());
                        binding.btnSelectCategory.setText(selectedCategory.getName());
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setPriorityChip(int priority) {
        binding.chipPriorityNone.setChecked(priority == Task.Priority.NONE);
        binding.chipPriorityLow.setChecked(priority == Task.Priority.LOW);
        binding.chipPriorityMedium.setChecked(priority == Task.Priority.MEDIUM);
        binding.chipPriorityHigh.setChecked(priority == Task.Priority.HIGH);
    }

    private void setRepeatChip(int interval) {
        binding.chipRepeatDaily.setChecked(interval == Task.RepeatInterval.DAILY);
        binding.chipRepeatWeekly.setChecked(interval == Task.RepeatInterval.WEEKLY);
        binding.chipRepeatMonthly.setChecked(interval == Task.RepeatInterval.MONTHLY);
    }

    private void saveTask() {
        String title = binding.editTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.layoutTitle.setError(getString(R.string.error_empty_title));
            return;
        }

        // Set all form values to ViewModel
        viewModel.setTitle(title);
        viewModel.setDescription(binding.editDescription.getText().toString().trim());
        
        if (binding.editNotes != null) {
            viewModel.setNotes(binding.editNotes.getText().toString().trim());
        }

        // Due date
        if (hasDueDate) {
            viewModel.setDueDate(selectedDueDate.getTimeInMillis());
            viewModel.setDueTime(selectedDueTime.getTimeInMillis());
        } else {
            viewModel.setDueDate(null);
            viewModel.setDueTime(null);
        }

        // Reminder
        viewModel.setHasReminder(binding.switchReminder.isChecked());
        if (binding.switchReminder.isChecked()) {
            viewModel.setReminderTime(selectedReminderTime.getTimeInMillis());
        }

        // Repeat
        viewModel.setIsRepeating(binding.switchRepeat.isChecked());

        // Save via ViewModel
        viewModel.saveTask();
    }

    private void navigateBack() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
