package com.somnath.customer_app.utils;

// Basic utility class for data validation
public class ValidationUtils {

    // Example: Basic email validation
    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    public static boolean isValidPhoneNumber(String phoneNumber) {

        if (phoneNumber == null) return false;
        phoneNumber = phoneNumber.trim(); // Trim leading/trailing spaces
        return phoneNumber.matches("^\\+91\\d{10}$");
    }


    public static boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

}
