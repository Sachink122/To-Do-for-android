package com.todoapp.di;

import android.content.Context;

import com.todoapp.data.dao.CategoryDao;
import com.todoapp.data.dao.SubtaskDao;
import com.todoapp.data.dao.TaskDao;
import com.todoapp.data.database.TodoDatabase;
import com.todoapp.data.repository.TaskRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * DatabaseModule - Hilt module for providing database dependencies.
 * 
 * This module provides the Room database instance and all DAOs
 * as singleton instances for the entire application.
 */
@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    /**
     * Provide executor service for background database operations
     */
    @Provides
    @Singleton
    public ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(4);
    }

    /**
     * Provide the Room database instance
     */
    @Provides
    @Singleton
    public TodoDatabase provideTodoDatabase(@ApplicationContext Context context) {
        return TodoDatabase.getInstance(context);
    }

    /**
     * Provide the Task DAO
     */
    @Provides
    @Singleton
    public TaskDao provideTaskDao(TodoDatabase database) {
        return database.taskDao();
    }

    /**
     * Provide the Subtask DAO
     */
    @Provides
    @Singleton
    public SubtaskDao provideSubtaskDao(TodoDatabase database) {
        return database.subtaskDao();
    }

    /**
     * Provide the Category DAO
     */
    @Provides
    @Singleton
    public CategoryDao provideCategoryDao(TodoDatabase database) {
        return database.categoryDao();
    }

    /**
     * Provide the Task Repository
     */
    @Provides
    @Singleton
    public TaskRepository provideTaskRepository(
            TaskDao taskDao,
            SubtaskDao subtaskDao,
            CategoryDao categoryDao,
            ExecutorService executorService) {
        return new TaskRepository(taskDao, subtaskDao, categoryDao, executorService);
    }
}
