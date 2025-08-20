package com.expensemanager.service;

/**
 * Generic wrapper class for service method responses
 * Provides consistent error handling and result structure
 */
public class ServiceResult<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    
    private ServiceResult(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }
    
    /**
     * Create successful result with data and message
     */
    public static <T> ServiceResult<T> success(T data, String message) {
        return new ServiceResult<>(true, message, data, null);
    }
    
    /**
     * Create successful result with data only
     */
    public static <T> ServiceResult<T> success(T data) {
        return new ServiceResult<>(true, "Thành công", data, null);
    }
    
    /**
     * Create successful result with message only
     */
    public static <T> ServiceResult<T> success(String message) {
        return new ServiceResult<>(true, message, null, null);
    }
    
    /**
     * Create error result with message
     */
    public static <T> ServiceResult<T> error(String message) {
        return new ServiceResult<>(false, message, null, null);
    }
    
    /**
     * Create error result with message and error code
     */
    public static <T> ServiceResult<T> error(String message, String errorCode) {
        return new ServiceResult<>(false, message, null, errorCode);
    }
    
    /**
     * Create error result from exception
     */
    public static <T> ServiceResult<T> error(Exception e) {
        return new ServiceResult<>(false, e.getMessage(), null, e.getClass().getSimpleName());
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public boolean isError() {
        return !success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public T getData() {
        return data;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Get data or throw exception if error
     */
    public T getDataOrThrow() throws ServiceException {
        if (!success) {
            throw new ServiceException(message, errorCode);
        }
        return data;
    }
    
    /**
     * Get data or return default value if error
     */
    public T getDataOrDefault(T defaultValue) {
        return success ? data : defaultValue;
    }
    
    @Override
    public String toString() {
        return "ServiceResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}