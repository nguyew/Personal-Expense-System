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
    
    // Update existing category
    public ServiceResult<Category> updateCategory (Category category) {
        try {
            // Validate input data
            ServiceResult<Void> validation = validateCategoryData(
                category.getUserID(),
                category.getCategoryName(),
                category.getCategoryType(),
                category.getDescription()
            );
            
            if (!validation.isSuccess()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if category exists and belongs to user
            Category existingCategory = categoryDAO.getCategoryById(category.getCategoryID());
            if (existingCategory == null) {
                return ServiceResult.error("Không tìm thấy danh mục");
            }
            
            if (existingCategory.getUserID() != category.getUserID() && !existingCategory.isDefault()) {
                return ServiceResult.error("Bạn không có quyền sửa danh mục này");
            }
            
            // Don't allow editing default categories
            if (existingCategory.isDefault()) {
                return ServiceResult.error("Không thể sửa danh mục mặc đinh6");
            }
            
            // Check if new name conflicts with existing categories (except current one)
            List<Category> existingCategories = categoryDAO.getCategoriesByUserAndType(
                category.getUserID(), category.getCategoryType());
            for (Category cat : existingCategories) {
                if (cat.getCategoryID() != category.getCategoryID() &&
                    cat.getCategoryName().equalsIgnoreCase(category.getCategoryName().trim())) {
                    return ServiceResult.error("Danh mục '" + category.getCategoryName() + "' đã tồn tại");
                }
            }
            
            // Update category
            boolean updated = categoryDAO.updateCategory(category);
            
            if (updated) {
                return ServiceResult.success(category, "Danh mục đã được cập nhật");
            } else {
                return ServiceResult.error("Không thể cập nhật danh mục");
            }
            
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    // Delete category
    public ServiceResult<Void> deleteCategory (int categoryID, int userID) {
        try {
            // Check if category exists and belongs to user
            Category category = categoryDAO.getCategoryById(categoryID);
            if (category == null) {
                return ServiceResult.error("Không tìm thấy danh mục");
            }
            
            if (category.getUserID() != userID && !category.isDefault()) {
                return ServiceResult.error("Bạn không có quyền xóa danh mục này");
            }
            
            // Do not allow deleting default categories
            if (category.isDefault()) {
                return ServiceResult.error("Không thể xóa danh mục mặc định");
            }
            
            // Check if category is being used in transactions
            int transactionCount = transactionDAO.getTransactionCountByCategory(categoryID);
            if (transactionCount > 0) {
                return ServiceResult.error("Không thể xóa danh mục đang có " + transactionCount + " giao dịch");
            }
            
            // Delete category
            
        } catch (Exception e) {
        }
    }
 
    private ServiceResult<Void> validateCategoryData(int userID, String categoryName,
                                                   String categoryType, String description) {
        // Check user exists
        User user  = userDAO.getUserById(userID);
        if (user == null) {
            return ServiceResult.error("Không tìm thấy tài khoản người dùng");
        }
        
        // Validate category name
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return ServiceResult.error("Tên danh mục không được để trống");
        }
        
        if (categoryName.trim().length() > 100) {
            return ServiceResult.error("Tống danh mục không được quá 100 ký tự");
        }
        
        // Validate category type
        if (categoryType == null ||
            (categoryType.toUpperCase().equals("INCOME") && !categoryType.toUpperCase().equals("EXPENSE"))) {
            return ServiceResult.error("Loại danh mục phải là INCOME hoặc EXPENSE");
        }
        
        // validate description
        if (description != null && description.trim().length() > 500) {
            return ServiceResult.error("Mô tả không được quá 500 ký tư");
        }
        
        return ServiceResult.success("Dữ liệu hợp lệ"); 
    }
    
    // Get default color for category type
    private String getDefaultColor(String categoryType) {
        if ("INCOME".equals(categoryType.toUpperCase())) {
            return "#4CAF50";
        } else {
            return "#F44336";
        }
    }
}
