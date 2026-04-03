package com.somnath.customer_app.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.snackbar.Snackbar;
import com.somnath.customer_app.R;
import com.somnath.customer_app.utils.ValidationUtils;
import com.somnath.customer_app.viewmodels.AuthViewModel;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = "RegistrationActivity";
    private static final int LOCATION_PERMISSION_CODE = 1001;
    private static final String COUNTRY_CODE = "+91";

    private AuthViewModel authViewModel;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    private CoordinatorLayout coordinatorLayout;
    private EditText etName, etAddress, etPhoneNumber, etOtp;
    private Button btnCompleteRegistration, btnUseCurrentLocation, btnSendOtp, btnVerifyOtp;
    private ProgressBar progressBar;
    private TextView tvMessage, tvResendOtp;
    private LinearLayout layoutPhoneInput, layoutOtpInput, layoutDetailsInput;
    private CheckBox cbPrivacyPolicy;

    private Double currentLat, currentLon;
    private String verifiedPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        initializeUI();
        setupPrivacyPolicyLink();
        setupPhoneNumberInput();
        setupListeners();
        observeViewModel();

        String phoneFromIntent = getIntent().getStringExtra("PHONE_NUMBER");
        if (phoneFromIntent != null && !phoneFromIntent.isEmpty()) {
            this.verifiedPhoneNumber = phoneFromIntent;
            updateUIState(RegistrationState.DETAILS_INPUT);
            displayMessage("Phone verified. Please complete your details.", false);
        } else {
            updateUIState(RegistrationState.PHONE_INPUT);
        }
    }

    private void initializeUI() {
        coordinatorLayout = findViewById(R.id.registration_coordinator_layout);
        layoutPhoneInput = findViewById(R.id.ll_phone_input_container);
        layoutOtpInput = findViewById(R.id.ll_otp_input_container);
        layoutDetailsInput = findViewById(R.id.ll_details_input_container);
        etPhoneNumber = findViewById(R.id.et_phone_number_reg);
        etOtp = findViewById(R.id.et_otp_reg);
        etName = findViewById(R.id.et_name);
        etAddress = findViewById(R.id.et_address);
        btnSendOtp = findViewById(R.id.btn_send_otp_reg);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp_reg);
        btnUseCurrentLocation = findViewById(R.id.btn_use_current_location);
        btnCompleteRegistration = findViewById(R.id.btn_complete_registration);
        progressBar = findViewById(R.id.progress_bar_registration);
        tvMessage = findViewById(R.id.tv_registration_message);
        tvResendOtp = findViewById(R.id.tv_resend_otp);
        cbPrivacyPolicy = findViewById(R.id.cb_privacy_policy);
    }

    private void setupPrivacyPolicyLink() {
        String fullText = "I agree to the Privacy Policy";
        String linkText = "Privacy Policy";
        SpannableString spannableString = new SpannableString(fullText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String url = "https://ritafoodland.in/privacy-policy";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
                ds.setColor(ContextCompat.getColor(RegistrationActivity.this, R.color.colorPrimary));
            }
        };

        int startIndex = fullText.indexOf(linkText);
        int endIndex = startIndex + linkText.length();

        spannableString.setSpan(clickableSpan, startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        cbPrivacyPolicy.setText(spannableString);
        cbPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
    }
    private void setupPhoneNumberInput() {
        String currentText = etPhoneNumber.getText().toString().trim();
        if (!currentText.startsWith(COUNTRY_CODE)) {
            etPhoneNumber.setText(COUNTRY_CODE);
            etPhoneNumber.setSelection(COUNTRY_CODE.length());
        }
        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (!text.startsWith(COUNTRY_CODE)) {
                    etPhoneNumber.setText(COUNTRY_CODE);
                    etPhoneNumber.setSelection(COUNTRY_CODE.length());
                }
            }
        });
    }
    private void setupListeners() {
        btnSendOtp.setOnClickListener(v -> requestOtpFromBackend());
        btnVerifyOtp.setOnClickListener(v -> verifyOtpWithBackend());
        btnUseCurrentLocation.setOnClickListener(v -> handleLocationButtonClick());
        btnCompleteRegistration.setOnClickListener(v -> submitRegistrationDetails());
        tvResendOtp.setOnClickListener(v -> resendOtp());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToLogin();
            }
        });
    }
    private void handleLocationButtonClick() {
        if (!isLocationEnabled()) {
            Snackbar.make(coordinatorLayout, "Location is required for accurate address detection.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("ENABLE", v -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .show();
        } else {
            requestLocationPermission();
        }
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    private void requestOtpFromBackend() {
        String phone = etPhoneNumber.getText().toString().trim().replaceAll("[\\s\\-]", "");
        if (!phone.startsWith(COUNTRY_CODE) && phone.length() == 10) {
            phone = COUNTRY_CODE + phone;
        }
        if (!ValidationUtils.isValidPhoneNumber(phone)) {
            displayMessage("Please enter a valid 10-digit mobile number.", true);
            return;
        }
        verifiedPhoneNumber = phone;
        btnSendOtp.setEnabled(false);
        showProgressBar(true);
        new Handler(Looper.getMainLooper()).postDelayed(() -> btnSendOtp.setEnabled(true), 30000);
        authViewModel.requestOtpFromBackend(phone);
    }
    private void verifyOtpWithBackend() {
        String otp = etOtp.getText().toString().trim();
        if (!isValidOtp(otp)) {
            displayMessage("Please enter a valid 6-digit OTP.", true);
            return;
        }
        showProgressBar(true);
        authViewModel.verifyOtpWithBackend(verifiedPhoneNumber, otp);
    }
    private boolean isValidOtp(String otp) {
        return otp != null && otp.matches("\\d{6}");
    }
    private void resendOtp() {
        tvResendOtp.setEnabled(false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> tvResendOtp.setEnabled(true), 30000);
        authViewModel.requestOtpFromBackend(verifiedPhoneNumber);
        displayMessage("Resending OTP...", false);
    }
    private void submitRegistrationDetails() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (!cbPrivacyPolicy.isChecked()) {
            displayMessage("Please agree to the Privacy Policy to continue.", true);
            return;
        }

        if (name.isEmpty() || address.isEmpty() || currentLat == null || currentLon == null) {
            displayMessage("Please fill all details and set location.", true);
            return;
        }
        authViewModel.registerUserOnBackend(verifiedPhoneNumber, name, "", address, currentLat, currentLon);
    }
    private void observeViewModel() {
        authViewModel.getLoading().observe(this, this::showProgressBar);
        authViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                displayMessage(error, true);
            }
        });
        authViewModel.getOtpSentSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                updateUIState(RegistrationState.OTP_INPUT);
                displayMessage("OTP sent successfully! Enter it below.", false);
            }
        });
        authViewModel.getLoginResult().observe(this, loginResponse -> {
            if (loginResponse != null && loginResponse.isSuccess()) {
                updateUIState(RegistrationState.DETAILS_INPUT);
                displayMessage("OTP verified! Now enter your details.", false);
            } else if (loginResponse != null) {
                displayMessage(loginResponse.getMessage(), true);
            }
        });
        authViewModel.getRegisterResult().observe(this, response -> {
            if (response != null && response.isSuccess()) {
                displayMessage("Registration Successful!", false);
                navigateToMenu();
            } else if (response != null) {
                displayMessage(response.getMessage(), true);
            }
        });
    }

    private enum RegistrationState { PHONE_INPUT, OTP_INPUT, DETAILS_INPUT }

    private void updateUIState(RegistrationState state) {
        layoutPhoneInput.setVisibility(state == RegistrationState.PHONE_INPUT ? View.VISIBLE : View.GONE);
        layoutOtpInput.setVisibility(state == RegistrationState.OTP_INPUT ? View.VISIBLE : View.GONE);
        layoutDetailsInput.setVisibility(state == RegistrationState.DETAILS_INPUT ? View.VISIBLE : View.GONE);
        tvResendOtp.setVisibility(state == RegistrationState.OTP_INPUT ? View.VISIBLE : View.GONE);
        if (state == RegistrationState.PHONE_INPUT) etPhoneNumber.requestFocus();
        else if (state == RegistrationState.OTP_INPUT) etOtp.requestFocus();
        else etName.requestFocus();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            fetchCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                displayMessage("Location permission is required to auto-detect address.", true);
            }
        }
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        showProgressBar(true);
        displayMessage("Fetching location...", false);

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                fusedLocationProviderClient.removeLocationUpdates(this);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLon = location.getLongitude();
                    fetchAddressFromCoordinates(location);
                } else {
                    showProgressBar(false);
                    displayMessage("Could not get location. Ensure GPS is on and try again.", true);
                }
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void fetchAddressFromCoordinates(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                etAddress.setText(addresses.get(0).getAddressLine(0));
                displayMessage("Location fetched!", false);
            }
        } catch (IOException e) {
            displayMessage("Error getting address from location.", true);
        } finally {
            showProgressBar(false);
        }
    }
    private void showProgressBar(boolean isVisible) {
        progressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void displayMessage(String message, boolean isError) {
        tvMessage.setText(message);
        tvMessage.setTextColor(ContextCompat.getColor(this, isError ? R.color.red : R.color.text_primary_dark));
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMenu() {
        startActivity(new Intent(this, MenuActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}
