package com.expensemanager.model;

import java.util.Date;

public class SavingTransaction {
    private int savingTransactionID;
    private int savingID;
    private double amount;
    private String transactionType; // DEPOSIT OR WITHDRAW
    private String description;
    private Date transactionDate;
    private Date createdDate;
    
    // Default constructor
    public SavingTransaction () {
        this.transactionDate = new Date();
        this.createdDate = new Date();
    }
    
    // Constructor with parameters
    public SavingTransaction(int savingTransactionID, int savingID, double amount, String transactionType, String description, Date transactionDate, Date createdDate) {
        this.savingTransactionID = savingTransactionID;
        this.savingID = savingID;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.transactionDate = transactionDate;
        this.createdDate = createdDate;
    }
    
    // Constructor for new transaction
    public SavingTransaction(int savingID, double amount, String transactionType, String description) {
        this.savingID = savingID;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.transactionDate = new Date();
        this.createdDate = new Date();
    }

    public int getSavingTransactionID() {
        return savingTransactionID;
    }

    public void setSavingTransactionID(int savingTransactionID) {
        this.savingTransactionID = savingTransactionID;
    }

    public int getSavingID() {
        return savingID;
    }

    public void setSavingID(int savingID) {
        this.savingID = savingID;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    // Helper methods
    public boolean isDeposit () {
        return "DEPOSIT".equals(transactionType);
    }
    
    public boolean isWithdrawal () {
        return "WITHDRAW".equals(transactionType);
    }
    
    public String getTransactionTypeText () {
        switch (transactionType) {
            case "DEPOSIT":
                return "Nạp tiền";
            case "WITHDRAW":
                return "Rút tiền";
            default:
                return "Không xác định";
        }
    }
    
    public String getFormattedAmount() {
        return String.format("%,.0f VNĐ", amount);
    }
    
    public String getAmountWithSign() {
        String sign = isDeposit() ? "+" : "-";
        return sign + String.format("%,.0f VNĐ", amount);
    }
    
    public String getAmountColorClass() {
        return isDeposit() ? "text-success" : "text-danger";
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s - %s (%s)", 
            getTransactionTypeText(), 
            getFormattedAmount(), 
            description != null ? description : "", 
            transactionDate != null ? transactionDate.toString() : "");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SavingTransaction that = (SavingTransaction) obj;
        return savingTransactionID == that.savingTransactionID;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(savingTransactionID);
    }
}
