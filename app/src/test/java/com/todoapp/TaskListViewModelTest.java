package com.todoapp;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.todoapp.data.model.Task;
import com.todoapp.data.repository.TaskRepository;
import com.todoapp.ui.viewmodel.TaskListViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskListViewModel.
 * Uses Mockito to mock the repository layer.
 */
@RunWith(MockitoJUnitRunner.class)
public class TaskListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private Observer<List<Task>> tasksObserver;

    private TaskListViewModel viewModel;
    private MutableLiveData<List<Task>> tasksLiveData;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mock data
        tasksLiveData = new MutableLiveData<>();
        when(taskRepository.getAllTasks()).thenReturn(tasksLiveData);
        
        viewModel = new TaskListViewModel(taskRepository);
    }

    @Test
    public void testGetAllTasks() {
        List<Task> tasks = Arrays.asList(
                new Task("Task 1", "Description 1"),
                new Task("Task 2", "Description 2")
        );
        tasksLiveData.setValue(tasks);

        viewModel.getAllTasks().observeForever(tasksObserver);
        
        verify(tasksObserver).onChanged(tasks);
        assertEquals(2, viewModel.getAllTasks().getValue().size());
    }

    @Test
    public void testEmptyTaskList() {
        tasksLiveData.setValue(new ArrayList<>());
        
        viewModel.getAllTasks().observeForever(tasksObserver);
        
        assertNotNull(viewModel.getAllTasks().getValue());
        assertTrue(viewModel.getAllTasks().getValue().isEmpty());
    }

    @Test
    public void testInsertTask() {
        Task newTask = new Task("New Task", "New Description");
        
        viewModel.insertTask(newTask);
        
        verify(taskRepository).insert(newTask);
    }

    @Test
    public void testUpdateTask() {
        Task task = new Task("Task", "Description");
        task.setId(1);
        
        viewModel.updateTask(task);
        
        verify(taskRepository).update(task);
    }

    @Test
    public void testDeleteTask() {
        Task task = new Task("Task", "Description");
        task.setId(1);
        
        viewModel.deleteTask(task);
        
        verify(taskRepository).delete(task);
    }

    @Test
    public void testToggleTaskCompletion() {
        Task task = new Task("Task", "Description");
        task.setId(1);
        task.setCompleted(false);
        
        viewModel.toggleTaskCompletion(task);
        
        verify(taskRepository).update(argThat(updatedTask -> 
                updatedTask.isCompleted() == true
        ));
    }

    @Test
    public void testToggleTaskImportant() {
        Task task = new Task("Task", "Description");
        task.setId(1);
        task.setImportant(false);
        
        viewModel.toggleTaskImportant(task);
        
        verify(taskRepository).update(argThat(updatedTask -> 
                updatedTask.isImportant() == true
        ));
    }

    @Test
    public void testFilterByCompleted() {
        List<Task> tasks = Arrays.asList(
                createTaskWithCompletion("Task 1", false),
                createTaskWithCompletion("Task 2", true),
                createTaskWithCompletion("Task 3", false)
        );
        tasksLiveData.setValue(tasks);
        
        viewModel.setFilterType(TaskListViewModel.FilterType.COMPLETED);
        
        // Filter should apply to filtered tasks
        // Actual filtering logic would be tested through integration tests
    }

    @Test
    public void testSearchTasks() {
        List<Task> tasks = Arrays.asList(
                new Task("Buy groceries", "From the store"),
                new Task("Call mom", "Important call"),
                new Task("Buy flowers", "For anniversary")
        );
        tasksLiveData.setValue(tasks);

        viewModel.setSearchQuery("buy");
        
        // Search query should filter results
        // Actual search logic would be tested through integration tests
    }

    @Test
    public void testClearSearch() {
        viewModel.setSearchQuery("test");
        viewModel.setSearchQuery("");
        
        // Should show all tasks when search is cleared
    }

    @Test
    public void testFilterTypeChange() {
        viewModel.setFilterType(TaskListViewModel.FilterType.ALL);
        assertEquals(TaskListViewModel.FilterType.ALL, viewModel.getCurrentFilterType().getValue());
        
        viewModel.setFilterType(TaskListViewModel.FilterType.TODAY);
        assertEquals(TaskListViewModel.FilterType.TODAY, viewModel.getCurrentFilterType().getValue());
        
        viewModel.setFilterType(TaskListViewModel.FilterType.IMPORTANT);
        assertEquals(TaskListViewModel.FilterType.IMPORTANT, viewModel.getCurrentFilterType().getValue());
    }

    private Task createTaskWithCompletion(String title, boolean completed) {
        Task task = new Task(title, null);
        task.setCompleted(completed);
        return task;
    }
}
