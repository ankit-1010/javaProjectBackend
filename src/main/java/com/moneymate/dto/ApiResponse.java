package com.moneymate.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private T data;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.status = 200;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.status = 200;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }

    public ApiResponse(boolean success, String message, int status, T data) {
        this.success = success;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
