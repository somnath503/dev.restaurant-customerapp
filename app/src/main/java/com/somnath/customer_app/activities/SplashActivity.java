package com.somnath.customer_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider

import com.somnath.customer_app.R;
import com.somnath.customer_app.viewmodels.AuthViewModel; // Import your AuthViewModel
public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000;
    private AuthViewModel authViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // the ViewModel to check the login state
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        new Handler().postDelayed(() -> {
            if (authViewModel.isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, MenuActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
    private void navigateToMenuActivity() {
        Intent intent = new Intent(SplashActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
