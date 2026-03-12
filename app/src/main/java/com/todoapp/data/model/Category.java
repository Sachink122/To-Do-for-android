package com.todoapp.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

import java.io.Serializable;

/**
 * Category Entity - Represents a category/label/tag for organizing tasks.
 * 
 * Categories allow users to group and filter tasks.
 * Each category has a name, color, and optional icon.
 */
@Entity(tableName = "categories")
public class Category implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "color")
    private String color; // Hex color code

    @ColumnInfo(name = "icon")
    private String icon; // Icon name or resource identifier

    @ColumnInfo(name = "is_default")
    private boolean isDefault; // Predefined category

    @ColumnInfo(name = "position")
    private int position; // For ordering

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "task_count")
    private int taskCount; // Cached count of tasks in this category

    // Default constructor required by Room
    public Category() {
        this.createdAt = System.currentTimeMillis();
        this.isDefault = false;
        this.position = 0;
        this.taskCount = 0;
    }

    // Convenient constructor
    @Ignore
    public Category(String name, String color) {
        this();
        this.name = name;
        this.color = color;
    }

    // Full constructor
    @Ignore
    public Category(String name, String color, String icon, boolean isDefault) {
        this();
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
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

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    /**
     * Get color as integer value for programmatic use
     */
    public int getColorInt() {
        if (color != null && !color.isEmpty()) {
            try {
                return android.graphics.Color.parseColor(color);
            } catch (IllegalArgumentException e) {
                return android.graphics.Color.GRAY;
            }
        }
        return android.graphics.Color.GRAY;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    // Predefined category colors
    public static class Colors {
        public static final String RED = "#F44336";
        public static final String PINK = "#E91E63";
        public static final String PURPLE = "#9C27B0";
        public static final String DEEP_PURPLE = "#673AB7";
        public static final String INDIGO = "#3F51B5";
        public static final String BLUE = "#2196F3";
        public static final String LIGHT_BLUE = "#03A9F4";
        public static final String CYAN = "#00BCD4";
        public static final String TEAL = "#009688";
        public static final String GREEN = "#4CAF50";
        public static final String LIGHT_GREEN = "#8BC34A";
        public static final String LIME = "#CDDC39";
        public static final String YELLOW = "#FFEB3B";
        public static final String AMBER = "#FFC107";
        public static final String ORANGE = "#FF9800";
        public static final String DEEP_ORANGE = "#FF5722";
        public static final String BROWN = "#795548";
        public static final String GREY = "#9E9E9E";
        public static final String BLUE_GREY = "#607D8B";
    }
}
