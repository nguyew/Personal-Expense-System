package com.expensemanager.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {
    private static final String SERVER = "localhost";
    private static final String PORT = "1433";
    private static final String DATABASE = "ExpenseManager";
    private static final String USERNAME = "SA";
    private static final String PASSWORD = "12345";
    
    private static final String CONNECTION_URL = 
            "jdbc:sqlserver://" + SERVER + ":" + PORT +
            ";databaseName=" + DATABASE +
            ";trustServerCertificate=true" +
            ";encrypt=false";
    
    // Singleton pattern
    private static DatabaseConnection instance;
    private Connection connection;
    
    private DatabaseConnection () {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            this.connection = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);
            System.out.println("Database connection succesfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("SQL Server JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection!");
            e.printStackTrace();
        }
    }
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else if (instance.getConnection() == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection () {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Error getting database connection!");
            e.printStackTrace();
        }
        return connection;
    }
    
    // Static methods for easy access
    public static Connection getDBConnection() throws SQLException {
        return getInstance().getConnection();
    }
    
    public void closeConnection () {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection!");
            e.printStackTrace();
        }
    }
    
    // Test connection method
    public static boolean testConnection () {
        try (Connection conn = getDBConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
