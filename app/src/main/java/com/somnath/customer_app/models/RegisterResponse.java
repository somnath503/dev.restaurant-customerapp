package com.somnath.customer_app.models;
public class RegisterResponse {
    private boolean success;
    private String message;
    private User user;
    private String authToken;
    public RegisterResponse(boolean success, String message, User user, String authToken) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.authToken = authToken;
    }
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public String getAuthToken() {
        return authToken;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}