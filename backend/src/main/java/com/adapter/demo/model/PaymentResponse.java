package com.adapter.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified payment response — always returned to the client
 * in this standardized shape, regardless of provider.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String transactionId;
    private String status;          // "SUCCESS", "FAILED", "PENDING"
    private String provider;
    private double amount;
    private String currency;
    private String message;
    private long timestamp;
    private Object rawProviderResponse; // the original provider-specific response (for debugging)
}
