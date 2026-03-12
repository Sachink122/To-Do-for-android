package com.todoapp.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.todoapp.data.model.Category;
import com.todoapp.data.model.Subtask;
import com.todoapp.data.model.Task;
import com.todoapp.data.repository.TaskRepository;
import com.todoapp.notifications.ReminderScheduler;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * AddEditTaskViewModel - ViewModel for creating and editing tasks.
 * 
 * Manages the task form state, validation, and save operations.
 * Supports both creating new tasks and editing existing ones.
 */
@HiltViewModel
public class AddEditTaskViewModel extends ViewModel {

    private final TaskRepository repository;
    private final ReminderScheduler reminderScheduler;

    // Task being edited (null for new task)
    private final MutableLiveData<Task> currentTask = new MutableLiveData<>();
    private final MutableLiveData<Long> taskId = new MutableLiveData<>();
    private boolean isEditMode = false;

    // Form fields
    private final MutableLiveData<String> title = new MutableLiveData<>("");
    private final MutableLiveData<String> description = new MutableLiveData<>("");
    private final MutableLiveData<Long> dueDate = new MutableLiveData<>();
    private final MutableLiveData<Long> dueTime = new MutableLiveData<>();
    private final MutableLiveData<Integer> priority = new MutableLiveData<>(Task.Priority.NONE);
    private final MutableLiveData<Long> categoryId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasReminder = new MutableLiveData<>(false);
    private final MutableLiveData<Long> reminderTime = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRepeating = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> repeatInterval = new MutableLiveData<>(Task.RepeatInterval.NONE);
    private final MutableLiveData<Long> repeatEndDate = new MutableLiveData<>();
    private final MutableLiveData<String> color = new MutableLiveData<>();
    private final MutableLiveData<String> notes = new MutableLiveData<>("");

    // Subtasks
    private final MutableLiveData<List<Subtask>> subtasks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Subtask>> deletedSubtasks = new MutableLiveData<>(new ArrayList<>());

    // Validation errors
    private final MutableLiveData<String> titleError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFormValid = new MutableLiveData<>(false);

    // UI state
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Long> savedTaskId = new MutableLiveData<>();

    @Inject
    public AddEditTaskViewModel(TaskRepository repository, ReminderScheduler reminderScheduler) {
        this.repository = repository;
        this.reminderScheduler = reminderScheduler;
    }

    /**
     * Load an existing task for editing
     */
    public void loadTask(long id) {
        if (id <= 0) {
            isEditMode = false;
            return;
        }

        isEditMode = true;
        taskId.setValue(id);

        repository.getTaskById(id).observeForever(task -> {
            if (task != null) {
                currentTask.setValue(task);
                populateFormFromTask(task);
            }
        });

        // Load subtasks
        repository.getSubtasksByTaskId(id).observeForever(loadedSubtasks -> {
            subtasks.setValue(loadedSubtasks != null ? new ArrayList<>(loadedSubtasks) : new ArrayList<>());
        });
    }

    /**
     * Populate form fields from an existing task
     */
    private void populateFormFromTask(Task task) {
        title.setValue(task.getTitle());
        description.setValue(task.getDescription());
        dueDate.setValue(task.getDueDate());
        dueTime.setValue(task.getDueTime());
        priority.setValue(task.getPriority());
        categoryId.setValue(task.getCategoryId());
        hasReminder.setValue(task.isHasReminder());
        reminderTime.setValue(task.getReminderTime());
        isRepeating.setValue(task.isRepeating());
        repeatInterval.setValue(task.getRepeatInterval());
        repeatEndDate.setValue(task.getRepeatEndDate());
        color.setValue(task.getColor());
        notes.setValue(task.getNotes());
        validateForm();
    }

    /**
     * Validate the form
     */
    public boolean validateForm() {
        boolean valid = true;
        String titleValue = title.getValue();

        // Validate title
        if (titleValue == null || titleValue.trim().isEmpty()) {
            titleError.setValue("Title is required");
            valid = false;
        } else if (titleValue.trim().length() < 2) {
            titleError.setValue("Title must be at least 2 characters");
            valid = false;
        } else {
            titleError.setValue(null);
        }

        isFormValid.setValue(valid);
        return valid;
    }

