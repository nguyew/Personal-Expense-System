package com.expensemanager.model;

import java.util.Date;


public class Saving {
    private int savingID;
    private int userID;
    private String savingName;
    private String description;
    private double targetAmount;
    private double currentAmount;
    private Date targetDate;
    private int priority; // 1-5 (1=Low, 5=High)
    private boolean isCompleted;
    private Date createdDate;
    private Date completedDate;
    
    public Saving () {
        this.currentAmount = 0.0;
        this.priority = 1;
        this.isCompleted = false;
        this.createdDate = new Date();
    }

    public Saving(int savingID, int userID, String savingName, String description, double targetAmount, double currentAmount, Date targetDate, int priority, boolean isCompleted, Date createdDate, Date completedDate) {
        this.savingID = savingID;
        this.userID = userID;
        this.savingName = savingName;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.createdDate = createdDate;
        this.completedDate = completedDate;
    }

    public int getSavingID() {
        return savingID;
    }

    public void setSavingID(int savingID) {
        this.savingID = savingID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getSavingName() {
        return savingName;
    }

    public void setSavingName(String savingName) {
        this.savingName = savingName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public Date getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }
    
    // Helper methods
    public double getCompletionPercentage () {
        if (targetAmount == 0) return 0;
        return Math.min(100, (currentAmount / targetAmount) * 100);
    }
    
    public double getRemainingAmount () {
        return Math.max(0, targetAmount - currentAmount);
    }
    
    public long getDaysRemaining () {
        if (targetDate == null) return -1;
        long diffInMillies = targetDate.getTime() - new Date().getTime();
        return diffInMillies / (1000 * 60 * 60 * 24);
    }
    
    public double getRequiredDailyAmount () {
        long daysRemaining = getDaysRemaining();
        if (daysRemaining <= 0) return 0;
        return getRemainingAmount() / daysRemaining;
    }
    
    public String getPriorityText () {
        switch (priority) {
            case 1: return "Rất thấp";
            case 2: return "Thấp";
            case 3: return "Trung bình";
            case 4: return "Cao";
            case 5: return "Rất cao";
            default: return "Không xác định";            
        }
    }
    
    public String getFormattedTargetAmount() {
        return String.format("%,.0f VNĐ", targetAmount);
    }

    public String getFormattedCurrentAmount() {
        return String.format("%,.0f VNĐ", currentAmount);
    }

    public String getFormattedRemainingAmount() {
        return String.format("%,.0f VNĐ", getRemainingAmount());
    }

    public String toString() {
        return savingName + " (" + String.format("%.1f", getCompletionPercentage()) + "%)";
    }
}
