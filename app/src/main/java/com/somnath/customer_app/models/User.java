// --- app\src\main\java\com\somnath\customer_app\models\User.java ---
package com.somnath.customer_app.models;

import com.somnath.customer_app.models.CustomerStatus;

public class User {
    private Long id;
    private String phone;
    private String name;
    private String email;
    private String address;
    private CustomerStatus status;
    private String firebaseUid;
    private String role;

    private String backupPhoneNumber;
    private Double latitude;
    private Double longitude;

    // Default constructor for Gson to use
    public User() {}

    // Optional: Constructor with all fields if you explicitly create User objects
    public User(Long id, String phone, String name, String email, String address,
                CustomerStatus status, String firebaseUid,
                String role,String backupPhoneNumber,Double latitude, Double longitude) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.email = email;
        this.address = address;
        this.status = status;
        this.firebaseUid = firebaseUid;
        this.role = role;
        this.backupPhoneNumber=backupPhoneNumber;
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public String getBackupPhoneNumber() {
        return backupPhoneNumber;
    }

    public void setBackupPhoneNumber(String backupPhoneNumber) {
        this.backupPhoneNumber = backupPhoneNumber;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // Getters and Setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}