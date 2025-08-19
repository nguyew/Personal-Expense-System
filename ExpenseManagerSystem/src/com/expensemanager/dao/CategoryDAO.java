package com.expensemanager.dao;

import com.expensemanager.model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    
    // Create new category
    public boolean createCategory(Category category) {
        String sql = "INSERT INTO Categories (CategoryName, CategoryType, Description, IconName, Color, UserID) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getCategoryType());
            pstmt.setString(3, category.getDescription());
            pstmt.setString(4, category.getIconName());
            pstmt.setString(5, category.getColor());
            pstmt.setInt(6, category.getUserID());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setCategoryID(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get categories by user and type
    public List<Category> getCategoriesByUserAndType(int userID, String categoryType) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories WHERE UserID = ? AND CategoryType = ? ORDER BY CategoryName";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setString(2, categoryType);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }
    
    // Get all categories by user
    public List<Category> getCategoriesByUser(int userID) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories WHERE UserID = ? ORDER BY CategoryType, CategoryName";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }
    
    // Get category by ID
    public Category getCategoryById(int categoryID) {
        String sql = "SELECT * FROM Categories WHERE CategoryID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting category by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Update category
    public boolean updateCategory(Category category) {
        String sql = "UPDATE Categories SET CategoryName = ?, Description = ?, IconName = ?, Color = ? WHERE CategoryID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getDescription());
            pstmt.setString(3, category.getIconName());
            pstmt.setString(4, category.getColor());
            pstmt.setInt(5, category.getCategoryID());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Delete category
    public boolean deleteCategory(int categoryID) {
        String sql = "DELETE FROM Categories WHERE CategoryID = ? AND IsDefault = 0";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryID);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Check if category has transactions
    public boolean hasCategoryTransactions(int categoryID) {
        String sql = "SELECT COUNT(*) FROM Transactions WHERE CategoryID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking category transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Helper method to map ResultSet to Category object
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryID(rs.getInt("CategoryID"));
        category.setCategoryName(rs.getString("CategoryName"));
        category.setCategoryType(rs.getString("CategoryType"));
        category.setDescription(rs.getString("Description"));
        category.setIconName(rs.getString("IconName"));
        category.setColor(rs.getString("Color"));
        category.setUserID(rs.getInt("UserID"));
        category.setDefault(rs.getBoolean("IsDefault"));
        category.setCreatedDate(rs.getTimestamp("CreatedDate"));
        return category;
    }
}