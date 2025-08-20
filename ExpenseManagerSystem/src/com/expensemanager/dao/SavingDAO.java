package com.expensemanager.dao;

import com.expensemanager.model.Saving;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SavingDAO {
    
    // Create new saving goal
    public boolean createSaving(Saving saving) {
        String sql = "INSERT INTO Savings (UserID, SavingName, Description, TargetAmount, CurrentAmount, TargetDate, Priority) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, saving.getUserID());
            pstmt.setString(2, saving.getSavingName());
            pstmt.setString(3, saving.getDescription());
            pstmt.setDouble(4, saving.getTargetAmount());
            pstmt.setDouble(5, saving.getCurrentAmount());
            pstmt.setDate(6, saving.getTargetDate() != null ? new java.sql.Date(saving.getTargetDate().getTime()) : null);
            pstmt.setInt(7, saving.getPriority());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        saving.setSavingID(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating saving: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get saving by ID
    public Saving getSavingById(int savingID) {
        String sql = "SELECT * FROM Savings WHERE SavingID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, savingID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSaving(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting saving by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Get savings by user
    public List<Saving> getSavingsByUser(int userID) {
        List<Saving> savings = new ArrayList<>();
        String sql = "SELECT * FROM Savings WHERE UserID = ? ORDER BY Priority DESC, TargetDate ASC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    savings.add(mapResultSetToSaving(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting savings by user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return savings;
    }
    
    // Get active savings (not completed)
    public List<Saving> getActiveSavings(int userID) {
        List<Saving> savings = new ArrayList<>();
        String sql = "SELECT * FROM Savings WHERE UserID = ? AND IsCompleted = 0 ORDER BY Priority DESC, TargetDate ASC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    savings.add(mapResultSetToSaving(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting active savings: " + e.getMessage());
            e.printStackTrace();
        }
        
        return savings;
    }
    
    // Update saving
    public boolean updateSaving(Saving saving) {
        String sql = "UPDATE Savings SET SavingName = ?, Description = ?, TargetAmount = ?, TargetDate = ?, Priority = ? WHERE SavingID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, saving.getSavingName());
            pstmt.setString(2, saving.getDescription());
            pstmt.setDouble(3, saving.getTargetAmount());
            pstmt.setDate(4, saving.getTargetDate() != null ? new java.sql.Date(saving.getTargetDate().getTime()) : null);
            pstmt.setInt(5, saving.getPriority());
            pstmt.setInt(6, saving.getSavingID());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating saving: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Add money to saving
    public boolean addToSaving(int savingID, double amount, String description) {
        String sql = "{call sp_UpdateSavingProgress(?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setInt(1, savingID);
            cstmt.setDouble(2, amount);
            cstmt.setString(3, "DEPOSIT");
            cstmt.setString(4, description);
            
            cstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error adding to saving: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Withdraw from saving
    public boolean withdrawFromSaving(int savingID, double amount, String description) {
        String sql = "{call sp_UpdateSavingProgress(?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setInt(1, savingID);
            cstmt.setDouble(2, amount);
            cstmt.setString(3, "WITHDRAW");
            cstmt.setString(4, description);
            
            cstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error withdrawing from saving: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Mark saving as completed
    public boolean markSavingCompleted(int savingID) {
        String sql = "UPDATE Savings SET IsCompleted = 1, CompletedDate = GETDATE() WHERE SavingID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, savingID);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking saving completed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Delete saving
    public boolean deleteSaving(int savingID) {
        String sql = "DELETE FROM Savings WHERE SavingID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, savingID);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting saving: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get saving transactions
    public List<SavingTransaction> getSavingTransactions(int savingID) {
        List<SavingTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM SavingTransactions WHERE SavingID = ? ORDER BY TransactionDate DESC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, savingID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SavingTransaction st = new SavingTransaction();
                    st.setSavingTransactionID(rs.getInt("SavingTransactionID"));
                    st.setSavingID(rs.getInt("SavingID"));
                    st.setAmount(rs.getDouble("Amount"));
                    st.setTransactionType(rs.getString("TransactionType"));
                    st.setDescription(rs.getString("Description"));
                    st.setTransactionDate(rs.getDate("TransactionDate"));
                    st.setCreatedDate(rs.getTimestamp("CreatedDate"));
                    transactions.add(st);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting saving transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    // Get savings summary for user
    public SavingSummary getSavingSummary(int userID) {
        String sql = "SELECT " +
                    "COUNT(*) as TotalSavings, " +
                    "COUNT(CASE WHEN IsCompleted = 1 THEN 1 END) as CompletedSavings, " +
                    "SUM(TargetAmount) as TotalTargetAmount, " +
                    "SUM(CurrentAmount) as TotalCurrentAmount " +
                    "FROM Savings WHERE UserID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    SavingSummary summary = new SavingSummary();
                    summary.setTotalSavings(rs.getInt("TotalSavings"));
                    summary.setCompletedSavings(rs.getInt("CompletedSavings"));
                    summary.setTotalTargetAmount(rs.getDouble("TotalTargetAmount"));
                    summary.setTotalCurrentAmount(rs.getDouble("TotalCurrentAmount"));
                    return summary;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting saving summary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Helper method to map ResultSet to Saving
    private Saving mapResultSetToSaving(ResultSet rs) throws SQLException {
        Saving saving = new Saving();
        saving.setSavingID(rs.getInt("SavingID"));
        saving.setUserID(rs.getInt("UserID"));
        saving.setSavingName(rs.getString("SavingName"));
        saving.setDescription(rs.getString("Description"));
        saving.setTargetAmount(rs.getDouble("TargetAmount"));
        saving.setCurrentAmount(rs.getDouble("CurrentAmount"));
        saving.setTargetDate(rs.getDate("TargetDate"));
        saving.setPriority(rs.getInt("Priority"));
        saving.setIsCompleted(rs.getBoolean("IsCompleted"));
        saving.setCreatedDate(rs.getTimestamp("CreatedDate"));
        saving.setCompletedDate(rs.getTimestamp("CompletedDate"));
        return saving;
    }
    
    // Inner class for saving transaction
    public static class SavingTransaction {
        private int savingTransactionID;
        private int savingID;
        private double amount;
        private String transactionType;
        private String description;
        private Date transactionDate;
        private Date createdDate;
        
        // Getters and Setters
        public int getSavingTransactionID() { return savingTransactionID; }
        public void setSavingTransactionID(int savingTransactionID) { this.savingTransactionID = savingTransactionID; }
        
        public int getSavingID() { return savingID; }
        public void setSavingID(int savingID) { this.savingID = savingID; }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Date getTransactionDate() { return transactionDate; }
        public void setTransactionDate(Date transactionDate) { this.transactionDate = transactionDate; }
        
        public Date getCreatedDate() { return createdDate; }
        public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    }
    
    // Inner class for saving summary
    public static class SavingSummary {
        private int totalSavings;
        private int completedSavings;
        private double totalTargetAmount;
        private double totalCurrentAmount;
        
        // Getters and Setters
        public int getTotalSavings() { return totalSavings; }
        public void setTotalSavings(int totalSavings) { this.totalSavings = totalSavings; }
        
        public int getCompletedSavings() { return completedSavings; }
        public void setCompletedSavings(int completedSavings) { this.completedSavings = completedSavings; }
        
        public double getTotalTargetAmount() { return totalTargetAmount; }
        public void setTotalTargetAmount(double totalTargetAmount) { this.totalTargetAmount = totalTargetAmount; }
        
        public double getTotalCurrentAmount() { return totalCurrentAmount; }
        public void setTotalCurrentAmount(double totalCurrentAmount) { this.totalCurrentAmount = totalCurrentAmount; }
        
        public int getActiveSavings() {
            return totalSavings - completedSavings;
        }
        
        public double getOverallCompletionPercentage() {
            if (totalTargetAmount == 0) return 0;
            return (totalCurrentAmount / totalTargetAmount) * 100;
        }
        
        public double getTotalRemainingAmount() {
            return Math.max(0, totalTargetAmount - totalCurrentAmount);
        }
    }
}