package com.todoapp.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.todoapp.data.model.Category;
import com.todoapp.data.repository.TaskRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * CategoryViewModel - ViewModel for category management.
 * 
 * Handles category CRUD operations and provides category data
 * for selection dialogs and the category management screen.
 */
@HiltViewModel
public class CategoryViewModel extends ViewModel {

    private final TaskRepository repository;

    // Category data
    private final LiveData<List<Category>> allCategories;
    private final LiveData<List<Category>> categoriesWithCounts;

    // UI state
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Edit mode
    private final MutableLiveData<Category> categoryToEdit = new MutableLiveData<>();

    @Inject
    public CategoryViewModel(TaskRepository repository) {
        this.repository = repository;
        this.allCategories = repository.getAllCategories();
        this.categoriesWithCounts = repository.getCategoriesWithTaskCounts();
    }

    // ==================== CATEGORY OPERATIONS ====================

    /**
     * Create a new category
     */
    public void createCategory(String name, String color) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue("Category name is required");
            return;
        }

        isSaving.setValue(true);
        Category category = new Category(name.trim(), color);
        repository.insertCategory(category);
        isSaving.setValue(false);
        saveSuccess.setValue(true);
    }

    /**
     * Create a new category with icon
     */
    public void createCategory(String name, String color, String icon) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue("Category name is required");
            return;
        }

        isSaving.setValue(true);
        Category category = new Category(name.trim(), color, icon, false);
        repository.insertCategory(category);
        isSaving.setValue(false);
        saveSuccess.setValue(true);
    }

    /**
     * Update an existing category
     */
    public void updateCategory(Category category) {
        if (category == null) {
            return;
        }

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            errorMessage.setValue("Category name is required");
            return;
        }

        isSaving.setValue(true);
        repository.updateCategory(category);
        isSaving.setValue(false);
        saveSuccess.setValue(true);
    }

    /**
     * Delete a category
     */
    public void deleteCategory(Category category) {
        if (category == null) {
            return;
        }

        // Don't delete default categories
        if (category.isDefault()) {
            errorMessage.setValue("Cannot delete default categories");
            return;
        }

        repository.deleteCategory(category);
    }

    /**
     * Delete a category by ID
     */
    public void deleteCategoryById(long categoryId) {
        repository.deleteCategoryById(categoryId);
    }

    /**
     * Set the category being edited
     */
    public void setCategoryToEdit(Category category) {
        categoryToEdit.setValue(category);
    }

    /**
     * Clear the category being edited
     */
    public void clearCategoryToEdit() {
        categoryToEdit.setValue(null);
    }

    // ==================== GETTERS ====================

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<Category>> getCategoriesWithCounts() {
        return categoriesWithCounts;
    }

    public LiveData<Boolean> getIsSaving() {
        return isSaving;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Category> getCategoryToEdit() {
        return categoryToEdit;
    }

    /**
     * Reset save success state
     */
    public void resetSaveSuccess() {
        saveSuccess.setValue(null);
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
}
