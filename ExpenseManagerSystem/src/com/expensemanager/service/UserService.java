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
            if (userDAO.findUserByUserName(username.trim()) != null) {
                return ServiceResult.error("Tên đăng nhập đã tồn tại");
            }
            
            // Check if email already exists
            if (userDAO.findUserByEmail(email.trim()) != null) {
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
    
    public ServiceResult<User> updateUserProfile (User user, String fullName,
                                                 String email, String phone) {
        try {
            // Validate input
            if (fullName == null || fullName.trim().isEmpty()) {
                return ServiceResult.error("Họ tên không được để trống");
            }
            
            if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
                return ServiceResult.error("Email không đúng định dạng");
            }
            
            if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
                return ServiceResult.error("Số điện thoại không đúng định dạng.");
            }
            
            // Check if new email is already used by another user
            if (email != null && !email.trim().isEmpty()) {
                User existingUser = userDAO.findUserByEmail(email.trim());
                if (existingUser != null && existingUser.getUserID() != user.getUserID()) {
                    return ServiceResult.error("Email đã được sử dụng bởi tài khoản khác");
                }
            }
            
            // Update user data
            user.setFullName(fullName.trim());
            user.setEmail(email != null ? email.trim() : "");
            user.setPhone(phone != null ? phone.trim() : "");
            
            // Save to database
            boolean updated = userDAO.updateUser(user);
            
            if (updated) {
                return ServiceResult.success(user, "Cập nhật thông tin thành công");
            } else {
                return ServiceResult.error("Không thể cập nhật thông tin");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    public ServiceResult<Void> changePassword (int userID, String currentPassword,
                                              String newPassword, String confirmPassword) {
        try {
            // Validate input
            if (currentPassword == null || currentPassword.isEmpty()) {
                return ServiceResult.error("Mật khẩu hiện tại không được để trống");
            }
            
            if (newPassword == null || newPassword.length() < 6) {
                return ServiceResult.error("Mật khẩu phải có ít nhất 6 ký tự.");
            }
            
            if (!newPassword.equals(confirmPassword)) {
                return ServiceResult.error("Mật khẩu mới và xác nhận không khớp");
            }
            
            // Get current user
            User user = userDAO.getUserById(userID);
            if (user == null) {
                return ServiceResult.error("Không tìm thấy tài khoản");
            }
            
            // Verify current password 
            String hashedCurrentPassword = hashPassword(currentPassword);
            if (!user.getPassword().equals(hashedCurrentPassword)) {
                return ServiceResult.error("Mật khẩu hiện tại không đúng");
            }
            
            // Update password
            user.setPassword(hashPassword(newPassword));
            boolean updated = userDAO.updateUser(user);
            
            if (updated) {
                return ServiceResult.success(null, "Đổi mật khẩu thành công");
            } else {
                return ServiceResult.error("Không thể dổi mật khẩu");
            }
        } catch (Exception e) {
            return ServiceResult.error("Lỗi hệ thống: " + e.getMessage());
        }
        
    }
    
    public ServiceResult<UserDashboard> getUserDashboard (int userID) {
        try {
            User user = userDAO.getUserById(userID);
            if (user == null) {
                return ServiceResult.error("Không tìm thấy tài khoản");
            }
            
            UserDashboard dashboard = new UserDashboard();
            dashboard.setUser(user);
            
            int currentMonth = DateUtils.getCurrentMonth();
            int currentYear = DateUtils.getCurrentYear();
            
            // Get current month statistics
            Map<String, Object> monthlyStats = transactionDAO.getMonthlyStatistics(userID, currentMonth, currentYear);
            dashboard.setCurrentMonthIncome((Double) monthlyStats.get("totalIncome"));
            dashboard.setCurrentMonthExpense((Double) monthlyStats.get("totalExpense"));
            dashboard.setCurrentMonthNet((Double) monthlyStats.get("netAmount"));
            
            // Get recent transaction
            List<Transaction> recentTransactions = transactionDAO.getRecentTransactions(userID, 5);
            dashboard.setRecentTransactions(recentTransactions);
            
            // Get budget alerts
            List<Budget> budgetAlerts = budgetDAO.getBudgetAlerts(userID, currentMonth, currentYear);
            dashboard.setBudgetAlerts(budgetAlerts);
            
            // Get saving progress
            List<Saving> savings = savingDAO.getSavingsByUser(userID);
            dashboard.setSavings(savings);
            
            // Calculate financial heath score
            dashboard.setFinancialHealthScore(calculateFinancialHealthScore());
        } catch (Exception e) {
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

    private int calculateFinancialHealthScore(int userID) {
        try {
            int score = 0;
            int currentMonth = DateUtils.getCurrentMonth();
            int currentYear = DateUtils.getCurrentYear();
            
            // Get current month statistics
            Map<String, Object> stats = transactionDAO.getMonthlyStatistics(userID, currentMonth, currentYear);
            double income = (Double) stats.get("totalIncome");
            double expense = (Double) stats.get("totalExpense");
            
            // Score based on savings rate (40 points max)
            if (income > 0) {
                double savingsRate = (income - expense) / income;
                if (savingsRate >= 0.2) score += 40;
                else if (savingsRate >= 0.1) score += 30;
                else if (savingsRate >= 0.05) score += 20;
                else if (savingsRate > 0) score += 10;
            }
            
            // Score based on udget adherence (30 points max)
            List<Budget> budgets = budgetDAO.getBudgetsByUserAndPeriod(userID, currentMonth, currentYear);
            if (!budgets.isEmpty()) {
                int budgetScore = 0;
                for (Budget budget : budgets) {
                    if ("OK".equals(budget.getStatus())) {
                        budgetScore += 10;
                    } else if ("WARNING".equals(budget.getStatus())) {
                        budgetScore += 5;
                    }
                }
                score += Math.min(30, budgetScore);
            }
            
            // Score based on saving goals progress (30 points max)
            List<Saving> savings = savingDAO.getSavingsByUser(userID);
            if (!savings.isEmpty()) {
                double avgProgress = savings.stream()
                        .mapToDouble(Saving::getCompletionPercentage)
                        .average()
                        .orElse(0);
                score += (int) Math.min(30, avgProgress * 0.3);
            }
            
            return Math.min(100, score);
            
        } catch (Exception e) {
            return 50; // Default socre on error
        }
    }
}
