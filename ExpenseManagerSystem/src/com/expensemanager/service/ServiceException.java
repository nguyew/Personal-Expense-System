package com.expensemanager.service;

/**
 * Custom exception for service layer operations
 */
public class ServiceException extends Exception {
    private String errorCode;
    
    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}