package com.expensemanager.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    
    private static final SimpleDateFormat defaultFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    private static final SimpleDateFormat datetimeFormatter = new SimpleDateFormat(DATETIME_FORMAT);
    private static final SimpleDateFormat sqlFormatter = new SimpleDateFormat(SQL_DATE_FORMAT);
    
    // Format date to string
    public static String formatDate (Date date) {
        if (date == null) return "";
        return defaultFormatter.format(date);
    }
    
    public static String formatDateTime (Date date) {
        if (date == null) return "";
        return datetimeFormatter.format(date);
    }
    
    public static String formatSqlDate (Date date) {
        if (date == null) return "";
        return sqlFormatter.format(date);
    }
    
    // Parse String to date
    public static Date parseDate (String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return defaultFormatter.parse(dateStr);
    }
    
    public static Date parseDateTime (String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return datetimeFormatter.parse(dateStr);
    }
    
    public static Date parseSqlDate (String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return sqlFormatter.parse(dateStr);
    }
    
    // Date calculations
    public static Date getFirstDayOfMonth (Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }
    
    public static Date getLastDayOfMonth (Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }
    
    public static Date getFirstDayOfYear (Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }
    
    public static Date getLastDayOfYear (Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
        return cal.getTime();
    }
    
    public static Date addDays (Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
    
    public static Date addMonths(Date date, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }

    public static Date addYears(Date date, int years) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, years);
        return cal.getTime();
    }
    
    // Get current time periods
    public static Date getCurrentMonthStart () {
        return getFirstDayOfMonth(new Date());
    }
    
    public static Date getCurrentMonthEnd () {
        return getLastDayOfMonth(new Date());
    }
    
    public static Date getCurrentYearStart () {
        return getFirstDayOfYear(new Date());
    }
    
    public static Date getCurrentYearEnd () {
        return getLastDayOfYear(new Date());
    }
    
    // Get specific month/year
    public static Date getMonthStart (int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month -1, 1); // Month is 0-based
        return cal.getTime();
    }
    
    public static Date getMonthEnd (int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1); // Month is 0-based
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }
    
    // Compare dates
    public static boolean isSameDay (Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    
    public static boolean isSameMonth (Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }
    
    public static int getCurrentMonth () {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1; // Month is 0-based
    }
    
    public static int getCurrentYear () {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }
    
    public static int getMonth (Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }
    
    public static int getYear (Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }
    
    // Validation methods
    public static boolean isValidDateRange (Date startDate, Date endDate) {
        return startDate != null && endDate != null && !startDate.after(endDate);
    }
    
    public static String getMonthName (int month) {
        String[] months = {
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
            "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        };
        
        if (month >= 1 && month <= 12) {
            return months[month - 1];
        }
        return "Không xác định";
    }
    
}
