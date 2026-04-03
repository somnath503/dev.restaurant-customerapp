package com.somnath.customer_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.somnath.customer_app.R;
import com.somnath.customer_app.adapters.OrderDetailItemAdapter;
import com.somnath.customer_app.models.Order;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderDateTime, tvTotalAmount;
    private RecyclerView rvOrderItems;
    private Button btnSeeInMenu;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        tvOrderDateTime = findViewById(R.id.tv_detail_order_date_time);
        tvTotalAmount = findViewById(R.id.tv_detail_total_amount);
        rvOrderItems = findViewById(R.id.rv_order_items);
        btnSeeInMenu = findViewById(R.id.btn_see_in_menu);

        // Setup toolbar - FIXED: Don't use setSupportActionBar
        setupToolbar();

        // Get the Order object from the Intent
        String orderJson = getIntent().getStringExtra("ORDER_DETAILS_JSON");
        if (orderJson != null) {
            Gson gson = new Gson();
            Type orderType = new TypeToken<Order>(){}.getType();
            Order order = gson.fromJson(orderJson, orderType);
            populateOrderDetails(order);
        }

        // Set button click listener
        btnSeeInMenu.setOnClickListener(v -> {
            Intent intent = new Intent(OrderDetailActivity.this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // FIXED: New setupToolbar method - no setSupportActionBar
    private void setupToolbar() {
        toolbar.setTitle("Your Order Details");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void populateOrderDetails(Order order) {
        if (order == null) return;

        // Set Date and Total Amount
        if (order.getOrderDate() != null) {
            String formattedDateTime = order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
            tvOrderDateTime.setText("Ordered on: " + formattedDateTime);
        }
        tvTotalAmount.setText(String.format(Locale.getDefault(), "Total Paid: ₹%s", order.getTotalAmount()));

        // Setup RecyclerView for the list of items
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        OrderDetailItemAdapter adapter = new OrderDetailItemAdapter(order.getItems());
        rvOrderItems.setAdapter(adapter);
    }
}
