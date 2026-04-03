package com.somnath.customer_app.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.somnath.customer_app.config.ApiConfig;
import com.somnath.customer_app.utils.NetworkInterceptor;

import java.time.LocalDateTime; // Make sure this is imported
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;
    private static ApiService apiService;
    private static NetworkInterceptor networkInterceptor = new NetworkInterceptor();

    public static ApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(networkInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // --- CRITICAL FIX: Configure Gson for LocalDateTime ---
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                            new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))) // Serialize to ISO string
                    .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                        // Deserialize from ISO string (handle potential parsing errors)
                        try {
                            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        } catch (Exception e) {
                            // Fallback or log if the backend sends a slightly different format
                            return LocalDateTime.parse(json.getAsString()); // Try default parser
                        }
                    })
                    .create();
            // --- End Gson Configuration ---

            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // <-- Use the configured Gson instance
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            getApiService();
        }
        return retrofit;
    }

    public static NetworkInterceptor getNetworkInterceptorInstance() {
        return networkInterceptor;
    }
}
