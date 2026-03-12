package com.todoapp;

import com.todoapp.util.DateUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for DateUtils utility class.
 */
public class DateUtilsTest {

    @Test
    public void testFormatDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, Calendar.JANUARY, 15, 0, 0, 0);
        Date date = calendar.getTime();
        
        String formatted = DateUtils.formatDate(date);
        assertNotNull(formatted);
        assertTrue(formatted.contains("Jan") || formatted.contains("15") || formatted.contains("2024"));
    }

    @Test
    public void testFormatTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 30);
        Date date = calendar.getTime();
        
        String formatted = DateUtils.formatTime(date);
        assertNotNull(formatted);
        // Could be "2:30 PM" or "14:30" depending on locale
        assertTrue(formatted.contains("30"));
    }

    @Test
    public void testFormatDateTime() {
        Date date = new Date();
        String formatted = DateUtils.formatDateTime(date);
        assertNotNull(formatted);
    }

    @Test
    public void testIsToday() {
        Date today = new Date();
        assertTrue(DateUtils.isToday(today));
        
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        assertFalse(DateUtils.isToday(yesterday.getTime()));
        
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        assertFalse(DateUtils.isToday(tomorrow.getTime()));
    }

    @Test
    public void testIsTomorrow() {
        Date today = new Date();
        assertFalse(DateUtils.isTomorrow(today));
        
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        assertTrue(DateUtils.isTomorrow(tomorrow.getTime()));
    }

    @Test
    public void testIsOverdue() {
        // Yesterday should be overdue
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        assertTrue(DateUtils.isOverdue(yesterday.getTime()));
        
        // Tomorrow should not be overdue
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        assertFalse(DateUtils.isOverdue(tomorrow.getTime()));
    }

    @Test
    public void testIsDueThisWeek() {
        Date today = new Date();
        assertTrue(DateUtils.isDueThisWeek(today));
        
        Calendar nextWeek = Calendar.getInstance();
        nextWeek.add(Calendar.WEEK_OF_YEAR, 1);
        nextWeek.add(Calendar.DAY_OF_YEAR, 1);
        assertFalse(DateUtils.isDueThisWeek(nextWeek.getTime()));
    }

    @Test
    public void testFormatRelativeDateForToday() {
        Date today = new Date();
        String relative = DateUtils.formatRelativeDate(today);
        assertNotNull(relative);
        // Should contain "Today" or similar indicator
    }

    @Test
    public void testGetStartOfDay() {
        Date now = new Date();
        Date startOfDay = DateUtils.getStartOfDay(now);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(startOfDay);
        
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testGetEndOfDay() {
        Date now = new Date();
        Date endOfDay = DateUtils.getEndOfDay(now);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(endOfDay);
        
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, cal.get(Calendar.MINUTE));
        assertEquals(59, cal.get(Calendar.SECOND));
    }

    @Test
    public void testGetStartOfWeek() {
        Date now = new Date();
        Date startOfWeek = DateUtils.getStartOfWeek(now);
        
        assertNotNull(startOfWeek);
        assertTrue(startOfWeek.before(now) || DateUtils.isToday(startOfWeek));
    }

    @Test
    public void testGetEndOfWeek() {
        Date now = new Date();
        Date endOfWeek = DateUtils.getEndOfWeek(now);
        
        assertNotNull(endOfWeek);
        assertTrue(endOfWeek.after(now) || endOfWeek.equals(now));
    }

    @Test
    public void testAddDays() {
        Date now = new Date();
        Date in3Days = DateUtils.addDays(now, 3);
        
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);
        
        Calendar futureCal = Calendar.getInstance();
        futureCal.setTime(in3Days);
        
        long diffMillis = futureCal.getTimeInMillis() - nowCal.getTimeInMillis();
        long diffDays = diffMillis / (24 * 60 * 60 * 1000);
        
        assertEquals(3, diffDays);
    }

    @Test
    public void testNullDate() {
        assertNull(DateUtils.formatDate(null));
        assertNull(DateUtils.formatTime(null));
        assertNull(DateUtils.formatDateTime(null));
        assertFalse(DateUtils.isToday(null));
        assertFalse(DateUtils.isTomorrow(null));
        assertFalse(DateUtils.isOverdue(null));
    }
}
