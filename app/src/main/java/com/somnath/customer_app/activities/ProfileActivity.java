package com.somnath.customer_app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // ADDED
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.somnath.customer_app.R;
import com.somnath.customer_app.models.User;
import com.somnath.customer_app.utils.ValidationUtils;
import com.somnath.customer_app.viewmodels.AuthViewModel;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private AuthViewModel authViewModel;
    private EditText etName, etEmail, etAddress, etBackupPhone;
    private TextView tvPhoneNumber, tvProfileTitle;
    private Button btnSaveProfile, btnUseCurrentLocation;
    private ProgressBar progressBar;
    private TextView tvMessage;
    private ImageView ivBackArrow;

    private FusedLocationProviderClient fusedLocationClient;

    private Double currentLatitude;
    private Double currentLongitude;

    private User currentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initUI();
        setupListeners();
        observeViewModel();

        if (authViewModel.isLoggedIn()) {
            authViewModel.fetchUserProfile();
        } else {
            showMessage("You are not logged in. Please log in.", true);
            navigateToLoginActivity();
        }
    }

    private void initUI() {
        ivBackArrow = findViewById(R.id.iv_back_arrow);
        tvProfileTitle = findViewById(R.id.tv_profile_title);
        tvPhoneNumber = findViewById(R.id.tv_profile_phone_number);
        etName = findViewById(R.id.et_profile_name);
        etEmail = findViewById(R.id.et_profile_email);
        etAddress = findViewById(R.id.et_profile_address);
        etBackupPhone = findViewById(R.id.et_profile_backup_phone);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnUseCurrentLocation = findViewById(R.id.btn_use_current_location_profile);
        progressBar = findViewById(R.id.progress_bar_profile);
        tvMessage = findViewById(R.id.tv_profile_message);
    }

    private void setupListeners() {
        // listener for the back arrow
        ivBackArrow.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnUseCurrentLocation.setOnClickListener(v -> checkLocationPermissionsAndGetAddress());
    }

    private void observeViewModel() {
        authViewModel.getLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading != null && isLoading ? View.VISIBLE : View.GONE);
            setUiEnabled(!(isLoading != null && isLoading));
        });

        authViewModel.getError().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showMessage(errorMessage, true);
                if (errorMessage.contains("401") || errorMessage.toLowerCase().contains("unauthorized")) {
                    authViewModel.logout();
                    navigateToLoginActivity();
                }
            } else {
                tvMessage.setVisibility(View.GONE);
            }
        });

        authViewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                currentUserProfile = user;
                displayProfile(user);
                showMessage("Profile loaded.", false);
            }
        });

        authViewModel.getLogoutComplete().observe(this, isLoggedOut -> {
            if (isLoggedOut != null && isLoggedOut) {
                navigateToLoginActivity();
            }
        });
    }

    private void displayProfile(User user) {
        // ADDED: Set the title to the user's name
        if (user.getName() != null && !user.getName().isEmpty()) {
            tvProfileTitle.setText(user.getName());
        } else {
            tvProfileTitle.setText("Your Profile"); // Fallback t
        }
        tvPhoneNumber.setText(user.getPhone());
        etName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etAddress.setText(user.getAddress());
        etBackupPhone.setText(user.getBackupPhoneNumber());

        currentLatitude = user.getLatitude();
        currentLongitude = user.getLongitude();
    }
    private void saveProfile() {
        if (currentUserProfile == null) {
            showMessage("Profile data not loaded yet. Please wait.", true);
            return;
        }
        String newAddress = etAddress.getText().toString().trim();
        String backupPhone = etBackupPhone.getText().toString().trim();
        if (newAddress.isEmpty()) {
            etAddress.setError("Address cannot be empty.");
            showMessage("Address is required.", true);
            return;
        }
        if (!backupPhone.isEmpty() && !ValidationUtils.isValidPhoneNumber(backupPhone)) {
            etBackupPhone.setError("Invalid backup phone number format.");
            showMessage("Please enter a valid backup phone number or leave it empty.", true);
            return;
        }

        String finalBackupPhone = backupPhone.isEmpty() ? null : backupPhone;

        authViewModel.updateUserProfile(currentUserProfile.getName(), currentUserProfile.getEmail(), newAddress, finalBackupPhone, currentLatitude, currentLongitude);
    }
    private void checkLocationPermissionsAndGetAddress() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        getCurrentLocationAddress();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAddress();
            } else {
                showMessage("Location permission denied. Please enter address manually.", true);
            }
        }
    }
    private void getCurrentLocationAddress() {
        showMessage("Fetching current location...", false);
        setUiEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showMessage("Location permission not granted.", true);
            setUiEnabled(true);
            progressBar.setVisibility(View.GONE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            progressBar.setVisibility(View.GONE);
            setUiEnabled(true);
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        etAddress.setText(addresses.get(0).getAddressLine(0));
                        showMessage("Location fetched successfully!", false);
                    } else {
                        showMessage("Could not find address for current location.", true);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Geocoder failed", e);
                    showMessage("Error getting address from location.", true);
                }
            } else {
                showMessage("Could not get current location. Ensure GPS is on.", true);
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            setUiEnabled(true);
            Log.e(TAG, "Failed to get location", e);
            showMessage("Failed to get current location: " + e.getMessage(), true);
        });
    }

    private void showMessage(String message, boolean isError) {
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setTextColor(ContextCompat.getColor(this, isError ? R.color.red : R.color.black));
            tvMessage.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setUiEnabled(boolean isEnabled) {
        btnSaveProfile.setEnabled(isEnabled);
        btnUseCurrentLocation.setEnabled(isEnabled);
        etAddress.setEnabled(isEnabled);
        etBackupPhone.setEnabled(isEnabled);
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
