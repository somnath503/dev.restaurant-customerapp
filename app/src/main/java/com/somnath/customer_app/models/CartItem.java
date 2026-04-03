package com.somnath.customer_app.models;

public class CartItem {
    private MenuItem menuItem;
    private int quantity;
    private Long restaurantId;

    public CartItem(MenuItem menuItem, int quantity, Long restaurantId) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.restaurantId = restaurantId;
    }

    // Getters
    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    // Helper method to calculate total price for this item
    public double getTotalPrice() {
        return menuItem.getPrice() * quantity;
    }
}