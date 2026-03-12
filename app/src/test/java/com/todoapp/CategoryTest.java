package com.todoapp;

import com.todoapp.data.model.Category;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the Category entity.
 */
public class CategoryTest {

    private Category category;

    @Before
    public void setUp() {
        category = new Category("Work", "#2196F3");
    }

    @Test
    public void testCategoryCreation() {
        assertNotNull(category);
        assertEquals("Work", category.getName());
        assertEquals("#2196F3", category.getColor());
    }

    @Test
    public void testCategoryName() {
        category.setName("Personal");
        assertEquals("Personal", category.getName());
    }

    @Test
    public void testCategoryColor() {
        category.setColor("#4CAF50");
        assertEquals("#4CAF50", category.getColor());
    }

    @Test
    public void testCategoryColorInt() {
        // Test color parsing
        int colorInt = category.getColorInt();
        assertNotEquals(0, colorInt);
    }

    @Test
    public void testCategoryIcon() {
        assertNull(category.getIconName());
        
        category.setIconName("work");
        assertEquals("work", category.getIconName());
    }

    @Test
    public void testCategoryPosition() {
        category.setPosition(5);
        assertEquals(5, category.getPosition());
    }

    @Test
    public void testDefaultCategory() {
        Category defaultCat = new Category("Inbox", "#9E9E9E");
        defaultCat.setDefault(true);
        
        assertTrue(defaultCat.isDefault());
    }

    @Test
    public void testTimestamps() {
        assertNotNull(category.getCreatedAt());
    }
}
