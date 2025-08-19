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
    private Date modifieldDate;
    
    // Additional fields for reports
    private String categoryName;
    private double currentSpent;
    private String status; // OK, WARNING, EXCEEDED
    
    public Budget () {
        this.alertThreshold = 80.0; // Default 80%
        this.createdDate = new Date();
        this.modifieldDate = new Date();
    }

    public Budget(int budgetID, int userID, int categoryID, double budgetAmount, int month, int year, double alertThreshold, Date createdDate, Date modifieldDate, String categoryName, double currentSpent, String status) {
        this.budgetID = budgetID;
        this.userID = userID;
        this.categoryID = categoryID;
        this.budgetAmount = budgetAmount;
        this.month = month;
        this.year = year;
        this.alertThreshold = alertThreshold;
        this.createdDate = createdDate;
        this.modifieldDate = modifieldDate;
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

    public Date getModifieldDate() {
        return modifieldDate;
    }

    public void setModifieldDate(Date modifieldDate) {
        this.modifieldDate = modifieldDate;
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
}
