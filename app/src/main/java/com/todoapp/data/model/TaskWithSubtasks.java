package com.todoapp.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskWithSubtasks - POJO class that combines a Task with its related Subtasks.
 * 
 * This is used by Room to perform a one-to-many relationship query,
 * fetching a task along with all its subtasks in a single query.
 */
public class TaskWithSubtasks implements Serializable {

    @Embedded
    private Task task;

    @Relation(
        parentColumn = "id",
        entityColumn = "task_id"
    )
    private List<Subtask> subtasks;

    public TaskWithSubtasks() {
        this.subtasks = new ArrayList<>();
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<Subtask> getSubtasks() {
        return subtasks != null ? subtasks : new ArrayList<>();
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    // Utility methods
    
    /**
     * Get the count of completed subtasks
     */
    public int getCompletedSubtaskCount() {
        if (subtasks == null) return 0;
        int count = 0;
        for (Subtask subtask : subtasks) {
            if (subtask.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the total count of subtasks
     */
    public int getTotalSubtaskCount() {
        return subtasks != null ? subtasks.size() : 0;
    }

    /**
     * Check if all subtasks are completed
     */
    public boolean areAllSubtasksCompleted() {
        if (subtasks == null || subtasks.isEmpty()) return true;
        for (Subtask subtask : subtasks) {
            if (!subtask.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get subtask completion progress (0.0 to 1.0)
     */
    public float getSubtaskProgress() {
        if (subtasks == null || subtasks.isEmpty()) return 0f;
        return (float) getCompletedSubtaskCount() / getTotalSubtaskCount();
    }

    @Override
    public String toString() {
        return "TaskWithSubtasks{" +
                "task=" + task +
                ", subtaskCount=" + getTotalSubtaskCount() +
                ", completedSubtasks=" + getCompletedSubtaskCount() +
                '}';
    }
}
