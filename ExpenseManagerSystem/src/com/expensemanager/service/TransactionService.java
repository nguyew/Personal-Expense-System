package com.expensemanager.service;

import com.expensemanager.dao.*;
import com.expensemanager.model.*;
import com.expensemanager.util.DateUtils;
import com.expensemanager.util.CurrencyUtils;
import java.sql.Date;
import java.util.*;

public class TransactionService {
    
    private final DAOFactory daoFactory;
    private final TransactionDAO transactionDAO;
    private final CategoryDAO categoryDAO;
    private final BudgetDAO budgetDAO;
    private final UserDAO userDAO;
    
    public TransactionService() {
        this.daoFactory = DAOFactory.getInstance();
        this.transactionDAO = daoFactory.getTransactionDAO();
        this.categoryDAO = daoFactory.getCategoryDAO();
        this.budgetDAO = daoFactory.getBudgetDAO();
        this.userDAO = daoFactory.getUserDAO();
    }
    
    // Create new transaction
    public ServiceResult<Transaction> createTransaction (int userID, int categoryID, double amount,
                                                        String transactionType, String description,
                                                        Date transactionDate, String location, String notes) {
        try {
            // Validate input data
            ServiceResult<Void> validation = validateTransactionData(userID, categoryID, amount, 
                                                                    transactionType, description, transactionDate);
            if (!validation.isSuccess()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Create transaction object
            Transaction transaction = new Transaction();
            transaction.setUserID(userID);
            transaction.setCategoryID(categoryID);
            transaction.setAmount(amount);
            transaction.setTransactionType(transactionType.toUpperCase());
            transaction.setDescription(description != null ? description.trim() : "");
            transaction.setTransactionDate(transactionDate);
            transaction.setLocation(location);
            transaction.setNotes(notes != null ? notes.trim() : "");
            transaction.setCreatedDate(new java.util.Date());
            transaction.setModifieldDate(new java.util.Date());
            
            // Save transaction
            boolean created = transactionDAO.createTransaction(transaction);
            
            if (created) {
                // Update budget tracking if this is an expense
                if ("EXPENSE".equals(transactionType.toUpperCase())) {
                    updateBudgetTracking(userID, categoryID, transactionDate);
                }
                
                return ServiceResult.success(transaction, "Giao dịch đã được tạo thành công");
            } else {
                return ServiceResult.error("Không thể tạo giao dịch");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }   
    }
    
    // Update existing transaction
    public ServiceResult<Transaction> updateTransaction (Transaction transaction) {
        try {
            // Validate input data
            ServiceResult<Void> validation = validateTransactionData(
                    transaction.getUserID(),
                    transaction.getCategoryID(),
                    transaction.getAmount(),
                    transaction.getTransactionType(),
                    transaction.getDescription(),
                    new Date(transaction.getTransactionDate().getTime())
            );
            
            if (!validation.isSuccess()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if transaction exists and belong to user
            Transaction existingTransaction = transactionDAO.getTransactionById(transaction.getTransactionID());
            if (existingTransaction == null) {
                return ServiceResult.error("Không tìm thấy giao dịch");
            }
            
            if (existingTransaction.getUserID() != transaction.getUserID()) {
                return ServiceResult.error("Bạn không có quyền sử dung giao dịch này");
            }
            
            // Update modification date
            transaction.setModifieldDate(new java.util.Date());
            
            // Update transaction
            boolean updated = transactionDAO.updateTransaction(transaction);
            
            if (updated) {
                // Update budget tracking if this is an expense
                if ("EXPENSE".equals(transaction.getTransactionType())) {
                    updateBudgetTracking(transaction.getUserID(), transaction.getCategoryID(),
                                        new Date(transaction.getTransactionDate().getTime()));
                }
                
                return ServiceResult.success(transaction, "Giao dịch đã được cập nhật");
            } else {
                return ServiceResult.error("Không thể cập nhật giao dịch");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    private ServiceResult<Void> validateTransactionData(int userID, int categoryID, double amount,
                                                       String transactionType, String description, Date transactionDate) {
        // Check user exists
        User user = userDAO.getUserById(userID);
        if (user == null) {
            return ServiceResult.error("Không tìm thấy tài khoản người dùng");
        }
        
        // Check category exists and belong to user
        Category category = categoryDAO.getCategoryById(categoryID);
        if (category == null) {
            return ServiceResult.error("Không tìm thấy dạnh mục");
        }
        
        if (category.getUserID() != userID && !category.isDefault()) {
            return ServiceResult.error("Bạn không có quyền sử dụng danh mục này");
        }
        
        // Validate amount
        if (amount <= 0) {
            return ServiceResult.error("Số tiền phải lớn hơn 0");
        }
        
        if (amount > 999999999999.99) { // ~1 trillion VNĐ
            return ServiceResult.error("Số tiền quá lớn");
        }
        
        // Validate transaction type
        if (transactionType == null ||
            (!transactionType.toUpperCase().equals("INCOME") && !transactionType.toUpperCase().equals("EXPENSE"))) {
            return ServiceResult.error("Loại giao dịch này phải là INCOME hoặc EXPENSE");
        }
        
        // Check if transaction type matches category type
        if (!transactionType.toUpperCase().equals(category.getCategoryType())) {
            return ServiceResult.error("Loại giao dịch này không khớp với loại danh mục");
        }
        
        // Validate description
        if (description != null && description.trim().length() > 500) {
            return ServiceResult.error("Mô tả không được quá 500 ký tự");
        }
        
        // Validata transaction date
        if (transactionDate == null) {
            return ServiceResult.error("Ngày giao dịch không được để trống");
        }
        
        Calendar today = Calendar.getInstance();
        Calendar transactionCal = Calendar.getInstance();
        transactionCal.setTime(transactionDate);
        
        // Check if date is not in future (allow today)
        if (transactionCal.after(today)) {
            return ServiceResult.error("Ngày giao dịch không thể là ngày trong tương lai");
        }
        
        // Check if date is not too far in past (5 years)
        Calendar fiveYearsAgo = Calendar.getInstance();
        fiveYearsAgo.add(Calendar.YEAR, -5);
        
        if (transactionCal.before(fiveYearsAgo)) {
            return ServiceResult.error("Ngày giao dịc không thể quá 5 năm trước");
        }
        
        return ServiceResult.success("Dữ liệu hợp lệ");
    }

    private void updateBudgetTracking(int userID, int categoryID, Date transactionDate) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(transactionDate);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            
            budgetDAO.getBudgetsByUserAndPeriod(userID, month, year);
        } catch (Exception e) {
            System.err.println("Warning: Could not update budget tracking: " + e.getMessage());
        }
    }
}
