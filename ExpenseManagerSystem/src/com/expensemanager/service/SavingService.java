package com.expensemanager.service;

import com.expensemanager.dao.*;
import com.expensemanager.model.*;
import com.expensemanager.util.DateUtils;
import com.expensemanager.util.CurrencyUtils;
import java.util.*;

public class SavingService {
    
    private final DAOFactory daoFactory;
    private final SavingDAO savingDAO;
    private final SavingTransactionDAO savingTransactionDAO;
    private final UserDAO userDAO;
    
    public SavingService() {
        this.daoFactory = DAOFactory.getInstance();
        this.savingDAO = daoFactory.getSavingDAO();
        this.savingTransactionDAO = daoFactory.getSavingTransactionDAO();
        this.userDAO = daoFactory.getUserDAO();
    }

    
    // Creat new saving goal
    public ServiceResult<Saving> createSaving (int userID, String savingName, String description,
                                            double targetAmount, Date targetDate, int priority) {
        try {            // Validate input data
            ServiceResult<Void> validation = validateSavingData(userID, savingName, description,
                                                              targetAmount, targetDate, priority);
            
            if (!validation.isSuccess()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if saving name already exists for this user
            List<Saving> existingSavings = savingDAO.getSavingsByUser(userID);
            for (Saving saving : existingSavings) {
                if (saving.getSavingName().equalsIgnoreCase(savingName.trim())) {
                    return ServiceResult.error("Mục tiêu tiết kiệm '" + savingName + "' đã tồn tại");
                }
            }
            
            // Create saving object
            Saving saving = new Saving();
            saving.setUserID(userID);
            saving.setSavingName(savingName.trim());
            saving.setDescription(description != null ? description.trim() : "");
            saving.setTargetAmount(targetAmount);
            saving.setCurrentAmount(0.0);
            saving.setTargetDate(targetDate);
            saving.setPriority(priority);
            saving.setIsCompleted(false);
            saving.setCreatedDate(new Date());
            
            // Save to database
            boolean created = savingDAO.createSaving(saving);
            
            if (created) {
                return ServiceResult.success(saving, "Mục tiêu tiết kiệm đã được tạo thành công");
            } else {
                return ServiceResult.error("Không thể tạo mục tiết kiệm");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Update existing saving goal
    public ServiceResult<Saving> updateSaving (Saving saving) {
        try {
            // Validate input data
            ServiceResult<Void> validation = validateSavingData(
                saving.getUserID(),
                saving.getSavingName(),
                saving.getDescription(),
                saving.getTargetAmount(),
                saving.getTargetDate(),
                saving.getPriority()
            );
            
            if (!validation.isSuccess()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if saving exists and belongs to user
            Saving existingSaving = savingDAO.getSavingById(saving.getSavingID());
            if (existingSaving == null) {
                return ServiceResult.error("Không tìm thấy mục tiêu tiết kiệm");
            }
            
            if (existingSaving.getUserID() != saving.getUserID()) {
                return ServiceResult.error("Bạn không có quyền sửa mục tiêu tiết kiệm này");
            }
            
            // Do not allow editing completed savings
            if (existingSaving.isIsCompleted()) {
                return ServiceResult.error("Không thể sửa mục tiêu tiết kiệm đã hoàn thành");
            }
            
            // Check if new name conflicts with existing savings (except current one)
            List<Saving> existingSavings = savingDAO.getSavingsByUser(saving.getUserID());
            for (Saving s : existingSavings) {
                if (s.getSavingID() != saving.getSavingID() &&
                    s.getSavingName().equalsIgnoreCase(saving.getSavingName().trim())) {
                    return ServiceResult.error("Mục tiêu tiết kiệm '" + saving.getSavingName() + "' đã tồn tại");
                }
            }
            
            // Preserve current amount and completion status
            saving.setCurrentAmount(existingSaving.getCurrentAmount());
            saving.setIsCompleted(existingSaving.isIsCompleted());
            saving.setCompletedDate(existingSaving.getCompletedDate());
            
            // Check if goal is now completed with new target amount
            if (saving.getCurrentAmount() >= saving.getTargetAmount() && !saving.isIsCompleted()) {
                saving.setIsCompleted(true);
                saving.setCompletedDate(new Date());
            }
            
            // Update saving
            boolean updated = savingDAO.updateSaving(saving);
            
            if (updated) {
                return ServiceResult.success(saving, "Mục tiêu tiết kiệm đã được cập nhật");
            } else {
                return ServiceResult.error("Không thể cập nhật mục tiêu tiết kiệm");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Delete saving goal
    public ServiceResult<Void> deleteSaving (int savingID, int userID) {
        try {
            // Check if saving exists and belongs to user
            Saving saving = savingDAO.getSavingById(savingID);
            if (saving == null) {
                return ServiceResult.error("Không tìm thấy mục tiêu tiết kiệm");
            }
            
            if (saving.getUserID() != userID) {
                return ServiceResult.error("Bạn không có quyền xóa mục tiêu tiết kiệm này");
            }
            
            // Check if saving has transactions
            int transactionCount = savingTransactionDAO.getSavingTransactionsCountBySaving(savingID);
            if (transactionCount > 0) {
                return ServiceResult.error("Không thể xóa mục tiêu tiết kiệm đang có " + transactionCount + " giao dịch");
            }
            
            // Delete saving
            boolean deleted = savingDAO.deleteSaving(savingID);
            
            if (deleted) {
                return ServiceResult.success("Mục tiêu tiết kiệm đã được xóa");
            } else {
                return ServiceResult.error("Không thể xóa mục tiêu tiết kiệm");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Add money to saving goal
    public ServiceResult<SavingTransaction> depositToSaving (int savingID, int userID, double amount, String description) {
        try {
            // Validate amount
            if (amount <= 0) {
                return ServiceResult.error("Số tiền gửi phải lớn hơn 0");
            }
            
            if (amount > 999999999999.99) {
                return ServiceResult.error("Số tiền gửi quá lớn");
            }
            
            // Check if saving exists and belongs to user
            Saving saving = savingDAO.getSavingById(savingID);
            if (saving == null) {
                return ServiceResult.error("Không tìm thấy mục tiêu tiết kiệm");
            }
            
            if (saving.getUserID() != userID) {
                return ServiceResult.error("Bạn không có quyền thêm tiền vào mục tiêu này");
            }
            
            if (saving.isIsCompleted()) {
                return ServiceResult.error("Mục tiêu tiết kiệm đã hoàn thành , không thể thêm tiền");
            }
            
            // Create saving transaction
            SavingTransaction transaction = new SavingTransaction();
            transaction.setSavingID(savingID);
            transaction.setAmount(amount);
            transaction.setTransactionType("DEPOSIT");
            transaction.setDescription(description != null ? description.trim(): "Gửi tiền tiết kiệm");
            transaction.setTransactionDate(new Date());
            transaction.setCreatedDate(new Date());
            
            // Add transaction
            boolean created = savingTransactionDAO.createSavingTransaction(transaction);
            
            if (created) {
                // Update saving current amount
                double newCurrentAmount = saving.getCurrentAmount() + amount;
                saving.setCurrentAmount(newCurrentAmount);
                
                // Check if goal is completed
                if (newCurrentAmount >= saving.getTargetAmount() && !saving.isIsCompleted()) {
                    saving.setIsCompleted(true);
                    saving.setCompletedDate(new Date());
                }
                
                savingDAO.updateSaving(saving);
                String message = "Đã gửi " + CurrencyUtils.formatCurrency(amount) + " vào mục tiêu tiết kiệm";
                if (saving.isIsCompleted()) {
                    message += ". 🎉 Chúc mừng! Bạn đã hoàn thành mục tiêu tiết kiệm!";
                }
                
                return ServiceResult.success(transaction, message);
            } else {
                return ServiceResult.error("Không thê gửi tiền vào mục tiêu tiết kiệm");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Withdraw money from saving goal
    public ServiceResult<SavingTransaction> withdrawFromSaving (int savingID, int userID, double amount, String description) {
        try {
            // Validate amount
            if (amount <= 0) {
                return ServiceResult.error("Số tiền rút phải lớn hơn 0");
            }
            
            // Check if saving exists and belongs to user
            Saving saving = savingDAO.getSavingById(savingID);
            if (saving == null) {
                return ServiceResult.error("Không tìm thấy mục tiêu tiết kiệm");
            }
            
            // Check if sufficient funds
            if (saving.getUserID() != userID) {
                return ServiceResult.error("Số dư không đủ. Số dư hiện tại: " + CurrencyUtils.formatCurrency(saving.getCurrentAmount()));
            }
            
            // Create saving transaction
            SavingTransaction transaction = new SavingTransaction();
            transaction.setSavingID(savingID);
            transaction.setAmount(amount);
            transaction.setTransactionType("WITHDRAW");
            transaction.setDescription(description != null ? description.trim() : "Rút tiền tiết kiệm");
            transaction.setTransactionDate(new Date());
            transaction.setCreatedDate(new Date());
            
            // Add transaction
            boolean created = savingTransactionDAO.createSavingTransaction(transaction);
            
            if (created) {
                // Update saving current amount
                double newCurrentAmount = saving.getCurrentAmount() - amount;
                saving.setCurrentAmount(newCurrentAmount);
                
                // If was completed but now below target, mark as incomplete
                if (saving.isIsCompleted() && newCurrentAmount < saving.getTargetAmount()) {
                    saving.setIsCompleted(false);
                    saving.setCompletedDate(null);
                }
                
                savingDAO.updateSaving(saving);
                
                return ServiceResult.success(transaction, 
                    "Đã rút " + CurrencyUtils.formatCurrency(amount) + " từ mục tiêu tiết kiệm");
            } else {
                return ServiceResult.error("Không thể rút tiền từ mục tiêu tiết kiệm");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Get all savings for a user
    public ServiceResult<List<Saving>> getUserSavings (int userID) {
        try {
            List<Saving> savings = savingDAO.getSavingsByUser(userID);
            
            // Sort by priority (high to low) and then by creation date (newest first)
            savings.sort((s1, s2) -> {
                int priorityCompare = Integer.compare(s2.getPriority(), s1.getPriority());
                if (priorityCompare != 0) return priorityCompare;
                
                return s2.getCreatedDate().compareTo(s1.getCreatedDate());
            });
            
            return ServiceResult.success(savings, "Lấy danh sách mục tiêu tiết kiệm thành công");
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Private helper methods
    private ServiceResult<Void> validateSavingData(int userID, String savingName, String description, 
                                                 double targetAmount, Date targetDate, int priority) {
        // Check user exists
        User user = userDAO.getUserById(userID);
        if (user == null) {
            return ServiceResult.error("Không tìm thấy tài khoản người dùng");
        }
        
        // Validate saving name
        if (savingName == null || savingName.trim().isEmpty()) {
            return ServiceResult.error("Tên mục tiêu tiết kiệm không được để trống");
        }
        
        if (savingName.trim().length() > 100) {
            return ServiceResult.error("Tên mục tiêu tiết kiệm không được quá 100 ký tự");
        }
        
        // Validate description
        if (description != null && description.trim().length() > 500) {
            return ServiceResult.error("Mô tả không được quá 500 ký tự");
        }
        
        // Validate target amount
        if (targetAmount <= 0) {
            return ServiceResult.error("Số tiền mục tiêu phải lớn hơn 0");
        }
        
        if (targetAmount > 999999999999.99) { // ~1 trillion VNĐ
            return ServiceResult.error("Số tiền mục tiêu quá lớn");
        }
        
        // Validate target date (optional)
        if (targetDate != null) {
            Date today = new Date();
            if (targetDate.before(today)) {
                return ServiceResult.error("Ngày mục tiêu không thể là ngày trong quá khứ");
            }
            
            // Not more than 50 years in future
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 50);
            if (targetDate.after(cal.getTime())) {
                return ServiceResult.error("Ngày mục tiêu không thể quá 50 năm");
            }
        }
        
        // Validate priority
        if (priority < 1 || priority > 5) {
            return ServiceResult.error("Độ ưu tiên phải từ 1 đến 5");
        }
        
        return ServiceResult.success("Dữ liệu hợp lệ");
    }
    
    // Inner class for saving progress summary data
    public static class SavingProgressSummary {
        private int totalSavings;
        private int activeCount;
        private int completedCount;
        private int highPriorityCount;
        private int overdueCount;
        private double totalTargetAmount;
        private double totalCurrentAmount;
        
        // Getters and setters
        public int getTotalSavings() { return totalSavings; }
        public void setTotalSavings(int totalSavings) { this.totalSavings = totalSavings; }
        
        public int getActiveCount() { return activeCount; }
        public void setActiveCount(int activeCount) { this.activeCount = activeCount; }
        
        public int getCompletedCount() { return completedCount; }
        public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
        
        public int getHighPriorityCount() { return highPriorityCount; }
        public void setHighPriorityCount(int highPriorityCount) { this.highPriorityCount = highPriorityCount; }
        
        public int getOverdueCount() { return overdueCount; }
        public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }
        
        public double getTotalTargetAmount() { return totalTargetAmount; }
        public void setTotalTargetAmount(double totalTargetAmount) { this.totalTargetAmount = totalTargetAmount; }
        
        public double getTotalCurrentAmount() { return totalCurrentAmount; }
        public void setTotalCurrentAmount(double totalCurrentAmount) { this.totalCurrentAmount = totalCurrentAmount; }
        
        // Helper methods
        public double getOverallProgress() {
            if (totalTargetAmount == 0) return 0;
            return (totalCurrentAmount / totalTargetAmount) * 100;
        }
        
        public double getCompletionRate() {
            if (totalSavings == 0) return 0;
            return ((double) completedCount / totalSavings) * 100;
        }
        
        public boolean hasOverdueSavings() {
            return overdueCount > 0;
        }
        
        public String getFormattedTotalTarget() {
            return CurrencyUtils.formatCurrency(totalTargetAmount);
        }
        
        public String getFormattedTotalCurrent() {
            return CurrencyUtils.formatCurrency(totalCurrentAmount);
        }
        
        public String getFormattedOverallProgress() {
            return String.format("%.1f%%", getOverallProgress());
        }
        
        public String getFormattedCompletionRate() {
            return String.format("%.1f%%", getCompletionRate());
        }
        
        @Override
        public String toString() {
            return "SavingProgressSummary{" +
                    "totalSavings=" + totalSavings +
                    ", activeCount=" + activeCount +
                    ", completedCount=" + completedCount +
                    ", highPriorityCount=" + highPriorityCount +
                    ", overdueCount=" + overdueCount +
                    ", totalTargetAmount=" + totalTargetAmount +
                    ", totalCurrentAmount=" + totalCurrentAmount +
                    ", overallProgress=" + String.format("%.1f%%", getOverallProgress()) +
                    ", completionRate=" + String.format("%.1f%%", getCompletionRate()) +
                    '}';
        }
    }
}