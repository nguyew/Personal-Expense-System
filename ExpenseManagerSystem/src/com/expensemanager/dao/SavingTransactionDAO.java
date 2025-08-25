package com.expensemanager.dao;

import com.expensemanager.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavingTransactionDAO {
    
    // Create new saving transaction
    public boolean createSavingTransaction (SavingTransaction savingTransaction) {
        String sql = "INSERT INTO SavingTransactions (SavingID, Amount, TransactionType, Description, TransactionDate) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, savingTransaction.getSavingID());
            pstmt.setDouble(2, savingTransaction.getAmount());
            pstmt.setString(3, savingTransaction.getTransactionType());
            pstmt.setString(4, savingTransaction.getDescription());
            pstmt.setDate(5, savingTransaction.getTransactionDate() != null ?
                new Date(savingTransaction.getTransactionDate().getTime()) : new Date(System.currentTimeMillis()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()){
                    if (generatedKeys.next()) {
                        savingTransaction.setSavingTransactionID(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating saving transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get saving transaction by ID
    public SavingTransaction getSavingTransactionById (int savingTransactionID) {
        String sql = "SELECT * FROM SavingTransactions WHERE SavingTransactionID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, savingTransactionID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSavingTransaction(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting saving transaction by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Helper method to map ResultSet to SavingTransaction
    private SavingTransaction mapResultSetToSavingTransaction(ResultSet rs) throws SQLException {
        SavingTransaction transaction = new SavingTransaction();
        transaction.setSavingTransactionID(rs.getInt("SavingTransactionID"));
        transaction.setSavingID(rs.getInt("SavingID"));
        transaction.setAmount(rs.getDouble("Amount"));
        transaction.setTransactionType(rs.getString("TransactionType"));
        transaction.setDescription(rs.getString("Description"));
        transaction.setTransactionDate(rs.getDate("TransactionDate"));
        transaction.setCreatedDate(rs.getTimestamp("CreatedDate"));
        return transaction;
    }
    
    // Inner class for transaction summary
    public static class SavingTransactionSummary {
        private int savingID;
        private int totalTransactions;
        private int totalDeposits;
        private int totalWithdrawals;
        private double totalDepositAmount;
        private double totalWithdrawalAmount;
        
        // Getters and Setters
        public int getSavingID() { return savingID; }
        public void setSavingID(int savingID) { this.savingID = savingID; }
        
        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
        
        public int getTotalDeposits() { return totalDeposits; }
        public void setTotalDeposits(int totalDeposits) { this.totalDeposits = totalDeposits; }
        
        public int getTotalWithdrawals() { return totalWithdrawals; }
        public void setTotalWithdrawals(int totalWithdrawals) { this.totalWithdrawals = totalWithdrawals; }
        
        public double getTotalDepositAmount() { return totalDepositAmount; }
        public void setTotalDepositAmount(double totalDepositAmount) { this.totalDepositAmount = totalDepositAmount; }
        
        public double getTotalWithdrawalAmount() { return totalWithdrawalAmount; }
        public void setTotalWithdrawalAmount(double totalWithdrawalAmount) { this.totalWithdrawalAmount = totalWithdrawalAmount; }
        
        public double getNetAmount() {
            return totalDepositAmount - totalWithdrawalAmount;
        }
    }
        
}
