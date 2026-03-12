package com.todoapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.todoapp.data.model.Task;
import com.todoapp.data.model.TaskWithSubtasks;
import com.todoapp.data.model.TaskWithCategory;

import java.util.List;

/**
 * TaskDao - Data Access Object for Task entity.
 * 
 * Provides all database operations for tasks including CRUD operations,
 * filtering, searching, and complex queries with relationships.
 */
@Dao
public interface TaskDao {

    // ==================== INSERT OPERATIONS ====================

    /**
     * Insert a single task
     * @return The row ID of the inserted task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    /**
     * Insert multiple tasks
     * @return Array of row IDs for inserted tasks
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<Task> tasks);

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Update a single task
     */
    @Update
    void update(Task task);

    /**
     * Update multiple tasks
     */
    @Update
    void updateAll(List<Task> tasks);

    /**
     * Mark a task as completed
     */
    @Query("UPDATE tasks SET is_completed = :isCompleted, completed_at = :completedAt, updated_at = :updatedAt WHERE id = :taskId")
    void setTaskCompleted(long taskId, boolean isCompleted, Long completedAt, long updatedAt);

    /**
     * Mark a task as archived
     */
    @Query("UPDATE tasks SET is_archived = :isArchived, updated_at = :updatedAt WHERE id = :taskId")
    void setTaskArchived(long taskId, boolean isArchived, long updatedAt);

    /**
     * Soft delete a task (move to trash)
     */
    @Query("UPDATE tasks SET is_deleted = 1, deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :taskId")
    void softDeleteTask(long taskId, long deletedAt, long updatedAt);

    /**
     * Restore a deleted task
     */
    @Query("UPDATE tasks SET is_deleted = 0, deleted_at = NULL, updated_at = :updatedAt WHERE id = :taskId")
    void restoreTask(long taskId, long updatedAt);

    /**
     * Update task priority
     */
    @Query("UPDATE tasks SET priority = :priority, updated_at = :updatedAt WHERE id = :taskId")
    void updatePriority(long taskId, int priority, long updatedAt);

    /**
     * Update task category
     */
    @Query("UPDATE tasks SET category_id = :categoryId, updated_at = :updatedAt WHERE id = :taskId")
    void updateCategory(long taskId, Long categoryId, long updatedAt);

    /**
     * Update task position (for manual reordering)
     */
    @Query("UPDATE tasks SET position = :position WHERE id = :taskId")
    void updatePosition(long taskId, int position);

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete a single task
     */
    @Delete
    void delete(Task task);

    /**
     * Delete a task by ID
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteById(long taskId);

    /**
     * Delete multiple tasks
     */
    @Delete
    void deleteAll(List<Task> tasks);

    /**
     * Delete all archived tasks
     */
    @Query("DELETE FROM tasks WHERE is_archived = 1")
    void deleteAllArchived();

    /**
     * Delete all completed tasks
     */
    @Query("DELETE FROM tasks WHERE is_completed = 1")
    void deleteAllCompleted();

    /**
     * Delete all tasks (dangerous - use with caution)
     */
    @Query("DELETE FROM tasks")
    void deleteAllTasks();

    // ==================== QUERY OPERATIONS - Single Task ====================

    /**
     * Get a task by ID (LiveData for observation)
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<Task> getTaskById(long taskId);

    /**
     * Get a task by ID (sync for one-time operations)
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskByIdSync(long taskId);

    /**
     * Get task with its subtasks
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<TaskWithSubtasks> getTaskWithSubtasks(long taskId);

    /**
     * Get task with its subtasks (sync)
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    TaskWithSubtasks getTaskWithSubtasksSync(long taskId);

    /**
     * Get task with its category
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<TaskWithCategory> getTaskWithCategory(long taskId);

    // ==================== QUERY OPERATIONS - All Tasks ====================

    /**
     * Get all tasks ordered by position (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE is_archived = 0 AND is_deleted = 0 ORDER BY position ASC, created_at DESC")
    LiveData<List<Task>> getAllTasks();

    /**
     * Get incomplete tasks (for widget) - sync version
     */
    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND is_archived = 0 AND is_deleted = 0 ORDER BY due_date ASC, priority DESC")
    List<Task> getIncompleteTasks();

