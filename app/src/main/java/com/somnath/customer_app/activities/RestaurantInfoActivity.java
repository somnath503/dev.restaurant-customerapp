package com.somnath.customer_app.activities;

import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.somnath.customer_app.R;
import com.somnath.customer_app.config.ApiConfig;
import com.somnath.customer_app.databinding.ActivityRestaurantInfoBinding;
import com.somnath.customer_app.models.Restaurant;
public class RestaurantInfoActivity extends AppCompatActivity {
    public static final String EXTRA_RESTAURANT_INFO = "RESTAURANT_INFO_EXTRA";
    private Restaurant restaurant;
    private ActivityRestaurantInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().hasExtra(EXTRA_RESTAURANT_INFO)) {
            // This is the final, correct, and compatible fix.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                restaurant = getIntent().getParcelableExtra(EXTRA_RESTAURANT_INFO, Restaurant.class);
            } else {
                // The annotation is now correctly placed on the variable declaration.
                @SuppressWarnings("deprecation")
                Restaurant result = getIntent().getParcelableExtra(EXTRA_RESTAURANT_INFO);
                restaurant = result;
            }
        }

        if (restaurant == null) {
            Toast.makeText(this, "Could not load restaurant details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        populateUI();
    }
    private void setupToolbar() {
        // FIXED: Don't use setSupportActionBar
        // CollapsingToolbar handles the title
        binding.toolbarInfo.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void populateUI() {
        binding.collapsingToolbarInfo.setTitle(restaurant.getName());
        binding.tvRestaurantName.setText(restaurant.getName());
        binding.tvRestaurantCuisine.setText(restaurant.getCuisine());
        binding.tvRestaurantDescription.setText(restaurant.getDescription());
        binding.tvRestaurantAddress.setText(restaurant.getAddress());
        binding.tvRestaurantPhone.setText(restaurant.getPhone());

        String fullImageUrl = buildFullImageUrl(restaurant.getImageUrl());
        Glide.with(this)
                .load(fullImageUrl)
                .placeholder(R.drawable.placeholder_restaurant)
                .error(R.drawable.error_restaurant)
                .into(binding.ivRestaurantImage);
    }
    private String buildFullImageUrl(String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty() || relativeUrl.startsWith("http")) {
            return relativeUrl;
        }
        String baseUrl = ApiConfig.BASE_URL;
        return baseUrl.endsWith("/") ? baseUrl + relativeUrl.replaceFirst("^/", "") : baseUrl + "/" + relativeUrl.replaceFirst("^/", "");
    }
}