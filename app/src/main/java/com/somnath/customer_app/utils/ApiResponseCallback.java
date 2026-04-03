package com.somnath.customer_app.utils;
public interface ApiResponseCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
    void onFailure(Throwable t); // For network errors, unexpected exceptions, etc.
}