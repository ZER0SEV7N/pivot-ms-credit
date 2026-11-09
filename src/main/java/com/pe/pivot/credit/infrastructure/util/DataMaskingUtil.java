package com.pe.pivot.credit.infrastructure.util;

/**
 * Utility class strictly responsible for masking sensitive Personally Identifiable Information (PII)
 * and account numbers in application logs.
 * Adheres to the Single Responsibility Principle (SRP).
 */
public final class DataMaskingUtil {

    private DataMaskingUtil() { } // Private constructor to prevent instantiation

    /**
     * Masks a credit number by replacing all but the last four digits with asterisks.
     * If the string has more than 4 characters, it will return a string with the last 4 characters visible and the rest masked.
     * If the String has less than 4 characters, it will return "****" to indicate that the number is too short to display.
     *
     * @param creditNumber - the credit number to be masked
     * @return - the masked credit number, or "****" if the input is null or too short
     */
    public static String maskCreditNumber(String creditNumber) {
        if(creditNumber == null || creditNumber.length() < 4) {
            return "****"; // Return masked if account number is too short or null
        }

        // If the string has more than 4 characters, mask all but the last 4
        int visibleChars = 4;
        String tail = creditNumber.substring(creditNumber.length() - visibleChars);
        return "*".repeat(creditNumber.length() - visibleChars) + tail;
    }
}
