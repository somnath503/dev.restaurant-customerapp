package com.somnath.customer_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.somnath.customer_app.R;
import com.somnath.customer_app.adapters.MenuAdapter;
import com.somnath.customer_app.config.ApiConfig;
import com.somnath.customer_app.models.CartItem;
import com.somnath.customer_app.models.MenuItem;
import com.somnath.customer_app.viewmodels.CartViewModel;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
public class MenuItemDetailActivity extends AppCompatActivity implements MenuAdapter.OnMenuItemClickListener {

    public static final String EXTRA_MENU_ITEM = "MENU_ITEM_EXTRA";
    public static final String EXTRA_RESTAURANT_ID = "RESTAURANT_ID_EXTRA";
    public static final String EXTRA_RELATED_ITEMS = "RELATED_ITEMS_EXTRA";
    // ViewModels and Data
    private CartViewModel cartViewModel;
    private MenuItem currentMenuItem;
    private Long restaurantId;
    private ArrayList<MenuItem> relatedItems;
    private int quantity = 1;

    // UI Components
    private ImageView ivItemImage, ivDecreaseQuantity, ivIncreaseQuantity;
    private TextView tvItemName, tvItemDescription, tvItemPrice, tvQuantity, tvRelatedItemsHeader;
    private Button btnAddToCart;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private RecyclerView rvRelatedItems;
    private MenuAdapter relatedItemsAdapter;
    private LinearLayout llAddToCartBar;

    private View bottomCartView;
    private TextView tvCartItemCount, tvCartTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_detail);

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        if (!extractIntentData() || !initUI()) {
            return; // Errors are handled within these methods
        }

        setupToolbar();
        populateUI();
        setupListeners();
        setupRelatedItemsRecyclerView();
        observeCartViewModel();
    }

    private boolean extractIntentData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null || !bundle.containsKey(EXTRA_MENU_ITEM)) {
            handleError("Could not load item details: Data missing.");
            return false;
        }

        currentMenuItem = bundle.getParcelable(EXTRA_MENU_ITEM);
        restaurantId = bundle.getLong(EXTRA_RESTAURANT_ID, -1L);
        relatedItems = bundle.getParcelableArrayList(EXTRA_RELATED_ITEMS);

        if (currentMenuItem == null || restaurantId == -1L) {
            handleError("Could not load item details: Invalid data provided.");
            return false;
        }
        return true;
    }

    private boolean initUI() {
        toolbar = findViewById(R.id.toolbar_detail);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        ivItemImage = findViewById(R.id.iv_detail_item_image);
        tvItemName = findViewById(R.id.tv_detail_item_name);
        tvItemDescription = findViewById(R.id.tv_detail_item_description);
        tvItemPrice = findViewById(R.id.tv_detail_item_price);
        ivDecreaseQuantity = findViewById(R.id.iv_decrease_quantity);
        ivIncreaseQuantity = findViewById(R.id.iv_increase_quantity);
        tvQuantity = findViewById(R.id.tv_quantity);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        rvRelatedItems = findViewById(R.id.rv_related_items);
        tvRelatedItemsHeader = findViewById(R.id.tv_related_items_header);
        llAddToCartBar = findViewById(R.id.ll_add_to_cart_bar);

        bottomCartView = findViewById(R.id.card_view_cart);

        if (bottomCartView != null) {
            tvCartItemCount = bottomCartView.findViewById(R.id.textViewCartItemCount);
            tvCartTotalPrice = bottomCartView.findViewById(R.id.textViewCartTotalPrice);
        }

        return true;
    }

    private void observeCartViewModel() {
        cartViewModel.getCartItems().observe(this, cartItems -> {
            boolean isCartEmpty = cartItems == null || cartItems.isEmpty();
            boolean isThisItemInCart = false;

            if (!isCartEmpty) {
                for (CartItem item : cartItems) {
                    if (Objects.equals(item.getMenuItem().getId(), currentMenuItem.getId())) {
                        isThisItemInCart = true;
                        break;
                    }
                }
            }
            llAddToCartBar.setVisibility(isThisItemInCart ? View.GONE : View.VISIBLE);
            bottomCartView.setVisibility(isCartEmpty ? View.GONE : View.VISIBLE);

            if (!isCartEmpty) {
                int count = 0;
                double total = 0.0;
                for (CartItem item : cartItems) {
                    count += item.getQuantity();
                    total += item.getTotalPrice();
                }
                tvCartItemCount.setText(String.format(Locale.getDefault(), "%d ITEM%S", count, count > 1 ? "S" : ""));
                tvCartTotalPrice.setText(String.format(Locale.getDefault(), "₹%.2f", total));
            }
        });
    }
    private void setupToolbar() {
        // Menu item name will be set from data
        toolbar.setTitle("Menu Item Details");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }


    private void populateUI() {
        collapsingToolbar.setTitle(currentMenuItem.getName());
        tvItemName.setText(currentMenuItem.getName());
        tvItemDescription.setText(currentMenuItem.getDescription());
        updateQuantityDisplay();

        String fullImageUrl = buildFullImageUrl(currentMenuItem.getImageUrl());
        Glide.with(this)
                .load(fullImageUrl)
                .placeholder(R.drawable.placeholder_menu_item)
                .error(R.drawable.error_menu_item)
                .centerCrop()
                .into(ivItemImage);
    }

    private void setupListeners() {
        ivIncreaseQuantity.setOnClickListener(v -> {
            quantity++;
            updateQuantityDisplay();
        });
        ivDecreaseQuantity.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityDisplay();
            }
        });
        btnAddToCart.setOnClickListener(v -> {
            cartViewModel.addItemToCart(currentMenuItem, quantity, restaurantId);
            Toast.makeText(this, quantity + " x " + currentMenuItem.getName() + " added", Toast.LENGTH_SHORT).show();
        });
        bottomCartView.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
    }

    private void setupRelatedItemsRecyclerView() {
        relatedItemsAdapter = new MenuAdapter();
        relatedItemsAdapter.setOnMenuItemClickListener(this); // Attach the listener to the adapter
        rvRelatedItems.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRelatedItems.setAdapter(relatedItemsAdapter);

        if (relatedItems != null && !relatedItems.isEmpty()) {
            tvRelatedItemsHeader.setVisibility(View.VISIBLE);
            rvRelatedItems.setVisibility(View.VISIBLE);
            String category = currentMenuItem.getCategory() != null ? currentMenuItem.getCategory() : "similar items";
            tvRelatedItemsHeader.setText("More from " + category);
            relatedItemsAdapter.setMenuItems(relatedItems);
        } else {
            tvRelatedItemsHeader.setVisibility(View.GONE);
            rvRelatedItems.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMenuItemClick(MenuItem menuItem) {
        cartViewModel.addItemToCart(menuItem, 1, restaurantId);
        Toast.makeText(this, "Added " + menuItem.getName() + " to cart", Toast.LENGTH_SHORT).show();
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(quantity));
        tvItemPrice.setText(String.format(Locale.getDefault(), "₹%.2f", currentMenuItem.getPrice() * quantity));
    }

    private String buildFullImageUrl(String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty() || relativeUrl.startsWith("http")) {
            return relativeUrl;
        }
        String baseUrl = ApiConfig.BASE_URL;
        return baseUrl.endsWith("/") ? baseUrl + relativeUrl.replaceFirst("^/", "") : baseUrl + "/" + relativeUrl.replaceFirst("^/", "");
    }

    private void handleError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}
