// app/src/main/java/com/somnath/customer_app/utils/ApiResponseCallback.java
package com.somnath.customer_app.utils;

// A generic interface for handling API response callbacks
public interface ApiResponseCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
    void onFailure(Throwable t); // For network errors, unexpected exceptions, etc.
}