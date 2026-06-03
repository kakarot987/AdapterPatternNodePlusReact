package com.adapter.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified payment request — the "Target" interface's data model.
 * Our application always works with this standardized format,
 * regardless of the underlying payment provider.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String orderId;
    private double amount;
    private String currency;
    private String customerEmail;
    private String description;
    private String provider; // "stripe", "paypal", "razorpay"
}
