package com.adapter.demo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Exact two-decimal conversions for this simulated USD/INR payment API.
 * A real gateway integration must also handle currency-specific minor units.
 */
public final class PaymentAmounts {
    private PaymentAmounts() {}

    public static long minorUnits(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be a positive finite number");
        }
        try {
            return BigDecimal.valueOf(amount).movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Amount must have at most 2 decimal places and fit in minor units", ex);
        }
    }

    public static int intMinorUnits(double amount) {
        try {
            return Math.toIntExact(minorUnits(amount));
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Amount exceeds provider limit", ex);
        }
    }

    public static String formatted(double amount) {
        minorUnits(amount);
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }
}
