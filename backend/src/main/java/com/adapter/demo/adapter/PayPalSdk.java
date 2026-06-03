package com.adapter.demo.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ============================================================
 *  ADAPTEE — PayPal's incompatible SDK (simulated)
 * ============================================================
 * PayPal uses completely different terminology and structure:
 *  - "orders" instead of "charges"
 *  - amounts as strings inside nested objects
 *  - its own status codes ("COMPLETED" not "succeeded")
 *  - different field names for everything
 *
 * We CANNOT modify this class (third-party library).
 * ============================================================
 */
@Slf4j
@Component
public class PayPalSdk {

    /**
     * PayPal uses "orders", not "charges".
     * Amount is a String inside a nested purchase_units object.
     */
    public Map<String, Object> createOrder(
            String intentType,           // "CAPTURE"
            List<Map<String, Object>> purchaseUnits,
            String payerEmail
    ) {
        log.info("[PayPal SDK] Creating order for payer: {}", payerEmail);

        String orderId = "PAY-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 20);
        return Map.of(
                "id", orderId,
                "intent", intentType,
                "status", "COMPLETED",
                "payer", Map.of("email_address", payerEmail),
                "purchase_units", purchaseUnits,
                "create_time", java.time.Instant.now().toString()
        );
    }

    public Map<String, Object> captureRefund(String captureId, Map<String, Object> amount) {
        log.info("[PayPal SDK] Issuing refund for capture: {}", captureId);
        return Map.of(
                "id", "REF-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 16),
                "status", "COMPLETED",
                "amount", amount
        );
    }

    public Map<String, Object> showOrderDetails(String orderId) {
        log.info("[PayPal SDK] Fetching order details: {}", orderId);
        return Map.of(
                "id", orderId,
                "status", "COMPLETED",
                "intent", "CAPTURE"
        );
    }
}
