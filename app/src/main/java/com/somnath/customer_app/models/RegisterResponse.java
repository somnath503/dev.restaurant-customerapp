// app/src/main/java/com/somnath/customer_app/models/RegisterResponse.java
package com.somnath.customer_app.models;

// This class is used to structure the data received from your backend API after a registration request.
// Similar to LoginResponse, it would typically contain the newly created user details and an API authentication token.

public class RegisterResponse {
    private boolean success;
    private String message;
    private User user; // The newly created user object
    private String authToken; // The API token for authenticated requests

    // Constructor
    public RegisterResponse(boolean success, String message, User user, String authToken) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.authToken = authToken;
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
}