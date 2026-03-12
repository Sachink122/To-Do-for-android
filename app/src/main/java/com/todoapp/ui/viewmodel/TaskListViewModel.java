package com.todoapp.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.todoapp.data.model.Category;
import com.todoapp.data.model.Task;
import com.todoapp.data.model.TaskStats;
import com.todoapp.data.model.TaskWithSubtasks;
import com.todoapp.data.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * TaskListViewModel - ViewModel for the task list screen.
 * 
 * Manages the task list state, filtering, sorting, and user interactions.
 * Follows MVVM architecture by exposing LiveData for UI observation.
 */
@HiltViewModel
public class TaskListViewModel extends ViewModel {

    private final TaskRepository repository;

    // Filter and sort states
    private final MutableLiveData<FilterType> currentFilter = new MutableLiveData<>(FilterType.ALL);
    private final MutableLiveData<SortType> currentSort = new MutableLiveData<>(SortType.DATE_CREATED);
    private final MutableLiveData<Long> selectedCategoryId = new MutableLiveData<>(null);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    // Task list with filter applied
    private final MediatorLiveData<List<Task>> filteredTasks = new MediatorLiveData<>();
    
    // Track current task source to remove when filter changes
    private LiveData<List<Task>> currentTaskSource = null;

    // UI state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isMultiSelectMode = new MutableLiveData<>(false);
    private final MutableLiveData<List<Long>> selectedTaskIds = new MutableLiveData<>(new ArrayList<>());

    // Statistics
    private final MutableLiveData<TaskStats> taskStats = new MutableLiveData<>();

    @Inject
    public TaskListViewModel(TaskRepository repository) {
        this.repository = repository;
        setupFilteredTasks();
        loadStats();
    }

    /**
     * Setup the filtered tasks MediatorLiveData to respond to filter changes
     */
    private void setupFilteredTasks() {
        // Initial load with all tasks
        currentTaskSource = repository.getAllTasks();
        filteredTasks.addSource(currentTaskSource, this::applyFilter);

        // React to filter changes
        filteredTasks.addSource(currentFilter, filter -> {
            reloadTasksForFilter();
        });

        // React to category selection
        filteredTasks.addSource(selectedCategoryId, categoryId -> {
            reloadTasksForFilter();
        });

        // React to search query
        filteredTasks.addSource(searchQuery, query -> {
            reloadTasksForFilter();
        });
    }

    /**
     * Reload tasks based on current filter
     */
    private void reloadTasksForFilter() {
        FilterType filter = currentFilter.getValue();
        if (filter == null) filter = FilterType.ALL;

        // Remove previous source to avoid duplicates
        if (currentTaskSource != null) {
            filteredTasks.removeSource(currentTaskSource);
        }

        switch (filter) {
            case PENDING:
                currentTaskSource = repository.getPendingTasks();
                break;
            case COMPLETED:
                currentTaskSource = repository.getCompletedTasks();
                break;
            case TODAY:
                currentTaskSource = repository.getTasksDueToday();
                break;
            case THIS_WEEK:
                currentTaskSource = repository.getTasksDueThisWeek();
                break;
            case OVERDUE:
                currentTaskSource = repository.getOverdueTasks();
                break;
            case ARCHIVED:
                currentTaskSource = repository.getArchivedTasks();
                break;
            case BY_CATEGORY:
                Long catId = selectedCategoryId.getValue();
                if (catId != null) {
                    currentTaskSource = repository.getTasksByCategory(catId);
                } else {
                    currentTaskSource = repository.getAllTasks();
                }
                break;
            case SEARCH:
                String query = searchQuery.getValue();
                if (query != null && !query.isEmpty()) {
                    currentTaskSource = repository.searchTasks(query);
                } else {
                    currentTaskSource = repository.getAllTasks();
                }
                break;
            case ALL:
            default:
                currentTaskSource = repository.getAllTasks();
                break;
        }

        // Observe the new source
        filteredTasks.addSource(currentTaskSource, this::applyFilter);
    }

