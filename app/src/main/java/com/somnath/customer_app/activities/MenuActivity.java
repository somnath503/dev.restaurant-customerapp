package com.somnath.customer_app.activities;

import static com.somnath.customer_app.config.ApiConfig.BASE_URL;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.somnath.customer_app.R;
import com.somnath.customer_app.adapters.CategoryAdapter;
import com.somnath.customer_app.adapters.ImageSliderAdapter;
import com.somnath.customer_app.adapters.MenuAdapter;
import com.somnath.customer_app.models.Restaurant;
import com.somnath.customer_app.viewmodels.AuthViewModel;
import com.somnath.customer_app.viewmodels.CartViewModel;
import com.somnath.customer_app.viewmodels.MenuViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MenuActivity extends AppCompatActivity implements MenuAdapter.OnMenuItemClickListener, CategoryAdapter.OnCategoryClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MenuActivity";
    private static final int REQUEST_CHECK_SETTINGS = 1001;

    private MenuViewModel viewModel;
    private AuthViewModel authViewModel;
    private CartViewModel cartViewModel;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerViewMenuItems, recyclerViewCategories;
    private MenuAdapter menuAdapter;
    private CategoryAdapter categoryAdapter;
    private EditText editTextSearchDishes;
    private ViewPager2 viewPagerImageSlider;
    private LinearLayout layoutSliderIndicators;
    private ImageSliderAdapter imageSliderAdapter;
    private Map<String, String> categoryImageMap = new HashMap<>();
    private MaterialCardView cardViewCart;
    private TextView textViewCartItemCount, textViewCartTotalPrice, textViewNavHeaderName;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private MaterialCardView cardViewRestaurantClosed;
    private TextView textViewOpeningTime;
    private View contentGroup;
    private ImageButton buttonCloseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initUI();
        setupNavigationDrawer();
        setupAdaptersAndRecyclerViews();
        setupViewModelsAndObservers();
        setupSearch();

        viewModel.fetchSingleRestaurantDetails();
        checkAndPromptForLocation();
    }

    private void initUI() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        cardViewRestaurantClosed = findViewById(R.id.cardViewRestaurantClosed);
        textViewOpeningTime = findViewById(R.id.textViewOpeningTime);
        buttonCloseStatus = findViewById(R.id.buttonCloseStatus);
        contentGroup = findViewById(R.id.nestedScrollView);
        if (contentGroup == null) {
            Log.e(TAG, "CRITICAL: Main content view with ID 'nestedScrollView' not found.");
        }
        ImageButton menuIcon = findViewById(R.id.imageButtonMenu);
        ImageButton profileIcon = findViewById(R.id.imageButtonProfile);
        if (menuIcon != null) menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        if (profileIcon != null) profileIcon.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        recyclerViewMenuItems = findViewById(R.id.recyclerViewMenuItems);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        editTextSearchDishes = findViewById(R.id.editTextSearchDishes);
        viewPagerImageSlider = findViewById(R.id.viewPagerImageSlider);
        layoutSliderIndicators = findViewById(R.id.layoutSliderIndicators);
        cardViewCart = findViewById(R.id.cardViewCart);
        if (cardViewCart != null) {
            textViewCartItemCount = cardViewCart.findViewById(R.id.textViewCartItemCount);
            textViewCartTotalPrice = cardViewCart.findViewById(R.id.textViewCartTotalPrice);
            cardViewCart.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, CartActivity.class)));
        }
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            textViewNavHeaderName = headerView.findViewById(R.id.textViewNavHeaderName);
        }
    }

    private void setupAdaptersAndRecyclerViews() {
        menuAdapter = new MenuAdapter();
        menuAdapter.setOnMenuItemClickListener(this);
        recyclerViewMenuItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMenuItems.setAdapter(menuAdapter);

        imageSliderAdapter = new ImageSliderAdapter(new ArrayList<>(), BASE_URL);
        viewPagerImageSlider.setAdapter(imageSliderAdapter);

        categoryAdapter = new CategoryAdapter();
        categoryAdapter.setOnCategoryClickListener(this);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    private void setupViewModelsAndObservers() {
        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        viewModel.getRestaurantDetails().observe(this, restaurant -> {
            if (restaurant != null) {
                if (contentGroup != null && cardViewRestaurantClosed != null) {
                    contentGroup.setVisibility(View.VISIBLE);
                    if (restaurant.isOpen()) {
                        cardViewRestaurantClosed.setVisibility(View.GONE);
                    } else {
                        cardViewRestaurantClosed.setVisibility(View.VISIBLE);
                        if (textViewOpeningTime != null) {
                            String openingTimeText = restaurant.getOpeningTime() != null && !restaurant.getOpeningTime().isEmpty()
                                    ? restaurant.getOpeningTime() : "We are not accepting orders now, but you can browse our menu.";
                            textViewOpeningTime.setText(openingTimeText);
                        }
                        if (buttonCloseStatus != null) {
                            buttonCloseStatus.setOnClickListener(v -> cardViewRestaurantClosed.setVisibility(View.GONE));
                        }
                    }
                }
                if (textViewNavHeaderName != null) textViewNavHeaderName.setText(restaurant.getName());
                TextView userAddress = findViewById(R.id.textViewUserAddress);
                if (userAddress != null) userAddress.setText(restaurant.getAddress());
                TextView tvDeliveryTime = findViewById(R.id.textViewDeliveryTime);
                if (tvDeliveryTime != null && restaurant.getDeliveryTime() != null) tvDeliveryTime.setText(restaurant.getDeliveryTime());
            }
        });

        viewModel.getFilteredItemList().observe(this, menuItems -> menuAdapter.setMenuItems(menuItems));
        viewModel.getItemList().observe(this, originalMenuItems -> {
            if (originalMenuItems != null && !originalMenuItems.isEmpty()) {
                prepareCategoryImages(originalMenuItems);
                categoryAdapter.setCategoryImages(categoryImageMap);
                List<String> categories = viewModel.extractCategories(originalMenuItems);
                categories.add(0, "All");
                categoryAdapter.setCategories(categories);
                viewModel.filterByCategory("All");
            }
        });
        viewModel.getSliderImages().observe(this, sliderImages -> {
            if (sliderImages != null && !sliderImages.isEmpty()) {
                imageSliderAdapter.updateData(sliderImages, BASE_URL);
                setupSliderIndicators(sliderImages.size());
                startAutoSlider(sliderImages.size());
            }
        });
        cartViewModel.getCartItemCount().observe(this, count -> {
            if (cardViewCart != null && textViewCartItemCount != null) {
                boolean hasItems = count != null && count > 0;
                cardViewCart.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                if (hasItems) textViewCartItemCount.setText(String.format(Locale.getDefault(), "%d ITEM%S", count, count > 1 ? "S" : ""));
            }
        });
        cartViewModel.getCartTotal().observe(this, total -> {
            if (textViewCartTotalPrice != null && total != null) textViewCartTotalPrice.setText(String.format(Locale.getDefault(), "₹%.2f", total));
        });

        authViewModel.getUserProfile().observe(this, user -> {
            if (user != null && (user.getAddress() == null || user.getAddress().trim().isEmpty())) {
                showUpdateLocationDialog();
            }
        });
    }

    private void checkAndPromptForLocation() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, response -> {
            Log.d(TAG, "Location settings are satisfied.");
            checkUserProfileForAddress();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ((ResolvableApiException) e).startResolutionForResult(MenuActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e(TAG, "Error starting location resolution.", sendEx);
                }
            }
        });
    }

    private void showUpdateLocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update Your Location")
                .setMessage("To ensure accurate delivery, please set your delivery address.")
                .setPositiveButton("Update Now", (dialog, which) -> startActivity(new Intent(MenuActivity.this, ProfileActivity.class)))
                .setNegativeButton("Later", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "You can update your address from your profile later.", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sliderHandler != null && sliderRunnable != null) sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sliderHandler != null && sliderRunnable != null) sliderHandler.postDelayed(sliderRunnable, 3000);
        navigationView.setCheckedItem(R.id.nav_home);
    }

    private void setupSearch() {
        editTextSearchDishes.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { viewModel.searchMenuItems(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void prepareCategoryImages(List<com.somnath.customer_app.models.MenuItem> menuItems) {
        categoryImageMap.clear();
        for (com.somnath.customer_app.models.MenuItem item : menuItems) {
            if (item.getCategory() != null && !categoryImageMap.containsKey(item.getCategory()) && item.getImageUrl() != null) {
                categoryImageMap.put(item.getCategory(), item.getImageUrl());
            }
        }
    }

    @Override
    public void onCategoryClick(String categoryName) {
        viewModel.filterByCategory(categoryName);
    }

    @Override
    public void onMenuItemClick(com.somnath.customer_app.models.MenuItem menuItem) {
        Restaurant currentRestaurant = viewModel.getRestaurantDetails().getValue();
        if (currentRestaurant != null && !currentRestaurant.isOpen()) {
            Toast.makeText(this, "The restaurant is currently closed.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<com.somnath.customer_app.models.MenuItem> fullMenuList = viewModel.getItemList().getValue();
        if (currentRestaurant == null || fullMenuList == null) {
            Toast.makeText(this, "Restaurant data is loading, please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<com.somnath.customer_app.models.MenuItem> relatedItems = fullMenuList.stream()
                .filter(item -> menuItem.getCategory().equals(item.getCategory()) && !item.getId().equals(menuItem.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        Intent intent = new Intent(this, MenuItemDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MenuItemDetailActivity.EXTRA_MENU_ITEM, menuItem);
        bundle.putLong(MenuItemDetailActivity.EXTRA_RESTAURANT_ID, currentRestaurant.getId());
        bundle.putParcelableArrayList(MenuItemDetailActivity.EXTRA_RELATED_ITEMS, relatedItems);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            int id = item.getItemId();
            if (id == R.id.nav_restaurant_info) {
                Restaurant currentRestaurant = viewModel.getRestaurantDetails().getValue();
                if (currentRestaurant != null) {
                    Intent intent = new Intent(this, RestaurantInfoActivity.class);
                    intent.putExtra(RestaurantInfoActivity.EXTRA_RESTAURANT_INFO, currentRestaurant);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Details are loading.", Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistoryActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_logout) {
                authViewModel.logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }, 250);
        return true;
    }

    private void setupSliderIndicators(int count) {
        layoutSliderIndicators.removeAllViews();
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 0, 8, 0);
        for (int i = 0; i < count; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dot_indicator_inactive));
            indicators[i].setLayoutParams(params);
            layoutSliderIndicators.addView(indicators[i]);
        }
        if (indicators.length > 0) {
            indicators[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dot_indicator_active));
        }
        viewPagerImageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateSliderIndicators(position);
            }
        });
    }

    private void updateSliderIndicators(int position) {
        for (int i = 0; i < layoutSliderIndicators.getChildCount(); i++) {
            ImageView indicator = (ImageView) layoutSliderIndicators.getChildAt(i);
            indicator.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), i == position ? R.drawable.dot_indicator_active : R.drawable.dot_indicator_inactive));
        }
    }

    private void checkUserProfileForAddress() {
        authViewModel.fetchUserProfile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "User enabled location settings.");
                checkUserProfileForAddress();
            } else {
                Log.w(TAG, "User CANCELED enabling location settings.");
                Toast.makeText(this, "Location is required for accurate delivery. Please enable it.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startAutoSlider(final int count) {
        if (count <= 1) return;
        sliderRunnable = () -> {
            if (viewPagerImageSlider != null) {
                viewPagerImageSlider.setCurrentItem((viewPagerImageSlider.getCurrentItem() + 1) % count, true);
            }
        };
        viewPagerImageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}
