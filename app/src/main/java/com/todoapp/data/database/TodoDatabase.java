package com.todoapp.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.todoapp.data.dao.CategoryDao;
import com.todoapp.data.dao.SubtaskDao;
import com.todoapp.data.dao.TaskDao;
import com.todoapp.data.model.Category;
import com.todoapp.data.model.Subtask;
import com.todoapp.data.model.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TodoDatabase - Room Database for the To-Do application.
 * 
 * This is the main database class that provides access to all DAOs.
 * It includes schema versioning, migrations, and pre-population of default data.
 */
@Database(
    entities = {Task.class, Subtask.class, Category.class},
    version = 2,
    exportSchema = true
)
public abstract class TodoDatabase extends RoomDatabase {

    // Database name
    private static final String DATABASE_NAME = "todo_database";

    // Singleton instance
    private static volatile TodoDatabase INSTANCE;

    // Background executor for database operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Abstract DAO methods
    public abstract TaskDao taskDao();
    public abstract SubtaskDao subtaskDao();
    public abstract CategoryDao categoryDao();

    /**
     * Get the singleton instance of the database.
     * Creates the database if it doesn't exist.
     */
    public static TodoDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (TodoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TodoDatabase.class,
                            DATABASE_NAME
                    )
                    .addCallback(roomCallback)
                    .addMigrations(MIGRATION_1_2) // Ready for future migrations
                    .fallbackToDestructiveMigration() // For development; remove in production
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback for database creation and opening.
     * Pre-populates the database with default categories.
     */
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Pre-populate database with default categories
            databaseWriteExecutor.execute(() -> {
                if (INSTANCE != null) {
                    CategoryDao categoryDao = INSTANCE.categoryDao();
                    
                    // Insert default categories
                    categoryDao.insert(new Category("Personal", Category.Colors.BLUE, "person", true));
                    categoryDao.insert(new Category("Work", Category.Colors.ORANGE, "work", true));
                    categoryDao.insert(new Category("Shopping", Category.Colors.GREEN, "shopping_cart", true));
                    categoryDao.insert(new Category("Health", Category.Colors.RED, "favorite", true));
                    categoryDao.insert(new Category("Finance", Category.Colors.PURPLE, "attach_money", true));
                    categoryDao.insert(new Category("Education", Category.Colors.CYAN, "school", true));
                    categoryDao.insert(new Category("Home", Category.Colors.AMBER, "home", true));
                    categoryDao.insert(new Category("Travel", Category.Colors.TEAL, "flight", true));
                }
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Can perform any initialization on database open
        }
    };

    /**
     * Migration from version 1 to 2 - Adding soft delete columns
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add is_deleted and deleted_at columns for soft delete feature
            database.execSQL("ALTER TABLE tasks ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tasks ADD COLUMN deleted_at INTEGER");
        }
    };

    /**
     * Close the database instance.
     * Call this when the application is being destroyed.
     */
    public static void closeDatabase() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }

    /**
     * Clear all tables (for testing or reset functionality)
     */
    public void clearAllTables() {
        databaseWriteExecutor.execute(() -> {
            taskDao().deleteAllTasks();
            subtaskDao().deleteAllSubtasks();
            categoryDao().deleteAllCategories();
        });
    }
}
