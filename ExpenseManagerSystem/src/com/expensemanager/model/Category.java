package com.expensemanager.model;

import java.time.LocalDateTime;

public class Category {
    public enum CategoryType {
        INCOME, EXPENSE
    }
    
    private int categoryID;
    private String categoryName;
    private CategoryType categoryType;
    private String description;
    private String iconName;
    private String color;
    private int userID;
    private boolean isDefault;
    private LocalDateTime createdDate;
    
    public Category() {
    }
    
    public Category(String categoryName, CategoryType categoryType, String description, int userID) {
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.description = description;
        this.userID = userID;
        this.isDefault = false;
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters và Setters
    public int getCategoryID() { return categoryID; }
    public void setCategoryID(int categoryID) { this.categoryID = categoryID; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public CategoryType getCategoryType() { return categoryType; }
    public void setCategoryType(CategoryType categoryType) { this.categoryType = categoryType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }
    
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    @Override
    public String toString() {
        return categoryName;
    }
}