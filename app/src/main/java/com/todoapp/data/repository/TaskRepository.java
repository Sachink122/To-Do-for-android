package com.todoapp.data.repository;

import androidx.lifecycle.LiveData;

import com.todoapp.data.dao.CategoryDao;
import com.todoapp.data.dao.SubtaskDao;
import com.todoapp.data.dao.TaskDao;
import com.todoapp.data.database.TodoDatabase;
import com.todoapp.data.model.BackupData;
import com.todoapp.data.model.Category;
import com.todoapp.data.model.FullTaskDetails;
import com.todoapp.data.model.Subtask;
import com.todoapp.data.model.Task;
import com.todoapp.data.model.TaskStats;
import com.todoapp.data.model.TaskWithSubtasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * TaskRepository - Repository pattern implementation for task data operations.
 * 
 * This is the single source of truth for task data. It abstracts the data sources
 * (local database, potential remote sync) from the ViewModel layer.
 * 
 * Benefits:
 * - Separates data access from UI logic
 * - Easy to swap data sources (e.g., add cloud sync)
 * - Testable via mock implementations
 */
@Singleton
public class TaskRepository {

    private final TaskDao taskDao;
    private final SubtaskDao subtaskDao;
    private final CategoryDao categoryDao;
    private final ExecutorService executorService;

    // Cached LiveData
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> pendingTasks;
    private final LiveData<List<Task>> completedTasks;
    private final LiveData<List<Category>> allCategories;

    @Inject
    public TaskRepository(TaskDao taskDao, SubtaskDao subtaskDao, 
                         CategoryDao categoryDao, ExecutorService executorService) {
        this.taskDao = taskDao;
        this.subtaskDao = subtaskDao;
        this.categoryDao = categoryDao;
        this.executorService = executorService;

        // Initialize cached LiveData
        this.allTasks = taskDao.getAllTasks();
        this.pendingTasks = taskDao.getPendingTasks();
        this.completedTasks = taskDao.getCompletedTasks();
        this.allCategories = categoryDao.getAllCategories();
    }

    // ==================== TASK OPERATIONS ====================

    /**
     * Insert a new task (without callback)
     */
    public void insertTask(Task task) {
        insertTask(task, null);
    }

    /**
     * Insert a new task
     * @return The ID of the inserted task via callback
     */
    public void insertTask(Task task, InsertCallback callback) {
        executorService.execute(() -> {
            long taskId = taskDao.insert(task);
            if (callback != null) {
                callback.onTaskInserted(taskId);
            }
        });
    }

    /**
     * Insert task with subtasks
     */
    public void insertTaskWithSubtasks(Task task, List<Subtask> subtasks, InsertCallback callback) {
        executorService.execute(() -> {
            long taskId = taskDao.insert(task);
            if (subtasks != null && !subtasks.isEmpty()) {
                for (Subtask subtask : subtasks) {
                    subtask.setTaskId(taskId);
                }
                subtaskDao.insertAll(subtasks);
            }
            if (callback != null) {
                callback.onTaskInserted(taskId);
            }
        });
    }

    /**
     * Update an existing task
     */
    public void updateTask(Task task) {
        executorService.execute(() -> {
            task.setUpdatedAt(System.currentTimeMillis());
            taskDao.update(task);
        });
    }

    /**
     * Delete a task (soft delete - moves to trash)
     */
    public void deleteTask(Task task) {
        deleteTask(task, null);
    }

