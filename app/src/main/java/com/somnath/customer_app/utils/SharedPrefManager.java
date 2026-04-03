package com.somnath.customer_app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPrefManager {

    private static final String TAG = "SharedPrefManager";
    private static final String SHARED_PREF_NAME = "customer_app_pref";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";

    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
        Log.d(TAG, "Auth token saved.");
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }
    public void saveUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
        Log.d(TAG, "User ID saved successfully: " + userId);
    }
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        return getAuthToken() != null;
    }

    public void clearData() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "Shared Preferences cleared.");
    }
}
