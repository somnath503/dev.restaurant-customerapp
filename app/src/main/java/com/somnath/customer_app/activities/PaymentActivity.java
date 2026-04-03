package com.somnath.customer_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import com.somnath.customer_app.R;
import com.somnath.customer_app.models.CartItem; // Keep this import
import com.somnath.customer_app.models.User;
import com.somnath.customer_app.viewmodels.AuthViewModel;
import com.somnath.customer_app.viewmodels.CartViewModel;
import com.somnath.customer_app.viewmodels.OrderViewModel;

import org.json.JSONObject;

import java.util.ArrayList; // Keep this import
import java.util.List;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private AuthViewModel authViewModel;

    private TextView tvTotalAmount;
    private RadioGroup rgUpiOptions;
    private RadioButton rbCod;
    private Button btnPay;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private double totalAmount = 0.0;
    private List<CartItem> currentCartItems;
    private User currentUserProfile;
    private static final double DELIVERY_FEE = 0.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initUI();
        setupToolbar();
        setupListeners();
        observeViewModels();

        authViewModel.fetchUserProfile();

        Checkout.preload(getApplicationContext());
    }

    private void initUI() {
        toolbar = findViewById(R.id.toolbar_payment);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        rgUpiOptions = findViewById(R.id.rg_upi_options);
        rbCod = findViewById(R.id.rb_cod);
        btnPay = findViewById(R.id.btn_pay);
        progressBar = findViewById(R.id.progress_bar_payment);
    }

    private void setupToolbar() {
        toolbar.setTitle("Complete Your Payment");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }


    private void observeViewModels() {
        cartViewModel.getCartTotal().observe(this, subtotal -> {
            if (subtotal != null) {
                totalAmount = subtotal + DELIVERY_FEE;
                tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));
                btnPay.setText(String.format(Locale.getDefault(), "Pay ₹%.2f", totalAmount));
            }
        });

        cartViewModel.getCartItems().observe(this, cartItems -> this.currentCartItems = cartItems);
        authViewModel.getUserProfile().observe(this, user -> this.currentUserProfile = user);

        orderViewModel.getPlacedOrder().observe(this, order -> {
            if (order != null) {
                cartViewModel.clearCart(); // Clear cart after successful order
                Intent intent = new Intent(this, OrderSuccessActivity.class);
                intent.putExtra(OrderSuccessActivity.EXTRA_ORDER_ID, String.valueOf(order.getId())); // Pass order ID
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });


        orderViewModel.getLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnPay.setEnabled(!isLoading);
        });

        orderViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                btnPay.setEnabled(true);
            }
        });

        orderViewModel.getRazorpayOrderResponse().observe(this, response -> {
            if (response != null && response.get("razorpayOrderId") != null) {
                String razorpayOrderId = response.get("razorpayOrderId");
                String keyId = response.get("keyId");
                startRazorpayPayment(keyId, razorpayOrderId);
            }
        });
    }

    private void setupListeners() {
        btnPay.setOnClickListener(v -> placeOrder());

        rgUpiOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                rbCod.setChecked(false);
            }
            updatePayButtonText();
        });

        rbCod.setOnClickListener(v -> {
            if (rbCod.isChecked()) {
                rgUpiOptions.clearCheck();
            }
            updatePayButtonText();
        });
    }

    private void placeOrder() {
        // Basic checks for cart and user profile
        if (currentCartItems == null || currentCartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUserProfile == null || currentUserProfile.getAddress() == null) {
            Toast.makeText(this, "Delivery address not found.", Toast.LENGTH_LONG).show();
            return;
        }
        boolean isCodSelected = rbCod.isChecked();
        boolean isUpiSelected = rgUpiOptions.getCheckedRadioButtonId() != -1;
        // Ensure a payment method is chosen
        if (!isCodSelected && !isUpiSelected) {
            Toast.makeText(this, "Please select a payment method.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Here, the 'isUpiSelected' variable is used to direct the flow
        if (isUpiSelected) {
            // If a UPI option was selected, start the Razorpay payment flow
            progressBar.setVisibility(View.VISIBLE);
            btnPay.setEnabled(false);
            orderViewModel.createRazorpayOrder(totalAmount);
        } else {
            // Otherwise, it must be COD. Place the order directly.
            orderViewModel.placeOrder(
                    currentCartItems,
                    currentUserProfile,
                    DELIVERY_FEE,
                    totalAmount,
                    "COD"
            );
        }
    }




    private void startRazorpayPayment(String keyId, String razorpayOrderId) {
        Checkout checkout = new Checkout();
        checkout.setKeyID(keyId);

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Rita's Kitchen");
            options.put("description", "Order ID: " + razorpayOrderId);
            options.put("order_id", razorpayOrderId);
            options.put("theme", new JSONObject().put("color", "#FFC107"));
            options.put("currency", "INR");
            options.put("amount", String.valueOf(Math.round(totalAmount * 100)));

            JSONObject prefill = new JSONObject();
            if (currentUserProfile.getEmail() != null) {
                prefill.put("email", currentUserProfile.getEmail());
            }
            prefill.put("contact", currentUserProfile.getPhone());
            options.put("prefill", prefill);

            checkout.open(this, options);

        } catch (Exception e) {
            Toast.makeText(this, "Error setting up payment: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            btnPay.setEnabled(true);
        }
    }

    // Locate this method in your PaymentActivity.java
    @Override
    public void onPaymentSuccess(String razorpayPaymentId, PaymentData data) {
        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
        Log.d("RAZORPAY_SUCCESS", "Payment ID: " + razorpayPaymentId + ", Order ID: " + data.getOrderId());

        // Since this is a single-restaurant app, no need to define or pass a specific restaurantId here.

        orderViewModel.placeOrder(
                currentCartItems,
                currentUserProfile, // Correct: Pass the entire User object
                DELIVERY_FEE,
                totalAmount,
                "Online"
        );
    }


    @Override
    public void onPaymentError(int code, String description, PaymentData data) {
        Log.e("RAZORPAY_ERROR", "Code: " + code + ", Description: " + description);
        Toast.makeText(this, "Payment Failed: " + description, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
        btnPay.setEnabled(true);
    }

    private void updatePayButtonText() {
        if (rbCod.isChecked()) {
            btnPay.setText("Place Order");
        } else {
            btnPay.setText(String.format(Locale.getDefault(), "Pay ₹%.2f", totalAmount));
        }
    }
}
