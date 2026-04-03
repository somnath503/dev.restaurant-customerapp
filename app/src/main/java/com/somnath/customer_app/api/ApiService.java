package com.somnath.customer_app.api;

import com.somnath.customer_app.models.CreateOrderRequest;
import com.somnath.customer_app.models.LoginRequest;
import com.somnath.customer_app.models.LoginResponse;
import com.somnath.customer_app.models.Restaurant;
import com.somnath.customer_app.models.MenuItem;
import com.somnath.customer_app.models.Order;
import com.somnath.customer_app.models.RegisterRequest;
import com.somnath.customer_app.models.RegisterResponse;
import com.somnath.customer_app.models.SliderImage;
import com.somnath.customer_app.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Header;

public interface ApiService {

    @POST("api/customer/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);


    @POST("api/customer/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @GET("api/customer/profile") // Matches new backend endpoint
    Call<User> getCustomerProfile(@Header("Authorization") String authToken); // Renamed for clarity

    @PUT("api/customer/profile") // Matches new backend endpoint
    Call<User> updateCustomerProfile(@Header("Authorization") String authToken, @Body User userProfile); // Body expects a User object

    @GET("api/public/restaurants/details")
    Call<Restaurant> getRestaurantDetails();

    @GET("api/public/restaurants/menu")
    Call<List<MenuItem>> getRestaurantMenu();
    @GET("api/public/restaurants/bestsellers") // Added as public endpoint in backend
    Call<List<MenuItem>> getBestsellers();

    @GET("api/public/restaurants/slider-images") //Public endpoint for slider images
    Call<List<SliderImage>> getSliderImages();

    @POST("api/orders")
    Call<Order> placeOrder(
            @Header("Authorization") String authToken,
            @Body Order orderDetails
    );

    @GET("api/orders/history")
    Call<List<Order>> getOrderHistory(@Header("Authorization") String authToken);

    @GET("api/orders/{orderId}")
    Call<Order> getOrderDetails(
            @Header("Authorization") String authToken,
            @Path("orderId") String orderId
    );
    @GET("api/public/menu-items/{menuItemId}/related")
    Call<List<MenuItem>> getRelatedItems(@Path("menuItemId") Long menuItemId);

    @POST("api/customer/payment/create-order")
    Call<Map<String, String>> createRazorpayOrder(
            @Header("Authorization") String authToken,
            @Body CreateOrderRequest request
    );

    @PUT("api/admin/orders/{orderId}/cancel") // Note: The backend endpoint is in OrderController, but the path is correct
    Call<Order> cancelOrder(
            @Header("Authorization") String authToken,
            @Path("orderId") Long orderId
    );

    @POST("api/customer/send-otp")
    Call<Void> sendOtp(@Body Map<String, String> payload);

    @POST("api/customer/verify-otp")
    Call<LoginResponse> verifyOtp(@Body Map<String, String> payload);

}