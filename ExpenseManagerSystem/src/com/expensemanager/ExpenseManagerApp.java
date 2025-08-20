package com.expensemanager;

import com.expensemanager.dao.DatabaseConnection;
import com.expensemanager.view.LoginForm;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ExpenseManagerApp {

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Error setting Look and Feel: " + e.getMessage());
        }
        
       // Test database connection
        System.out.println("Testing database connection...");
        if (DatabaseConnection.testConnection()) {
            System.out.println("Database connection successful!");
        } else {
            System.err.println("Database connection failed!");
            System.err.println("Please check your database configuration.");
            return; // Exit if can't connect to database
        }
        
//        // Start the application
//        SwingUtilities.invokeLater(() -> {
//            try {
//                new LoginForm().setVisible(true);
//            } catch (Exception e) {
//                System.err.println("Error starting application: " + e.getMessage());
//                e.printStackTrace();
//            }
//        });
    }
}
