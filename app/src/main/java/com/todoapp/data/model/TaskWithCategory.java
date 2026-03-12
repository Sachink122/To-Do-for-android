package com.todoapp.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.List;

/**
 * TaskWithCategory - POJO class that combines a Task with its Category.
 * 
 * Used for fetching tasks with their category information in a single query.
 */
public class TaskWithCategory implements Serializable {

    @Embedded
    private Task task;

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    private Category category;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "TaskWithCategory{" +
                "task=" + task +
                ", category=" + category +
                '}';
    }
}
