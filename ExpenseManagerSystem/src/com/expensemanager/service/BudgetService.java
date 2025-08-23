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
            Budget existingBudget = budgetDAO.getBudgetByCategoryAndPeriod(userID, categoryID, month, year);
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
            double currentSpent = calculateSpentAmount();
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

    private ServiceResult<Void> validateBudgetData(int userID, int categoryID, double budgetAmount, int month, int year, double alertThreshold) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private double calculateSpentAmount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
