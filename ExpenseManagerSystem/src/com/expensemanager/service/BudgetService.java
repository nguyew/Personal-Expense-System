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
    
    // Update existing budget
    public ServiceResult<Budget> updateBudget (Budget budget) {
        try {
            // Validate input data
            ServiceResult<Void> validation = validateBudgetData(
                    budget.getUserID(),
                    budget.getCategoryID(),
                    budget.getBudgetAmount(),
                    budget.getMonth(),
                    budget.getYear(),
                    budget.getAlertThreshold()
            );
            
            if (!validation.isSuccess()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if budget exists and belongs to user
            Budget existingBudget = budgetDAO.getBudgetById(budget.getBudgetID());
            if (existingBudget == null) {
                return ServiceResult.error("Không tìm thấy ngân sách");
            }
            
            if (existingBudget.getUserID() != budget.getUserID()) {
                return ServiceResult.error("Bạn không có quyền sửa ngân sách này");
            }
            
            // Update modification date
            budget.setModifiedDate(new Date());
            
            // Recalculate current spent and status
            double currentSpent = calculateSpentAmount(budget.getUserID(), budget.getCategoryID(),
                                                     budget.getMonth(), budget.getYear());
            budget.setCurrentSpent(currentSpent);
            budget.updateStatus();
            
            // Update budget
            boolean updated = budgetDAO.updateBudget(budget);
            
            if (updated) {
                // Get category name for response
                Category category = categoryDAO.getCategoryById(budget.getCategoryID());
                budget.setCategoryName(category.getCategoryName());
                
                return ServiceResult.success(budget, "ngân sách đã được cập nhật");
            } else {
                return ServiceResult.error("Không thể cập nhật danh sách");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Delete budget
    public ServiceResult<Void> deleteBudget (int budgetID, int userID) {
        try {
            // Check if budget exists and belongs to user
            Budget budget = budgetDAO.getBudgetById(budgetID);
            if (budget == null) {
                return ServiceResult.error("Không tìm thấy ngân sách");
            }
            
            if (budget.getUserID() != userID) {
                return ServiceResult.error("Bạn không quyền xóa ngân sách này");
            }
            
            // Delete budget
            boolean deleted = budgetDAO.deleteBudget(budgetID);
            
            if (deleted) {
                return ServiceResult.success("Ngân sách đã được xóa");
            } else {
                return ServiceResult.error("Không thể xóa ngân sách");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Get all budget for a user in specific period
    public ServiceResult<List<Budget>> getBudgetByPeriod (int userID, int month, int year) {
        try {
            List<Budget> budgets = budgetDAO.getBudgetsByUserAndPeriod(userID, month, year);
            
            // Update spent amounts and status for all budgets
            for (Budget budget : budgets) {
                double currentSpent = calculateSpentAmount(userID, budget.getCategoryID(), month, year);
                budget.setCurrentSpent(currentSpent);
                budget.updateStatus();
                
                // Get category name
                Category category = categoryDAO.getCategoryById(budget.getCategoryID());
                budget.setCategoryName(category.getCategoryName());
            }
            
            // Sort by status (alerts first) and then by budget amount
            budgets.sort((b1, b2) -> {
                // Status priority: EXCEEDED > WARNING > OK
                int statusCompare = getStatusPriority(b2.getStatus()) - getStatusPriority(b1.getStatus());
                if (statusCompare != 0) return statusCompare;
                
                // Then by budget amount descending
                return Double.compare(b2.getBudgetAmount(), b1.getBudgetAmount());
            });
            
            return ServiceResult.success(budgets, "Lấy danh sách ngân sách thành công");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Get budget alerts for current period
    public ServiceResult<List<Budget>> getBudgetAlerts (int userID) {
        try {
            int currentMonth = DateUtils.getCurrentMonth();
            int currentYear = DateUtils.getCurrentYear();
            
            List<Budget> allBudgets = budgetDAO.getBudgetsByUserAndPeriod(userID, currentMonth, currentYear);
            List<Budget> alerts = new ArrayList<>();
            
            // Filter budgets that have alerts (WARNING or EXCEED)
            for (Budget budget : allBudgets) {
                double currentSpent = calculateSpentAmount(userID, budget.getCategoryID(), currentMonth, currentYear);
                budget.setCurrentSpent(currentSpent);
                budget.updateStatus();
                
                if ("WARNING".equals(budget.getStatus()) || "EXCEEDED".equals(budget.getStatus())) {
                    // Get category name
                    Category category = categoryDAO.getCategoryById(budget.getCategoryID());
                    budget.setCategoryName(category.getCategoryName());
                    alerts.add(budget);
                }
            }
            
            // Sort by severity (EXCEED first)
            alerts.sort((b1, b2) -> getStatusPriority(b2.getStatus()) - getStatusPriority(b1.getStatus()));
            
            return ServiceResult.success(alerts, "Lấy danh sách cảnh báo ngân sách thành công");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Get budget summary for a period
    public ServiceResult<BudgetSummary> getBudgetSummary (int userID, int month, int year) {
        try {
            List<Budget> budgets = budgetDAO.getBudgetsByUserAndPeriod(userID, month, year);
            
            BudgetSummary summary = new BudgetSummary();
            summary.setMonth(month);
            summary.setYear(year);
            summary.setTotalBudgets(budgets.size());
            
            double totalBudgetAmount = 0;
            double totalSpentAmount = 0;
            int warningCount = 0;
            int exceededCount = 0;
            
            for (Budget budget : budgets) {
                double currentSpent = calculateSpentAmount(userID, budget.getCategoryID(), month, year);
                budget.setCurrentSpent(currentSpent);
                budget.updateStatus();
                
                totalBudgetAmount += budget.getBudgetAmount();
                totalSpentAmount += currentSpent;
                
                switch (budget.getStatus()) {
                    case "WARNING":
                        warningCount++;
                        break;
                    case "EXCEEDED":
                        exceededCount++;
                        break;
                }
            }
            
            summary.setTotalBudgetAmount(totalBudgetAmount);
            summary.setTotalSpentAmount(totalSpentAmount);
            summary.setWarningCount(warningCount);
            summary.setExceededCount(exceededCount);
            summary.setOkCount(budgets.size() - warningCount - exceededCount);
            
            return ServiceResult.success(summary, "Lấy tổng hợp ngân sách thành công");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Create budget from previous month
    public ServiceResult<List<Budget>> copyBudgetFromPreviousMonth (int userID, int targetMonth, int targetYear) {
        try {
            // Calculate previous month
            int prevMonth = targetMonth - 1;
            int prevYear = targetYear;
            if (prevMonth <= 0) {
                prevMonth = 12;
                prevYear--;
            }
            
            // Get budgets from previous month
            List<Budget> previousBudgets = budgetDAO.getBudgetsByUserAndPeriod(userID, prevMonth, prevYear);
            if (previousBudgets.isEmpty()) {
                return ServiceResult.error("Không tìm thấy ngân sách tháng trước để sao chép");
            }
            
            // Check if target month already has budgets
            List<Budget> existingBudgets = budgetDAO.getBudgetsByUserAndPeriod(userID, targetMonth, targetYear);
            if (!existingBudgets.isEmpty()) {
                return ServiceResult.error("Tháng " + targetMonth + "/" + targetYear);
            }
            
            List<Budget> newBudgets = new ArrayList<>();
            
            // Create new budgets based on previous month
            for (Budget prevBudget : previousBudgets) {
                Budget newBudget = new Budget();
                newBudget.setUserID(userID);
                newBudget.setCategoryID(prevBudget.getCategoryID());
                newBudget.setBudgetAmount(prevBudget.getBudgetAmount());
                newBudget.setMonth(targetMonth);
                newBudget.setYear(targetYear);
                newBudget.setAlertThreshold(prevBudget.getAlertThreshold());
                newBudget.setCreatedDate(new Date());
                newBudget.setModifiedDate(new Date());
                newBudget.setCurrentSpent(0.0); // New month, no spending yet
                newBudget.setStatus("OK");
                
                boolean created = budgetDAO.createBudget(newBudget);
                if (created) {
                    // Get category name
                    Category category = categoryDAO.getCategoryById(newBudget.getCategoryID());
                    newBudget.setCategoryName(category.getCategoryName());
                    newBudgets.add(newBudget);
                }
            }
            
            if (newBudgets.isEmpty()) {
                return ServiceResult.error("Không thể sao chép ngân sách");
            }
            
            return ServiceResult.success(newBudgets,
                    "Đã sao chép " + newBudgets.size() + " ngân sách từ tháng " + prevMonth + "/" + prevYear);
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Suggest budget amounts based on historical ending
    public ServiceResult<Map<Integer, Double>> suggestBudgetAmounts (int userID, int month, int year) {
        try {
            // Get expense categories for user
            List<Category> expenseCategories = categoryDAO.getCategoriesByUserAndType(userID, "EXPENSE");
            Map<Integer, Double> suggestions = new HashMap<>();
            
            // Look at last 3 months spending for each category
            for (Category category : expenseCategories) {
                double totalSpent = 0;
                int monthsWithData = 0;
                
                for (int i = 1; i <= 3; i++) {
                    int checkMonth = month - i;
                    int checkYear = year;
                    if (checkMonth <= 0) {
                        checkMonth += 12;
                        checkYear--;
                    }
                    
                    double monthSpent = calculateSpentAmount(userID, category.getCategoryID(), checkMonth, checkYear);
                    if (monthSpent > 0) {
                        totalSpent += monthSpent;
                        monthsWithData++;
                    }
                }
                
                if (monthsWithData > 0) {
                    double averageSpent = totalSpent / monthsWithData;
                    // Suggest 10% more than average spending
                    double suggestedBudget = averageSpent * 1.1;
                    suggestions.put(category.getCategoryID(), suggestedBudget);
;                }
            }
            
            return ServiceResult.success(suggestions, "Đã tạo gợi ý ngân sách dựa trên lịch sử chi tiêu");
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

    private int getStatusPriority(String status) {
        switch (status) {
            case "EXCEED": return 3;
            case "WARNING": return 2;
            case "OK": return 1;
            default: return 0;
        }
    }
}