    /**
     * Get all tasks (sync) - excluding deleted
     */
    @Query("SELECT * FROM tasks WHERE is_deleted = 0 ORDER BY created_at DESC")
    List<Task> getAllTasksSync();

    /**
     * Get all tasks with subtasks (excluding deleted)
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE is_archived = 0 AND is_deleted = 0 ORDER BY position ASC, created_at DESC")
    LiveData<List<TaskWithSubtasks>> getAllTasksWithSubtasks();

    /**
     * Get pending (not completed) tasks (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND is_archived = 0 AND is_deleted = 0 ORDER BY priority DESC, due_date ASC, created_at DESC")
    LiveData<List<Task>> getPendingTasks();

    /**
     * Get completed tasks (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE is_completed = 1 AND is_archived = 0 AND is_deleted = 0 ORDER BY completed_at DESC")
    LiveData<List<Task>> getCompletedTasks();

    /**
     * Get deleted tasks (trash)
     */
    @Query("SELECT * FROM tasks WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    LiveData<List<Task>> getDeletedTasks();

    /**
     * Get deleted tasks count
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE is_deleted = 1")
    LiveData<Integer> getDeletedTasksCount();

    /**
     * Permanently delete all deleted tasks (empty trash)
     */
    @Query("DELETE FROM tasks WHERE is_deleted = 1")
    void emptyTrash();

    /**
     * Get archived tasks
     */
    @Query("SELECT * FROM tasks WHERE is_archived = 1 ORDER BY updated_at DESC")
    LiveData<List<Task>> getArchivedTasks();

    // ==================== QUERY OPERATIONS - Filtered Tasks ====================

    /**
     * Get tasks by category (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE category_id = :categoryId AND is_archived = 0 AND is_deleted = 0 ORDER BY priority DESC, due_date ASC")
    LiveData<List<Task>> getTasksByCategory(long categoryId);

    /**
     * Get tasks by priority (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE priority = :priority AND is_archived = 0 AND is_deleted = 0 ORDER BY due_date ASC, created_at DESC")
    LiveData<List<Task>> getTasksByPriority(int priority);

    /**
     * Get tasks due today (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE due_date >= :startOfDay AND due_date < :endOfDay AND is_archived = 0 AND is_deleted = 0 ORDER BY priority DESC, due_time ASC")
    LiveData<List<Task>> getTasksDueToday(long startOfDay, long endOfDay);

    /**
     * Get tasks due this week (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE due_date >= :startOfWeek AND due_date < :endOfWeek AND is_archived = 0 AND is_deleted = 0 ORDER BY due_date ASC, priority DESC")
    LiveData<List<Task>> getTasksDueThisWeek(long startOfWeek, long endOfWeek);

    /**
     * Get overdue tasks (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE due_date < :now AND is_completed = 0 AND is_archived = 0 AND is_deleted = 0 ORDER BY due_date ASC, priority DESC")
    LiveData<List<Task>> getOverdueTasks(long now);

    /**
     * Get tasks with future reminders (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE has_reminder = 1 AND reminder_time > :now AND is_completed = 0 AND is_archived = 0 AND is_deleted = 0 ORDER BY reminder_time ASC")
    LiveData<List<Task>> getTasksWithReminders(long now);

    /**
     * Get all tasks that have reminders enabled (for notification list)
     * Shows all tasks with has_reminder = 1, regardless of reminder time
     */
    @Query("SELECT * FROM tasks WHERE has_reminder = 1 AND is_completed = 0 AND is_archived = 0 AND is_deleted = 0 ORDER BY reminder_time ASC")
    LiveData<List<Task>> getTasksWithRemindersEnabled();

    /**
     * Get all tasks with reminders including recent past ones (for notification history)
     * Shows reminders from past 24 hours and all future reminders
     */
    @Query("SELECT * FROM tasks WHERE has_reminder = 1 AND reminder_time > :past24Hours AND is_completed = 0 AND is_archived = 0 AND is_deleted = 0 ORDER BY reminder_time DESC")
    LiveData<List<Task>> getAllTasksWithReminders(long past24Hours);

    /**
     * Get tasks needing reminder scheduling (sync, excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE has_reminder = 1 AND reminder_time > :now AND is_completed = 0 AND is_archived = 0 AND is_deleted = 0")
    List<Task> getTasksWithRemindersSync(long now);

    /**
     * Get repeating tasks (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE is_repeating = 1 AND is_archived = 0 AND is_deleted = 0 ORDER BY due_date ASC")
    LiveData<List<Task>> getRepeatingTasks();

    // ==================== SEARCH OPERATIONS ====================

    /**
     * Search tasks by title or description (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND is_archived = 0 AND is_deleted = 0 ORDER BY priority DESC, created_at DESC")
    LiveData<List<Task>> searchTasks(String query);

    /**
     * Search tasks (sync, excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND is_archived = 0 AND is_deleted = 0 ORDER BY priority DESC, created_at DESC")
    List<Task> searchTasksSync(String query);

    // ==================== STATISTICS OPERATIONS ====================

    /**
     * Get count of all tasks (excluding deleted)
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE is_archived = 0 AND is_deleted = 0")
    LiveData<Integer> getTotalTaskCount();

    /**
     * Get count of completed tasks (excluding deleted)
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1 AND is_archived = 0 AND is_deleted = 0")
    LiveData<Integer> getCompletedTaskCount();

    /**
     * Get count of pending tasks (excluding deleted)
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0 AND is_archived = 0 AND is_deleted = 0")
    LiveData<Integer> getPendingTaskCount();

    /**
     * Get count of overdue tasks (excluding deleted)
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE due_date < :now AND is_completed = 0 AND is_archived = 0 AND is_deleted = 0")
    LiveData<Integer> getOverdueTaskCount(long now);

    /**
     * Get count of tasks completed today (excluding deleted)
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE completed_at >= :startOfDay AND completed_at < :endOfDay AND is_deleted = 0")
    int getCompletedTodayCountSync(long startOfDay, long endOfDay);

    /**
     * Get count of tasks completed this week (excluding deleted)
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE completed_at >= :startOfWeek AND completed_at < :endOfWeek AND is_deleted = 0")
    int getCompletedThisWeekCountSync(long startOfWeek, long endOfWeek);

    /**
     * Get count of tasks in a category (excluding deleted)
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE category_id = :categoryId AND is_archived = 0 AND is_deleted = 0")
    int getTaskCountByCategory(long categoryId);

    // ==================== DATE-BASED QUERIES FOR CALENDAR VIEW ====================

    /**
     * Get tasks for a specific date range (excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE due_date >= :startDate AND due_date < :endDate AND is_archived = 0 AND is_deleted = 0 ORDER BY due_time ASC, priority DESC")
    LiveData<List<Task>> getTasksForDateRange(long startDate, long endDate);

    /**
     * Get all tasks with due dates (for calendar view, excluding deleted)
     */
    @Query("SELECT * FROM tasks WHERE due_date IS NOT NULL AND is_archived = 0 AND is_deleted = 0 ORDER BY due_date ASC")
    LiveData<List<Task>> getTasksWithDueDates();

    /**
     * Get distinct dates with tasks (excluding deleted)
     */
    @Query("SELECT DISTINCT due_date FROM tasks WHERE due_date IS NOT NULL AND is_archived = 0 AND is_deleted = 0 ORDER BY due_date ASC")
    LiveData<List<Long>> getDatesWithTasks();
}
