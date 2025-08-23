package com.expensemanager.service.category;

import com.expensemanager.model.Category;
import com.expensemanager.util.CurrencyUtils;
import com.expensemanager.util.DateUtils;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CategoryUsageStatistics {
    private Category category;
    private int transactionCount;
    private double totalAmount;
    private double averageAmount;
    private Date firstTransactionDate;
    private Date lastTransactionDate;
    
    public CategoryUsageStatistics() {
        this.transactionCount = 0;
        this.totalAmount = 0.0;
        this.averageAmount = 0.0;
    }
    
    public CategoryUsageStatistics(Category category, int transactionCount, double totalAmount) {
        this.category = category;
        this.transactionCount = transactionCount;
        this.totalAmount = totalAmount;
        this.averageAmount = transactionCount > 0 ? totalAmount / transactionCount : 0.0;
    }
    
    // Getters and Setters
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public int getTransactionCount() {
        return transactionCount;
    }
    
    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
        // Recalculate average when transaction count changes
        this.averageAmount = transactionCount > 0 ? totalAmount / transactionCount : 0.0;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        // Recalculate average when total amount changes
        this.averageAmount = transactionCount > 0 ? totalAmount / transactionCount : 0.0;
    }
    
    public double getAverageAmount() {
        return averageAmount;
    }
    
    public void setAverageAmount(double averageAmount) {
        this.averageAmount = averageAmount;
    }
    
    public Date getFirstTransactionDate() {
        return firstTransactionDate;
    }
    
    public void setFirstTransactionDate(Date firstTransactionDate) {
        this.firstTransactionDate = firstTransactionDate;
    }
    
    public Date getLastTransactionDate() {
        return lastTransactionDate;
    }
    
    public void setLastTransactionDate(Date lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }
    
    // Helper methods
    public String getCategoryName() {
        return category != null ? category.getCategoryName() : "N/A";
    }
    
    public String getCategoryType() {
        return category != null ? category.getCategoryType() : "N/A";
    }
    
    public String getCategoryColor() {
        return category != null ? category.getColor() : "#757575";
    }
    
    public String getFormattedTotalAmount() {
        return CurrencyUtils.formatCurrency(totalAmount);
    }
    
    public String getFormattedAverageAmount() {
        return CurrencyUtils.formatCurrency(averageAmount);
    }
    
    public String getFormattedFirstTransactionDate() {
        return firstTransactionDate != null ? DateUtils.formatDate(firstTransactionDate) : "N/A";
    }
    
    public String getFormattedLastTransactionDate() {
        return lastTransactionDate != null ? DateUtils.formatDate(lastTransactionDate) : "N/A";
    }
    
    public boolean hasTransactions() {
        return transactionCount > 0;
    }
    
    public long getDaysBetweenFirstAndLast() {
        if (firstTransactionDate == null || lastTransactionDate == null) {
            return 0;
        }
        
        long diffInMillis = lastTransactionDate.getTime() - firstTransactionDate.getTime();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }
    
    public double getAverageAmountPerDay() {
        long days = getDaysBetweenFirstAndLast();
        if (days <= 0) return totalAmount; // Single day or no transactions
        return totalAmount / (days + 1); // +1 to include both start and end dates
    }
    
    public String getFormattedAverageAmountPerDay() {
        return CurrencyUtils.formatCurrency(getAverageAmountPerDay());
    }
    
    public double getTransactionFrequency() {
        long days = getDaysBetweenFirstAndLast();
        if (days <= 0) return transactionCount; // Single day
        return (double) transactionCount / (days + 1);
    }
    
    public String getUsageFrequency() {
        if (transactionCount == 0) return "Không sử dụng";
        
        long days = getDaysBetweenFirstAndLast();
        if (days == 0) return "Mới sử dụng";
        
        double transactionsPerDay = getTransactionFrequency();
        
        if (transactionsPerDay >= 1.0) return "Rất thường xuyên";
        else if (transactionsPerDay >= 0.5) return "Thường xuyên";
        else if (transactionsPerDay >= 0.2) return "Trung bình";
        else if (transactionsPerDay >= 0.1) return "Ít khi";
        else return "Hiếm khi";
    }
    
    public String getActivityLevel() {
        if (transactionCount == 0) return "Không hoạt động";
        else if (transactionCount >= 50) return "Rất tích cực";
        else if (transactionCount >= 20) return "Tích cực";
        else if (transactionCount >= 10) return "Trung bình";
        else if (transactionCount >= 5) return "Ít hoạt động";
        else return "Rất ít";
    }
    
    public String getActivityColor() {
        if (transactionCount == 0) return "#757575"; // Gray
        else if (transactionCount >= 50) return "#4CAF50"; // Green
        else if (transactionCount >= 20) return "#8BC34A"; // Light Green
        else if (transactionCount >= 10) return "#FF9800"; // Orange
        else if (transactionCount >= 5) return "#FF5722"; // Deep Orange
        else return "#F44336"; // Red
    }
    
    public boolean isActive() {
        return transactionCount > 0;
    }
    
    public boolean isFrequentlyUsed() {
        return transactionCount >= 10;
    }
    
    public boolean isHighValue() {
        return averageAmount >= 1000000; // 1 million VND
    }
    
    public boolean isRecent() {
        if (lastTransactionDate == null) return false;
        
        Date now = new Date();
        long daysSinceLastTransaction = TimeUnit.DAYS.convert(
            now.getTime() - lastTransactionDate.getTime(), TimeUnit.MILLISECONDS);
        
        return daysSinceLastTransaction <= 30; // Active in last 30 days
    }
    
    public String getUsagePeriod() {
        if (firstTransactionDate == null || lastTransactionDate == null) {
            return "Chưa có giao dịch";
        }
        
        if (DateUtils.isSameDay(firstTransactionDate, lastTransactionDate)) {
            return "Chỉ 1 ngày";
        }
        
        long days = getDaysBetweenFirstAndLast();
        if (days < 7) {
            return days + " ngày";
        } else if (days < 30) {
            return (days / 7) + " tuần";
        } else if (days < 365) {
            return (days / 30) + " tháng";
        } else {
            return (days / 365) + " năm";
        }
    }
    
    public double getUsageIntensity() {
        // Combine frequency and amount to get overall usage intensity (0-100)
        double frequencyScore = Math.min(getTransactionFrequency() * 20, 50); // Max 50 points
        double amountScore = Math.min(averageAmount / 100000, 50); // Max 50 points, normalized by 100k VND
        
        return Math.min(100, frequencyScore + amountScore);
    }
    
    public String getUsageIntensityLevel() {
        double intensity = getUsageIntensity();
        if (intensity >= 80) return "Rất cao";
        else if (intensity >= 60) return "Cao";
        else if (intensity >= 40) return "Trung bình";
        else if (intensity >= 20) return "Thấp";
        else return "Rất thấp";
    }
    
    public String getFormattedUsageIntensity() {
        return String.format("%.1f%%", getUsageIntensity());
    }
    
    @Override
    public String toString() {
        return "CategoryUsageStatistics{" +
                "categoryName='" + getCategoryName() + '\'' +
                ", categoryType='" + getCategoryType() + '\'' +
                ", transactionCount=" + transactionCount +
                ", totalAmount=" + totalAmount +
                ", averageAmount=" + averageAmount +
                ", firstTransactionDate=" + firstTransactionDate +
                ", lastTransactionDate=" + lastTransactionDate +
                ", usageFrequency='" + getUsageFrequency() + '\'' +
                ", activityLevel='" + getActivityLevel() + '\'' +
                ", usageIntensity=" + String.format("%.1f%%", getUsageIntensity()) +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CategoryUsageStatistics that = (CategoryUsageStatistics) obj;
        
        return category != null ? 
               (category.getCategoryID() == that.category.getCategoryID()) : 
               that.category == null;
    }
    
    @Override
    public int hashCode() {
        return category != null ? Integer.hashCode(category.getCategoryID()) : 0;
    }
}