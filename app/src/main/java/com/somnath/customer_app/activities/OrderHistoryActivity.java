package com.somnath.customer_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.somnath.customer_app.R;
import com.somnath.customer_app.adapters.OrderAdapter;
import com.somnath.customer_app.models.Order;
import com.somnath.customer_app.utils.Event;
import com.somnath.customer_app.viewmodels.OrderViewModel;

import java.util.ArrayList;
import java.util.Collections; // Make sure this is imported

public class OrderHistoryActivity extends AppCompatActivity {

    private OrderViewModel orderViewModel;
    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private ProgressBar progressBarLoading;
    private TextView textViewNoOrdersMessage;
    private SwipeRefreshLayout swipeRefreshLayoutOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        initializeUIComponents();
        setupToolbar();
        setupRecyclerView();
        observeViewModelData();

        orderViewModel.fetchOrderHistory();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(OrderHistoryActivity.this, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeUIComponents() {
        recyclerViewOrders = findViewById(R.id.rv_order_history);
        progressBarLoading = findViewById(R.id.progress_bar);
        textViewNoOrdersMessage = findViewById(R.id.tv_no_orders);
        swipeRefreshLayoutOrders = findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayoutOrders.setOnRefreshListener(() -> {
            Toast.makeText(this, "Refreshing order history...", Toast.LENGTH_SHORT).show();
            orderViewModel.fetchOrderHistory();
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar_order_history);
        toolbar.setTitle("Order History");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }


    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, new ArrayList<>());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewOrders.setLayoutManager(layoutManager);
        recyclerViewOrders.setAdapter(orderAdapter);

        orderAdapter.setOnOrderClickListener(new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
            }

            @Override
            public void onCancelOrderClick(Order order) {
                orderViewModel.cancelOrder(order.getId());
            }
        });
    }


    private void observeViewModelData() {
        orderViewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading && !swipeRefreshLayoutOrders.isRefreshing()) {
                progressBarLoading.setVisibility(View.VISIBLE);
                recyclerViewOrders.setVisibility(View.GONE);
            } else {
                progressBarLoading.setVisibility(View.GONE);
                if (swipeRefreshLayoutOrders.isRefreshing()) {
                    swipeRefreshLayoutOrders.setRefreshing(false);
                }
            }
        });

        orderViewModel.getError().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        orderViewModel.getOrderHistory().observe(this, orders -> {
            if (orders != null && !orders.isEmpty()) {
                recyclerViewOrders.setVisibility(View.VISIBLE);
                textViewNoOrdersMessage.setVisibility(View.GONE);
                orderAdapter.setOrders(orders);

            } else {
                recyclerViewOrders.setVisibility(View.GONE);
                textViewNoOrdersMessage.setVisibility(View.VISIBLE);
            }
        });

        orderViewModel.getCancelledOrder().observe(this, event -> {
            if (event != null) {
                Order cancelledOrder = event.getContentIfNotHandled();
                if (cancelledOrder != null) {
                    Toast.makeText(this, "Order #" + cancelledOrder.getId() + " has been successfully cancelled.", Toast.LENGTH_SHORT).show();
                    orderViewModel.fetchOrderHistory(); // Refresh the list after cancellation
                }
            }
        });
    }


}
