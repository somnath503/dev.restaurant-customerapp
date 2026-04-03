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
        btnProceedToPayment = findViewById(R.id.btn_proceed_to_payment);
        // Setup toolbar also check it exist first
        setupToolbar();
        btnProceedToPayment.setOnClickListener(v -> {
            startActivity(new Intent(CheckoutActivity.this, PaymentActivity.class));
            finish();
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Checkout");
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }
}
