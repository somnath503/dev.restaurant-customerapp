// --- app\\src\\main\\java\\com\\somnath\\customer_app\\repositories\\OrderRepository.java ---
// app/src/main/java/com/somnath/customer_app/repositories/OrderRepository.java
package com.somnath.customer_app.repositories;

import android.content.Context; // Needed for SharedPrefManager
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.somnath.customer_app.api.ApiService;
import com.somnath.customer_app.api.RetrofitClient;
import com.somnath.customer_app.models.CreateOrderRequest;
import com.somnath.customer_app.models.Order;
import com.somnath.customer_app.utils.ApiResponseCallback;
import com.somnath.customer_app.utils.SharedPrefManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {

    private static final String TAG = "OrderRepository";
    private ApiService apiService;
    private SharedPrefManager sharedPrefManager;

    public OrderRepository(Context context) {
        this.apiService = RetrofitClient.getApiService();
        this.sharedPrefManager = SharedPrefManager.getInstance(context);
    }

    // Method to place a new order
    public void placeOrder(Order orderDetails, ApiResponseCallback<Order> callback) {
        String authToken = sharedPrefManager.getAuthToken();
        if (authToken == null || authToken.isEmpty()) {
            callback.onError("User not authenticated. Cannot place order.");
            return;
        }

        Log.d(TAG, "Placing order with auth token: Bearer " + authToken);

        apiService.placeOrder("Bearer " + authToken, orderDetails).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Order placed successfully");
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to place order: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Order placement failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Log.e(TAG, "Place order API call failed", t);
                callback.onFailure(t);
            }
        });
    }


    public void createRazorpayOrder(double totalAmount, MutableLiveData<Map<String, String>> responseLiveData, MutableLiveData<String> errorLiveData) {
        String authToken = sharedPrefManager.getAuthToken();
        if (authToken == null || authToken.isEmpty()) {
            errorLiveData.postValue("User is not logged in.");
            return;
        }

        CreateOrderRequest request = new CreateOrderRequest(totalAmount);

        // --- FIX: Use the apiService instance directly and pass the token ---
        apiService.createRazorpayOrder("Bearer " + authToken, request).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    responseLiveData.postValue(response.body());
                } else {
                    errorLiveData.postValue("Could not initiate payment. Session may have expired.");
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "createRazorpayOrder failed", t);
                errorLiveData.postValue("Network error. Please check your connection.");
            }
        });
    }
    // Method to get order history for the logged-in user
    public void getOrderHistory(ApiResponseCallback<List<Order>> callback) {
        String authToken = sharedPrefManager.getAuthToken();
        if (authToken == null) {
            callback.onError("User not authenticated. Cannot fetch order history.");
            return;
        }

        apiService.getOrderHistory("Bearer " + authToken).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to fetch order history: " + response.code();
                    try {
                        errorMessage += " - " + response.errorBody().string();
                    } catch (IOException | NullPointerException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(errorMessage);
                    // Handle 401 Unauthorized
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e(TAG, "Get order history API call failed", t);
                callback.onFailure(t);
            }
        });
    }

    // Method to get details for a specific order
    public void getOrderDetails(String orderId, ApiResponseCallback<Order> callback) {
        String authToken = sharedPrefManager.getAuthToken();
        if (authToken == null) {
            callback.onError("User not authenticated. Cannot fetch order details.");
            return;
        }

        apiService.getOrderDetails("Bearer " + authToken, orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to fetch order details for " + orderId + ": " + response.code();
                    try {
                        errorMessage += " - " + response.errorBody().string();
                    } catch (IOException | NullPointerException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(errorMessage);
                    // Handle 401 Unauthorized
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Log.e(TAG, "Get order details API call failed for " + orderId, t);
                callback.onFailure(t);
            }
        });
    }

    public void cancelOrder(Long orderId, ApiResponseCallback<Order> callback) {
        String authToken = sharedPrefManager.getAuthToken();
        if (authToken == null) {
            callback.onError("User not authenticated. Cannot cancel order.");
            return;
        }

        apiService.cancelOrder("Bearer " + authToken, orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Successfully cancelled on the backend
                    callback.onSuccess(response.body());
                } else {
                    // Handle specific error codes from the backend
                    String errorMessage = "Failed to cancel order: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            // This will capture messages like "The 60-second window has passed."
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException | NullPointerException e) {
                        Log.e(TAG, "Error reading error body for cancel order", e);
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Log.e(TAG, "Cancel order API call failed", t);
                callback.onFailure(t);
            }
        });
    }
    public String getUserId(){
        return sharedPrefManager.getUserId();
    }
    public boolean isUserLoggedIn(){
        return sharedPrefManager.isLoggedIn();
    }
}
