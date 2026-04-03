package com.somnath.customer_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.somnath.customer_app.adapters.CartAdapter;
import com.somnath.customer_app.databinding.ActivityCartBinding;
import com.somnath.customer_app.models.CartItem;
import com.somnath.customer_app.viewmodels.CartViewModel;

import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupCheckoutButton();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbarCart.setTitle("Your Cart");
        binding.toolbarCart.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter();
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(cartAdapter);

        cartAdapter.setOnCartItemChangeListener(new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                cartViewModel.updateItemQuantity(item, newQuantity);
            }

            @Override
            public void onRemoveItem(CartItem item) {
                cartViewModel.removeCartItem(item);
                Toast.makeText(CartActivity.this, item.getMenuItem().getName() + " removed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCheckoutButton() {
        binding.btnAction.setText("Proceed to Payment");
        binding.btnAction.setOnClickListener(v -> {
            List<CartItem> items = cartViewModel.getCartItems().getValue();
            if (items == null || items.isEmpty()) {
                Toast.makeText(CartActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            // Navigate directly to PaymentActivity (skip CheckoutActivity)
            Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
            startActivity(intent);
        });
    }


    private void observeViewModel() {
        cartViewModel.getCartItems().observe(this, cartItems -> {
            cartAdapter.setCartItems(cartItems);
            boolean isCartEmpty = cartItems == null || cartItems.isEmpty();

            binding.tvEmptyCartMessage.setVisibility(isCartEmpty ? View.VISIBLE : View.GONE);
            binding.rvCartItems.setVisibility(isCartEmpty ? View.GONE : View.VISIBLE);
            binding.cardCheckoutSummary.setVisibility(isCartEmpty ? View.GONE : View.VISIBLE);
            binding.btnAction.setEnabled(!isCartEmpty);
        });

        cartViewModel.getCartTotal().observe(this, total -> {
            if (total != null) {
                binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "₹%.2f", total));
            }
        });
    }
}
