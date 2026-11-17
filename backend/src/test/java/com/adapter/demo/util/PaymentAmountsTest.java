package com.adapter.demo.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentAmountsTest {
    @Test
    void convertsExactMinorUnitsWithoutFloatingPointRounding() {
        assertThat(PaymentAmounts.minorUnits(99.99)).isEqualTo(9999L);
        assertThat(PaymentAmounts.minorUnits(0.01)).isEqualTo(1L);
        assertThat(PaymentAmounts.formatted(12.5)).isEqualTo("12.50");
        assertThat(PaymentAmounts.intMinorUnits(99.99)).isEqualTo(9999);
    }

    @Test
    void rejectsExtraFractionalDigitsAndNonFiniteAmounts() {
        for (double amount : new double[]{1.005, 0, -5, Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MAX_VALUE}) {
            assertThatThrownBy(() -> PaymentAmounts.minorUnits(amount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> PaymentAmounts.intMinorUnits(30_000_000))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
