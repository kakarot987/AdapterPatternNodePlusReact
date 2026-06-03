package com.adapter.demo.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * ============================================================
 *  ADAPTEE — Razorpay's incompatible SDK (simulated)
 * ============================================================
 * Razorpay (popular in India) has yet another API style:
 *  - Uses "paise" (1/100 of a Rupee), like cents for INR
 *  - Uses "payment_id" prefix
 *  - Status is "captured" not "succeeded" or "COMPLETED"
 *  - Has its own contact/notes system
 * ============================================================
 */
@Slf4j
@Component
public class RazorpaySdk {

    public Map<String, Object> createPayment(
            int amountInPaise,
            String currency,
            String receipt,
            Map<String, String> notes
    ) {
        log.info("[Razorpay SDK] Creating payment: {} paise, receipt: {}", amountInPaise, receipt);

        String paymentId = "pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        return Map.of(
                "id", paymentId,
                "entity", "payment",
                "amount", amountInPaise,
                "currency", currency,
                "status", "captured",
                "receipt", receipt,
                "notes", notes,
                "created_at", System.currentTimeMillis() / 1000
        );
    }

    public Map<String, Object> createRefund(String paymentId, int amountInPaise) {
        log.info("[Razorpay SDK] Creating refund for payment: {}", paymentId);
        return Map.of(
                "id", "rfnd_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14),
                "entity", "refund",
                "amount", amountInPaise,
                "payment_id", paymentId,
                "status", "processed"
        );
    }

    public Map<String, Object> fetchPayment(String paymentId) {
        log.info("[Razorpay SDK] Fetching payment: {}", paymentId);
        return Map.of(
                "id", paymentId,
                "entity", "payment",
                "status", "captured"
        );
    }
}
