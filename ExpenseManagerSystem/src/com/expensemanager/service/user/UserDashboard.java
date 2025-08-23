package com.expensemanager.service.user;

import com.expensemanager.model.*;
import java.util.List;

public class UserDashboard {
    private User user;
    private double currentMonthIncome;
    private double currentMonthExpense;
    private double currentMonthNet;
    private List<Transaction> recentTransactions;
    private List<Budget> budgetAlerts;
    private List<Saving> savings;
    private int financialHealthScore;
    
    public UserDashboard () {
        this.currentMonthIncome = 0.0;
        this.currentMonthExpense = 0.0;
        this.currentMonthNet = 0.0;
        this.financialHealthScore = 0;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getCurrentMonthIncome() {
        return currentMonthIncome;
    }

    public void setCurrentMonthIncome(double currentMonthIncome) {
        this.currentMonthIncome = currentMonthIncome;
    }

    public double getCurrentMonthExpense() {
        return currentMonthExpense;
    }

    public void setCurrentMonthExpense(double currentMonthExpense) {
        this.currentMonthExpense = currentMonthExpense;
    }

    public double getCurrentMonthNet() {
        return currentMonthNet;
    }

    public void setCurrentMonthNet(double currentMonthNet) {
        this.currentMonthNet = currentMonthNet;
    }

    public List<Transaction> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<Transaction> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<Budget> getBudgetAlerts() {
        return budgetAlerts;
    }

    public void setBudgetAlerts(List<Budget> budgetAlerts) {
        this.budgetAlerts = budgetAlerts;
    }

    public List<Saving> getSavings() {
        return savings;
    }

    public void setSavings(List<Saving> savings) {
        this.savings = savings;
    }

    public int getFinancialHealthScore() {
        return financialHealthScore;
    }

    public void setFinancialHealthScore(int financialHealthScore) {
        this.financialHealthScore = financialHealthScore;
    }
    
    // Helper methods
    public String getFormattedCurrentMonthIncome () {
        return String.format("%,.0f VNĐ", currentMonthIncome);
    }
    
    public String getFormattedCurrentMonthExpense () {
        return String.format("%,.0f VNĐ", currentMonthExpense);
    }
    
    public String getFormattedCurrentMonthNet () {
        if (currentMonthNet >= 0) {
            return "+" + String.format("%,.0f VNĐ", currentMonthNet);
        } else {
            return "-" + String.format("%,.0f VNĐ", Math.abs(currentMonthNet));
        }
    }
    
    public boolean isNetPositive () {
        return currentMonthNet >= 0;
    }
    
    public boolean hasBudgetAlerts () {
        return budgetAlerts != null && !budgetAlerts.isEmpty();
    }
    
    public int getBudgetAlertCount () {
        return budgetAlerts != null ? budgetAlerts.size() : 0;
    }
    
    public boolean hasSavings () {
        return savings != null && !savings.isEmpty();
    }
    
    public int getActiveSavingsCount () {
        if (savings == null) return 0;
        return (int) savings.stream()
                .filter(s -> !s.isIsCompleted())
                .count();
    }
    
    public String getFinancialHealthLevel () {
        if (financialHealthScore >= 80) return "Tốt";
        else if (financialHealthScore >= 60) return "Khá";
        else if (financialHealthScore >= 40) return "Trung bình";
        else return "Cần cải thiện";
    }
    
    public String getFinancialHealthColor() {
        if (financialHealthScore >= 80) return "#4CAF50"; // Green
        else if (financialHealthScore >= 60) return "#8BC34A"; // Light Green
        else if (financialHealthScore >= 40) return "#FF9800"; // Orange
        else return "#F44336"; // Red
    }
    
    @Override
    public String toString() {
        return "UserDashboard{" +
                "user=" + (user != null ? user.getFullName() : "null") +
                ", currentMonthIncome=" + currentMonthIncome +
                ", currentMonthExpense=" + currentMonthExpense +
                ", currentMonthNet=" + currentMonthNet +
                ", financialHealthScore=" + financialHealthScore +
                ", recentTransactions=" + (recentTransactions != null ? recentTransactions.size() : 0) +
                ", budgetAlerts=" + (budgetAlerts != null ? budgetAlerts.size() : 0) +
                ", savings=" + (savings != null ? savings.size() : 0) +
                '}';
    }
    
}
