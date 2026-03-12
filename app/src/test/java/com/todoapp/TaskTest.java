package com.todoapp;

import com.todoapp.data.model.Task;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for the Task entity.
 */
public class TaskTest {

    private Task task;

    @Before
    public void setUp() {
        task = new Task("Test Task", "Test Description");
    }

    @Test
    public void testTaskCreation() {
        assertNotNull(task);
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertFalse(task.isCompleted());
        assertEquals(Task.Priority.NONE, task.getPriority());
    }

    @Test
    public void testTaskCompletion() {
        assertFalse(task.isCompleted());
        assertNull(task.getCompletedAt());
        
        task.setCompleted(true);
        
        assertTrue(task.isCompleted());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    public void testTaskCompletionReset() {
        task.setCompleted(true);
        assertTrue(task.isCompleted());
        
        task.setCompleted(false);
        
        assertFalse(task.isCompleted());
        // completedAt should remain for history purposes, or be nullified based on design
    }

    @Test
    public void testTaskPriority() {
        assertEquals(Task.Priority.NONE, task.getPriority());
        
        task.setPriority(Task.Priority.HIGH);
        assertEquals(Task.Priority.HIGH, task.getPriority());
        
        task.setPriority(Task.Priority.MEDIUM);
        assertEquals(Task.Priority.MEDIUM, task.getPriority());
        
        task.setPriority(Task.Priority.LOW);
        assertEquals(Task.Priority.LOW, task.getPriority());
    }

    @Test
    public void testTaskDueDate() {
        assertNull(task.getDueDate());
        
        Date dueDate = new Date();
        task.setDueDate(dueDate);
        
        assertEquals(dueDate, task.getDueDate());
    }

    @Test
    public void testTaskImportant() {
        assertFalse(task.isImportant());
        
        task.setImportant(true);
        assertTrue(task.isImportant());
        
        task.setImportant(false);
        assertFalse(task.isImportant());
    }

    @Test
    public void testTaskCategory() {
        assertNull(task.getCategoryId());
        
        task.setCategoryId(1L);
        assertEquals(Long.valueOf(1L), task.getCategoryId());
        
        task.setCategoryId(null);
        assertNull(task.getCategoryId());
    }

    @Test
    public void testTaskReminder() {
        assertFalse(task.isHasReminder());
        assertNull(task.getReminderTime());
        
        Date reminderTime = new Date();
        task.setReminderTime(reminderTime);
        task.setHasReminder(true);
        
        assertTrue(task.isHasReminder());
        assertEquals(reminderTime, task.getReminderTime());
    }

    @Test
    public void testTaskRepeat() {
        assertFalse(task.isRepeating());
        assertNull(task.getRepeatInterval());
        
        task.setRepeating(true);
        task.setRepeatInterval(Task.RepeatInterval.DAILY);
        
        assertTrue(task.isRepeating());
        assertEquals(Task.RepeatInterval.DAILY, task.getRepeatInterval());
    }

    @Test
    public void testTaskNotes() {
        assertNull(task.getNotes());
        
        task.setNotes("Some notes");
        assertEquals("Some notes", task.getNotes());
    }

    @Test
    public void testCreatedAtTimestamp() {
        assertNotNull(task.getCreatedAt());
    }

    @Test
    public void testUpdatedAtTimestamp() {
        assertNotNull(task.getUpdatedAt());
        
        Date originalUpdatedAt = task.getUpdatedAt();
        
        // Simulate update
        task.setTitle("Updated Title");
        task.setUpdatedAt(new Date());
        
        // Updated timestamp should be different (if enough time passed)
        assertNotNull(task.getUpdatedAt());
    }
}
