package com.somnath.customer_app.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.somnath.customer_app.models.CartItem;
import com.somnath.customer_app.models.MenuItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Singleton repository to hold the cart state for the entire app.
 */
public class CartRepository {

    private static CartRepository instance;
    private final MutableLiveData<List<CartItem>> cartItemsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final ArrayList<CartItem> cartItems = new ArrayList<>();
    private Long currentRestaurantId = null;

    private CartRepository() {}

    public static synchronized CartRepository getInstance() {
        if (instance == null) {
            instance = new CartRepository();
        }
        return instance;
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartItemsLiveData;
    }

    public void addItemToCart(MenuItem item, int quantity, Long restaurantId) {
        if (currentRestaurantId != null && !currentRestaurantId.equals(restaurantId)) {
            clearCart();
        }
        currentRestaurantId = restaurantId;

        for (CartItem cartItem : cartItems) {
            if (Objects.equals(cartItem.getMenuItem().getId(), item.getId())) {
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                updateLiveData();
                return;
            }
        }
        cartItems.add(new CartItem(item, quantity, restaurantId));
        updateLiveData();
    }

    public void updateItemQuantity(CartItem itemToUpdate, int newQuantity) {
        if (newQuantity <= 0) {
            removeItemFromCart(itemToUpdate);
            return;
        }
        for (CartItem cartItem : cartItems) {
            if (Objects.equals(cartItem.getMenuItem().getId(), itemToUpdate.getMenuItem().getId())) {
                cartItem.setQuantity(newQuantity);
                updateLiveData();
                return;
            }
        }
    }

    public void removeItemFromCart(CartItem itemToRemove) {
        cartItems.removeIf(cartItem -> Objects.equals(cartItem.getMenuItem().getId(), itemToRemove.getMenuItem().getId()));
        if (cartItems.isEmpty()) {
            currentRestaurantId = null;
        }
        updateLiveData();
    }

    public void clearCart() {
        cartItems.clear();
        currentRestaurantId = null;
        updateLiveData();
    }

    private void updateLiveData() {
        cartItemsLiveData.postValue(new ArrayList<>(cartItems));
    }
}