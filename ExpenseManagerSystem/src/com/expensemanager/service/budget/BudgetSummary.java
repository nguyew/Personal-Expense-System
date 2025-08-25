package com.expensemanager.service.budget;

import com.expensemanager.util.*;

public class BudgetSummary {
    private int month;
    private int year;
    private int totalBudgets;
    private double totalBudgetAmount;
    private double totalSpentAmount;
    private int okCount;
    private int warningCount;
    private int exceededCount;
    
    // Getters and setters
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        
        public int getTotalBudgets() { return totalBudgets; }
        public void setTotalBudgets(int totalBudgets) { this.totalBudgets = totalBudgets; }
        
        public double getTotalBudgetAmount() { return totalBudgetAmount; }
        public void setTotalBudgetAmount(double totalBudgetAmount) { this.totalBudgetAmount = totalBudgetAmount; }
        
        public double getTotalSpentAmount() { return totalSpentAmount; }
        public void setTotalSpentAmount(double totalSpentAmount) { this.totalSpentAmount = totalSpentAmount; }
        
        public int getOkCount() { return okCount; }
        public void setOkCount(int okCount) { this.okCount = okCount; }
        
        public int getWarningCount() { return warningCount; }
        public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
        
        public int getExceededCount() { return exceededCount; }
        public void setExceededCount(int exceededCount) { this.exceededCount = exceededCount; }
        
        // Helper methods
        public double getRemainingBudget () {
            return Math.max(0, totalBudgetAmount - totalSpentAmount);
        }
        
        public double getUsagePercentage () {
            if (totalBudgetAmount == 0) return 0;
            return  (totalSpentAmount / totalBudgetAmount) * 100;
        }
        
        public boolean hasAlerts () {
            return warningCount > 0 || exceededCount > 0;
        }
        
        public String getFormattedTotalBudget () {
            return CurrencyUtils.formatCurrency(totalBudgetAmount);
        }
        
        public String getFormattedTotalSpent () {
            return CurrencyUtils.formatCurrency(totalSpentAmount);
        }
        
        public String getFormattedRemainingBudget () {
            return CurrencyUtils.formatCurrency(getRemainingBudget());
        }
        
        public String getFormattedUsagePercentage () {
            return String.format("%.1f%%", getUsagePercentage());
        }
        
        public String getMonthYearString () {
            return DateUtils.getMonthName(month) + " " + year;
        }
        
        public String toString () {
            return "BudgetSummary{" +
                    "month=" + month +
                    ", year=" + year +
                    ", totalBudgets=" + totalBudgets +
                    ", totalBudgetAmount=" + totalBudgetAmount +
                    ", totalSpentAmount=" + totalSpentAmount +
                    ", okCount=" + okCount +
                    ", warningCount=" + warningCount +
                    ", exceededCount=" + exceededCount +
                    ", usagePercentage=" + String.format("%.1f%%", getUsagePercentage()) +
                    '}';
        }
}
