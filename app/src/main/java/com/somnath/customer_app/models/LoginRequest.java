// app/src/main/java/com/somnath/customer_app/models/LoginRequest.java
package com.somnath.customer_app.models;
public class LoginRequest {
    private String phoneNumber;
    private String firebaseUid;

    public LoginRequest(String phoneNumber, String firebaseUid) {
        this.phoneNumber = phoneNumber;
        this.firebaseUid = firebaseUid;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }
}