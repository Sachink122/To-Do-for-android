package com.todoapp;

import com.todoapp.data.model.Subtask;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the Subtask entity.
 */
public class SubtaskTest {

    private Subtask subtask;

    @Before
    public void setUp() {
        subtask = new Subtask("Buy groceries", 1L);
    }

    @Test
    public void testSubtaskCreation() {
        assertNotNull(subtask);
        assertEquals("Buy groceries", subtask.getTitle());
        assertEquals(1L, subtask.getTaskId());
        assertFalse(subtask.isCompleted());
    }

    @Test
    public void testSubtaskTitle() {
        subtask.setTitle("Updated title");
        assertEquals("Updated title", subtask.getTitle());
    }

    @Test
    public void testSubtaskCompletion() {
        assertFalse(subtask.isCompleted());
        
        subtask.setCompleted(true);
        assertTrue(subtask.isCompleted());
        
        subtask.setCompleted(false);
        assertFalse(subtask.isCompleted());
    }

    @Test
    public void testSubtaskPosition() {
        subtask.setPosition(0);
        assertEquals(0, subtask.getPosition());
        
        subtask.setPosition(5);
        assertEquals(5, subtask.getPosition());
    }

    @Test
    public void testSubtaskTaskId() {
        assertEquals(1L, subtask.getTaskId());
        
        subtask.setTaskId(2L);
        assertEquals(2L, subtask.getTaskId());
    }

    @Test
    public void testSubtaskTimestamps() {
        assertNotNull(subtask.getCreatedAt());
    }
}