    /**
     * Save the task (create or update)
     */
    public void saveTask() {
        if (!validateForm()) {
            return;
        }

        isSaving.setValue(true);

        Task task = isEditMode && currentTask.getValue() != null 
            ? currentTask.getValue() 
            : new Task();

        // Set all fields
        task.setTitle(title.getValue() != null ? title.getValue().trim() : "");
        task.setDescription(description.getValue());
        task.setDueDate(dueDate.getValue());
        task.setDueTime(dueTime.getValue());
        task.setPriority(priority.getValue() != null ? priority.getValue() : Task.Priority.NONE);
        task.setCategoryId(categoryId.getValue());
        task.setHasReminder(hasReminder.getValue() != null && hasReminder.getValue());
        task.setReminderTime(reminderTime.getValue());
        task.setRepeating(isRepeating.getValue() != null && isRepeating.getValue());
        task.setRepeatInterval(repeatInterval.getValue() != null ? repeatInterval.getValue() : Task.RepeatInterval.NONE);
        task.setRepeatEndDate(repeatEndDate.getValue());
        task.setColor(color.getValue());
        task.setNotes(notes.getValue());

        List<Subtask> subtaskList = subtasks.getValue();
        List<Subtask> toDelete = deletedSubtasks.getValue();

        if (isEditMode) {
            // Update existing task
            repository.updateTask(task);
            
            // Handle subtasks
            if (subtaskList != null) {
                for (Subtask subtask : subtaskList) {
                    if (subtask.getId() == 0) {
                        subtask.setTaskId(task.getId());
                        repository.insertSubtask(subtask);
                    } else {
                        repository.updateSubtask(subtask);
                    }
                }
            }
            
            // Delete removed subtasks
            if (toDelete != null) {
                for (Subtask subtask : toDelete) {
                    repository.deleteSubtask(subtask);
                }
            }

            // Schedule reminder if needed
            if (task.isHasReminder() && task.getReminderTime() != null) {
                reminderScheduler.scheduleReminder(task);
            } else {
                reminderScheduler.cancelReminder(task.getId());
            }

            savedTaskId.setValue(task.getId());
            isSaving.setValue(false);
            saveSuccess.setValue(true);
        } else {
            // Create new task
            repository.insertTaskWithSubtasks(task, subtaskList, newTaskId -> {
                // Schedule reminder if needed
                if (task.isHasReminder() && task.getReminderTime() != null) {
                    task.setId(newTaskId);
                    reminderScheduler.scheduleReminder(task);
                }

                savedTaskId.postValue(newTaskId);
                isSaving.postValue(false);
                saveSuccess.postValue(true);
            });
        }
    }

    // ==================== SUBTASK OPERATIONS ====================

    /**
     * Add a new subtask
     */
    public void addSubtask(String subtaskTitle) {
        if (subtaskTitle == null || subtaskTitle.trim().isEmpty()) {
            return;
        }

        List<Subtask> current = subtasks.getValue();
        if (current == null) current = new ArrayList<>();

        Subtask subtask = new Subtask();
        subtask.setTitle(subtaskTitle.trim());
        subtask.setPosition(current.size());

        current.add(subtask);
        subtasks.setValue(current);
    }

