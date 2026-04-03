package com.somnath.customer_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback; // Import this
import androidx.appcompat.app.AppCompatActivity;

import com.somnath.customer_app.R;

public class OrderSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        TextView tvOrderId = findViewById(R.id.tv_success_order_id);
        Button btnDone = findViewById(R.id.btn_success_done);

        // Get order ID from intent
        String orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId != null) {
            tvOrderId.setText("Order ID: #" + orderId);
        } else {
            tvOrderId.setText("Order placed successfully!");
        }

        btnDone.setOnClickListener(v -> {
            // Navigate back to the main menu (e.g., MenuActivity)
            navigateToMenuActivity();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Intercept the back press and redirect to MenuActivity
                navigateToMenuActivity();
            }
        });
    }

    private void navigateToMenuActivity() {
        Intent intent = new Intent(OrderSuccessActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finish this activity
    }

}
