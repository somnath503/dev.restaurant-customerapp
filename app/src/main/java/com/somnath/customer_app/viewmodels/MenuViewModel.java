package com.somnath.customer_app.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.somnath.customer_app.api.ApiService;
import com.somnath.customer_app.models.MenuItem;
import com.somnath.customer_app.models.Restaurant;
import com.somnath.customer_app.models.SliderImage; // NEW IMPORT
import com.somnath.customer_app.repositories.RestaurantRepository;
import com.somnath.customer_app.utils.ApiResponseCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import retrofit2.Call;

public class MenuViewModel extends AndroidViewModel {

    private static final String TAG = "MenuViewModel";
    private ApiService apiService;

    private RestaurantRepository restaurantRepository;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public LiveData<Boolean> getLoading() { return _loading; }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    private final MutableLiveData<List<MenuItem>> _menuItemList = new MutableLiveData<>();
    public LiveData<List<MenuItem>> getItemList() { return _menuItemList; } // This will hold ALL menu items

    private final MutableLiveData<Restaurant> _restaurantDetails = new MutableLiveData<>();
    public LiveData<Restaurant> getRestaurantDetails() { return _restaurantDetails; }

    private final MutableLiveData<List<MenuItem>> _filteredMenuItems = new MutableLiveData<>();
    public LiveData<List<MenuItem>> getFilteredItemList() { return _filteredMenuItems; } // This will be used for display (search/category filtered)

    private List<MenuItem> originalMenuItems = new ArrayList<>(); // Store original unfiltered items

    private final MutableLiveData<List<MenuItem>> _bestsellersList = new MutableLiveData<>(); // NEW: For bestsellers
    public LiveData<List<MenuItem>> getBestsellersList() { return _bestsellersList; }

    private final MutableLiveData<List<SliderImage>> _sliderImages = new MutableLiveData<>(); // NEW: For slider images
    public LiveData<List<SliderImage>> getSliderImages() { return _sliderImages; }


    public MenuViewModel(@NonNull Application application) {
        super(application);
        restaurantRepository = new RestaurantRepository(application);
        Log.d(TAG, "MenuViewModel initialized.");
    }

