package com.expensemanager;

import com.expensemanager.service.*;
import com.expensemanager.model.User;
import com.expensemanager.dao.DatabaseConnection;

public class TestUserService {
    
    public static void main(String[] args) {
        System.out.println("=== TESTING USER SERVICE ===\n");
        
        // Test database connection first
        if (!DatabaseConnection.testConnection()) {
            System.err.println("Database connection failed. Cannot proceed with tests.");
            return;
        }
        
        UserService userService = new UserService();
        
        // Test 1: User Registration
        System.out.println("1. Testing User Registration:");
        testUserRegistration(userService);
        
        // Test 2: User Authentication
        System.out.println("\n2. Testing User Authentication:");
        testUserAuthentication(userService);
        
        // Test 3: Password Change
        System.out.println("\n3. Testing Password Change:");
        testPasswordChange(userService);
        
        // Test 4: Profile Update
        System.out.println("\n4. Testing Profile Update:");
        testProfileUpdate(userService);
        
        // Test 5: Dashboard Data
        System.out.println("\n5. Testing Dashboard Data:");
        testDashboardData(userService);
        
        // Test 6: Input Validation
        System.out.println("\n6. Testing Input Validation:");
        testInputValidation(userService);
        
        System.out.println("\n=== USER SERVICE TESTS COMPLETED ===");
    }
    
    private static void testUserRegistration(UserService userService) {
        System.out.println("Testing user registration...");
        
        // Test successful registration
        String username = "testuser_" + System.currentTimeMillis();
        ServiceResult<User> result = userService.registerUser(
            username, 
            "password123", 
            "password123", 
            "Test User", 
            "testuser@example.com", 
            "0901234567"
        );
        
        if (result.isSuccess()) {
            System.out.println("✓ Registration successful: " + result.getData().getFullName());
            System.out.println("  User ID: " + result.getData().getUserID());
            System.out.println("  Message: " + result.getMessage());
        } else {
            System.out.println("✗ Registration failed: " + result.getMessage());
        }
        
        // Test duplicate username
        ServiceResult<User> duplicateResult = userService.registerUser(
            username, // Same username
            "password456", 
            "password456", 
            "Another User", 
            "another@example.com", 
            "0907654321"
        );
        
        if (duplicateResult.isError()) {
            System.out.println("✓ Duplicate username validation works: " + duplicateResult.getMessage());
        } else {
            System.out.println("✗ Duplicate username validation failed");
        }
    }
    
    private static void testUserAuthentication(UserService userService) {
        System.out.println("Testing user authentication...");
        
        // Test with existing admin user
        ServiceResult<User> loginResult = userService.authenticateUser("admin", "admin123");
        
        if (loginResult.isSuccess()) {
            User user = loginResult.getData();
            System.out.println("✓ Login successful: " + user.getFullName());
            System.out.println("  User ID: " + user.getUserID());
            System.out.println("  Last login updated: " + user.getLastLogin());
        } else {
            System.out.println("✗ Login failed: " + loginResult.getMessage());
        }
        
        // Test with wrong password
        ServiceResult<User> wrongPasswordResult = userService.authenticateUser("admin", "wrongpassword");
        
        if (wrongPasswordResult.isError()) {
            System.out.println("✓ Wrong password validation works: " + wrongPasswordResult.getMessage());
        } else {
            System.out.println("✗ Wrong password validation failed");
        }
        
        // Test with empty credentials
        ServiceResult<User> emptyResult = userService.authenticateUser("", "");
        
        if (emptyResult.isError()) {
            System.out.println("✓ Empty credentials validation works: " + emptyResult.getMessage());
        } else {
            System.out.println("✗ Empty credentials validation failed");
        }
    }
    
