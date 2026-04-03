package com.somnath.customer_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.somnath.customer_app.R;
import com.somnath.customer_app.models.LoginResponse;
import com.somnath.customer_app.utils.ValidationUtils;
import com.somnath.customer_app.viewmodels.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String COUNTRY_CODE = "+91";

    private AuthViewModel authViewModel;

    // UI elements
    private TextView tvAppName, tvMessage, tvGoToRegister, tvResendOtp;
    private EditText etPhoneNumber, etOtp;
    private Button btnSendOtp, btnVerifyOtp;
    private ProgressBar progressBar;
    private TextInputLayout tilOtp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initUI();
        setupPhoneNumberInput();
        setupButtonListeners();
        observeViewModel();
        updateUiForInputState(true);
    }

    private void initUI() {
        tvAppName = findViewById(R.id.tv_login_title);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etOtp = findViewById(R.id.et_otp);
        btnSendOtp = findViewById(R.id.btn_send_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        progressBar = findViewById(R.id.progress_bar);
        tvMessage = findViewById(R.id.tv_message);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);
        tvResendOtp = findViewById(R.id.tv_resend_otp);
        tilOtp = findViewById(R.id.til_otp);

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

    private void setupButtonListeners() {
        btnSendOtp.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim().replaceAll("[\\s\\-]", "");
            if (!phoneNumber.startsWith("+91") && phoneNumber.length() == 10) {
                phoneNumber = "+91" + phoneNumber;
            }

            if (!ValidationUtils.isValidPhoneNumber(phoneNumber)) {
                showMessage("Please enter a valid phone number (e.g., +91XXXXXXXXXX).", true);
                return;
            }

            btnSendOtp.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> btnSendOtp.setEnabled(true), 30000);

            authViewModel.requestOtpFromBackend(phoneNumber);
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim().replaceAll("[\\s\\-]", "");
            if (!phoneNumber.startsWith("+91") && phoneNumber.length() == 10) {
                phoneNumber = "+91" + phoneNumber;
            }

            if (!isValidOtp(otp)) {
                showMessage("Please enter a valid 6-digit OTP.", true);
                return;
            }

            authViewModel.verifyOtpWithBackend(phoneNumber, otp);
        });

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });

        tvResendOtp.setOnClickListener(v -> resendOtp());
    }

    private boolean isValidOtp(String otp) {
        return otp != null && otp.matches("\\d{6}");  // Exactly 6 digits
    }

    private void resendOtp() {
        tvResendOtp.setEnabled(false);
        // 30-second cooldown for the resend link
        new Handler(Looper.getMainLooper()).postDelayed(() -> tvResendOtp.setEnabled(true), 30000);
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        authViewModel.requestOtpFromBackend(phoneNumber);
        showMessage("Resending OTP...", false);
    }

    private void observeViewModel() {
        authViewModel.getLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            setUiEnabled(!isLoading);
        });

        authViewModel.getError().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showMessage(errorMessage, true);
            }
        });

        authViewModel.getOtpSentSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                updateUiForInputState(false);
                showMessage("OTP sent successfully! Enter it below.", false);
            }
        });

        authViewModel.getLoginResult().observe(this, loginResponse -> {
            if (loginResponse != null) {
                if (loginResponse.isSuccess()) {
                    if (loginResponse.isNewUser()) {
                        showMessage("Phone number verified! Please complete your registration.", false);
                        navigateToRegistrationActivity();
                    } else {
                        showMessage("Login Successful!", false);
                        navigateToMenuActivity();
                    }
                } else {
                    showMessage(loginResponse.getMessage(), true);
                }
            }
        });
    }

    private void setUiEnabled(boolean enabled) {
        etPhoneNumber.setEnabled(enabled);
        etOtp.setEnabled(enabled);
        btnSendOtp.setEnabled(enabled);
        btnVerifyOtp.setEnabled(enabled);
        tvGoToRegister.setEnabled(enabled);
        tvResendOtp.setEnabled(enabled);
    }

    private void updateUiForInputState(boolean showPhoneInput) {
        etPhoneNumber.setVisibility(showPhoneInput ? View.VISIBLE : View.GONE);
        btnSendOtp.setVisibility(showPhoneInput ? View.VISIBLE : View.GONE);
        tvGoToRegister.setVisibility(showPhoneInput ? View.VISIBLE : View.GONE);

        tilOtp.setVisibility(showPhoneInput ? View.GONE : View.VISIBLE);
        btnVerifyOtp.setVisibility(showPhoneInput ? View.GONE : View.VISIBLE);
        tvResendOtp.setVisibility(showPhoneInput ? View.GONE : View.VISIBLE);

        if (showPhoneInput) {
            etPhoneNumber.requestFocus();
        } else {
            etOtp.requestFocus();
        }
    }

    private void showMessage(String message, boolean isError) {
        tvMessage.setText(message);
        tvMessage.setTextColor(ContextCompat.getColor(this, isError ? R.color.red : R.color.black));
        tvMessage.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToRegistrationActivity() {
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        intent.putExtra("PHONE_NUMBER", etPhoneNumber.getText().toString().trim());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMenuActivity() {
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
