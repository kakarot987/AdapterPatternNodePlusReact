package com.adapter.demo.adapter;

import com.adapter.demo.model.PaymentRequest;
import com.adapter.demo.model.PaymentResponse;
import com.adapter.demo.port.PaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ============================================================
 *  ADAPTER — Razorpay Adapter
 * ============================================================
 * Bridges:
 *   - PaymentPort (Target)  ←→  RazorpaySdk (Adaptee)
 *
 * Responsibilities:
 *   ✔  Convert dollar/currency amounts → paise (×100)
 *   ✔  Build Razorpay's "notes" map from request fields
 *   ✔  Map "captured" → "SUCCESS"
 *   ✔  Handle receipt generation from orderId
 * ============================================================
 */
@Slf4j
@Component("razorpayAdapter")
@RequiredArgsConstructor
public class RazorpayPaymentAdapter implements PaymentPort {

    private final RazorpaySdk razorpaySdk;  // the Adaptee

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("[RazorpayAdapter] Adapting payment request for Razorpay...");

        // ADAPTATION: Razorpay uses paise (like cents for INR)
        int amountInPaise = (int) Math.round(request.getAmount() * 100);

        // ADAPTATION: Razorpay uses a "notes" map for extra fields
        Map<String, String> notes = Map.of(
                "email", request.getCustomerEmail(),
                "description", request.getDescription(),
                "orderId", request.getOrderId()
        );

        Map<String, Object> rzpResponse = razorpaySdk.createPayment(
                amountInPaise,
                request.getCurrency().toUpperCase(),
                "receipt_" + request.getOrderId(),  // ADAPTATION: generate receipt id
                notes
        );

        return buildResponse(rzpResponse, request.getAmount(), request.getCurrency());
    }

    @Override
    public PaymentResponse refundPayment(String transactionId, double amount) {
        log.info("[RazorpayAdapter] Adapting refund for Razorpay...");

        int amountInPaise = (int) Math.round(amount * 100);
        Map<String, Object> refundResponse = razorpaySdk.createRefund(transactionId, amountInPaise);

        return PaymentResponse.builder()
                .transactionId((String) refundResponse.get("id"))
                .status(mapStatus((String) refundResponse.get("status")))
                .provider(getProviderName())
                .amount(amount)
                .currency("INR")
                .message("Refund processed via Razorpay")
                .timestamp(System.currentTimeMillis())
                .rawProviderResponse(refundResponse)
                .build();
    }

    @Override
    public PaymentResponse getPaymentStatus(String transactionId) {
        log.info("[RazorpayAdapter] Fetching Razorpay payment status...");

        Map<String, Object> paymentResponse = razorpaySdk.fetchPayment(transactionId);
        return buildResponse(paymentResponse, 0, "INR");
    }

    @Override
    public String getProviderName() {
        return "razorpay";
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private PaymentResponse buildResponse(Map<String, Object> rzpResp, double amount, String currency) {
        return PaymentResponse.builder()
                .transactionId((String) rzpResp.get("id"))
                .status(mapStatus((String) rzpResp.getOrDefault("status", "unknown")))
                .provider(getProviderName())
                .amount(amount)
                .currency(currency)
                .message("Payment processed via Razorpay")
                .timestamp(System.currentTimeMillis())
                .rawProviderResponse(rzpResp)
                .build();
    }

    /** Razorpay → unified status mapping */
    private String mapStatus(String rzpStatus) {
        return switch (rzpStatus) {
            case "captured", "processed" -> "SUCCESS";
            case "created", "authorized" -> "PENDING";
            case "failed", "refunded"    -> "FAILED";
            default                       -> "UNKNOWN";
        };
    }
}
