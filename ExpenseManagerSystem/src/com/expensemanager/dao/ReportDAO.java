package com.expensemanager.dao;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ReportDAO {
    
    // Get expense by category for chart
    public Map<String, Double> getExpenseByCategory (int userID, Date startDate, Date endDate) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT c.CategoryName, SUM(t.Amount) as TotalAmount " +
                    "FROM Transactions t " +
                    "INNER JOIN Categories c ON t.CategoryID = c.CategoryID " +
                    "WHERE t.UserID = ? AND t.TransactionType = 'EXPENSE' " +
                    "AND t.TransactionDate BETWEEN ? AND ? " +
                    "GROUP BY c.CategoryName " +
                    "ORDER BY TotalAmount DESC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setDate(2, new Date(startDate.getTime()));
            pstmt.setDate(3, new Date(endDate.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("CategoryName"), rs.getDouble("TotalAmount"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense by category: " + e.getMessage());
            e.printStackTrace();;
        }
        
        return result;
    }
    
    // Get monthly trend data
    public List<MonthlyData> getMonthlyTrend (int userID, int numberOfMonths) {
        List<MonthlyData> result = new ArrayList<>();
        String sql = "SELECT " +
                    "YEAR(TransactionDate) as Year, " +
                    "MONTH(TransactionDate) as Month, " +
                    "SUM(CASE WHEN TransactionType = 'INCOME' THEN Amount ELSE 0 END) as TotalIncome, " +
                    "SUM(CASE WHEN TransactionType = 'EXPENSE' THEN Amount ELSE 0 END) as TotalExpense " +
                    "FROM Transactions " +
                    "WHERE UserID = ? AND TransactionDate >= DATEADD(MONTH, -?, GETDATE()) " +
                    "GROUP BY YEAR(TransactionDate), MONTH(TransactionDate) " +
                    "ORDER BY Year, Month";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, numberOfMonths);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MonthlyData data = new MonthlyData();
                    data.setYear(rs.getInt("Year"));
                    data.setMonth(rs.getInt("Month"));
                    data.setTotalIncome(rs.getDouble("TotalIncome"));
                    data.setTotalExpense(rs.getDouble("TotalExpense"));
                    result.add(data);
                }
            } 
        } catch (SQLException e) {
            System.err.println("Error getting monthly trend: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
        
    // Get daily expense for current month
    public Map<Integer, Double> getDailyExpenseCurrentMonth (int userID) {
        Map<Integer, Double> result = new HashMap<>();
        String sql = "SELECT DAY(TransactionDate) as Day, SUM(Amount) as DailyAmount " +
                    "FROM Transactions " +
                    "WHERE UserID = ? AND TransactionType = 'EXPENSE' " +
                    "AND MONTH(TransactionDate) = MONTH(GETDATE()) " +
                    "AND YEAR(TransactionDate) = YEAR(GETDATE()) " +
                    "GROUP BY DAY(TransactionDate) " +
                    "ORDER BY Day";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("Day"), rs.getDouble("DailyAmount"));
                }
            } 
        } catch (SQLException e) {
            System.err.println("Error getting daily expense: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // Get top expense categories
    public List<CategoryExpense> getTopExpenseCategories(int userID, Date startDate, Date endDate, int limit) {
        List<CategoryExpense> result = new ArrayList<>();
        String sql = "SELECT TOP (?) c.CategoryName, c.Color, SUM(t.Amount) as TotalAmount, COUNT(t.TransactionID) as TransactionCount " +
                    "FROM Transactions t " +
                    "INNER JOIN Categories c ON t.CategoryID = c.CategoryID " +
                    "WHERE t.UserID = ? AND t.TransactionType = 'EXPENSE' " +
                    "AND t.TransactionDate BETWEEN ? AND ? " +
                    "GROUP BY c.CategoryName, c.Color " +
                    "ORDER BY TotalAmount DESC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            pstmt.setInt(2, userID);
            pstmt.setDate(3, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(4, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CategoryExpense ce = new CategoryExpense();
                    ce.setCategoryName(rs.getString("CategoryName"));
                    ce.setColor(rs.getString("Color"));
                    ce.setTotalAmount(rs.getDouble("TotalAmount"));
                    ce.setTransactionCount(rs.getInt("TransactionCount"));
                    result.add(ce);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting top expense categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // Inner classes for report data
    public static class MonthlyData {
        private int year;
        private int month;
        private double totalIncome;
        private double totalExpense;
        
        // Getters and Setters
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        
        public double getTotalIncome() { return totalIncome; }
        public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }
        
        public double getTotalExpense() { return totalExpense; }
        public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }
        
        public double getNetAmount() { return totalIncome - totalExpense; }
        
        public String getMonthYearString() { return month + "/" + year; }
    }
    
    public static class CategoryExpense {
        private String categoryName;
        private String color;
        private double totalAmount;
        private int transactionCount;
        
        // Getters and Setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
        
        public int getTransactionCount() { return transactionCount; }
        public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
        
        public double getAverageAmount() {
            return transactionCount > 0 ? totalAmount / transactionCount : 0;
        }
    }
}
