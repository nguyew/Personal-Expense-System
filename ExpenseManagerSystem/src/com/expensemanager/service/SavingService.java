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

    private ServiceResult<Void> validateSavingData(int userID, String savingName, String description, double targetAmount, Date targetDate, int priority) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}