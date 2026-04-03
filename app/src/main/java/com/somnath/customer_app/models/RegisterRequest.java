package com.somnath.customer_app.models;
public class RegisterRequest {
    private String phoneNumber;
    private String firebaseUid;
    private String name;
    private String email;
    private String address;
    private Double latitude;
    private Double longitude;


    public RegisterRequest(String phoneNumber, String firebaseUid, String name, String email, String address, Double latitude, Double longitude) {
        this.phoneNumber = phoneNumber;
        this.firebaseUid = firebaseUid;
        this.name = name;
        this.email = email;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
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
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() { // ADDED: Getter for the new field
        return address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) { // ADDED: Setter for the new field
        this.address = address;
    }
}