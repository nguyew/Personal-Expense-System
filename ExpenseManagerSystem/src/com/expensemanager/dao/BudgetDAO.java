package com.expensemanager.dao;

import com.expensemanager.model.Budget;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BudgetDAO {
    
    // Create new budget
    public boolean createBudget (Budget budget) {
        String sql = "INSERT INTO Budget (UserID, CategoryID, BudgetAmount, Month, Year, AlertThreshold) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, budget.getUserID());
            pstmt.setInt(2, budget.getCategoryID());
            pstmt.setDouble(3, budget.getBudgetAmount());
            pstmt.setInt(4, budget.getMonth());
            pstmt.setInt(5, budget.getYear());
            pstmt.setDouble(6, budget.getAlertThreshold());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        budget.setBudgetID(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating budget: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get budget by ID
    public Budget getBudgetById (int budgetID) {
        String sql = "SELECT b.*, c.CategoryName FROM Budget b " +
                    "INNER JOIN Categories c ON b.CategoryID = c.CategoryID " +
                    "WHERE b.BudgetID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, budgetID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBudget(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting budget by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Get budget by user and period 
    public List<Budget> getBudgetsByUserAndPeriod (int userID, int month, int year) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT b.*, c.CategoryName, " +
                    "ISNULL((SELECT SUM(Amount) FROM Transactions t WHERE t.CategoryID = b.CategoryID " +
                    "AND t.UserID = b.UserID AND MONTH(t.TransactionDate) = b.Month " +
                    "AND YEAR(t.TransactionDate) = b.Year AND t.TransactionType = 'EXPENSE'), 0) as CurrentSpent " +
                    "FROM Budget b " +
                    "INNER JOIN Categories c ON b.CategoryID = c.CategoryID " +
                    "WHERE b.UserID = ? AND b.Month = ? AND b.Year = ? " +
                    "ORDER BY c.CategoryName";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Budget budget = mapResultSetToBudget(rs);
                    budget.setCurrentSpent(rs.getDouble("CurrentSpent"));
                    
                    // Calculate status
                        if (budget.isExceeded()) {
                        budget.setStatus("EXCEED");
                    } else if (budget.isWarning()) {
                        budget.setStatus("WARNING");
                    } else {
                        budget.setStatus("OK");
                    }
                    
                    budgets.add(budget);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting budgets by user and period: " + e.getMessage());
            e.printStackTrace();
        }
        
        return budgets;
    }
    
    // Get budget by category and period
    public Budget getBudgetByCategory(int userID, int categoryID, int month, int year) {
        String sql = "SELECT b.*, c.CategoryName FROM Budget b " +
                    "INNER JOIN Categories c ON b.CategoryID = c.CategoryID " +
                    "WHERE b.UserID = ? AND b.CategoryID = ? AND b.Month = ? AND b.Year = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, categoryID);
            pstmt.setInt(3, month);
            pstmt.setInt(4, year);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBudget(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting budget by category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Update budget
    public boolean updateBudget(Budget budget) {
        String sql = "UPDATE Budget SET BudgetAmount = ?, AlertThreshold = ?, ModifiedDate = GETDATE() WHERE BudgetID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, budget.getBudgetAmount());
            pstmt.setDouble(2, budget.getAlertThreshold());
            pstmt.setInt(3, budget.getBudgetID());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating budget: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Delete budget
    public boolean deleteBudget(int budgetID) {
        String sql = "DELETE FROM Budget WHERE BudgetID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, budgetID);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting budget: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Check budget alerts
    public List<Budget> getBudgetAlerts(int userID, int month, int year) {
        List<Budget> alerts = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             CallableStatement cstmt = conn.prepareCall("{call sp_CheckBudgetAlert(?, ?, ?)}")) {
            
            cstmt.setInt(1, userID);
            cstmt.setInt(2, month);
            cstmt.setInt(3, year);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    Budget budget = new Budget();
                    budget.setBudgetID(rs.getInt("BudgetID"));
                    budget.setBudgetAmount(rs.getDouble("BudgetAmount"));
                    budget.setCategoryName(rs.getString("CategoryName"));
                    budget.setCurrentSpent(rs.getDouble("CurrentSpent"));
                    budget.setStatus(rs.getString("Status"));
                    
                    // Only return WARNING or EXCEED budgets
                    if ("WARNING".equals(budget.getStatus()) || "EXCEED".equals(budget.getStatus())) {
                        alerts.add(budget);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting budget alerts: " + e.getMessage());
            e.printStackTrace();
        }
        
        return alerts;
    }
    
    // Helper method to map ResultSet to Budget
    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setBudgetID(rs.getInt("BudgetID"));
        budget.setUserID(rs.getInt("UserID"));
        budget.setCategoryID(rs.getInt("CategoryID"));
        budget.setBudgetAmount(rs.getDouble("BudgetAmount"));
        budget.setMonth(rs.getInt("Month"));
        budget.setYear(rs.getInt("Year"));
        budget.setAlertThreshold(rs.getDouble("AlertThreshold"));
        budget.setCreatedDate(rs.getTimestamp("CreatedDate"));
        budget.setModifiedDate(rs.getTimestamp("ModifiedDate"));
        budget.setCategoryName(rs.getString("CategoryName"));
        return budget;
    }
}
