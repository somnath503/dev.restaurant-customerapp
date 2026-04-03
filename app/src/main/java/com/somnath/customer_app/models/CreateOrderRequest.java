package com.somnath.customer_app.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the JSON body for creating a Razorpay order.
 * Example: {"amount": 123.45}
 */
public class CreateOrderRequest {

    @SerializedName("amount")
    private double amount;

    public CreateOrderRequest(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
