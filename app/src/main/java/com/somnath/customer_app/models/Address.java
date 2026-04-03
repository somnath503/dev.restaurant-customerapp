// app/src/main/java/com/somnath/customer_app/models/Address.java
package com.somnath.customer_app.models;

public class Address {
    private String id;
    private String userId; // Link to the user this address belongs to
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String apartmentNumber; // Optional
    private String deliveryInstructions; // Optional

    public Address(String id, String userId, String street, String city, String state, String zipCode, String country, String apartmentNumber, String deliveryInstructions) {
        this.id = id;
        this.userId = userId;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        this.apartmentNumber = apartmentNumber;
        this.deliveryInstructions = deliveryInstructions;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCountry() {
        return country;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }
}