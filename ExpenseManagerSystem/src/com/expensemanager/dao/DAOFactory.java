package com.expensemanager.dao;


public class DAOFactory {
    private static DAOFactory instance;
    
    // DAO instances
    private UserDAO userDAO;
    private CategoryDAO categoryDAO;
    private TransactionDAO transactionDAO;
    private BudgetDAO budgetDAO;
    private SavingDAO savingDAO;
    private SavingTransactionDAO savingTransactionDAO;
    private ReportDAO reportDAO;
    private RecurringTransactionDAO recurringTransactionDAO;
    
    private DAOFactory () {
        
    }
    
    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }
    
    // Lazy initialization of DAOs
    public UserDAO getUserDAO () {
        if (userDAO == null) {
            userDAO = new UserDAO();
        }
        return userDAO;
    }
    
    public CategoryDAO getCategoryDAO () {
        if (categoryDAO == null) {
            categoryDAO = new CategoryDAO();
        }
        return categoryDAO;
    }
    
    public TransactionDAO getTransactionDAO () {
        if (transactionDAO == null) {
            transactionDAO = new TransactionDAO();
        }
        return transactionDAO;
    }
    
    public BudgetDAO getBudgetDAO () {
        if (budgetDAO == null) {
            budgetDAO = new BudgetDAO();
        }
        return budgetDAO;
    }
    
    public SavingDAO getSavingDAO () {
        if (savingDAO == null) {
            savingDAO = new SavingDAO();
        }
        return savingDAO;
    }
    
    public SavingTransactionDAO getSavingTransactionDAO () {
        if (savingTransactionDAO == null) {
            savingTransactionDAO = new SavingTransactionDAO();
        }
        return savingTransactionDAO;
    }
    
    public ReportDAO getReportDAO () {
        if (reportDAO == null) {
            reportDAO = new ReportDAO();
        }
        return reportDAO;
    }
    
    public RecurringTransactionDAO getRecurringTransactionDAO () {
        if (recurringTransactionDAO == null) {
            recurringTransactionDAO = new RecurringTransactionDAO();
        }
        return recurringTransactionDAO;
    }
    
    // Method to test all DAO connections
    public boolean testAllConnections() {
        try {
            // Test basic connection
            boolean basicConnection = DatabaseConnection.testConnection();
            if (!basicConnection) {
                System.err.println("Basic database connection failed!");
                return false;
            }
            
            System.out.println("✓ Database connection successful");
            
            // Test each DAO
            System.out.println("Testing DAO classes...");
            
            // Test UserDAO
            try {
                getUserDAO().getAllUsers();
                System.out.println("✓ UserDAO working");
            } catch (Exception e) {
                System.err.println("✗ UserDAO failed: " + e.getMessage());
                return false;
            }
            
            // Test CategoryDAO  
            try {
                getCategoryDAO().getCategoriesByUser(1);
                System.out.println("✓ CategoryDAO working");
            } catch (Exception e) {
                System.err.println("✗ CategoryDAO failed: " + e.getMessage());
                return false;
            }
            
            // Test TransactionDAO
            try {
                getTransactionDAO().getTransactionsByUser(1);
                System.out.println("✓ TransactionDAO working");
            } catch (Exception e) {
                System.err.println("✗ TransactionDAO failed: " + e.getMessage());
                return false;
            }
            
            // Test BudgetDAO
            try {
                getBudgetDAO().getBudgetsByUserAndPeriod(1, 8, 2025);
                System.out.println("✓ BudgetDAO working");
            } catch (Exception e) {
                System.err.println("✗ BudgetDAO failed: " + e.getMessage());
                return false;
            }
            
            // Test SavingDAO
            try {
                getSavingDAO().getSavingsByUser(1);
                System.out.println("✓ SavingDAO working");
            } catch (Exception e) {
                System.err.println("✗ SavingDAO failed: " + e.getMessage());
                return false;
            }
            
            // Test SavingTransactionDAO
            try {
                getSavingTransactionDAO().getSavingTransactionsBySaving(1);
                System.out.println("✓ SavingTransactionDAO working");
            } catch (Exception e) {
                System.err.println("✗ SavingTransactionDAO failed: " + e.getMessage());
                return false;
            }
            
            // Test ReportDAO
            try {
                getReportDAO().getMonthlyTrend(1, 12); // Test với user ID = 1, 12 tháng gần đây
                System.out.println("✓ ReportDAO working");
            } catch (Exception e) {
                System.err.println("✗ ReportDAO failed: " + e.getMessage());
                return false;
            }

            // Test RecurringTransactionDAO
            try {
                getRecurringTransactionDAO().getActiveRecurringTransactions(1); // Test với user ID = 1
                System.out.println("✓ RecurringTransactionDAO working");
            } catch (Exception e) {
                System.err.println("✗ RecurringTransactionDAO failed: " + e.getMessage());
                return false;
            }
            
            System.out.println("All DAO tests passed!");
            return true;
            
        } catch (Exception e) {
            System.err.println("DAO Factory test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }  
    }
    
    // Close all DAO connections (if needed)
    public void closeAll () {
        // Reset all DAO instances
        userDAO = null;
        categoryDAO = null;
        transactionDAO = null;
        budgetDAO = null;
        savingDAO = null;
        savingTransactionDAO = null;
        recurringTransactionDAO = null;
        reportDAO = null;
    }
    
    // Reset factory instance (for testing purposes)
    public static void resetInstance () {
        if (instance != null) {
            instance.closeAll();
            instance = null;
        }
    }
    
}
