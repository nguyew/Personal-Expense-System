package com.expensemanager.service;

import com.expensemanager.util.CurrencyUtils;

/**
 * Data model for monthly trend analysis
 */
public class MonthlyTrend {
    private int month;
    private int year;
    private String monthName;
    private double income;
    private double expense;
    private double net;
    
    public MonthlyTrend() {
        this.income = 0.0;
        this.expense = 0.0;
        this.net = 0.0;
    }
    
    public MonthlyTrend(int month, int year, String monthName, double income, double expense) {
        this.month = month;
        this.year = year;
        this.monthName = monthName;
        this.income = income;
        this.expense = expense;
        this.net = income - expense;
    }
    
    // Getters and Setters
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public String getMonthName() {
        return monthName;
    }
    
    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }
    
    public double getIncome() {
        return income;
    }
    
    public void setIncome(double income) {
        this.income = income;
        updateNet();
    }
    
    public double getExpense() {
        return expense;
    }
    
    public void setExpense(double expense) {
        this.expense = expense;
        updateNet();
    }
    
    public double getNet() {
        return net;
    }
    
    public void setNet(double net) {
        this.net = net;
    }
    
    // Helper methods
    private void updateNet() {
        this.net = income - expense;
    }
    
    public String getMonthYearString() {
        return monthName + " " + year;
    }
    
    public String getShortMonthYear() {
        return monthName.substring(0, 3) + " " + String.valueOf(year).substring(2);
    }
    
    public String getFormattedIncome() {
        return CurrencyUtils.formatCurrency(income);
    }
    
    public String getFormattedExpense() {
        return CurrencyUtils.formatCurrency(expense);
    }
    
    public String getFormattedNet() {
        if (net >= 0) {
            return "+" + CurrencyUtils.formatCurrency(net);
        } else {
            return "-" + CurrencyUtils.formatCurrency(Math.abs(net));
        }
    }
    
    public boolean isNetPositive() {
        return net >= 0;
    }
    
    public double getSavingsRate() {
        if (income == 0) return 0;
        return (net / income) * 100;
    }
    
    public String getFormattedSavingsRate() {
        return String.format("%.1f%%", getSavingsRate());
    }
    
    public String getTrendIndicator() {
        if (net > 0) return "↗️"; // Positive trend
        else if (net < 0) return "↘️"; // Negative trend
        else return "➡️"; // Neutral
    }
    
    @Override
    public String toString() {
        return "MonthlyTrend{" +
                "month=" + month +
                ", year=" + year +
                ", monthName='" + monthName + '\'' +
                ", income=" + income +
                ", expense=" + expense +
                ", net=" + net +
                ", savingsRate=" + String.format("%.1f%%", getSavingsRate()) +
                '}';
    }
}