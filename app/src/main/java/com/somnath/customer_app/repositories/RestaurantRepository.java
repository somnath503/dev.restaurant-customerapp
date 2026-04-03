package com.somnath.customer_app.repositories;

import android.content.Context;
import android.util.Log;

import com.somnath.customer_app.api.ApiService;
import com.somnath.customer_app.api.RetrofitClient;
import com.somnath.customer_app.models.MenuItem;
import com.somnath.customer_app.models.Restaurant;
import com.somnath.customer_app.models.SliderImage;
import com.somnath.customer_app.utils.ApiResponseCallback;
import com.somnath.customer_app.utils.SharedPrefManager;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository {

    private static final String TAG = "RestaurantRepository";
    private ApiService apiService;
    public SharedPrefManager sharedPrefManager;

    public RestaurantRepository(Context context) {
        this.apiService = RetrofitClient.getApiService();
        this.sharedPrefManager = SharedPrefManager.getInstance(context);
        Log.d(TAG, "RestaurantRepository initialized.");
    }
    public void getRestaurantDetails(ApiResponseCallback<Restaurant> callback) {
        Log.d(TAG, "Repository: Calling backend API to get details for THE restaurant.");

        if (apiService == null) {
            Log.e(TAG, "ApiService is null. Cannot fetch restaurant details.");
            callback.onError("API Service not initialized.");
            return;
        }

        apiService.getRestaurantDetails().enqueue(new Callback<Restaurant>() { // Calling the parameterless method
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Repository: Restaurant details fetched successfully.");
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to fetch restaurant details: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (IOException | NullPointerException e) {
                        Log.e(TAG, "Error reading error body for restaurant details", e);
                    }
                    Log.w(TAG, "Repository: Backend API call failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                Log.e(TAG, "Repository: Get restaurant details API call failed.", t);
                callback.onFailure(t);
            }
        });
    }
    public void getRestaurantMenu(ApiResponseCallback<List<MenuItem>> callback) {
        Log.d(TAG, "Repository: Calling backend API to get menu for THE restaurant.");

        if (apiService == null) {
            Log.e(TAG, "ApiService is null. Cannot fetch restaurant menu.");
            callback.onError("API Service not initialized.");
            return;
        }

        apiService.getRestaurantMenu().enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Repository: Menu fetched successfully. Item count: " + response.body().size());
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to fetch menu: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (IOException | NullPointerException e) {
                        Log.e(TAG, "Error reading error body for menu", e);
                    }
                    Log.w(TAG, "Repository: Backend API call failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                Log.e(TAG, "Repository: Get restaurant menu API call failed.", t);
                callback.onFailure(t);
            }
        });
    }
    public void getBestsellers(ApiResponseCallback<List<MenuItem>> callback) {
        Log.d(TAG, "Repository: Calling backend API to get bestsellers.");

        if (apiService == null) {
            Log.e(TAG, "ApiService is null. Cannot fetch bestsellers.");
            callback.onError("API Service not initialized.");
            return;
        }

        apiService.getBestsellers().enqueue(new Callback<List<MenuItem>>() {
            @Override
            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Repository: Bestsellers fetched successfully. Count: " + response.body().size());
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to fetch bestsellers: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (IOException | NullPointerException e) {
                        Log.e(TAG, "Error reading error body for bestsellers", e);
                    }
                    Log.w(TAG, "Repository: Backend API call failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                Log.e(TAG, "Repository: Get bestsellers API call failed.", t);
                callback.onFailure(t);
            }
        });
    }

    // RestaurantRepository.java
    public void getSliderImages(ApiResponseCallback<List<SliderImage>> callback) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<SliderImage>> call = apiService.getSliderImages();
        call.enqueue(new Callback<List<SliderImage>>() {
            @Override
            public void onResponse(Call<List<SliderImage>> call, Response<List<SliderImage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch slider images");
                }
            }
            @Override
            public void onFailure(Call<List<SliderImage>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    // ... (other methods you might have in this repository)
}