    private static void testPasswordChange(UserService userService) {
        System.out.println("Testing password change...");
        
        // First login to get user
        ServiceResult<User> loginResult = userService.authenticateUser("admin", "admin123");
        
        if (loginResult.isSuccess()) {
            User user = loginResult.getData();
            
            // Test successful password change
            ServiceResult<Void> changeResult = userService.changePassword(
                user.getUserID(), 
                "admin123", 
                "newpassword123", 
                "newpassword123"
            );
            
            if (changeResult.isSuccess()) {
                System.out.println("✓ Password changed successfully: " + changeResult.getMessage());
                
                // Test login with new password
                ServiceResult<User> newLoginResult = userService.authenticateUser("admin", "newpassword123");
                
                if (newLoginResult.isSuccess()) {
                    System.out.println("✓ Login with new password successful");
                    
                    // Change back to original password
                    userService.changePassword(user.getUserID(), "newpassword123", "admin123", "admin123");
                    System.out.println("  Password restored to original");
                } else {
                    System.out.println("✗ Login with new password failed");
                }
            } else {
                System.out.println("✗ Password change failed: " + changeResult.getMessage());
            }
            
            // Test wrong current password
            ServiceResult<Void> wrongCurrentResult = userService.changePassword(
                user.getUserID(), 
                "wrongcurrent", 
                "newpass", 
                "newpass"
            );
            
            if (wrongCurrentResult.isError()) {
                System.out.println("✓ Wrong current password validation works: " + wrongCurrentResult.getMessage());
            } else {
                System.out.println("✗ Wrong current password validation failed");
            }
            
            // Test password mismatch
            ServiceResult<Void> mismatchResult = userService.changePassword(
                user.getUserID(), 
                "admin123", 
                "newpass1", 
                "newpass2"
            );
            
            if (mismatchResult.isError()) {
                System.out.println("✓ Password mismatch validation works: " + mismatchResult.getMessage());
            } else {
                System.out.println("✗ Password mismatch validation failed");
            }
        } else {
            System.out.println("Cannot test password change - login failed");
        }
    }
    
    private static void testProfileUpdate(UserService userService) {
        System.out.println("Testing profile update...");
        
        // First login to get user
        ServiceResult<User> loginResult = userService.authenticateUser("admin", "admin123");
        
        if (loginResult.isSuccess()) {
            User user = loginResult.getData();
            String originalFullName = user.getFullName();
            String originalEmail = user.getEmail();
            String originalPhone = user.getPhone();
            
            // Test successful profile update
            ServiceResult<User> updateResult = userService.updateUserProfile(
                user, 
                "Updated Admin Name", 
                "updated.admin@example.com", 
                "0909888777"
            );
            
            if (updateResult.isSuccess()) {
                User updatedUser = updateResult.getData();
                System.out.println("✓ Profile updated successfully: " + updateResult.getMessage());
                System.out.println("  New name: " + updatedUser.getFullName());
                System.out.println("  New email: " + updatedUser.getEmail());
                System.out.println("  New phone: " + updatedUser.getPhone());
                
                // Restore original data
                userService.updateUserProfile(user, originalFullName, originalEmail, originalPhone);
                System.out.println("  Profile restored to original");
            } else {
                System.out.println("✗ Profile update failed: " + updateResult.getMessage());
            }
            
            // Test invalid email
            ServiceResult<User> invalidEmailResult = userService.updateUserProfile(
                user, 
                "Test User", 
                "invalid-email", 
                "0901234567"
            );
            
            if (invalidEmailResult.isError()) {
                System.out.println("✓ Invalid email validation works: " + invalidEmailResult.getMessage());
            } else {
                System.out.println("✗ Invalid email validation failed");
            }
            
            // Test invalid phone
            ServiceResult<User> invalidPhoneResult = userService.updateUserProfile(
                user, 
                "Test User", 
                "test@example.com", 
                "invalid-phone"
            );
            
            if (invalidPhoneResult.isError()) {
                System.out.println("✓ Invalid phone validation works: " + invalidPhoneResult.getMessage());
            } else {
                System.out.println("✗ Invalid phone validation failed");
            }
        } else {
            System.out.println("Cannot test profile update - login failed");
        }
    }
    
