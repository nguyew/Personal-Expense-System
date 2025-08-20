package com.expensemanager;

import com.expensemanager.dao.*;
import com.expensemanager.model.User;
import com.expensemanager.model.Category;
import com.expensemanager.util.DateUtils;
import com.expensemanager.util.CurrencyUtils;
import java.util.List;

public class TestClasses {
    public static void main(String[] args) {
        System.out.println("=== Testing Expense Manager Classes ===\n");
        
        // Test database connection
        System.out.println("1. Testing Database Connection:");
        boolean isConnected = DatabaseConnection.testConnection();
        System.out.println("Connection status: " + (isConnected ? "SUCCESS" : "FAILED"));
        
        if (isConnected) {
            // Test using DAOFactory
            System.out.println("\n2. Testing DAOFactory:");
            DAOFactory daoFactory = DAOFactory.getInstance();
            
            // Test UserDAO
            System.out.println("\n3. Testing UserDAO:");
            UserDAO userDAO = daoFactory.getUserDAO();
            User testUser = userDAO.findUser("admin", "admin123");
            
            if (testUser != null) {
                System.out.println("User found: " + testUser.getFullName());
                System.out.println("User ID: " + testUser.getUserID());
                
                // Test CategoryDAO
                System.out.println("\n4. Testing CategoryDAO:");
                CategoryDAO categoryDAO = daoFactory.getCategoryDAO();
                List<Category> categories = categoryDAO.getCategoriesByUser(testUser.getUserID());
                System.out.println("Total categories: " + categories.size());
                
                for (Category category : categories) {
                    System.out.println("- " + category.getCategoryName() + " (" + category.getCategoryType() + ")");
                }
            } else {
                System.out.println("User not found! Check database data or create sample data.");
                
                // Try to create a test user if none exists
                System.out.println("\n5. Creating test user...");
                User newUser = new User("admin", "admin123", "Administrator", "admin@test.com", "0123456789");
                boolean userCreated = userDAO.createUser(newUser);
                
                if (userCreated) {
                    System.out.println("Test user created successfully!");
                    testUser = userDAO.findUser("admin", "admin123");
                    System.out.println("New user ID: " + testUser.getUserID());
                    
                    // Create some default categories
                    CategoryDAO categoryDAO = daoFactory.getCategoryDAO();
                    createDefaultCategories(categoryDAO, testUser.getUserID());
                } else {
                    System.out.println("Failed to create test user.");
                }
            }
        }
        
        // Test utility classes
        System.out.println("\n6. Testing Utility Classes:");
        
        // Test DateUtils
        System.out.println("Current month: " + DateUtils.getCurrentMonth());
        System.out.println("Current year: " + DateUtils.getCurrentYear());
        System.out.println("Month name: " + DateUtils.getMonthName(8));
        
        // Test CurrencyUtils
        double amount = 1500000;
        System.out.println("Formatted currency: " + CurrencyUtils.formatCurrency(amount));
        System.out.println("Formatted amount: " + CurrencyUtils.formatAmount(amount));
        
        System.out.println("\n=== Test completed! ===");
        System.out.println("If database connection was successful and user was found/created,");
        System.out.println("you can now run TestDAOLayer.java for comprehensive testing.");
    }
    
    private static void createDefaultCategories(CategoryDAO categoryDAO, int userID) {
        System.out.println("\nCreating default categories...");
        
        // Income categories
        String[][] incomeCategories = {
            {"Lương", "Thu nhập từ công việc chính"},
            {"Thưởng", "Các khoản thưởng, bonus"},
            {"Đầu tư", "Thu nhập từ đầu tư"},
            {"Khác", "Các thu nhập khác"}
        };
        
        // Expense categories
        String[][] expenseCategories = {
            {"Ăn uống", "Chi phí ăn uống hàng ngày"},
            {"Di chuyển", "Chi phí giao thông, xăng xe"},
            {"Mua sắm", "Quần áo, đồ dùng cá nhân"},
            {"Giải trí", "Xem phim, du lịch, thể thao"},
            {"Hóa đơn", "Điện, nước, internet, điện thoại"},
            {"Y tế", "Khám bệnh, thuốc men"},
            {"Giáo dục", "Học phí, sách vở"},
            {"Khác", "Chi phí khác"}
        };
        
        // Create income categories
        for (String[] cat : incomeCategories) {
            Category category = new Category(cat[0], "INCOME", cat[1], userID);
            categoryDAO.createCategory(category);
        }
        
        // Create expense categories
        for (String[] cat : expenseCategories) {
            Category category = new Category(cat[0], "EXPENSE", cat[1], userID);
            categoryDAO.createCategory(category);
        }
        
        System.out.println("Default categories created successfully!");
    }
}