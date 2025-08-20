package com.expensemanager;

import com.expensemanager.dao.DatabaseConnection;
import com.expensemanager.dao.UserDAO;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.model.User;
import com.expensemanager.util.DateUtils;
import com.expensemanager.util.CurrencyUtils;

public class TestClasses {
    public static void main(String[] args) {
        System.out.println("=== Testing Expense Manager Classes ===\n");
        
        // Test database connection
        System.out.println("1. Testing Database Connection:");
        boolean isConnected = DatabaseConnection.testConnection();
        System.out.println("Connection status: " + (isConnected ? "SUCCESS" : "FAILED"));
        
        if (isConnected) {
            // Test UserDAO
            System.out.println("\n2. Testing UserDAO:");
            UserDAO userDAO = new UserDAO();
            User testUser = userDAO.findUser("admin", "admin123");
            
            if (testUser != null) {
                System.out.println("User found: " + testUser.getFullName());
                System.out.println("User ID: " + testUser.getUserID());
                
                // Test CategoryDAO
                System.out.println("\n3. Testing CategoryDAO:");
                CategoryDAO categoryDAO = new CategoryDAO();
                var categories = categoryDAO.getCategoriesByUser(testUser.getUserID());
                System.out.println("Total categories: " + categories.size());
                
                for (var category : categories) {
                    System.out.println("- " + category.getCategoryName() + " (" + category.getCategoryType() + ")");
                }
            } else {
                System.out.println("User not found! Check database data.");
            }
        }
        
        // Test utility classes
        System.out.println("\n4. Testing Utility Classes:");
        
        // Test DateUtils
        System.out.println("Current month: " + DateUtils.getCurrentMonth());
        System.out.println("Current year: " + DateUtils.getCurrentYear());
        System.out.println("Month name: " + DateUtils.getMonthName(8));
        
        // Test CurrencyUtils
        double amount = 1500000;
        System.out.println("Formatted currency: " + CurrencyUtils.formatCurrency(amount));
        System.out.println("Formatted amount: " + CurrencyUtils.formatAmount(amount));
        
        System.out.println("\n=== Test completed! ===");
    }
}