    /**
     * Delete a task with callback (soft delete - moves to trash)
     */
    public void deleteTask(Task task, Runnable onComplete) {
        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            taskDao.softDeleteTask(task.getId(), now, now);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    /**
     * Delete a task by ID (soft delete - moves to trash)
     */
    public void deleteTaskById(long taskId) {
        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            taskDao.softDeleteTask(taskId, now, now);
        });
    }

    /**
     * Set task completion status
     */
    public void setTaskCompleted(long taskId, boolean isCompleted) {
        executorService.execute(() -> {
            Long completedAt = isCompleted ? System.currentTimeMillis() : null;
            taskDao.setTaskCompleted(taskId, isCompleted, completedAt, System.currentTimeMillis());
        });
    }

    /**
     * Toggle task completion
     */
    public void toggleTaskCompleted(Task task) {
        toggleTaskCompleted(task, null);
    }

    /**
     * Toggle task completion with callback
     */
    public void toggleTaskCompleted(Task task, Runnable onComplete) {
        executorService.execute(() -> {
            boolean newStatus = !task.isCompleted();
            Long completedAt = newStatus ? System.currentTimeMillis() : null;
            taskDao.setTaskCompleted(task.getId(), newStatus, completedAt, System.currentTimeMillis());
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    /**
     * Toggle task important status
     */
    public void toggleTaskImportant(long taskId) {
        executorService.execute(() -> {
            Task task = taskDao.getTaskByIdSync(taskId);
            if (task != null) {
                task.setImportant(!task.isImportant());
                task.setUpdatedAt(System.currentTimeMillis());
                taskDao.update(task);
            }
        });
    }

    /**
     * Archive a task
     */
    public void archiveTask(long taskId) {
        executorService.execute(() -> 
            taskDao.setTaskArchived(taskId, true, System.currentTimeMillis()));
    }

    /**
     * Unarchive a task
     */
    public void unarchiveTask(long taskId) {
        executorService.execute(() -> 
            taskDao.setTaskArchived(taskId, false, System.currentTimeMillis()));
    }

    /**
     * Soft delete a task (move to trash)
     */
    public void softDeleteTask(long taskId) {
        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            taskDao.softDeleteTask(taskId, now, now);
        });
    }

    /**
     * Soft delete a task with callback
     */
    public void softDeleteTask(Task task, Runnable callback) {
        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            taskDao.softDeleteTask(task.getId(), now, now);
            if (callback != null) {
                callback.run();
            }
        });
    }

    /**
     * Restore a deleted task from trash
     */
    public void restoreTask(long taskId) {
        executorService.execute(() -> 
            taskDao.restoreTask(taskId, System.currentTimeMillis()));
    }

    /**
     * Permanently delete a task
     */
    public void permanentlyDeleteTask(long taskId) {
        executorService.execute(() -> taskDao.deleteById(taskId));
    }

    /**
     * Empty trash (permanently delete all deleted tasks)
     */
    public void emptyTrash() {
        executorService.execute(() -> taskDao.emptyTrash());
    }

    /**
     * Get deleted tasks
     */
    public LiveData<List<Task>> getDeletedTasks() {
        return taskDao.getDeletedTasks();
    }

    /**
     * Get deleted tasks count
     */
    public LiveData<Integer> getDeletedTasksCount() {
        return taskDao.getDeletedTasksCount();
    }

    /**
     * Update task priority
     */
    public void updateTaskPriority(long taskId, int priority) {
        executorService.execute(() -> 
            taskDao.updatePriority(taskId, priority, System.currentTimeMillis()));
    }

    /**
     * Update task category
     */
    public void updateTaskCategory(long taskId, Long categoryId) {
        executorService.execute(() -> 
            taskDao.updateCategory(taskId, categoryId, System.currentTimeMillis()));
    }

    /**
     * Archive all completed tasks
     */
    public void archiveCompletedTasks() {
        executorService.execute(() -> {
            List<Task> completed = taskDao.getAllTasksSync();
            for (Task task : completed) {
                if (task.isCompleted()) {
                    taskDao.setTaskArchived(task.getId(), true, System.currentTimeMillis());
                }
            }
        });
    }

    /**
     * Delete all archived tasks
     */
    public void deleteArchivedTasks() {
        executorService.execute(taskDao::deleteAllArchived);
    }

    // ==================== TASK QUERIES ====================

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getPendingTasks() {
        return pendingTasks;
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return completedTasks;
    }

    public LiveData<List<Task>> getArchivedTasks() {
        return taskDao.getArchivedTasks();
    }

    public LiveData<Task> getTaskById(long taskId) {
        return taskDao.getTaskById(taskId);
    }

    public LiveData<TaskWithSubtasks> getTaskWithSubtasks(long taskId) {
        return taskDao.getTaskWithSubtasks(taskId);
    }

    public LiveData<List<TaskWithSubtasks>> getAllTasksWithSubtasks() {
        return taskDao.getAllTasksWithSubtasks();
    }

    public LiveData<List<Task>> getTasksByCategory(long categoryId) {
        return taskDao.getTasksByCategory(categoryId);
    }

    public LiveData<List<Task>> getTasksByPriority(int priority) {
        return taskDao.getTasksByPriority(priority);
    }

    public LiveData<List<Task>> getTasksDueToday() {
        long[] dayBounds = getDayBounds(0);
        return taskDao.getTasksDueToday(dayBounds[0], dayBounds[1]);
    }

    public LiveData<List<Task>> getTasksDueThisWeek() {
        long[] weekBounds = getWeekBounds();
        return taskDao.getTasksDueThisWeek(weekBounds[0], weekBounds[1]);
    }

    public LiveData<List<Task>> getOverdueTasks() {
        return taskDao.getOverdueTasks(System.currentTimeMillis());
    }

    public LiveData<List<Task>> getTasksWithReminders() {
        return taskDao.getTasksWithReminders(System.currentTimeMillis());
    }

    public LiveData<List<Task>> getTasksWithRemindersEnabled() {
        return taskDao.getTasksWithRemindersEnabled();
    }

    public LiveData<List<Task>> getAllTasksWithReminders() {
        // Get reminders from past 24 hours and all future ones
        long past24Hours = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        return taskDao.getAllTasksWithReminders(past24Hours);
    }

    public void getTasksWithRemindersSync(TasksCallback callback) {
        executorService.execute(() -> {
            List<Task> tasks = taskDao.getTasksWithRemindersSync(System.currentTimeMillis());
            callback.onTasksLoaded(tasks);
        });
    }

    public LiveData<List<Task>> searchTasks(String query) {
        return taskDao.searchTasks(query);
    }

    public LiveData<List<Task>> getTasksForDateRange(long startDate, long endDate) {
        return taskDao.getTasksForDateRange(startDate, endDate);
    }

    public LiveData<List<Task>> getTasksWithDueDates() {
        return taskDao.getTasksWithDueDates();
    }

    // ==================== SUBTASK OPERATIONS ====================

    public void insertSubtask(Subtask subtask) {
        executorService.execute(() -> subtaskDao.insert(subtask));
    }

    public void insertSubtasks(List<Subtask> subtasks) {
        executorService.execute(() -> subtaskDao.insertAll(subtasks));
    }

    public void updateSubtask(Subtask subtask) {
        executorService.execute(() -> subtaskDao.update(subtask));
    }

    public void deleteSubtask(Subtask subtask) {
        executorService.execute(() -> subtaskDao.delete(subtask));
    }

    public void deleteSubtaskById(long subtaskId) {
        executorService.execute(() -> subtaskDao.deleteById(subtaskId));
    }

    public void setSubtaskCompleted(long subtaskId, boolean isCompleted) {
        executorService.execute(() -> {
            Long completedAt = isCompleted ? System.currentTimeMillis() : null;
            subtaskDao.setSubtaskCompleted(subtaskId, isCompleted, completedAt);
        });
    }

    public void toggleSubtaskCompleted(Subtask subtask) {
        executorService.execute(() -> {
            boolean newStatus = !subtask.isCompleted();
            Long completedAt = newStatus ? System.currentTimeMillis() : null;
            subtaskDao.setSubtaskCompleted(subtask.getId(), newStatus, completedAt);
        });
    }

    public LiveData<List<Subtask>> getSubtasksByTaskId(long taskId) {
        return subtaskDao.getSubtasksByTaskId(taskId);
    }

    public void deleteCompletedSubtasks(long taskId) {
        executorService.execute(() -> subtaskDao.deleteCompletedByTaskId(taskId));
    }

    // ==================== CATEGORY OPERATIONS ====================

    public void insertCategory(Category category) {
        executorService.execute(() -> categoryDao.insert(category));
    }

    public void updateCategory(Category category) {
        executorService.execute(() -> categoryDao.update(category));
    }

    public void deleteCategory(Category category) {
        executorService.execute(() -> categoryDao.delete(category));
    }

    public void deleteCategoryById(long categoryId) {
        executorService.execute(() -> categoryDao.deleteById(categoryId));
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<Category> getCategoryById(long categoryId) {
        return categoryDao.getCategoryById(categoryId);
    }

    public LiveData<List<Category>> getCategoriesWithTaskCounts() {
        return categoryDao.getCategoriesWithTaskCounts();
    }

    public LiveData<List<Category>> searchCategories(String query) {
        return categoryDao.searchCategories(query);
    }

    // ==================== STATISTICS ====================

    public LiveData<Integer> getTotalTaskCount() {
        return taskDao.getTotalTaskCount();
    }

    public LiveData<Integer> getCompletedTaskCount() {
        return taskDao.getCompletedTaskCount();
    }

    public LiveData<Integer> getPendingTaskCount() {
        return taskDao.getPendingTaskCount();
    }

    public LiveData<Integer> getOverdueTaskCount() {
        return taskDao.getOverdueTaskCount(System.currentTimeMillis());
    }

    public void getTaskStats(StatsCallback callback) {
        executorService.execute(() -> {
            TaskStats stats = new TaskStats();
            
            List<Task> allTasks = taskDao.getAllTasksSync();
            int total = 0, completed = 0, pending = 0, overdue = 0;
            long now = System.currentTimeMillis();
            
            for (Task task : allTasks) {
                // Skip archived and deleted tasks
                if (!task.isArchived() && !task.isDeleted()) {
                    total++;
                    if (task.isCompleted()) {
                        completed++;
                    } else {
                        pending++;
                        if (task.getDueDate() != null && task.getDueDate() < now) {
                            overdue++;
                        }
                    }
                }
            }
            
            stats.setTotalTasks(total);
            stats.setCompletedTasks(completed);
            stats.setPendingTasks(pending);
            stats.setOverdueTasks(overdue);
            
            // Today stats
            long[] todayBounds = getDayBounds(0);
            stats.setCompletedToday(taskDao.getCompletedTodayCountSync(todayBounds[0], todayBounds[1]));
            
            // Week stats
            long[] weekBounds = getWeekBounds();
            stats.setCompletedThisWeek(taskDao.getCompletedThisWeekCountSync(weekBounds[0], weekBounds[1]));
            
            callback.onStatsLoaded(stats);
        });
    }

    // ==================== BACKUP/RESTORE ====================

    public void getBackupData(String appVersion, BackupCallback callback) {
        executorService.execute(() -> {
            List<Task> tasks = taskDao.getAllTasksSync();
            List<Subtask> subtasks = subtaskDao.getAllSubtasksSync();
            List<Category> categories = categoryDao.getAllCategoriesSync();
            
            BackupData backupData = new BackupData(tasks, subtasks, categories, appVersion);
            callback.onBackupReady(backupData);
        });
    }

    public void restoreFromBackup(BackupData backupData, RestoreCallback callback) {
        executorService.execute(() -> {
            try {
                // Clear existing data
                taskDao.deleteAllTasks();
                subtaskDao.deleteAllSubtasks();
                categoryDao.deleteAllCategories();
                
                // Restore categories
                if (backupData.getCategories() != null) {
                    categoryDao.insertAll(backupData.getCategories());
                }
                
                // Restore tasks
                if (backupData.getTasks() != null) {
                    taskDao.insertAll(backupData.getTasks());
                }
                
                // Restore subtasks
                if (backupData.getSubtasks() != null) {
                    subtaskDao.insertAll(backupData.getSubtasks());
                }
                
                callback.onRestoreComplete(true, "Restore successful");
            } catch (Exception e) {
                callback.onRestoreComplete(false, "Restore failed: " + e.getMessage());
            }
        });
    }

    // ==================== HELPER METHODS ====================

    private long[] getDayBounds(int daysOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        long endOfDay = calendar.getTimeInMillis();
        
        return new long[]{startOfDay, endOfDay};
    }

    private long[] getWeekBounds() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfWeek = calendar.getTimeInMillis();
        
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        long endOfWeek = calendar.getTimeInMillis();
        
        return new long[]{startOfWeek, endOfWeek};
    }

    // ==================== CALLBACKS ====================

    public interface InsertCallback {
        void onTaskInserted(long taskId);
    }

    public interface TasksCallback {
        void onTasksLoaded(List<Task> tasks);
    }

    public interface StatsCallback {
        void onStatsLoaded(TaskStats stats);
    }

    public interface BackupCallback {
        void onBackupReady(BackupData backupData);
    }

    public interface RestoreCallback {
        void onRestoreComplete(boolean success, String message);
    }
}
