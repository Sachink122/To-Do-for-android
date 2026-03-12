package com.todoapp.data.model;

import java.io.Serializable;
import java.util.List;

/**
 * BackupData - Container for all app data to be exported/imported.
 * 
 * Used for JSON serialization when creating or restoring backups.
 */
public class BackupData implements Serializable {

    private int version;
    private long timestamp;
    private String appVersion;
    private List<Task> tasks;
    private List<Subtask> subtasks;
    private List<Category> categories;

    public BackupData() {
        this.version = 1;
        this.timestamp = System.currentTimeMillis();
    }

    public BackupData(List<Task> tasks, List<Subtask> subtasks, List<Category> categories, String appVersion) {
        this();
        this.tasks = tasks;
        this.subtasks = subtasks;
        this.categories = categories;
        this.appVersion = appVersion;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public int getTotalTasks() {
        return tasks != null ? tasks.size() : 0;
    }

    public int getTotalCategories() {
        return categories != null ? categories.size() : 0;
    }
}
