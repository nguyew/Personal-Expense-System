package com.expensemanager.service;

import com.expensemanager.dao.*;
import com.expensemanager.model.*;
import com.expensemanager.util.DateUtils;
import com.expensemanager.util.CurrencyUtils;
import java.util.*;

public class BudgetService {
   
    private final DAOFactory daoFactory;
    private final BudgetDAO budgetDAO;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;
    private final UserDAO userDAO;
    
    public BudgetService () {
        this.daoFactory = DAOFactory.getInstance();
        this.budgetDAO = daoFactory.getBudgetDAO();
        this.categoryDAO = daoFactory.getCategoryDAO();
        this.transactionDAO = daoFactory.getTransactionDAO();
        this.userDAO = daoFactory.getUserDAO();
    }
    
    // Create new budget for a category
    public ServiceResult<Budget> createBudget (int userID, int categoryID, double budgetAmount,
                                            int month, int year, double alertThreshold) {
        try {
            // Validate input data
            ServiceResult<Void> validation = validateBudgetData(userID, categoryID, budgetAmount,
                                                              month, year, alertThreshold);
            if (!validation.isSuccess()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if budget already exists for this categpry and period
            Budget existingBudget = budgetDAO.getBudgetByCategory(userID, categoryID, month, year);
            if (existingBudget != null) {
                return ServiceResult.error("Ngân sách cho danh mục này trong tháng " + month + "/" + year + " đã tồn tại");
            }
            
            // Create budget object
            Budget budget = new Budget();
            budget.setUserID(userID);
            budget.setCategoryID(categoryID);
            budget.setBudgetAmount(budgetAmount);
            budget.setMonth(month);
            budget.setYear(year);
            budget.setAlertThreshold(alertThreshold);
            budget.setCreatedDate(new Date());
            budget.setModifiedDate(new Date());
            
            // Calculate current amount
            double currentSpent = calculateSpentAmount(userID, categoryID, month, year);
            budget.setCurrentSpent(currentSpent);
            budget.updateStatus();
            
            // Save budget
            boolean created = budgetDAO.createBudget(budget);
            
            if (created) {
                // Get category name for response 
                Category category = categoryDAO.getCategoryById(categoryID);
                budget.setCategoryName(category.getCategoryName());
                
                return ServiceResult.success(budget, "Ngân sách đã được tạo thành công");
            } else {
                return ServiceResult.error("Không thể tạo ngân sách");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    private ServiceResult<Void> validateBudgetData(int userID, int categoryID, double budgetAmount, 
                                                 int month, int year, double alertThreshold) {
        // Check user exists
        User user = userDAO.getUserById(userID);
        if (user == null) {
            return ServiceResult.error("Không tìm thấy tài khoản người dùng");
        }
        
        // Check category exists and belongs to user or is default
        Category category = categoryDAO.getCategoryById(categoryID);
        if (category == null) {
            return ServiceResult.error("Không tìm thấy danh mục");
        }
        
        if (category.getUserID() != userID && !category.isDefault()) {
            return ServiceResult.error("Bạn không có quyền sử dụng mục này");
        }
        
        // Only allows expense categories for budget
        if (!"EXPENSE".equals(category.getCategoryType())) {
            return ServiceResult.error("Chỉ có thể tạo danh sách cho danh mục chi tiêu");
        }
        
        // Validate budget amount
        if (budgetAmount <= 0) {
            return ServiceResult.error("Số tiền ngân sách phải lớn hơn 0");
        }
        
        if (budgetAmount > 999999999999.99) { // ~1 trillion VNĐ
            return ServiceResult.error("Số tiền ngân sách quá lớn");
        }
        
        // Validate month and year
        if (month < 1 || month > 12) {
            return ServiceResult.error("Tháng phải từ 1 đến 12");
        }
        
        if (year < 2020 || year > 2050) {
            return ServiceResult.error("Năm không hợp lệ");
        }
        
        // Validate alert threshold 
        if (alertThreshold < 0 || alertThreshold > 100) {
            return ServiceResult.error("Ngưỡng cảnh báo phải từ 0% đến 100%");
        }
        
        return ServiceResult.success("Dữ liêu hợp lệ");
    }

    private double calculateSpentAmount(int userID, int categoryID, int month, int year) { 
        try {
            return transactionDAO.getTotalExpenseByCategory(userID, categoryID, month, year);
        } catch (Exception e) {
            System.err.println("Warning: Could not calculate spent amount: " + e.getMessage());
            return 0.0;
        }
    }
}
