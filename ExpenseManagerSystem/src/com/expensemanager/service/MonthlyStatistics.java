package com.expensemanager.service;

import com.expensemanager.util.CurrencyUtils;
import com.expensemanager.util.DateUtils;
import java.util.Map;

/**
 * Data model for monthly financial statistics
 */
public class MonthlyStatistics {
    private int month;
    private int year;
    private double totalIncome;
    private double totalExpense;
    private double netAmount;
    private int incomeCount;
    private int expenseCount;
    private Map<String, Double> expenseByCategory;
    
    public MonthlyStatistics() {
        this.totalIncome = 0.0;
        this.totalExpense = 0.0;
        this.netAmount = 0.0;
        this.incomeCount = 0;
        this.expenseCount = 0;
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
    
    public double getTotalIncome() {
        return totalIncome;
    }
    
    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
        updateNetAmount();
    }
    
    public double getTotalExpense() {
        return totalExpense;
    }
    
    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
        updateNetAmount();
    }
    
    public double getNetAmount() {
        return netAmount;
    }
    
    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }
    
    public int getIncomeCount() {
        return incomeCount;
    }
    
    public void setIncomeCount(int incomeCount) {
        this.incomeCount = incomeCount;
    }
    
    public int getExpenseCount() {
        return expenseCount;
    }
    
    public void setExpenseCount(int expenseCount) {
        this.expenseCount = expenseCount;
    }
    
    public Map<String, Double> getExpenseByCategory() {
        return expenseByCategory;
    }
    
    public void setExpenseByCategory(Map<String, Double> expenseByCategory) {
        this.expenseByCategory = expenseByCategory;
    }
    
    // Helper methods
    private void updateNetAmount() {
        this.netAmount = totalIncome - totalExpense;
    }
    
    public String getMonthYearString() {
        return DateUtils.getMonthName(month) + " " + year;
    }
    
    public String getFormattedTotalIncome() {
        return CurrencyUtils.formatCurrency(totalIncome);
    }
    
    public String getFormattedTotalExpense() {
        return CurrencyUtils.formatCurrency(totalExpense);
    }
    
    public String getFormattedNetAmount() {
        if (netAmount >= 0) {
            return "+" + CurrencyUtils.formatCurrency(netAmount);
        } else {
            return "-" + CurrencyUtils.formatCurrency(Math.abs(netAmount));
        }
    }
    
    public boolean isNetPositive() {
        return netAmount >= 0;
    }
    
    public double getSavingsRate() {
        if (totalIncome == 0) return 0;
        return (netAmount / totalIncome) * 100;
    }
    
    public String getFormattedSavingsRate() {
        return String.format("%.1f%%", getSavingsRate());
    }
    
    public int getTotalTransactions() {
        return incomeCount + expenseCount;
    }
    
    public double getAverageIncome() {
        if (incomeCount == 0) return 0;
        return totalIncome / incomeCount;
    }
    
    public double getAverageExpense() {
        if (expenseCount == 0) return 0;
        return totalExpense / expenseCount;
    }
    
    public String getFormattedAverageIncome() {
        return CurrencyUtils.formatCurrency(getAverageIncome());
    }
    
    public String getFormattedAverageExpense() {
        return CurrencyUtils.formatCurrency(getAverageExpense());
    }
    
    @Override
    public String toString() {
        return "MonthlyStatistics{" +
                "month=" + month +
                ", year=" + year +
                ", totalIncome=" + totalIncome +
                ", totalExpense=" + totalExpense +
                ", netAmount=" + netAmount +
                ", incomeCount=" + incomeCount +
                ", expenseCount=" + expenseCount +
                ", savingsRate=" + String.format("%.1f%%", getSavingsRate()) +
                '}';
    }
}