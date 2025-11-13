package com.tongji.auth.util;

import java.util.regex.Pattern;

public final class IdentifierValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private IdentifierValidator() {
    }

    /**
     * Validates phone number format (Mainland China 11 digits, starting with 1).
     *
     * @param phone phone number string.
     * @return whether it matches the phone number regex.
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates email format (case-insensitive).
     *
     * @param email email string.
     * @return whether it matches the email regex.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
