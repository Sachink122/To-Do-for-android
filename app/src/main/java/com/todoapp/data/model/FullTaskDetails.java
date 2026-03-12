package com.todoapp.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * FullTaskDetails - Complete task information including subtasks and category.
 * 
 * This is a comprehensive POJO combining all task-related data for display
 * in the task detail screen.
 */
public class FullTaskDetails implements Serializable {

    private Task task;
    private List<Subtask> subtasks;
    private Category category;

    public FullTaskDetails() {
        this.subtasks = new ArrayList<>();
    }

    public FullTaskDetails(Task task, List<Subtask> subtasks, Category category) {
        this.task = task;
        this.subtasks = subtasks != null ? subtasks : new ArrayList<>();
        this.category = category;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks != null ? subtasks : new ArrayList<>();
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // Utility methods
    public int getCompletedSubtaskCount() {
        int count = 0;
        for (Subtask subtask : subtasks) {
            if (subtask.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalSubtaskCount() {
        return subtasks.size();
    }

    public boolean hasSubtasks() {
        return !subtasks.isEmpty();
    }

    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public String getCategoryColor() {
        return category != null ? category.getColor() : null;
    }
}
