package com.adapter.demo.port;

import com.adapter.demo.model.PaymentRequest;
import com.adapter.demo.model.PaymentResponse;

/**
 * ============================================================
 *  ADAPTER PATTERN — TARGET INTERFACE
 * ============================================================
 * This is the interface our application code depends on.
 * Every payment gateway, regardless of its own API shape,
 * will be adapted to conform to this contract.
 *
 *  Client (Controller)
 *      │
 *      ▼
 *  PaymentPort  ◄──── the "Target"
 *      │
 *  ┌───┴────────────────┐
 *  ▼                    ▼
 *  StripeAdapter    PayPalAdapter  ◄──── "Adapters"
 *      │                    │
 *  StripeSDK         PayPalSDK     ◄──── "Adaptees"
 * ============================================================
 */
public interface PaymentPort {

    /**
     * Process a payment using the underlying gateway.
     * Implementations translate the unified request into
     * whatever the specific provider expects.
     */
    PaymentResponse processPayment(PaymentRequest request);

    /**
     * Refund a previously completed transaction.
     */
    PaymentResponse refundPayment(String transactionId, double amount);

    /**
     * Check the status of a transaction.
     */
    PaymentResponse getPaymentStatus(String transactionId);

    /**
     * Human-readable name of the provider this adapter wraps.
     */
    String getProviderName();
}
