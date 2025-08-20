package com.expensemanager.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class CurrencyUtils {
    
    private static final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    
    // Format amount to Vietnamese currency
    public static String formatCurrency (double amount) {
        if (amount == 0) return "0 VNĐ";
        return currencyFormat.format(amount) + " VNĐ";
    }
    
    public static String formatAmount (double amount) {
        return decimalFormat.format(amount);
    }
    
    // Parse currency string to double
    public static double parseCurrency (String currencyStr) throws NumberFormatException {
        if (currencyStr == null || currencyStr.trim().isEmpty()) {
            return 0.0;
        }
        
        // Remove currency symbol and whitespace
        String cleanStr = currencyStr.replace("VNĐ", "").replace("đ", "").trim();
        
        // Remove thousands seperators 
        cleanStr = cleanStr.replace(",", "").replace(".", "");
        
        return Double.parseDouble(cleanStr);
    }
    
    // Validation methods
    public static boolean isValidAmount (String amountStr) {
        try {
            double amount = parseCurrency(amountStr);
            return amount >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isPositiveAmount (double amount) {
        return amount > 0;
    }
    
    // Calculation methods
    public static double calculatePercentage (double part, double total) {
        if (total == 0) return 0;
        return (part / total) * 100;
    }
    
    public static double calculatePercentageAmount (double total, double percentage) {
        return (total * percentage) / 100;
    }
    
    // Format for display tables
    public static String formatForTable (double amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000);
        } else {
            return String.format("%.0f", amount);
        }
    }
    
    // Color coding for amounts
    public static String getAmountColor (double amount, boolean isIncome) {
        if (isIncome) {
            return "#4CAF50"; // Green for income
        } else {
            return "#F44336"; // Red for expense
        }
    }
}
