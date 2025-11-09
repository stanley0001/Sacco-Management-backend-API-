package com.example.demo.finance.payments.utils;

public final class PhoneNumberUtils {

    private PhoneNumberUtils() {
    }

    /**
     * Normalize phone numbers for M-PESA API calls.
     * Ensures the number starts with the country code without a leading plus sign (e.g., 2547...).
     */
    public static String normalizeForMpesa(String rawNumber) {
        if (rawNumber == null) {
            return null;
        }

        String trimmed = rawNumber.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        // Remove all non-digit characters (covers '+', spaces, dashes, etc.)
        String digitsOnly = trimmed.replaceAll("\\D", "");

        if (digitsOnly.isEmpty()) {
            return digitsOnly;
        }

        if (digitsOnly.startsWith("0")) {
            return "254" + digitsOnly.substring(1);
        }

        if (digitsOnly.startsWith("254")) {
            return digitsOnly;
        }

        return "254" + digitsOnly;
    }

    /**
     * Normalize phone numbers for SMS providers that expect a leading plus sign (e.g., +2547...).
     */
    public static String normalizeForSms(String rawNumber) {
        String normalized = normalizeForMpesa(rawNumber);
        if (normalized == null || normalized.isEmpty()) {
            return normalized;
        }
        return normalized.startsWith("+") ? normalized : "+" + normalized;
    }
}
