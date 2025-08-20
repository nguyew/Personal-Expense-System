package com.expensemanager.model;

import java.util.Date;


public class Budget {
    private int budgetID;
    private int userID;
    private int categoryID;
    private double budgetAmount;
    private int month;
    private int year;
    private double alertThreshold;
    private Date createdDate;
    private Date modifiedDate;
    
    // Additional fields for reports
    private String categoryName;
    private double currentSpent;
    private String status; // OK, WARNING, EXCEEDED
    
    public Budget () {
        this.alertThreshold = 80.0; // Default 80%
        this.createdDate = new Date();
        this.modifiedDate = new Date();
        this.currentSpent = 0.0;
        this.status = "OK";
    }

    public Budget(int budgetID, int userID, int categoryID, double budgetAmount, int month, int year, double alertThreshold, Date createdDate, Date modifiedDate, String categoryName, double currentSpent, String status) {
        this.budgetID = budgetID;
        this.userID = userID;
        this.categoryID = categoryID;
        this.budgetAmount = budgetAmount;
        this.month = month;
        this.year = year;
        this.alertThreshold = alertThreshold;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.categoryName = categoryName;
        this.currentSpent = currentSpent;
        this.status = status;
    }

    public int getBudgetID() {
        return budgetID;
    }

    public void setBudgetID(int budgetID) {
        this.budgetID = budgetID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

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

    public double getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(double alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getCurrentSpent() {
        return currentSpent;
    }

    public void setCurrentSpent(double currentSpent) {
        this.currentSpent = currentSpent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    // Helper methods
    public boolean isExceeded () {
        return currentSpent > budgetAmount;
    }
    
    public boolean isWarning () {
        if (budgetAmount == 0) return false;
        double percentageUsed = (currentSpent / budgetAmount) * 100;
        return percentageUsed >= alertThreshold && percentageUsed < 100;
    }
    
    public double getUsedPercentage () {
        if (budgetAmount == 0) return 0;
        return (currentSpent / budgetAmount) * 100;
    }
    
    public double getRemainingAmount () {
        return budgetAmount - currentSpent;
    }
    
    public String calculateStatus () {
        if (isExceeded()) {
            return "EXCEED";
        } else if (isWarning()) {
            return "WARNING";
        } else {
            return "OK";
        }
    }
    
    public void updateStatus () {
        this.status = calculateStatus();
    }
    
    public String getFormattedBudgetAmount () {
        return String.format("%,.0f VNĐ", budgetAmount);
    }
    
    public String getFormattedCurrentSpent () {
        return String.format("%,. 0f VNĐ", currentSpent);
    }
    
    public String getFormattedRemainingAmount() {
        double remaining = getRemainingAmount();
        if (remaining < 0) {
            return String.format("-%,.0f VNĐ", Math.abs(remaining));
        } else {
            return String.format("%,.0f VNĐ", remaining);
        }
    }
    
    @Override
    public String toString() {
        return "Budget{" +
                "budgetID=" + budgetID +
                ", categoryName='" + categoryName + '\'' +
                ", budgetAmount=" + budgetAmount +
                ", currentSpent=" + currentSpent +
                ", status='" + status + '\'' +
                ", month=" + month +
                ", year=" + year +
                '}';
    }
}
