package com.somnath.customer_app.models;
public class LoginResponse {
    private boolean success;
    private String message;
    private User user; // The user object
    private String authToken; // The API token for authenticated requests
    private boolean newUser; //  To indicate if the user is new to the backend system

    public LoginResponse(boolean success, String message, User user, String authToken, boolean newUser) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.authToken = authToken;
        this.newUser = newUser; // Initialize new field
    }

    // Getters
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

    public boolean isNewUser() { // ADDED: Getter for the new field
        return newUser;
    }

    // Setters (optional)
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

    public void setNewUser(boolean newUser) { // ADDED: Setter for the new field
        this.newUser = newUser;
    }
}