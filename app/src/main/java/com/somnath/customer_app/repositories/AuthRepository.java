package com.somnath.customer_app.repositories;

import android.content.Context;
import android.util.Log;
import com.somnath.customer_app.api.ApiService;
import com.somnath.customer_app.api.RetrofitClient;
import com.somnath.customer_app.models.LoginResponse;
import com.somnath.customer_app.models.RegisterRequest;
import com.somnath.customer_app.models.RegisterResponse;
import com.somnath.customer_app.models.User;
import com.somnath.customer_app.utils.ApiResponseCallback;
import com.somnath.customer_app.utils.SharedPrefManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final ApiService apiService;
    private final SharedPrefManager sharedPrefManager;

    public AuthRepository(Context context) {
        this.apiService = RetrofitClient.getApiService();
        this.sharedPrefManager = SharedPrefManager.getInstance(context);
        Log.d(TAG, "AuthRepository initialized.");
    }

    /**
     * Calls the backend to request an OTP for the given phone number.
     * Replaces the old Firebase phone verification.
     */
    public void requestOtpFromBackend(String phoneNumber, ApiResponseCallback<Void> callback) {
        Log.d(TAG, "Repository: Calling backend /send-otp for phone: " + phoneNumber);
        Map<String, String> payload = new HashMap<>();
        payload.put("phoneNumber", phoneNumber);

        apiService.sendOtp(payload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing response body", e);
                    }
                    Log.e(TAG, "OTP send failed: Code " + response.code() + " - " + errorBody);
                    callback.onError("Failed to send OTP. Code: " + response.code() + " - " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "OTP send request failed", t);
                callback.onFailure(t);
            }
        });
    }

    /**
     * Calls the backend to verify the OTP and login/check user status.
     * Replaces the old Firebase credential sign-in and backend /login call.
     */
    public void verifyOtpWithBackend(String phoneNumber, String otp, ApiResponseCallback<LoginResponse> callback) {
        Log.d(TAG, "Repository: Calling backend /verify-otp for phone: " + phoneNumber);
        Map<String, String> payload = new HashMap<>();
        payload.put("phoneNumber", phoneNumber);
        payload.put("otp", otp);

        apiService.verifyOtp(payload).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    // If login is successful and it's an existing user, a token will be present.
                    if (loginResponse.isSuccess() && loginResponse.getAuthToken() != null) {
                        sharedPrefManager.saveAuthToken(loginResponse.getAuthToken());
                        if (loginResponse.getUser() != null && loginResponse.getUser().getId() != null) {
                            sharedPrefManager.saveUserId(loginResponse.getUser().getId().toString());
                        }
                    }
                    callback.onSuccess(loginResponse);
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing response body", e);
                    }
                    Log.w(TAG, "OTP Verification failed: " + response.code() + " - " + errorBody);
                    callback.onError("OTP Verification failed: " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "OTP verification request failed", t);
                callback.onFailure(t);
            }
        });
    }

    /**
     * Handles new user registration. This is called *after* a successful OTP verification
     * confirms the user is new. The logic remains the same, but the call to it will change.
     */
    public void registerUser(RegisterRequest request, final ApiResponseCallback<RegisterResponse> callback) {
        Log.d(TAG, "Repository: Calling backend /register API for phone: " + request.getPhoneNumber());
        apiService.registerUser(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse.isSuccess()) {
                        sharedPrefManager.saveAuthToken(registerResponse.getAuthToken());
                        if (registerResponse.getUser() != null && registerResponse.getUser().getId() != null) {
                            sharedPrefManager.saveUserId(registerResponse.getUser().getId().toString());
                        }
                    }
                    callback.onSuccess(registerResponse);
                } else {
                    String errorMsg = "Registration failed with code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing error body for registration", e);
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e(TAG, "Network request for registration failed", t);
                callback.onFailure(t);
            }
        });
    }

    /**
     * Logs out the user by clearing local shared preferences.
     * The Firebase signOut call is removed.
     */
    public void logout() {
        Log.d(TAG, "Repository: Logging out user and clearing local data.");
        sharedPrefManager.clearData();
        if (RetrofitClient.getNetworkInterceptorInstance() != null) {
            RetrofitClient.getNetworkInterceptorInstance().setAuthToken(null);
        } else {
            Log.w(TAG, "NetworkInterceptor instance is null, cannot clear token on logout.");
        }
    }

    /**
     * Checks if the user is logged in based on the presence of a local auth token.
     */
    public boolean isLoggedIn() {
        return sharedPrefManager.isLoggedIn();
    }

    // --- The following methods are kept as they are still relevant ---

    public void getCustomerProfile(ApiResponseCallback<User> callback) {
        String authToken = sharedPrefManager.getAuthToken();
        if (authToken == null || authToken.isEmpty()) {
            Log.w(TAG, "Repository: Cannot fetch profile, auth token is null.");
            callback.onError("User not authenticated. Please log in.");
            return;
        }
        apiService.getCustomerProfile("Bearer " + authToken).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Get Profile failed: " + response.code();
                    try {
                        if(response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) { /* ... */ }
                    callback.onError(errorMsg);
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    public void updateCustomerProfile(User userProfile, ApiResponseCallback<User> callback) {
        String authToken = sharedPrefManager.getAuthToken();
        if (authToken == null || authToken.isEmpty()) {
            callback.onError("User not authenticated.");
            return;
        }
        apiService.updateCustomerProfile("Bearer " + authToken, userProfile).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Update profile failed. Code: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }
}
