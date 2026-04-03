// --- app\\src\\main\\java\\com\\somnath\\customer_app\\viewmodels\\OrderViewModel.java ---
package com.somnath.customer_app.viewmodels;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.somnath.customer_app.models.User;
import com.somnath.customer_app.utils.Event;
import com.somnath.customer_app.utils.SharedPrefManager;

import com.somnath.customer_app.models.Address;
import com.somnath.customer_app.models.CartItem;
import com.somnath.customer_app.models.Order;
import com.somnath.customer_app.models.OrderItem;
import com.somnath.customer_app.repositories.OrderRepository;
import com.somnath.customer_app.utils.ApiResponseCallback;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class OrderViewModel extends AndroidViewModel {

    private static final String TAG = "OrderViewModel";

    private final OrderRepository orderRepository;

    // LiveData for loading state, exposed publicly as immutable LiveData.
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> getLoading() { return _loading; }

    // LiveData for error messages.
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    // LiveData to hold the result of a newly placed order.
    private final MutableLiveData<Order> _placedOrder = new MutableLiveData<>();
    public LiveData<Order> getPlacedOrder() { return _placedOrder; }

    // LiveData to hold the list of past orders for the history screen.
    private final MutableLiveData<List<Order>> _orderHistory = new MutableLiveData<>();
    public LiveData<List<Order>> getOrderHistory() { return _orderHistory; }

    // LiveData to hold the details of a single fetched order.
    private final MutableLiveData<Order> _orderDetails = new MutableLiveData<>();
    public LiveData<Order> getOrderDetails() { return _orderDetails; }

    // --- NEW --- LiveData to confirm an order status update (e.g., cancellation).
    private final MutableLiveData<Order> _updatedOrder = new MutableLiveData<>();
    public LiveData<Order> getUpdatedOrder() { return _updatedOrder; }
    private final MutableLiveData<Map<String, String>> _razorpayOrderResponse = new MutableLiveData<>();
    public LiveData<Map<String, String>> getRazorpayOrderResponse() {
        return _razorpayOrderResponse;
    }

    private final MutableLiveData<Event<Order>> _cancelledOrder = new MutableLiveData<>();
    public LiveData<Event<Order>> getCancelledOrder() { return _cancelledOrder; }

    public OrderViewModel(@NonNull Application application) {
        super(application);
        this.orderRepository = new OrderRepository(application);
        Log.d(TAG, "OrderViewModel initialized.");
    }
    public void createRazorpayOrder(double totalAmount) {
        _error.setValue(null);
        orderRepository.createRazorpayOrder(totalAmount, _razorpayOrderResponse, _error);
    }


    public void placeOrder(List<CartItem> cartItems, User user,
                           double deliveryFee, double totalAmount, String paymentMethod) {
        _loading.setValue(true);
        _error.setValue(null);

        // Safety check for user and address
        if (user == null || user.getAddress() == null || user.getAddress().isEmpty()) {
            _loading.setValue(false);
            _error.setValue("Cannot place order: User profile or address is missing.");
            return;
        }

        try {
            // 1. Create the main Order object for the backend
            Order orderToSend = new Order();
            orderToSend.setDeliveryAddress(user.getAddress()); // Get address from the User object
            orderToSend.setPaymentMethod(paymentMethod);
            orderToSend.setOrderDate(LocalDateTime.now());

            // Get location from the User object
            orderToSend.setLatitude(user.getLatitude());
            orderToSend.setLongitude(user.getLongitude());

            // Set financial details
            orderToSend.setTotalAmount(String.valueOf(totalAmount));
            orderToSend.setDeliveryFee(String.valueOf(deliveryFee));

            // 2. Convert CartItems to OrderItems for the request
            double subtotal = 0.0;
            List<com.somnath.customer_app.models.OrderItem> orderItemsForBackend = new ArrayList<>();
            if (cartItems != null) {
                for (CartItem cartItem : cartItems) {
                    com.somnath.customer_app.models.OrderItem orderItem = new com.somnath.customer_app.models.OrderItem(
                            cartItem.getMenuItem().getId(),
                            cartItem.getMenuItem().getName(),
                            cartItem.getMenuItem().getPrice(),
                            cartItem.getQuantity(),
                            cartItem.getMenuItem().getImageUrl()
                    );
                    orderItemsForBackend.add(orderItem);
                    subtotal += cartItem.getTotalPrice();
                }
            }
            orderToSend.setItems(orderItemsForBackend);
            orderToSend.setSubtotal(String.valueOf(subtotal));

            // 3. Call the repository to place the order
            orderRepository.placeOrder(orderToSend, new ApiResponseCallback<Order>() {
                @Override
                public void onSuccess(Order result) {
                    _loading.setValue(false);
                    _placedOrder.setValue(result);
                }

                @Override
                public void onError(String errorMessage) {
                    _loading.setValue(false);
                    _error.setValue(errorMessage);
                }

                @Override
                public void onFailure(Throwable t) {
                    _loading.setValue(false);
                    _error.setValue("Network error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            _loading.setValue(false);
            _error.setValue("Error preparing order: " + e.getMessage());
        }
    }


    public void fetchOrderHistory() {
        if (!orderRepository.isUserLoggedIn()) {
            _error.setValue("You must be logged in to view order history.");
            return;
        }

        _loading.setValue(true);
        _error.setValue(null);
        _orderHistory.setValue(null);

        orderRepository.getOrderHistory(new ApiResponseCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> result) {
                _loading.setValue(false);
                _orderHistory.setValue(result);
            }

            @Override
            public void onError(String errorMessage) {
                _loading.setValue(false);
                _error.setValue("Failed to load order history: " + errorMessage);
            }

            @Override
            public void onFailure(Throwable t) {
                _loading.setValue(false);
                _error.setValue("Network error loading history: " + t.getMessage());
            }
        });
    }

    /**
     * Fetches the complete details for a single order by its ID.
     * @param orderId The unique identifier for the order.
     */
    public void fetchOrderDetails(String orderId) {
        if (!orderRepository.isUserLoggedIn()) {
            _error.setValue("You must be logged in to view order details.");
            return;
        }
        _loading.setValue(true);
        _error.setValue(null);
        _orderDetails.setValue(null);

        orderRepository.getOrderDetails(orderId, new ApiResponseCallback<Order>() {
            @Override
            public void onSuccess(Order result) {
                _loading.setValue(false);
                _orderDetails.setValue(result);
            }

            @Override
            public void onError(String errorMessage) {
                _loading.setValue(false);
                _error.setValue("Failed to load order details: " + errorMessage);
            }

            @Override
            public void onFailure(Throwable t) {
                _loading.setValue(false);
                _error.setValue("Network error loading details: " + t.getMessage());
            }
        });
    }

    /**
     * --- NEW METHOD ---
     * Sends a request to cancel a specific order.
     * This assumes the backend has an endpoint to handle this action.
     * @param orderId The ID of the order to be canceled.
     */
    public void cancelOrder(Long orderId) {
        _loading.setValue(true);
        _error.setValue(null);
        // Do not set value to null here anymore, a new Event will be a fresh signal.

        orderRepository.cancelOrder(orderId, new ApiResponseCallback<Order>() {
            @Override
            public void onSuccess(Order result) {
                _loading.setValue(false);
                // --- MODIFICATION 2: Wrap the result in an Event ---
                _cancelledOrder.setValue(new Event<>(result));
            }

            @Override
            public void onError(String errorMessage) {
                _loading.setValue(false);
                // Also wrap errors if you want them to be single-fire events
                _error.setValue(errorMessage);
            }

            @Override
            public void onFailure(Throwable t) {
                _loading.setValue(false);
                _error.setValue("Network error during cancellation: " + t.getMessage());
            }
        });
    }


    /**
     * Helper method to safely parse the user ID from shared preferences.
     * @return The user ID as a Long, or null if it's missing or invalid.
     */
    private Long getParsedUserId() {
        try {
            String userIdString = orderRepository.getUserId();
            return userIdString != null ? Long.parseLong(userIdString) : null;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not parse user ID from SharedPreferences", e);
            return null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "OrderViewModel onCleared.");
    }
}
