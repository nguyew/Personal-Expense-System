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
    
}
