package com.todoapp.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.todoapp.data.model.Category;
import com.todoapp.data.model.FullTaskDetails;
import com.todoapp.data.model.Subtask;
import com.todoapp.data.model.Task;
import com.todoapp.data.model.TaskWithSubtasks;
import com.todoapp.data.repository.TaskRepository;
import com.todoapp.notifications.ReminderScheduler;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * TaskDetailViewModel - ViewModel for the task detail screen.
 * 
 * Manages task details, subtask interactions, and task actions
 * like complete, archive, delete.
 */
@HiltViewModel
public class TaskDetailViewModel extends ViewModel {

    private final TaskRepository repository;
    private final ReminderScheduler reminderScheduler;

    // Task data
    private final MutableLiveData<Long> taskId = new MutableLiveData<>();
    private final LiveData<TaskWithSubtasks> taskWithSubtasks;
    private final LiveData<Task> task;
    private final LiveData<List<Subtask>> subtasks;
    private final MutableLiveData<Category> category = new MutableLiveData<>();

    // UI state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> taskDeleted = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public TaskDetailViewModel(TaskRepository repository, ReminderScheduler reminderScheduler) {
        this.repository = repository;
        this.reminderScheduler = reminderScheduler;
        
        // Use Transformations to react to taskId changes
        task = Transformations.switchMap(taskId, id -> {
            if (id != null && id > 0) {
                return repository.getTaskById(id);
            }
            return new MutableLiveData<>(null);
        });
        
        taskWithSubtasks = Transformations.switchMap(taskId, id -> {
            if (id != null && id > 0) {
                return repository.getTaskWithSubtasks(id);
            }
            return new MutableLiveData<>(null);
        });
        
        subtasks = Transformations.switchMap(taskId, id -> {
            if (id != null && id > 0) {
                return repository.getSubtasksByTaskId(id);
            }
            return new MutableLiveData<>(new ArrayList<>());
        });
    }

    /**
     * Load task details by ID
     */
    public void loadTask(long id) {
        if (id <= 0) {
            errorMessage.setValue("Invalid task ID");
            return;
        }

        taskId.setValue(id);
        isLoading.setValue(true);

        // Load category when task changes
        task.observeForever(loadedTask -> {
            if (loadedTask != null && loadedTask.getCategoryId() != null) {
                repository.getCategoryById(loadedTask.getCategoryId()).observeForever(cat -> {
                    category.setValue(cat);
                });
            }
            isLoading.setValue(false);
        });
    }

    // ==================== TASK ACTIONS ====================

    /**
     * Toggle task completion
     */
    public void toggleTaskCompleted() {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            repository.toggleTaskCompleted(currentTask);
        }
    }

    /**
     * Mark task as completed
     */
    public void completeTask() {
        Long id = taskId.getValue();
        if (id != null) {
            repository.setTaskCompleted(id, true);
        }
    }

    /**
     * Mark task as not completed
     */
    public void uncompleteTask() {
        Long id = taskId.getValue();
        if (id != null) {
            repository.setTaskCompleted(id, false);
        }
    }

    /**
     * Archive the task
     */
    public void archiveTask() {
        Long id = taskId.getValue();
        if (id != null) {
            repository.archiveTask(id);
        }
    }

    /**
     * Unarchive the task
     */
    public void unarchiveTask() {
        Long id = taskId.getValue();
        if (id != null) {
            repository.unarchiveTask(id);
        }
    }

    /**
     * Delete the task
     */
    public void deleteTask() {
        Long id = taskId.getValue();
        if (id != null) {
            // Cancel any scheduled reminders
            reminderScheduler.cancelReminder(id);
            repository.deleteTaskById(id);
            taskDeleted.setValue(true);
        }
    }

    /**
     * Update task priority
     */
    public void updatePriority(int priority) {
        Long id = taskId.getValue();
        if (id != null) {
            repository.updateTaskPriority(id, priority);
        }
    }

    /**
     * Update task category
     */
    public void updateCategory(Long categoryId) {
        Long id = taskId.getValue();
        if (id != null) {
            repository.updateTaskCategory(id, categoryId);
        }
    }

    // ==================== SUBTASK ACTIONS ====================

    /**
     * Toggle subtask completion
     */
    public void toggleSubtaskCompleted(Subtask subtask) {
        repository.toggleSubtaskCompleted(subtask);
    }

    /**
     * Add a new subtask
     */
    public void addSubtask(String title) {
        Long id = taskId.getValue();
        android.util.Log.d("TaskDetailViewModel", "addSubtask called with title: " + title + ", taskId: " + id);
        if (id != null && id > 0 && title != null && !title.trim().isEmpty()) {
            Subtask subtask = new Subtask(id, title.trim());
            android.util.Log.d("TaskDetailViewModel", "Inserting subtask for task: " + id);
            repository.insertSubtask(subtask);
        } else {
            android.util.Log.e("TaskDetailViewModel", "Failed to add subtask - id: " + id + ", title: " + title);
        }
    }

    /**
     * Delete a subtask
     */
    public void deleteSubtask(Subtask subtask) {
        repository.deleteSubtask(subtask);
    }

    /**
     * Update subtask title
     */
    public void updateSubtaskTitle(Subtask subtask, String newTitle) {
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            subtask.setTitle(newTitle.trim());
            repository.updateSubtask(subtask);
        }
    }

    /**
     * Delete completed subtasks
     */
    public void deleteCompletedSubtasks() {
        Long id = taskId.getValue();
        if (id != null) {
            repository.deleteCompletedSubtasks(id);
        }
    }

    // ==================== REMINDER ACTIONS ====================

    /**
     * Schedule or reschedule reminder
     */
    public void scheduleReminder(long reminderTime) {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            currentTask.setHasReminder(true);
            currentTask.setReminderTime(reminderTime);
            repository.updateTask(currentTask);
            reminderScheduler.scheduleReminder(currentTask);
        }
    }

    /**
     * Cancel reminder
     */
    public void cancelReminder() {
        Task currentTask = task.getValue();
        Long id = taskId.getValue();
        if (currentTask != null && id != null) {
            currentTask.setHasReminder(false);
            currentTask.setReminderTime(null);
            repository.updateTask(currentTask);
            reminderScheduler.cancelReminder(id);
        }
    }

    /**
     * Snooze reminder (reschedule for later)
     */
    public void snoozeReminder(long snoozeMinutes) {
        Task currentTask = task.getValue();
        if (currentTask != null) {
            long newReminderTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000);
            currentTask.setReminderTime(newReminderTime);
            repository.updateTask(currentTask);
            reminderScheduler.scheduleReminder(currentTask);
        }
    }

    // ==================== GETTERS ====================

    public LiveData<Long> getTaskId() {
        return taskId;
    }

    public LiveData<TaskWithSubtasks> getTaskWithSubtasks() {
        return taskWithSubtasks;
    }

    public LiveData<Task> getTask() {
        return task;
    }

    public LiveData<List<Subtask>> getSubtasks() {
        return subtasks;
    }

    public LiveData<Category> getCategory() {
        return category;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getTaskDeleted() {
        return taskDeleted;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<Category>> getAllCategories() {
        return repository.getAllCategories();
    }

    /**
     * Get full task details as a combined object
     */
    public FullTaskDetails getFullTaskDetails() {
        Task t = task.getValue();
        List<Subtask> s = subtasks.getValue();
        Category c = category.getValue();
        
        if (t != null) {
            return new FullTaskDetails(t, s, c);
        }
        return null;
    }
}
