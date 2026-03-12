package com.todoapp.data.model;

/**
 * TaskStats - Statistics for completed tasks analytics.
 * 
 * Used for displaying task completion stats in the analytics section.
 */
public class TaskStats {

    private int totalTasks;
    private int completedTasks;
    private int pendingTasks;
    private int overdueTasks;
    private int completedToday;
    private int completedThisWeek;
    private int completedThisMonth;
    private int tasksCreatedToday;
    private int tasksCreatedThisWeek;

    public TaskStats() {
    }

    // Getters and Setters
    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public int getOverdueTasks() {
        return overdueTasks;
    }

    public void setOverdueTasks(int overdueTasks) {
        this.overdueTasks = overdueTasks;
    }

    public int getCompletedToday() {
        return completedToday;
    }

    public void setCompletedToday(int completedToday) {
        this.completedToday = completedToday;
    }

    public int getCompletedThisWeek() {
        return completedThisWeek;
    }

    public void setCompletedThisWeek(int completedThisWeek) {
        this.completedThisWeek = completedThisWeek;
    }

    public int getCompletedThisMonth() {
        return completedThisMonth;
    }

    public void setCompletedThisMonth(int completedThisMonth) {
        this.completedThisMonth = completedThisMonth;
    }

    public int getTasksCreatedToday() {
        return tasksCreatedToday;
    }

    public void setTasksCreatedToday(int tasksCreatedToday) {
        this.tasksCreatedToday = tasksCreatedToday;
    }

    public int getTasksCreatedThisWeek() {
        return tasksCreatedThisWeek;
    }

    public void setTasksCreatedThisWeek(int tasksCreatedThisWeek) {
        this.tasksCreatedThisWeek = tasksCreatedThisWeek;
    }

    // Calculated properties
    public float getCompletionRate() {
        if (totalTasks == 0) return 0f;
        return (float) completedTasks / totalTasks * 100;
    }

    public float getPendingRate() {
        if (totalTasks == 0) return 0f;
        return (float) pendingTasks / totalTasks * 100;
    }

    // Alias methods for convenience
    public int getPendingCount() {
        return pendingTasks;
    }

    public int getCompletedCount() {
        return completedTasks;
    }

    public int getOverdueCount() {
        return overdueTasks;
    }
}
