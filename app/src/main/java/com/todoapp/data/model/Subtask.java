package com.todoapp.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;

import java.io.Serializable;

/**
 * Subtask Entity - Represents a subtask/checklist item within a parent task.
 * 
 * Each subtask belongs to a parent Task and can be individually marked as complete.
 * Used for breaking down larger tasks into smaller actionable items.
 */
@Entity(
    tableName = "subtasks",
    foreignKeys = @ForeignKey(
        entity = Task.class,
        parentColumns = "id",
        childColumns = "task_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index(value = "task_id")
)
public class Subtask implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "task_id")
    private long taskId; // Foreign key to parent Task

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    @ColumnInfo(name = "completed_at")
    private Long completedAt;

    @ColumnInfo(name = "position")
    private int position; // For ordering subtasks

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Default constructor required by Room
    public Subtask() {
        this.createdAt = System.currentTimeMillis();
        this.isCompleted = false;
        this.position = 0;
    }

    // Convenient constructor
    @Ignore
    public Subtask(long taskId, String title) {
        this();
        this.taskId = taskId;
        this.title = title;
    }

    // Constructor with position
    @Ignore
    public Subtask(long taskId, String title, int position) {
        this();
        this.taskId = taskId;
        this.title = title;
        this.position = position;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
        this.completedAt = completed ? System.currentTimeMillis() : null;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", title='" + title + '\'' +
                ", isCompleted=" + isCompleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subtask subtask = (Subtask) o;
        return id == subtask.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
