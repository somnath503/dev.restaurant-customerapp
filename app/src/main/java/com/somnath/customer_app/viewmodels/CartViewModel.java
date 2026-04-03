package com.somnath.customer_app.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.somnath.customer_app.models.CartItem;
import com.somnath.customer_app.models.MenuItem;
import com.somnath.customer_app.repositories.CartRepository;
import java.util.List;

public class    CartViewModel extends AndroidViewModel {

    private final CartRepository cartRepository;



    public CartViewModel(@NonNull Application application) {
        super(application);
        cartRepository = CartRepository.getInstance();
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartRepository.getCartItems();
    }

    public LiveData<Double> getCartTotal() {
        return Transformations.map(cartRepository.getCartItems(), items -> {
            double total = 0.0;
            if (items != null) {
                for (CartItem item : items) {
                    total += item.getTotalPrice();
                }
            }
            return total;
        });
    }

    public LiveData<Integer> getCartItemCount() {
        return Transformations.map(cartRepository.getCartItems(), items -> {
            int count = 0;
            if (items != null) {
                for (CartItem item : items) {
                    count += item.getQuantity();
                }
            }
            return count;
        });
    }

    public void addItemToCart(MenuItem item, int quantity, Long restaurantId) {
        cartRepository.addItemToCart(item, quantity, restaurantId);
    }

    public void updateItemQuantity(CartItem item, int newQuantity) {
        cartRepository.updateItemQuantity(item, newQuantity);
    }

    public void removeCartItem(CartItem item) {
        cartRepository.removeItemFromCart(item);
    }

    public void clearCart() {
        cartRepository.clearCart();
    }
}