// --- app/src/main/java/com/somnath/customer_app/models/OrderItem.java ---
package com.somnath.customer_app.models;

import com.google.gson.annotations.SerializedName; // <<< ADD THIS IMPORT

public class OrderItem {
    private Long menuItemId;

    // --- THIS IS THE FIX ---
    // The @SerializedName annotation tells Gson to map the "menuItemName" field
    // from the JSON to this "name" field in the Java object.
    @SerializedName("menuItemName")
    private String name;
    // --- END OF FIX ---

    private double price;
    private int quantity;
    private String imageUrl;

    // Constructor
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
