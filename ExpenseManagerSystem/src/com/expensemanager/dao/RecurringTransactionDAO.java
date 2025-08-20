package com.expensemanager.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecurringTransactionDAO {
    
    // Create recurring transaction
    public boolean createRecurringTransaction(RecurringTransaction recurring) {
        String sql = "INSERT INTO RecurringTransactions (UserID, CategoryID, Amount, TransactionType, Description, Frequency, StartDate, EndDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, recurring.getUserID());
            pstmt.setInt(2, recurring.getCategoryID());
            pstmt.setDouble(3, recurring.getAmount());
            pstmt.setString(4, recurring.getTransactionType());
            pstmt.setString(5, recurring.getDescription());
            pstmt.setString(6, recurring.getFrequency());
            pstmt.setDate(7, new java.sql.Date(recurring.getStartDate().getTime()));
            pstmt.setDate(8, recurring.getEndDate() != null ? new java.sql.Date(recurring.getEndDate().getTime()) : null);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        recurring.setRecurringID(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating recurring transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get active recurring transactions
    public List<RecurringTransaction> getActiveRecurringTransactions(int userID) {
        List<RecurringTransaction> transactions = new ArrayList<>();
        String sql = "SELECT rt.*, c.CategoryName FROM RecurringTransactions rt " +
                    "INNER JOIN Categories c ON rt.CategoryID = c.CategoryID " +
                    "WHERE rt.UserID = ? AND rt.IsActive = 1 " +
                    "AND (rt.EndDate IS NULL OR rt.EndDate >= GETDATE()) " +
                    "ORDER BY rt.CreatedDate DESC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    RecurringTransaction rt = mapResultSetToRecurringTransaction(rs);
                    rt.setCategoryName(rs.getString("CategoryName"));
                    transactions.add(rt);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting active recurring transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    // Update last executed date
    public boolean updateLastExecuted(int recurringID, Date lastExecuted) {
        String sql = "UPDATE RecurringTransactions SET LastExecuted = ? WHERE RecurringID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, new java.sql.Date(lastExecuted.getTime()));
            pstmt.setInt(2, recurringID);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating last executed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Helper method and inner class
    private RecurringTransaction mapResultSetToRecurringTransaction(ResultSet rs) throws SQLException {
        RecurringTransaction rt = new RecurringTransaction();
        rt.setRecurringID(rs.getInt("RecurringID"));
        rt.setUserID(rs.getInt("UserID"));
        rt.setCategoryID(rs.getInt("CategoryID"));
        rt.setAmount(rs.getDouble("Amount"));
        rt.setTransactionType(rs.getString("TransactionType"));
        rt.setDescription(rs.getString("Description"));
        rt.setFrequency(rs.getString("Frequency"));
        rt.setStartDate(rs.getDate("StartDate"));
        rt.setEndDate(rs.getDate("EndDate"));
        rt.setLastExecuted(rs.getDate("LastExecuted"));
        rt.setActive(rs.getBoolean("IsActive"));
        rt.setCreatedDate(rs.getTimestamp("CreatedDate"));
        return rt;
    }
    
    public static class RecurringTransaction {
        private int recurringID;
        private int userID;
        private int categoryID;
        private double amount;
        private String transactionType;
        private String description;
        private String frequency;
        private Date startDate;
        private Date endDate;
        private Date lastExecuted;
        private boolean isActive;
        private Date createdDate;
        private String categoryName;
        
        // Constructors
        public RecurringTransaction() {}
        
        // Getters and Setters
        public int getRecurringID() { return recurringID; }
        public void setRecurringID(int recurringID) { this.recurringID = recurringID; }
        
        public int getUserID() { return userID; }
        public void setUserID(int userID) { this.userID = userID; }
        
        public int getCategoryID() { return categoryID; }
        public void setCategoryID(int categoryID) { this.categoryID = categoryID; }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }
        
        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }
        
        public Date getLastExecuted() { return lastExecuted; }
        public void setLastExecuted(Date lastExecuted) { this.lastExecuted = lastExecuted; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public Date getCreatedDate() { return createdDate; }
        public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
        
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        
        // Helper methods
        public String getFrequencyText() {
            switch (frequency) {
                case "DAILY": return "Hàng ngày";
                case "WEEKLY": return "Hàng tuần";
                case "MONTHLY": return "Hàng tháng";
                case "YEARLY": return "Hàng năm";
                default: return "Không xác định";
            }
        }
        
        public boolean shouldExecute() {
            if (!isActive) return false;
            if (endDate != null && new Date().after(endDate)) return false;
            
            Calendar lastExec = Calendar.getInstance();
            if (lastExecuted != null) {
                lastExec.setTime(lastExecuted);
            } else {
                lastExec.setTime(startDate);
                lastExec.add(Calendar.DAY_OF_MONTH, -1); // Subtract 1 day to trigger first execution
            }
            
            Calendar now = Calendar.getInstance();
            
            switch (frequency) {
                case "DAILY":
                    return !isSameDay(lastExec, now);
                case "WEEKLY":
                    lastExec.add(Calendar.WEEK_OF_YEAR, 1);
                    return !now.before(lastExec);
                case "MONTHLY":
                    lastExec.add(Calendar.MONTH, 1);
                    return !now.before(lastExec);
                case "YEARLY":
                    lastExec.add(Calendar.YEAR, 1);
                    return !now.before(lastExec);
                default:
                    return false;
            }
        }
        
        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }
    }
}