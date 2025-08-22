package com.expensemanager.model;

import java.util.Date;


public class Transaction {
    private int transactionID;
    private int userID;
    private int categoryID;
    private double amount;
    private String transactionType; // INCOME or EXPENSE
    private String description;
    private Date transactionDate;
    private String location;
    private String notes;
    private Date createdDate;
    private Date modifiedDate;
    
    // Additional fields for join queries
    private String categoryName;
    private String categoryColor;
    private String userName;
    
    public Transaction () {
        this.createdDate = new Date();
        this.modifiedDate = new Date();
    }

    public Transaction(int transactionID, int userID, int categoryID, double amount, String transactionType, String description, Date transactionDate, String location, String notes, Date createdDate, Date modifiedDate, String categoryName, String categoryColor, String userName) {
        this.transactionID = transactionID;
        this.userID = userID;
        this.categoryID = categoryID;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.transactionDate = transactionDate;
        this.location = location;
        this.notes = notes;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.categoryName = categoryName;
        this.categoryColor = categoryColor;
        this.userName = userName;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public void setModifiedDate(Date modifieldDate) {
        this.modifiedDate = modifieldDate;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    // Helper methods
    public boolean isIncome () {
        return "INCOME".equals(transactionType);
    }
    
    public boolean isExpense () {
        return "EXPENSE".equals(transactionType);
    }
    
    public String getFormattedAmount () {
        return String.format("%,.0f VNĐ", amount);
    }
    
    public String toString () {
        return "Transaction{" +
                "transactionID=" + transactionID +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                ", description='" + description + '\'' +
                ", transactionDate=" + transactionDate +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
