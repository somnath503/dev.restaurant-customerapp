package com.somnath.customer_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.somnath.customer_app.R;

public class CheckoutActivity extends AppCompatActivity {

    private Button btnProceedToPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize button
        btnProceedToPayment = findViewById(R.id.btn_proceed_to_payment);

        // Setup toolbar - Check if it exists first
        setupToolbar();

        // Button click listener - UPDATED HERE
        btnProceedToPayment.setOnClickListener(v -> {
            // Add finish() to remove CheckoutActivity from back stack
            startActivity(new Intent(CheckoutActivity.this, PaymentActivity.class));
            finish();  // ← ADD THIS LINE - Closes CheckoutActivity after navigation
        });
    }

    private void setupToolbar() {
        // Try to find toolbar - may not exist in layout
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Only setup if toolbar exists
        if (toolbar != null) {
            toolbar.setTitle("Checkout");
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }
}
