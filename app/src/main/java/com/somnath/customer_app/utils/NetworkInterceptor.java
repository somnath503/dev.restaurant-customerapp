// app/src/main/java/com/somnath/customer_app/utils/NetworkInterceptor.java
package com.somnath.customer_app.utils;

import android.util.Log;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

// This interceptor can be used to add headers like Authentication tokens
public class NetworkInterceptor implements Interceptor {

    // You would typically inject or get your auth token here,
    // e.g., from SharedPrefManager
    private String authToken = null; // Placeholder

    // You might need a way to update the token, e.g., after login
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        // Add Authorization header if token is available
        if (authToken != null && !authToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        Request newRequest = requestBuilder.build();
        Response response = chain.proceed(newRequest);

        // Handle 401 errors by logging (token refresh handled elsewhere, e.g., in ViewModel)
        if (response.code() == 401 && authToken != null) {
            Log.e("NetworkInterceptor", "401 Unauthorized - Token may be expired. Handle refresh in calling code.");
            // You can throw an exception or handle retry logic in the calling API layer
        }

        return response;
    }


}
