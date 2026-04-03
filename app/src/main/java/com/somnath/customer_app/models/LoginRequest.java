// app/src/main/java/com/somnath/customer_app/models/LoginRequest.java
package com.somnath.customer_app.models;

// This class is used to structure the data sent to your backend API for login.
// For OTP-based login, you might send phone number and OTP.
// Note: Firebase Phone Auth directly handles the OTP verification with Firebase,
// but you might still need a backend API call after successful Firebase auth
// to get/create a user record in your own database and get an auth token for your API.

public class LoginRequest {
    // Example fields for sending phone number and Firebase User ID after successful auth
    private String phoneNumber;
    private String firebaseUid;
    // Add other fields if your backend requires them (e.g., device token)

    public LoginRequest(String phoneNumber, String firebaseUid) {
        this.phoneNumber = phoneNumber;
        this.firebaseUid = firebaseUid;
    }

    // Getters (usually not needed for a request object)
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    // Setters (optional)
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }
}