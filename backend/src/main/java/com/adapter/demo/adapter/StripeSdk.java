package com.adapter.demo.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * ============================================================
 *  ADAPTEE — Stripe's incompatible SDK (simulated)
 * ============================================================
 * Imagine this is the actual stripe-java SDK with its own
 * opinionated API. It uses "charge" terminology, works in
 * cents, and returns Stripe-specific response objects.
 *
 * We CANNOT modify this class (it's a third-party library).
 * This is exactly why we need the Adapter Pattern.
 * ============================================================
 */
@Slf4j
@Component
public class StripeSdk {

    /**
     * Stripe thinks in cents, not dollars.
     * Stripe uses "charge" not "payment".
     */
    public Map<String, Object> createCharge(
            long amountInCents,
            String currency,
            String receiptEmail,
            String description,
            Map<String, String> metadata
    ) {
        log.info("[Stripe SDK] Creating charge: {} {} cents for {}", amountInCents, currency, receiptEmail);

        // Simulate Stripe's response format
        String chargeId = "ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        return Map.of(
                "id", chargeId,
                "object", "charge",
                "amount", amountInCents,
                "currency", currency.toLowerCase(),
                "status", "succeeded",
                "receipt_email", receiptEmail,
                "description", description,
                "paid", true,
                "created", System.currentTimeMillis() / 1000
        );
    }

    public Map<String, Object> createRefund(String chargeId, long amountInCents) {
        log.info("[Stripe SDK] Creating refund for charge: {}", chargeId);
        return Map.of(
                "id", "re_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24),
                "object", "refund",
                "amount", amountInCents,
                "charge", chargeId,
                "status", "succeeded"
        );
    }

    public Map<String, Object> retrieveCharge(String chargeId) {
        log.info("[Stripe SDK] Retrieving charge: {}", chargeId);
        return Map.of(
                "id", chargeId,
                "object", "charge",
                "status", "succeeded",
                "paid", true
        );
    }
}