    private static void testDashboardData(UserService userService) {
        System.out.println("Testing dashboard data retrieval...");
        
        // First login to get user
        ServiceResult<User> loginResult = userService.authenticateUser("admin", "admin123");
        
        if (loginResult.isSuccess()) {
            User user = loginResult.getData();
            
            ServiceResult<UserDashboard> dashboardResult = userService.getUserDashboard(user.getUserID());
            
            if (dashboardResult.isSuccess()) {
                UserDashboard dashboard = dashboardResult.getData();
                System.out.println("✓ Dashboard data loaded successfully");
                System.out.println("  User: " + dashboard.getUser().getFullName());
                System.out.println("  Current month income: " + dashboard.getFormattedCurrentMonthIncome());
                System.out.println("  Current month expense: " + dashboard.getFormattedCurrentMonthExpense());
                System.out.println("  Current month net: " + dashboard.getFormattedCurrentMonthNet());
                System.out.println("  Financial health score: " + dashboard.getFinancialHealthScore() + "/100 (" + dashboard.getFinancialHealthLevel() + ")");
                System.out.println("  Recent transactions: " + (dashboard.getRecentTransactions() != null ? dashboard.getRecentTransactions().size() : 0));
                System.out.println("  Budget alerts: " + dashboard.getBudgetAlertCount());
                System.out.println("  Active savings: " + dashboard.getActiveSavingsCount());
            } else {
                System.out.println("✗ Dashboard data loading failed: " + dashboardResult.getMessage());
            }
        } else {
            System.out.println("Cannot test dashboard - login failed");
        }
    }
    
    private static void testInputValidation(UserService userService) {
        System.out.println("Testing input validation...");
        
        // Test short username
        ServiceResult<User> shortUsernameResult = userService.registerUser(
            "ab", "password123", "password123", "Test User", "test@example.com", "0901234567"
        );
        
        if (shortUsernameResult.isError()) {
            System.out.println("✓ Short username validation works: " + shortUsernameResult.getMessage());
        } else {
            System.out.println("✗ Short username validation failed");
        }
        
        // Test invalid username characters
        ServiceResult<User> invalidUsernameResult = userService.registerUser(
            "test@user", "password123", "password123", "Test User", "test@example.com", "0901234567"
        );
        
        if (invalidUsernameResult.isError()) {
            System.out.println("✓ Invalid username characters validation works: " + invalidUsernameResult.getMessage());
        } else {
            System.out.println("✗ Invalid username characters validation failed");
        }
        
        // Test short password
        ServiceResult<User> shortPasswordResult = userService.registerUser(
            "testuser", "123", "123", "Test User", "test@example.com", "0901234567"
        );
        
        if (shortPasswordResult.isError()) {
            System.out.println("✓ Short password validation works: " + shortPasswordResult.getMessage());
        } else {
            System.out.println("✗ Short password validation failed");
        }
        
        // Test empty full name
        ServiceResult<User> emptyNameResult = userService.registerUser(
            "testuser", "password123", "password123", "", "test@example.com", "0901234567"
        );
        
        if (emptyNameResult.isError()) {
            System.out.println("✓ Empty full name validation works: " + emptyNameResult.getMessage());
        } else {
            System.out.println("✗ Empty full name validation failed");
        }
        
        // Test invalid email format
        ServiceResult<User> invalidEmailResult = userService.registerUser(
            "testuser", "password123", "password123", "Test User", "invalid.email", "0901234567"
        );
        
        if (invalidEmailResult.isError()) {
            System.out.println("✓ Invalid email format validation works: " + invalidEmailResult.getMessage());
        } else {
            System.out.println("✗ Invalid email format validation failed");
        }
        
        // Test invalid phone format
        ServiceResult<User> invalidPhoneResult = userService.registerUser(
            "testuser", "password123", "password123", "Test User", "test@example.com", "123"
        );
        
        if (invalidPhoneResult.isError()) {
            System.out.println("✓ Invalid phone format validation works: " + invalidPhoneResult.getMessage());
        } else {
            System.out.println("✗ Invalid phone format validation failed");
        }
        
        System.out.println("\n📊 Validation Test Summary:");
        System.out.println("  - Username: Length (≥3), Characters (alphanumeric + underscore)");
        System.out.println("  - Password: Length (≥6), Confirmation match");
        System.out.println("  - Email: Valid format (optional)");
        System.out.println("  - Phone: Vietnamese format (optional)");
        System.out.println("  - Full name: Not empty");
    }
}