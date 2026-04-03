package com.somnath.customer_app;

import android.app.Application;

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        FirebaseApp.initializeApp(this); // This line
        // ... rest of your global initialization (e.g., Dagger, Timber, etc.)
    }
}