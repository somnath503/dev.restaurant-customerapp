package com.somnath.customer_app.viewmodels;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.somnath.customer_app.repositories.AuthRepository;
import com.somnath.customer_app.models.LoginResponse;
import com.somnath.customer_app.models.RegisterRequest;
import com.somnath.customer_app.models.RegisterResponse;
import com.somnath.customer_app.models.User;
import com.somnath.customer_app.utils.ApiResponseCallback;
import com.somnath.customer_app.utils.ValidationUtils;

public class AuthViewModel extends AndroidViewModel {

    private static final String TAG = "AuthViewModel";
    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> getLoading() { return _loading; }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    private final MutableLiveData<Boolean> _otpSentSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> getOtpSentSuccess() { return _otpSentSuccess; }

    private final MutableLiveData<LoginResponse> _loginResult = new MutableLiveData<>();
    public LiveData<LoginResponse> getLoginResult() { return _loginResult; }

    private final MutableLiveData<RegisterResponse> _registerResult = new MutableLiveData<>();
    public LiveData<RegisterResponse> getRegisterResult() { return _registerResult; }

    private final MutableLiveData<User> _userProfile = new MutableLiveData<>();
    public LiveData<User> getUserProfile() { return _userProfile; }

    private final MutableLiveData<Boolean> _logoutComplete = new MutableLiveData<>();
    public LiveData<Boolean> getLogoutComplete() { return _logoutComplete; }

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        Log.d(TAG, "AuthViewModel initialized.");
    }

    public void requestOtpFromBackend(String phoneNumber) {
        _loading.setValue(true);
        _error.setValue(null);
        _otpSentSuccess.setValue(false);

        if (!ValidationUtils.isValidPhoneNumber(phoneNumber)) {
            _loading.setValue(false);
            _error.setValue("Please enter a valid phone number (e.g., +91XXXXXXXXXX).");
            return;
        }

        authRepository.requestOtpFromBackend(phoneNumber, new ApiResponseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _loading.setValue(false);
                _otpSentSuccess.setValue(true);
            }
            @Override
            public void onError(String errorMessage) {
                _loading.setValue(false);
                _error.setValue("Failed to send OTP: " + errorMessage);
                _otpSentSuccess.setValue(false);
            }
            @Override
            public void onFailure(Throwable t) {
                _loading.setValue(false);
                _error.setValue("Network error: " + t.getMessage());
                _otpSentSuccess.setValue(false);
            }
        });
    }

    public void verifyOtpWithBackend(String phoneNumber, String otp) {
        _loading.setValue(true);
        _error.setValue(null);

        authRepository.verifyOtpWithBackend(phoneNumber, otp, new ApiResponseCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse result) {
                _loading.setValue(false);
                _loginResult.setValue(result);
            }
            @Override
            public void onError(String errorMessage) {
                _loading.setValue(false);
                _error.setValue(errorMessage);
            }
            @Override
            public void onFailure(Throwable t) {
                _loading.setValue(false);
                _error.setValue("Network error during verification: " + t.getMessage());
            }
        });
    }

    public void registerUserOnBackend(String phoneNumber, String name, String email, String address, Double latitude, Double longitude) {
        _loading.setValue(true);
        _error.setValue(null);
        _registerResult.setValue(null);

        RegisterRequest registerRequest = new RegisterRequest(phoneNumber, null, name, email, address, latitude, longitude);

        authRepository.registerUser(registerRequest, new ApiResponseCallback<RegisterResponse>() {
            @Override
            public void onSuccess(RegisterResponse result) {
                Log.d(TAG, "Backend Registration was successful.");
                _loading.setValue(false);
                _registerResult.setValue(result);
            }
            @Override
            public void onError(String errorMessage) {
                Log.w(TAG, "Backend Registration failed with error: " + errorMessage);
                _loading.setValue(false);
                _error.setValue("Registration failed: " + errorMessage);
            }
            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Backend Registration network request failed.", t);
                _loading.setValue(false);
                _error.setValue("A network error occurred during registration: " + t.getMessage());
            }
        });
    }

    public void fetchUserProfile() {
        _loading.setValue(true);
        _error.setValue(null);
        if (!authRepository.isLoggedIn()) {
            _loading.setValue(false);
            _error.setValue("User not logged in.");
            return;
        }
        authRepository.getCustomerProfile(new ApiResponseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                _loading.setValue(false);
                _userProfile.setValue(result);
            }
            @Override
            public void onError(String errorMessage) {
                _loading.setValue(false);
                _error.setValue("Failed to load profile: " + errorMessage);
            }
            @Override
            public void onFailure(Throwable t) {
                _loading.setValue(false);
                _error.setValue("Network error fetching profile: " + t.getMessage());
            }
        });
    }

    public void updateUserProfile(String name, String email, String address, String backupPhoneNumber, Double latitude, Double longitude) {
        _loading.setValue(true);
        _error.setValue(null);

        if (!authRepository.isLoggedIn()) {
            _loading.setValue(false);
            _error.setValue("User not logged in.");
            return;
        }
        User updatedUser = new User();
        updatedUser.setName(name);
        updatedUser.setEmail(email);
        updatedUser.setAddress(address);
        updatedUser.setBackupPhoneNumber(backupPhoneNumber);
        updatedUser.setLatitude(latitude);
        updatedUser.setLongitude(longitude);

        authRepository.updateCustomerProfile(updatedUser, new ApiResponseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                _loading.setValue(false);
                _userProfile.setValue(result);
            }
            @Override
            public void onError(String errorMessage) {
                _loading.setValue(false);
                _error.setValue("Failed to update profile: " + errorMessage);
            }
            @Override
            public void onFailure(Throwable t) {
                _loading.setValue(false);
                _error.setValue("Network error updating profile: " + t.getMessage());
            }
        });
    }

    public void logout() {
        Log.d(TAG, "AuthViewModel: Logging out user.");
        authRepository.logout();
        _loginResult.setValue(null);
        _registerResult.setValue(null);
        _userProfile.setValue(null);
        _otpSentSuccess.setValue(false);
        _loading.setValue(false);
        _error.setValue(null);
        _logoutComplete.setValue(true);
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }
}
