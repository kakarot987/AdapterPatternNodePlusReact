package com.adapter.demo.adapter;

import com.adapter.demo.model.PaymentRequest;
import com.adapter.demo.model.PaymentResponse;
import com.adapter.demo.port.PaymentPort;
import com.adapter.demo.util.PaymentAmounts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ============================================================
 *  ADAPTER — Stripe Adapter
 * ============================================================
 * Bridges the gap between:
 *   - What our app expects  → PaymentPort (Target)
 *   - What Stripe provides  → StripeSdk   (Adaptee)
 *
 * Responsibilities:
 *   ✔  Convert dollar amounts → cents  (×100)
 *   ✔  Map PaymentRequest    → Stripe charge params
 *   ✔  Map Stripe response   → unified PaymentResponse
 *   ✔  Translate Stripe statuses ("succeeded") → our statuses ("SUCCESS")
 * ============================================================
 */
@Slf4j
@Component("stripeAdapter")
@RequiredArgsConstructor
public class StripePaymentAdapter implements PaymentPort {

    private final StripeSdk stripeSdk;  // the Adaptee — injected

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("[StripeAdapter] Adapting payment request for Stripe...");

        // ADAPTATION: convert dollars to cents
        long amountInCents = PaymentAmounts.minorUnits(request.getAmount());

        // ADAPTATION: map our model to Stripe's expected params
        Map<String, Object> stripeResponse = stripeSdk.createCharge(
                amountInCents,
                request.getCurrency(),
                request.getCustomerEmail(),
                request.getDescription(),
                Map.of("orderId", request.getOrderId())
        );

        // ADAPTATION: translate Stripe's response back to our unified model
        return buildResponse(stripeResponse, request.getAmount(), request.getCurrency());
    }

    @Override
    public PaymentResponse refundPayment(String transactionId, double amount) {
        log.info("[StripeAdapter] Adapting refund for Stripe...");

        long amountInCents = PaymentAmounts.minorUnits(amount);
        Map<String, Object> refundResponse = stripeSdk.createRefund(transactionId, amountInCents);

        return PaymentResponse.builder()
                .transactionId((String) refundResponse.get("id"))
                .status(mapStatus((String) refundResponse.get("status")))
                .provider(getProviderName())
                .amount(amount)
                .currency("USD")
                .message("Refund processed via Stripe")
                .timestamp(System.currentTimeMillis())
                .rawProviderResponse(refundResponse)
                .build();
    }

    @Override
    public PaymentResponse getPaymentStatus(String transactionId) {
        log.info("[StripeAdapter] Fetching Stripe charge status...");

        Map<String, Object> chargeResponse = stripeSdk.retrieveCharge(transactionId);
        return buildResponse(chargeResponse, 0, "USD");
    }

    @Override
    public String getProviderName() {
        return "stripe";
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private PaymentResponse buildResponse(Map<String, Object> stripeResp, double amount, String currency) {
        // ADAPTATION: Stripe uses "succeeded", we use "SUCCESS"
        String unifiedStatus = mapStatus((String) stripeResp.getOrDefault("status", "unknown"));

        return PaymentResponse.builder()
                .transactionId((String) stripeResp.get("id"))
                .status(unifiedStatus)
                .provider(getProviderName())
                .amount(amount)
                .currency(currency)
                .message("Payment processed via Stripe")
                .timestamp(System.currentTimeMillis())
                .rawProviderResponse(stripeResp)
                .build();
    }

    /** Stripe → unified status mapping */
    private String mapStatus(String stripeStatus) {
        if (stripeStatus == null) return "UNKNOWN";
        return switch (stripeStatus) {
            case "succeeded" -> "SUCCESS";
            case "pending"   -> "PENDING";
            case "failed"    -> "FAILED";
            default          -> "UNKNOWN";
        };
    }
}
