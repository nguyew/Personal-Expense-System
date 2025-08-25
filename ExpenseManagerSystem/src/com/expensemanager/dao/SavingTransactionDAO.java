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
}
