package com.somnath.customer_app.models;

import com.google.gson.annotations.SerializedName;

public class OrderItem {
    private Long menuItemId;

    @SerializedName("menuItemName")
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;
    public OrderItem(Long menuItemId, String name, double price, int quantity, String imageUrl) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // --- GETTERS AND SETTERS ---

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Helper method
    public double getTotalPrice() {
        return price * quantity;
    }
}
