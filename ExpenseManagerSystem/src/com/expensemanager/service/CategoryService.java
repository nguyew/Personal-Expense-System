package com.expensemanager.service;

import com.expensemanager.dao.*;
import com.expensemanager.model.Category;
import com.expensemanager.model.User;
import java.util.*;

public class CategoryService {
    
    private final DAOFactory daoFactory;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;
    private final UserDAO userDAO;
    
    public CategoryService () {
        this.daoFactory = DAOFactory.getInstance();
        this.categoryDAO = daoFactory.getCategoryDAO();
        this.transactionDAO = daoFactory.getTransactionDAO();
        this.userDAO = daoFactory.getUserDAO();
    }
    
    // Create new category
    public ServiceResult<Category> createCategory (int userID, String categoryName,
                                                String categoryType, String description,
                                                String iconName, String color) {
        try {
            // Validate input data
            ServiceResult<Void> validation = validateCategoryData(userID, categoryName, categoryType, description);
            if (!validation.isSuccess()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if category name already exists for this user and type
            List<Category> existingCategories = categoryDAO.getCategoriesByUserAndType(userID, categoryType);
            for (Category category : existingCategories) {
                if (category.getCategoryName().equalsIgnoreCase(categoryName.trim())) {
                    return ServiceResult.error("Danh mục '" + categoryName + "' đã tồn tại");
                }
            }
            
            // Create category object
            Category category = new Category();
            category.setCategoryName(categoryName.trim());
            category.setCategoryType(categoryType.toUpperCase());
            category.setDescription(description != null ? description.trim() : "");
            category.setUserID(userID);
            category.setIconName(iconName != null ? iconName.trim() : "");
            category.setColor(color != null ? color.trim() : getDefaultColor(categoryType));
            category.setDefault(false);
            category.setCreatedDate(new Date());
            
            // Save category
            boolean created = categoryDAO.createCategory(category);
            
            if (created) {
                return ServiceResult.success(category, "Danh mục đã được tạo thành công");
            } else {
                return ServiceResult.error("Không thể tạo danh mục");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    private ServiceResult<Void> validateCategoryData(int userID, String categoryName, String categoryType, String description) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String getDefaultColor(String categoryType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
