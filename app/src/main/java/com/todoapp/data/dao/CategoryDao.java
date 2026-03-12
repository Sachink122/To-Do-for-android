package com.todoapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.todoapp.data.model.Category;

import java.util.List;

/**
 * CategoryDao - Data Access Object for Category entity.
 * 
 * Provides all database operations for categories/labels/tags
 * used to organize tasks.
 */
@Dao
public interface CategoryDao {

    // ==================== INSERT OPERATIONS ====================

    /**
     * Insert a single category
     * @return The row ID of the inserted category
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Category category);

    /**
     * Insert multiple categories
     * @return Array of row IDs for inserted categories
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<Category> categories);

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Update a single category
     */
    @Update
    void update(Category category);

    /**
     * Update category name
     */
    @Query("UPDATE categories SET name = :name WHERE id = :categoryId")
    void updateName(long categoryId, String name);

    /**
     * Update category color
     */
    @Query("UPDATE categories SET color = :color WHERE id = :categoryId")
    void updateColor(long categoryId, String color);

    /**
     * Update category position
     */
    @Query("UPDATE categories SET position = :position WHERE id = :categoryId")
    void updatePosition(long categoryId, int position);

    /**
     * Update task count for a category
     */
    @Query("UPDATE categories SET task_count = :taskCount WHERE id = :categoryId")
    void updateTaskCount(long categoryId, int taskCount);

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete a single category
     */
    @Delete
    void delete(Category category);

    /**
     * Delete a category by ID
     */
    @Query("DELETE FROM categories WHERE id = :categoryId")
    void deleteById(long categoryId);

    /**
     * Delete all custom (non-default) categories
     */
    @Query("DELETE FROM categories WHERE is_default = 0")
    void deleteAllCustomCategories();

    /**
     * Delete all categories (dangerous)
     */
    @Query("DELETE FROM categories")
    void deleteAllCategories();

    // ==================== QUERY OPERATIONS ====================

    /**
     * Get category by ID
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    LiveData<Category> getCategoryById(long categoryId);

    /**
     * Get category by ID (sync)
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    Category getCategoryByIdSync(long categoryId);

    /**
     * Get category by name
     */
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    Category getCategoryByName(String name);

    /**
     * Get all categories ordered by position
     */
    @Query("SELECT * FROM categories ORDER BY position ASC, created_at ASC")
    LiveData<List<Category>> getAllCategories();

    /**
     * Get all categories (sync)
     */
    @Query("SELECT * FROM categories ORDER BY position ASC, created_at ASC")
    List<Category> getAllCategoriesSync();

    /**
     * Get default categories
     */
    @Query("SELECT * FROM categories WHERE is_default = 1 ORDER BY position ASC")
    LiveData<List<Category>> getDefaultCategories();

    /**
     * Get custom categories (user-created)
     */
    @Query("SELECT * FROM categories WHERE is_default = 0 ORDER BY position ASC, created_at DESC")
    LiveData<List<Category>> getCustomCategories();

    /**
     * Get categories with task counts
     * Note: Uses a subquery to count tasks in each category
     */
    @Query("SELECT c.*, (SELECT COUNT(*) FROM tasks t WHERE t.category_id = c.id AND t.is_archived = 0) as task_count FROM categories c ORDER BY c.position ASC")
    LiveData<List<Category>> getCategoriesWithTaskCounts();

    /**
     * Search categories by name
     */
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%' ORDER BY position ASC")
    LiveData<List<Category>> searchCategories(String query);

    // ==================== COUNT OPERATIONS ====================

    /**
     * Get total category count
     */
    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();

    /**
     * Get next position for new category
     */
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM categories")
    int getNextPosition();

    /**
     * Check if a category name already exists
     */
    @Query("SELECT COUNT(*) FROM categories WHERE name = :name")
    int countCategoriesByName(String name);

    /**
     * Check if a category name exists (excluding a specific category)
     */
    @Query("SELECT COUNT(*) FROM categories WHERE name = :name AND id != :excludeId")
    int countCategoriesByNameExcluding(String name, long excludeId);
}
