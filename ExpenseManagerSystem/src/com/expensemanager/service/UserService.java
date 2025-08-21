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
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    public ServiceResult<User> registerUser (String username, String password,
                                            String confirmPassword, String fullName,
                                            String email, String phone) {
        
        try {
            // Validate input data
            ServiceResult<Void> validation = validateUserData(username, password, 
                                                            confirmPassword, fullName, email, phone);
            if (!validation.isSuccess()) {
                return ServiceResult.error(validation.getMessage());
            }
            
            // Check if username already exists
            if (userDAO.findUserByUsername) {
                return ServiceResult.error("Tên đăng nhập đã tồn tại");
            }
            
            // Check if email already exists
            if (userDAO.findUserByEmail) {
                return ServiceResult.error("Email đã được sử dụng");
            }
            
            // Create new user
            User newUser = new User();
            newUser.setUsername(username.trim());
            newUser.setPassword(hashPassword(password));
            newUser.setFullName(fullName.trim());
            newUser.setEmail(email.trim());
            newUser.setPhone(phone.trim());
            newUser.setActive(true);
            newUser.setCreatedDate(new Date());
            
            // Save to database
            boolean created = userDAO.createUser(newUser);
            
            if (created) {
                // Create default categories for new user
                createDefaultCategories(newUser.getUserID());
                return ServiceResult.success(newUser, "Đăng ký thành công");
            } else {
                return ServiceResult.error("Không thể tạo tài khoản");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    private ServiceResult<Void> validateUserData(String username, String password, 
                                               String confirmPassword, String fullName, 
                                               String email, String phone) {
        // Username validation
        if (username == null || username.trim().length() < 3) {
            return ServiceResult.error("Tên đăng nhập phải có ít nhất 3 ký tự");
        }
        
        if (!username.trim().matches("^[a-zA-Z0-9_]{3,20}$")) {
            return ServiceResult.error("Tên đăng nhập chỉ được chứa chữ, số và dấu gạch dưới");
        }
        
        // Password validation
        if (password == null || password.length() < 6) {
            return ServiceResult.error("Mật khẩu phải có ít nhất 6 ký tự");
        }
        
        if (!password.equals(confirmPassword)) {
            return ServiceResult.error("Mật khẩu và xác nhận không khớp");
        }
        
        // Full name validation
        if (fullName == null || fullName.trim().isEmpty()) {
            return ServiceResult.error("Họ tên không được để trống");
        }
        
        // Email validation
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return ServiceResult.error("Email không đúng định dạng");
        }
        
        // Phone validation
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return ServiceResult.error("Số điện thoại không đúng định dạng (VD: 0901234567)");
        }
        
        return ServiceResult.success(null, "Dữ liệu hợp lệ");
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

    private void createDefaultCategories(int userID) {
        CategoryDAO categoryDAO = daoFactory.getCategoryDAO();
        
        // Default income categories
        String[][] incomeCategories = {
            {"Lương", "Thu nhập từ công việc chính"},
            {"Thưởng", "Các khoản thường, bonus"},
            {"Đầu tư", "Thu nhập từ đầu tư"},
            {"Thu nhập khác", "Các thu nhập khác"}
        };
        
        // Default expense categories
        String[][] expenseCategories = {
            {"Ăn uống", "Chi phí ăn uống hàng ngày"},
            {"Di chuyển", "Chi phí giao thông, xăng xe"},
            {"Mua sắm", "Quần áo, đồ dùng cá nhân"},
            {"Giải trí", "Xem phim, du lịch, thể thao"},
            {"Hóa đơn", "Điện, nước, internet, điện thoại"},
            {"Y tế", "Khám bệnh, thuốc men"},
            {"Giáo dục", "Học phí, sách vở"},
            {"Chi phí khác", "Chi phí khác"}
        };
        
        // Create income categories
        for (String[] cat: incomeCategories) {
            Category category = new Category(cat[0], "INCOME", cat[1], userID);
            categoryDAO.createCategory(category);
        }
        
        // Create expense categories
        for (String[] cat : expenseCategories) {
            Category category = new Category(cat[0], "EXPENSE", cat[1], userID);
            categoryDAO.createCategory(category);
        }
    }
}
