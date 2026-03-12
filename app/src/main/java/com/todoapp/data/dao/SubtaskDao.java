package com.todoapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.todoapp.data.model.Subtask;

import java.util.List;

/**
 * SubtaskDao - Data Access Object for Subtask entity.
 * 
 * Provides all database operations for subtasks/checklist items
 * within parent tasks.
 */
@Dao
public interface SubtaskDao {

    // ==================== INSERT OPERATIONS ====================

    /**
     * Insert a single subtask
     * @return The row ID of the inserted subtask
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Subtask subtask);

    /**
     * Insert multiple subtasks
     * @return Array of row IDs for inserted subtasks
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<Subtask> subtasks);

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Update a single subtask
     */
    @Update
    void update(Subtask subtask);

    /**
     * Update multiple subtasks
     */
    @Update
    void updateAll(List<Subtask> subtasks);

    /**
     * Mark a subtask as completed
     */
    @Query("UPDATE subtasks SET is_completed = :isCompleted, completed_at = :completedAt WHERE id = :subtaskId")
    void setSubtaskCompleted(long subtaskId, boolean isCompleted, Long completedAt);

    /**
     * Update subtask position
     */
    @Query("UPDATE subtasks SET position = :position WHERE id = :subtaskId")
    void updatePosition(long subtaskId, int position);

    /**
     * Update subtask title
     */
    @Query("UPDATE subtasks SET title = :title WHERE id = :subtaskId")
    void updateTitle(long subtaskId, String title);

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete a single subtask
     */
    @Delete
    void delete(Subtask subtask);

    /**
     * Delete a subtask by ID
     */
    @Query("DELETE FROM subtasks WHERE id = :subtaskId")
    void deleteById(long subtaskId);

    /**
     * Delete all subtasks for a specific task
     */
    @Query("DELETE FROM subtasks WHERE task_id = :taskId")
    void deleteAllByTaskId(long taskId);

    /**
     * Delete completed subtasks for a task
     */
    @Query("DELETE FROM subtasks WHERE task_id = :taskId AND is_completed = 1")
    void deleteCompletedByTaskId(long taskId);

    /**
     * Delete all subtasks (dangerous)
     */
    @Query("DELETE FROM subtasks")
    void deleteAllSubtasks();

    // ==================== QUERY OPERATIONS ====================

    /**
     * Get subtask by ID
     */
    @Query("SELECT * FROM subtasks WHERE id = :subtaskId")
    LiveData<Subtask> getSubtaskById(long subtaskId);

    /**
     * Get subtask by ID (sync)
     */
    @Query("SELECT * FROM subtasks WHERE id = :subtaskId")
    Subtask getSubtaskByIdSync(long subtaskId);

    /**
     * Get all subtasks for a task ordered by position
     */
    @Query("SELECT * FROM subtasks WHERE task_id = :taskId ORDER BY position ASC, created_at ASC")
    LiveData<List<Subtask>> getSubtasksByTaskId(long taskId);

    /**
     * Get all subtasks for a task (sync)
     */
    @Query("SELECT * FROM subtasks WHERE task_id = :taskId ORDER BY position ASC, created_at ASC")
    List<Subtask> getSubtasksByTaskIdSync(long taskId);

    /**
     * Get all subtasks (sync for backup)
     */
    @Query("SELECT * FROM subtasks ORDER BY task_id, position ASC")
    List<Subtask> getAllSubtasksSync();

    /**
     * Get completed subtasks for a task
     */
    @Query("SELECT * FROM subtasks WHERE task_id = :taskId AND is_completed = 1 ORDER BY completed_at DESC")
    LiveData<List<Subtask>> getCompletedSubtasksByTaskId(long taskId);

    /**
     * Get pending subtasks for a task
     */
    @Query("SELECT * FROM subtasks WHERE task_id = :taskId AND is_completed = 0 ORDER BY position ASC")
    LiveData<List<Subtask>> getPendingSubtasksByTaskId(long taskId);

    // ==================== COUNT OPERATIONS ====================

    /**
     * Get total subtask count for a task
     */
    @Query("SELECT COUNT(*) FROM subtasks WHERE task_id = :taskId")
    int getSubtaskCountByTaskId(long taskId);

    /**
     * Get completed subtask count for a task
     */
    @Query("SELECT COUNT(*) FROM subtasks WHERE task_id = :taskId AND is_completed = 1")
    int getCompletedSubtaskCountByTaskId(long taskId);

    /**
     * Get pending subtask count for a task
     */
    @Query("SELECT COUNT(*) FROM subtasks WHERE task_id = :taskId AND is_completed = 0")
    int getPendingSubtaskCountByTaskId(long taskId);

    /**
     * Get next position for new subtask
     */
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM subtasks WHERE task_id = :taskId")
    int getNextPositionForTask(long taskId);
}