    /**
     * Apply sorting and additional filtering to tasks
     */
    private void applyFilter(List<Task> tasks) {
        if (tasks == null) {
            filteredTasks.setValue(new ArrayList<>());
            return;
        }

        List<Task> result = new ArrayList<>(tasks);

        // Apply search filter if in search mode
        String query = searchQuery.getValue();
        if (query != null && !query.isEmpty() && currentFilter.getValue() != FilterType.SEARCH) {
            List<Task> filtered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (Task task : result) {
                if ((task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerQuery))) {
                    filtered.add(task);
                }
            }
            result = filtered;
        }

        // Apply sorting
        SortType sort = currentSort.getValue();
        if (sort != null) {
            result = sortTasks(result, sort);
        }

        filteredTasks.setValue(result);
    }

    /**
     * Sort tasks based on sort type
     */
    private List<Task> sortTasks(List<Task> tasks, SortType sortType) {
        List<Task> sorted = new ArrayList<>(tasks);
        
        switch (sortType) {
            case DATE_CREATED:
                sorted.sort((t1, t2) -> Long.compare(t2.getCreatedAt(), t1.getCreatedAt()));
                break;
            case DATE_DUE:
                sorted.sort((t1, t2) -> {
                    if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                    if (t1.getDueDate() == null) return 1;
                    if (t2.getDueDate() == null) return -1;
                    return Long.compare(t1.getDueDate(), t2.getDueDate());
                });
                break;
            case PRIORITY:
                sorted.sort((t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));
                break;
            case ALPHABETICAL:
                sorted.sort((t1, t2) -> {
                    if (t1.getTitle() == null) return 1;
                    if (t2.getTitle() == null) return -1;
                    return t1.getTitle().compareToIgnoreCase(t2.getTitle());
                });
                break;
            case CUSTOM:
                sorted.sort((t1, t2) -> Integer.compare(t1.getPosition(), t2.getPosition()));
                break;
        }
        
        return sorted;
    }

    /**
     * Load task statistics
     */
    private void loadStats() {
        repository.getTaskStats(stats -> {
            taskStats.postValue(stats);
        });
    }

    /**
     * Refresh task statistics (public method for external refresh)
     */
    public void refreshStats() {
        loadStats();
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Set the current filter
     */
    public void setFilter(FilterType filter) {
        currentFilter.setValue(filter);
    }

    /**
     * Set the sort type
     */
    public void setSort(SortType sort) {
        currentSort.setValue(sort);
        // Re-apply filter with new sort
        List<Task> current = filteredTasks.getValue();
        if (current != null) {
            applyFilter(current);
        }
    }

    /**
     * Set search query
     */
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
        if (query != null && !query.isEmpty()) {
            currentFilter.setValue(FilterType.SEARCH);
        }
    }

    /**
     * Clear search
     */
    public void clearSearch() {
        searchQuery.setValue("");
        currentFilter.setValue(FilterType.ALL);
    }

    /**
     * Filter by category
     */
    public void filterByCategory(Long categoryId) {
        selectedCategoryId.setValue(categoryId);
        currentFilter.setValue(FilterType.BY_CATEGORY);
    }

    /**
     * Toggle task completion
     */
    public void toggleTaskCompleted(Task task) {
        repository.toggleTaskCompleted(task, this::loadStats);
    }

    /**
     * Update task completion status
     */
    public void updateTaskCompletion(long taskId, boolean isCompleted) {
        repository.setTaskCompleted(taskId, isCompleted);
        // Delay stats load slightly to ensure DB update completes
        loadStats();
    }

    /**
     * Toggle task importance
     */
    public void toggleImportant(long taskId) {
        repository.toggleTaskImportant(taskId);
    }

    /**
     * Insert a task
     */
    public void insertTask(Task task) {
        repository.insertTask(task);
        loadStats();
    }

    /**
     * Refresh tasks list
     */
    public void refreshTasks() {
        reloadTasksForFilter();
        loadStats();
    }

    /**
     * Delete a task
     */
    public void deleteTask(Task task) {
        repository.deleteTask(task, this::loadStats);
    }

    /**
     * Archive a task
     */
    public void archiveTask(Task task) {
        repository.archiveTask(task.getId());
    }

    /**
     * Unarchive a task
     */
    public void unarchiveTask(Task task) {
        repository.unarchiveTask(task.getId());
    }

    /**
     * Archive all completed tasks
     */
    public void archiveCompletedTasks() {
        repository.archiveCompletedTasks();
    }

    /**
     * Delete all archived tasks
     */
    public void deleteArchivedTasks() {
        repository.deleteArchivedTasks();
    }

    // ==================== MULTI-SELECT OPERATIONS ====================

    /**
     * Enter multi-select mode
     */
    public void enterMultiSelectMode() {
        isMultiSelectMode.setValue(true);
        selectedTaskIds.setValue(new ArrayList<>());
    }

    /**
     * Exit multi-select mode
     */
    public void exitMultiSelectMode() {
        isMultiSelectMode.setValue(false);
        selectedTaskIds.setValue(new ArrayList<>());
    }

    /**
     * Toggle task selection
     */
    public void toggleTaskSelection(long taskId) {
        List<Long> selected = selectedTaskIds.getValue();
        if (selected == null) selected = new ArrayList<>();

        if (selected.contains(taskId)) {
            selected.remove(taskId);
        } else {
            selected.add(taskId);
        }
        selectedTaskIds.setValue(selected);

        // Exit multi-select if no items selected
        if (selected.isEmpty()) {
            exitMultiSelectMode();
        }
    }

    /**
     * Delete selected tasks
     */
    public void deleteSelectedTasks() {
        List<Long> selected = selectedTaskIds.getValue();
        if (selected != null) {
            for (Long taskId : selected) {
                repository.deleteTaskById(taskId);
            }
        }
        exitMultiSelectMode();
        loadStats();
    }

    /**
     * Complete selected tasks
     */
    public void completeSelectedTasks() {
        List<Long> selected = selectedTaskIds.getValue();
        if (selected != null) {
            for (Long taskId : selected) {
                repository.setTaskCompleted(taskId, true);
            }
        }
        exitMultiSelectMode();
        loadStats();
    }

    // ==================== GETTERS ====================

    public LiveData<List<Task>> getFilteredTasks() {
        return filteredTasks;
    }

    public LiveData<FilterType> getCurrentFilter() {
        return currentFilter;
    }

    public LiveData<SortType> getCurrentSort() {
        return currentSort;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsMultiSelectMode() {
        return isMultiSelectMode;
    }

    public LiveData<List<Long>> getSelectedTaskIds() {
        return selectedTaskIds;
    }

    public LiveData<TaskStats> getTaskStats() {
        return taskStats;
    }

    public LiveData<List<Category>> getAllCategories() {
        return repository.getAllCategories();
    }

    public LiveData<List<TaskWithSubtasks>> getAllTasksWithSubtasks() {
        return repository.getAllTasksWithSubtasks();
    }

    public LiveData<Integer> getTotalTaskCount() {
        return repository.getTotalTaskCount();
    }

    public LiveData<Integer> getPendingTaskCount() {
        return repository.getPendingTaskCount();
    }

    public LiveData<Integer> getCompletedTaskCount() {
        return repository.getCompletedTaskCount();
    }

    // ==================== DELETED TASKS (TRASH) ====================

    /**
     * Get all deleted tasks
     */
    public LiveData<List<Task>> getDeletedTasks() {
        return repository.getDeletedTasks();
    }

    /**
     * Get deleted tasks count
     */
    public LiveData<Integer> getDeletedTasksCount() {
        return repository.getDeletedTasksCount();
    }

    /**
     * Restore a deleted task
     */
    public void restoreTask(long taskId) {
        repository.restoreTask(taskId);
    }

    /**
     * Permanently delete a task
     */
    public void permanentlyDeleteTask(long taskId) {
        repository.permanentlyDeleteTask(taskId);
    }

    /**
     * Empty trash (permanently delete all deleted tasks)
     */
    public void emptyTrash() {
        repository.emptyTrash();
    }

    // ==================== ENUMS ====================

    public enum FilterType {
        ALL,
        PENDING,
        COMPLETED,
        TODAY,
        THIS_WEEK,
        OVERDUE,
        ARCHIVED,
        BY_CATEGORY,
        SEARCH
    }

    public enum SortType {
        DATE_CREATED,
        DATE_DUE,
        PRIORITY,
        ALPHABETICAL,
        CUSTOM
    }
}