    /**
     * Update a subtask title
     */
    public void updateSubtaskTitle(int position, String newTitle) {
        List<Subtask> current = subtasks.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            current.get(position).setTitle(newTitle);
            subtasks.setValue(current);
        }
    }

    /**
     * Toggle subtask completion
     */
    public void toggleSubtaskCompleted(int position) {
        List<Subtask> current = subtasks.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            Subtask subtask = current.get(position);
            subtask.setCompleted(!subtask.isCompleted());
            subtasks.setValue(current);
        }
    }

    /**
     * Remove a subtask
     */
    public void removeSubtask(int position) {
        List<Subtask> current = subtasks.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            Subtask removed = current.remove(position);
            
            // Track deletion for existing subtasks
            if (removed.getId() > 0) {
                List<Subtask> deleted = deletedSubtasks.getValue();
                if (deleted == null) deleted = new ArrayList<>();
                deleted.add(removed);
                deletedSubtasks.setValue(deleted);
            }
            
            // Update positions
            for (int i = position; i < current.size(); i++) {
                current.get(i).setPosition(i);
            }
            
            subtasks.setValue(current);
        }
    }

    /**
     * Reorder subtasks
     */
    public void moveSubtask(int fromPosition, int toPosition) {
        List<Subtask> current = subtasks.getValue();
        if (current == null || fromPosition < 0 || toPosition < 0 
            || fromPosition >= current.size() || toPosition >= current.size()) {
            return;
        }

        Subtask moved = current.remove(fromPosition);
        current.add(toPosition, moved);

        // Update all positions
        for (int i = 0; i < current.size(); i++) {
            current.get(i).setPosition(i);
        }

        subtasks.setValue(current);
    }

    // ==================== SETTERS ====================

    public void setTitle(String value) {
        title.setValue(value);
        validateForm();
    }

    public void setDescription(String value) {
        description.setValue(value);
    }

    public void setDueDate(Long value) {
        dueDate.setValue(value);
        // Auto-set reminder time if enabled and not set
        if (hasReminder.getValue() != null && hasReminder.getValue() && reminderTime.getValue() == null && value != null) {
            // Default reminder: 1 hour before due
            reminderTime.setValue(value - 3600000);
        }
    }

    public void setDueTime(Long value) {
        dueTime.setValue(value);
    }

    public void setPriority(int value) {
        priority.setValue(value);
    }

    public void setCategoryId(Long value) {
        categoryId.setValue(value);
    }

    public void setHasReminder(boolean value) {
        hasReminder.setValue(value);
        if (!value) {
            reminderTime.setValue(null);
        }
    }

    public void setReminderTime(Long value) {
        reminderTime.setValue(value);
        if (value != null) {
            hasReminder.setValue(true);
        }
    }

    public void setIsRepeating(boolean value) {
        isRepeating.setValue(value);
        if (!value) {
            repeatInterval.setValue(Task.RepeatInterval.NONE);
            repeatEndDate.setValue(null);
        }
    }

    public void setRepeatInterval(int value) {
        repeatInterval.setValue(value);
        if (value != Task.RepeatInterval.NONE) {
            isRepeating.setValue(true);
        }
    }

    public void setRepeatEndDate(Long value) {
        repeatEndDate.setValue(value);
    }

    public void setColor(String value) {
        color.setValue(value);
    }

    public void setNotes(String value) {
        notes.setValue(value);
    }

    // ==================== GETTERS ====================

    public boolean isEditMode() {
        return isEditMode;
    }

    public LiveData<Task> getCurrentTask() {
        return currentTask;
    }

    public LiveData<String> getTitle() {
        return title;
    }

    public LiveData<String> getDescription() {
        return description;
    }

    public LiveData<Long> getDueDate() {
        return dueDate;
    }

    public LiveData<Long> getDueTime() {
        return dueTime;
    }

    public LiveData<Integer> getPriority() {
        return priority;
    }

    public LiveData<Long> getCategoryId() {
        return categoryId;
    }

    public LiveData<Boolean> getHasReminder() {
        return hasReminder;
    }

    public LiveData<Long> getReminderTime() {
        return reminderTime;
    }

    public LiveData<Boolean> getIsRepeating() {
        return isRepeating;
    }

    public LiveData<Integer> getRepeatInterval() {
        return repeatInterval;
    }

    public LiveData<Long> getRepeatEndDate() {
        return repeatEndDate;
    }

    public LiveData<String> getColor() {
        return color;
    }

    public LiveData<String> getNotes() {
        return notes;
    }

    public LiveData<List<Subtask>> getSubtasks() {
        return subtasks;
    }

    public LiveData<String> getTitleError() {
        return titleError;
    }

    public LiveData<Boolean> getIsFormValid() {
        return isFormValid;
    }

    public LiveData<Boolean> getIsSaving() {
        return isSaving;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Long> getSavedTaskId() {
        return savedTaskId;
    }

    public LiveData<List<Category>> getAllCategories() {
        return repository.getAllCategories();
    }

    /**
     * Reset save success state (call after handling navigation)
     */
    public void resetSaveSuccess() {
        saveSuccess.setValue(null);
    }
}