    public void fetchSingleRestaurantDetails() {
        _loading.setValue(true);
        _error.setValue(null);
        _restaurantDetails.setValue(null);
        _menuItemList.setValue(null);
        _filteredMenuItems.setValue(null);
        originalMenuItems.clear();
        _bestsellersList.setValue(null); // Clear bestsellers on new fetch
        _sliderImages.setValue(null); // Clear slider images on new fetch

        Log.d(TAG, "MenuViewModel: Calling repository to fetch details for the single restaurant.");

        restaurantRepository.getRestaurantDetails(new ApiResponseCallback<Restaurant>() {
            @Override
            public void onSuccess(Restaurant result) {
                Log.d(TAG, "MenuViewModel: Single Restaurant details fetched successfully: " + (result != null ? result.getName() : "null"));
                _restaurantDetails.setValue(result);
                _error.setValue(null);
                // After getting restaurant details, trigger fetching menu, bestsellers, and slider images
                fetchRestaurantMenu();
                fetchBestsellers(); // NEW CALL
                fetchSliderImages(); // NEW CALL
            }

            @Override
            public void onError(String errorMessage) {
                Log.w(TAG, "MenuViewModel: Failed to fetch single restaurant details: " + errorMessage);
                _loading.setValue(false); // End loading only if this is the only thing loading
                _error.setValue("Failed to load restaurant details: " + errorMessage);
                _menuItemList.setValue(null);
                _filteredMenuItems.setValue(null);
                originalMenuItems.clear();
                _bestsellersList.setValue(null);
                _sliderImages.setValue(null);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "MenuViewModel: Get single restaurant details API call failed", t);
                _loading.setValue(false); // End loading only if this is the only thing loading
                _error.setValue("Network error loading restaurant details: " + t.getMessage());
                _menuItemList.setValue(null);
                _filteredMenuItems.setValue(null);
                originalMenuItems.clear();
                _bestsellersList.setValue(null);
                _sliderImages.setValue(null);
            }
        });
    }
    public void fetchRestaurantMenu() {
        _menuItemList.setValue(null);
        originalMenuItems.clear();

        Log.d(TAG, "MenuViewModel: Calling repository to fetch ALL menu items for the restaurant.");

        restaurantRepository.getRestaurantMenu(new ApiResponseCallback<List<MenuItem>>() {
            @Override
            public void onSuccess(List<MenuItem> result) {
                Log.d("MenuViewModel_DEBUG", "SUCCESS: Full menu fetched. Count: " + (result != null ? result.size() : 0));

                // This is correct: we save the full list for later use (categories/search)
                originalMenuItems = result != null ? result : new ArrayList<>();
                _menuItemList.setValue(originalMenuItems);

                // THE BUG WAS HERE: We must NOT update the filtered list here.
                // _filteredMenuItems.setValue(originalMenuItems);  // THIS LINE IS NOW REMOVED.

                _error.setValue(null);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("MenuViewModel_DEBUG", "ERROR fetching full menu: " + errorMessage);
                _error.setValue("Failed to load all menu: " + errorMessage);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("MenuViewModel_DEBUG", "FAILURE fetching full menu: " + t.getMessage(), t);
                _error.setValue("Network error loading all menu: " + t.getMessage());
            }
        });
    }
    public void fetchBestsellers() {
        _bestsellersList.setValue(null);

        Log.d(TAG, "MenuViewModel: Calling repository to fetch bestsellers.");

        restaurantRepository.getBestsellers(new ApiResponseCallback<List<MenuItem>>() {
            @Override
            public void onSuccess(List<MenuItem> result) {
                Log.d(TAG, "MenuViewModel: Bestsellers fetched successfully. Item count: " + (result != null ? result.size() : 0));
                _bestsellersList.setValue(result != null ? result : new ArrayList<>());

                _filteredMenuItems.setValue(result);
            }

            @Override
            public void onError(String errorMessage) {
                Log.w(TAG, "MenuViewModel: Failed to fetch bestsellers: " + errorMessage);
                _bestsellersList.setValue(new ArrayList<>()); // Set empty list if error
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "MenuViewModel: Get bestsellers API call failed", t);
                _bestsellersList.setValue(new ArrayList<>()); // Set empty list if failure
            }
        });
    }

    public void fetchSliderImages() {
        _sliderImages.setValue(null);
        Log.d(TAG, "MenuViewModel: Calling repository to fetch slider images.");
        restaurantRepository.getSliderImages(new ApiResponseCallback<List<SliderImage>>() {
            @Override
            public void onSuccess(List<SliderImage> result) {
                Log.d(TAG, "MenuViewModel: Slider images fetched successfully. Image count: " + (result != null ? result.size() : 0));
                _sliderImages.setValue(result != null ? result : new ArrayList<>());
                _loading.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                Log.w(TAG, "MenuViewModel: Failed to fetch slider images: " + errorMessage);
                _sliderImages.setValue(new ArrayList<>()); // Set empty list on error to avoid crashing the UI
                _loading.setValue(false); // Stop loading even if this part fails
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "MenuViewModel: Get slider images API call failed", t);
                _sliderImages.setValue(new ArrayList<>()); // Set empty list on failure
                _loading.setValue(false); // Stop loading even if this part fails
            }
        });
    }
    public List<String> extractCategories(List<MenuItem> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> uniqueCategories = new HashSet<>();
        for (MenuItem item : menuItems) {
            if (item.getCategory() != null && !item.getCategory().isEmpty()) {
                uniqueCategories.add(item.getCategory());
            }
        }
        List<String> sortedCategories = new ArrayList<>(uniqueCategories);
        Collections.sort(sortedCategories);
        return sortedCategories;
    }

    public void searchMenuItems(String query) {
        if (originalMenuItems == null || originalMenuItems.isEmpty()) {
            _filteredMenuItems.setValue(new ArrayList<>());
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            _filteredMenuItems.setValue(originalMenuItems);
            return;
        }

        String lowerCaseQuery = query.toLowerCase().trim();
        List<MenuItem> filteredList = new ArrayList<>();
        for (MenuItem item : originalMenuItems) {
            if (item.getName().toLowerCase().contains(lowerCaseQuery) ||
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerCaseQuery))) {
                filteredList.add(item);
            }
        }
        _filteredMenuItems.setValue(filteredList);
    }

    public void filterByCategory(String category) {
        if (originalMenuItems == null || originalMenuItems.isEmpty()) {
            _filteredMenuItems.setValue(new ArrayList<>());
            return;
        }

        if (category == null || category.equalsIgnoreCase("All") || category.trim().isEmpty()) {
            _filteredMenuItems.setValue(originalMenuItems); // Use the full list
            return;
        }
        String lowerCaseCategory = category.toLowerCase().trim();
        List<MenuItem> filteredList = new ArrayList<>();
        for (MenuItem item : originalMenuItems) {
            if (item.getCategory() != null && item.getCategory().toLowerCase().equals(lowerCaseCategory)) {
                filteredList.add(item);
            }
        }
        _filteredMenuItems.setValue(filteredList);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "MenuViewModel onCleared.");
    }
}