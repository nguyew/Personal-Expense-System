package com.expensemanager.service;

import com.expensemanager.dao.*;
import com.expensemanager.model.*;
import com.expensemanager.util.DateUtils;
import com.expensemanager.util.CurrencyUtils;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class UserService {
    
    private final DAOFactory daoFactory;
    private final UserDAO userDAO;
    private final TransactionDAO transactionDAO;
    private final BudgetDAO budgetDAO;
    private final SavingDAO savingDAO;
    
    // Email and phone validation patterns
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^(\\+84|84|0)[1-9][0-9]{8,9}$");
    
    public UserService () {
        this.daoFactory = DAOFactory.getInstance();
        this.userDAO = daoFactory.getUserDAO();
        this.transactionDAO = daoFactory.getTransactionDAO();
        this.budgetDAO = daoFactory.getBudgetDAO();
        this.savingDAO = daoFactory.getSavingDAO();
    }
    
    public ServiceResult<User> authenticateUser (String username, String password) {
        try {
            // Input validation
            if (username == null || username.trim().isEmpty()) {
                return ServiceResult.error("Tên đăng nhập không được để trống");
            } 
            
            if (password == null || password.isEmpty()) {
                return ServiceResult.error("Mật khẩu không được để trống");
            }
            
            // Hash password for comparison
            String hashedPassword = hashPassword(password);
            
            // Find user in database
            User user = userDAO.findUser(username.trim(), hashedPassword);
            
            if (user == null) {
                return ServiceResult.error("Tên đăng nhập hoặc mật khẩu không đúng");
            }
            
            if (!user.isActive()) {
                return ServiceResult.error("Tài khoản đã bị khóa");
            }
            
            // Update last login
            user.setLastLogin(new Date());
            userDAO.updateUser(user);
            
            return ServiceResult.success(user, "Đăng nhập thành công");
        } catch (Exception e) {
            return ServiceResult.error("lỗi hệ thống: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
