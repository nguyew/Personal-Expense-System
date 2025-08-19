package com.expensemanager.dao;

import com.expensemanager.model.Transaction;
import com.expensemanager.util.DateUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {
    
    // Create new transaction
    public boolean createTransaction (Transaction transaction) {
        String sql = "INSERT INTO Transactions (UserID, CategoryID, Amount, TransactionType, Description, TransactionDate, Location, Notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, transaction.getUserID());
            pstmt.setInt(2, transaction.getCategoryID());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getTransactionType());
            pstmt.setString(5, transaction.getDescription());
            pstmt.setDate(6, new java.sql.Date(transaction.getTransactionDate().getTime()));
            pstmt.setString(7, transaction.getLocation());
            pstmt.setString(8, transaction.getNotes());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setTransactionID(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get transaction by ID
    
    public Transaction getTransactionById (int transactionID) {
        String sql = "SELECT * FROM vw_TransactionDetails WHERE TransactionID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, transactionID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaction by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Get transaction by user
    
    public List<Transaction> getTransactionsByUser (int userID) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM vw_TransactionDetails WHERE UserID = ? ORDER BY TransactionDate DESC, CreatedDate DESC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting transactions by user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    // Get transaction by date range
    public List<Transaction> getTransactionsByDateRange (int userID, Date startDate, Date endDate) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM vw_TransactionDetails WHERE UserID = ? AND TransactionDate BETWEEN ? AND ? ORDER BY TransactionDate DESC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setDate(2, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(3, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            } 
        } catch (SQLException e) {
            System.err.println("Error getting transations by date range: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    // Get transactions by category
    public List<Transaction> getTransactionsByCategory (int userID, int categoryID) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM vw_TransactionDetails WHERE UserID = ? AND CategoryID = ? ORDER BY TransactionDate DESC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, categoryID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting transactions by category  " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    // Search transactions
    public List<Transaction> searchTransactions(int userID, String keyword, Date startDate, Date endDate) {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM vw_TransactionDetails WHERE UserID = ?");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (Description LIKE ? OR CategoryName LIKE ? OR Location LIKE ?)");
        }
        
        if (startDate != null && endDate != null) {
            sql.append(" AND TransactionDate BETWEEN ? AND ?");
        }
        
        sql.append(" ORDER BY TransactionDate DESC");
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            pstmt.setInt(paramIndex++, userID);
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            
            if (startDate != null && endDate != null) {
                pstmt.setDate(paramIndex++, new java.sql.Date(startDate.getTime()));
                pstmt.setDate(paramIndex++, new java.sql.Date(endDate.getTime()));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    // Update transaction
    public boolean updateTransaction(Transaction transaction) {
        String sql = "UPDATE Transactions SET CategoryID = ?, Amount = ?, TransactionType = ?, Description = ?, TransactionDate = ?, Location = ?, Notes = ?, ModifiedDate = GETDATE() WHERE TransactionID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, transaction.getCategoryID());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getTransactionType());
            pstmt.setString(4, transaction.getDescription());
            pstmt.setDate(5, new java.sql.Date(transaction.getTransactionDate().getTime()));
            pstmt.setString(6, transaction.getLocation());
            pstmt.setString(7, transaction.getNotes());
            pstmt.setInt(8, transaction.getTransactionID());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Delele transaction
    public boolean deleteTransaction (int transactionID) {
        String sql = "DELETE FROM Transactions WHERE TransactionID = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, transactionID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get total amount by category and period
    public double getTotalAmountByCategory(int userID, int categoryID, String transactionType, Date startDate, Date endDate) {
        String sql = "SELECT ISNULL(SUM(Amount), 0) FROM Transactions WHERE UserID = ? AND CategoryID = ? AND TransactionType = ? AND TransactionDate BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, categoryID);
            pstmt.setString(3, transactionType);
            pstmt.setDate(4, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(5, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total amount by category: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    // Get expense summary by category for period
    public Map<String, Double> getExpenseSummaryByCategory(int userID, Date startDate, Date endDate) {
        Map<String, Double> summary = new HashMap<>();
        String sql = "SELECT c.CategoryName, SUM(t.Amount) as TotalAmount FROM Transactions t " +
                    "INNER JOIN Categories c ON t.CategoryID = c.CategoryID " +
                    "WHERE t.UserID = ? AND t.TransactionType = 'EXPENSE' AND t.TransactionDate BETWEEN ? AND ? " +
                    "GROUP BY c.CategoryName ORDER BY TotalAmount DESC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setDate(2, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(3, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    summary.put(rs.getString("CategoryName"), rs.getDouble("TotalAmount"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting expense summary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return summary;
    }
    
    // Get income vs expense for period
    public Map<String, Double> getIncomeVsExpense(int userID, Date startDate, Date endDate) {
        Map<String, Double> result = new HashMap<>();
        String sql = "SELECT TransactionType, SUM(Amount) as TotalAmount FROM Transactions " +
                    "WHERE UserID = ? AND TransactionDate BETWEEN ? AND ? " +
                    "GROUP BY TransactionType";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setDate(2, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(3, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("TransactionType"), rs.getDouble("TotalAmount"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting income vs expense: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // Get recent transactions (for dashboard)
    public List<Transaction> getRecentTransactions(int userID, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT TOP (?) * FROM vw_TransactionDetails WHERE UserID = ? ORDER BY TransactionDate DESC, CreatedDate DESC";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            pstmt.setInt(2, userID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting recent transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    // Get monthly statistics
    public Map<String, Object> getMonthlyStatistics(int userID, int month, int year) {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                    "SUM(CASE WHEN TransactionType = 'INCOME' THEN Amount ELSE 0 END) as TotalIncome, " +
                    "SUM(CASE WHEN TransactionType = 'EXPENSE' THEN Amount ELSE 0 END) as TotalExpense, " +
                    "COUNT(CASE WHEN TransactionType = 'INCOME' THEN 1 END) as IncomeCount, " +
                    "COUNT(CASE WHEN TransactionType = 'EXPENSE' THEN 1 END) as ExpenseCount " +
                    "FROM Transactions WHERE UserID = ? AND MONTH(TransactionDate) = ? AND YEAR(TransactionDate) = ?";
        
        try (Connection conn = DatabaseConnection.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double totalIncome = rs.getDouble("TotalIncome");
                    double totalExpense = rs.getDouble("TotalExpense");
                    
                    stats.put("totalIncome", totalIncome);
                    stats.put("totalExpense", totalExpense);
                    stats.put("netAmount", totalIncome - totalExpense);
                    stats.put("incomeCount", rs.getInt("IncomeCount"));
                    stats.put("expenseCount", rs.getInt("ExpenseCount"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting monthly statistics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }

   // Helper method to map ResultSet to Transaction
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionID(rs.getInt("TransactionID"));
        transaction.setUserID(rs.getInt("UserID"));
        transaction.setCategoryID(rs.getInt("CategoryID"));
        transaction.setAmount(rs.getDouble("Amount"));
        transaction.setTransactionType(rs.getString("TransactionType"));
        transaction.setDescription(rs.getString("Description"));
        transaction.setTransactionDate(rs.getDate("TransactionDate"));
        transaction.setLocation(rs.getString("Location"));
        transaction.setCreatedDate(rs.getTimestamp("CreatedDate"));
        
        // Additional fields from view
        transaction.setCategoryName(rs.getString("CategoryName"));
        transaction.setCategoryColor(rs.getString("CategoryColor"));
        transaction.setUserName(rs.getString("UserName"));
        
        return transaction;
    }
}
