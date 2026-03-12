package com.todoapp.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

import java.io.Serializable;

/**
 * Task Entity - Represents a single task in the to-do list.
 * 
 * This is the primary entity for storing tasks in the Room database.
 * Contains all task-related information including title, description,
 * due date, priority, category, and reminder settings.
 */
@Entity(tableName = "tasks")
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    @ColumnInfo(name = "due_date")
    private Long dueDate; // Nullable - timestamp in milliseconds

    @ColumnInfo(name = "due_time")
    private Long dueTime; // Nullable - time in milliseconds from midnight

    @ColumnInfo(name = "priority")
    private int priority; // 0 = None, 1 = Low, 2 = Medium, 3 = High

    @ColumnInfo(name = "category_id")
    private Long categoryId; // Foreign key to Category

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted;

    @ColumnInfo(name = "completed_at")
    private Long completedAt;

    @ColumnInfo(name = "is_archived")
    private boolean isArchived;

    @ColumnInfo(name = "has_reminder")
    private boolean hasReminder;

    @ColumnInfo(name = "reminder_time")
    private Long reminderTime; // Timestamp when reminder should fire

    @ColumnInfo(name = "is_repeating")
    private boolean isRepeating;

    @ColumnInfo(name = "repeat_interval")
    private int repeatInterval; // 0 = None, 1 = Daily, 2 = Weekly, 3 = Monthly

    @ColumnInfo(name = "repeat_end_date")
    private Long repeatEndDate; // Nullable - when to stop repeating

    @ColumnInfo(name = "color")
    private String color; // Hex color code for task card

    @ColumnInfo(name = "position")
    private int position; // For manual ordering

    @ColumnInfo(name = "notes")
    private String notes; // Additional notes

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    private boolean isDeleted;

    @ColumnInfo(name = "deleted_at")
    private Long deletedAt;

    // Default constructor required by Room
    public Task() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.priority = Priority.NONE;
        this.isCompleted = false;
        this.isArchived = false;
        this.isDeleted = false;
        this.hasReminder = false;
        this.isRepeating = false;
        this.repeatInterval = RepeatInterval.NONE;
        this.position = 0;
    }

    // Convenient constructor for quick task creation
    @Ignore
    public Task(String title) {
        this();
        this.title = title;
    }

    // Full constructor
    @Ignore
    public Task(String title, String description, Long dueDate, int priority) {
        this();
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getDueTime() {
        return dueTime;
    }

    public void setDueTime(Long dueTime) {
        this.dueTime = dueTime;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
        this.completedAt = completed ? System.currentTimeMillis() : null;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isHasReminder() {
        return hasReminder;
    }

    public void setHasReminder(boolean hasReminder) {
        this.hasReminder = hasReminder;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(Long reminderTime) {
        this.reminderTime = reminderTime;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getRepeatEndDate() {
        return repeatEndDate;
    }

    public void setRepeatEndDate(Long repeatEndDate) {
        this.repeatEndDate = repeatEndDate;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.updatedAt = System.currentTimeMillis();
    }

    // Utility methods
    public boolean isOverdue() {
        if (dueDate == null || isCompleted) {
            return false;
        }
        return System.currentTimeMillis() > dueDate;
    }

    public boolean isDueToday() {
        if (dueDate == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        long startOfDay = now - (now % (24 * 60 * 60 * 1000));
        long endOfDay = startOfDay + (24 * 60 * 60 * 1000);
        return dueDate >= startOfDay && dueDate < endOfDay;
    }

    public boolean isDueTomorrow() {
        if (dueDate == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        long startOfTomorrow = (now - (now % (24 * 60 * 60 * 1000))) + (24 * 60 * 60 * 1000);
        long endOfTomorrow = startOfTomorrow + (24 * 60 * 60 * 1000);
        return dueDate >= startOfTomorrow && dueDate < endOfTomorrow;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isCompleted=" + isCompleted +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    // Date convenience methods for UI layer
    @Ignore
    public java.util.Date getDueDateAsDate() {
        return dueDate != null ? new java.util.Date(dueDate) : null;
    }

    @Ignore
    public void setDueDateFromDate(java.util.Date date) {
        this.dueDate = date != null ? date.getTime() : null;
        this.updatedAt = System.currentTimeMillis();
    }

    @Ignore
    public java.util.Date getReminderTimeAsDate() {
        return reminderTime != null ? new java.util.Date(reminderTime) : null;
    }

    @Ignore
    public void setReminderTimeFromDate(java.util.Date date) {
        this.reminderTime = date != null ? date.getTime() : null;
        this.updatedAt = System.currentTimeMillis();
    }

    @Ignore
    public java.util.Date getCreatedAtAsDate() {
        return new java.util.Date(createdAt);
    }

    @Ignore
    public java.util.Date getUpdatedAtAsDate() {
        return new java.util.Date(updatedAt);
    }

    @Ignore
    public java.util.Date getCompletedAtAsDate() {
        return completedAt != null ? new java.util.Date(completedAt) : null;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
        this.deletedAt = deleted ? System.currentTimeMillis() : null;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Check if task is important (starred)
    @Ignore
    public boolean isImportant() {
        return this.color != null && this.color.equals("#FFD700"); // Gold color = important
    }

    @Ignore
    public void setImportant(boolean important) {
        this.color = important ? "#FFD700" : null;
        this.updatedAt = System.currentTimeMillis();
    }

    // Priority constants
    public static class Priority {
        public static final int NONE = 0;
        public static final int LOW = 1;
        public static final int MEDIUM = 2;
        public static final int HIGH = 3;
    }

    // Repeat interval constants
    public static class RepeatInterval {
        public static final int NONE = 0;
        public static final int DAILY = 1;
        public static final int WEEKLY = 2;
        public static final int MONTHLY = 3;
        public static final int YEARLY = 4;
    }
}
