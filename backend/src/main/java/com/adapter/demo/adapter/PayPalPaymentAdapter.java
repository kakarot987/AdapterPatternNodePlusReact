package com.adapter.demo.adapter;

import com.adapter.demo.model.PaymentRequest;
import com.adapter.demo.model.PaymentResponse;
import com.adapter.demo.port.PaymentPort;
import com.adapter.demo.util.PaymentAmounts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * ============================================================
 *  ADAPTER — PayPal Adapter
 * ============================================================
 * Bridges:
 *   - PaymentPort (Target)  ←→  PayPalSdk (Adaptee)
 *
 * Responsibilities:
 *   ✔  Build PayPal's nested "purchase_units" structure
 *   ✔  Map amount to PayPal's string-based money format
 *   ✔  Translate "COMPLETED" → "SUCCESS"
 *   ✔  Extract transaction ID from PayPal's order ID format
 * ============================================================
 */
@Slf4j
@Component("paypalAdapter")
@RequiredArgsConstructor
public class PayPalPaymentAdapter implements PaymentPort {

    private final PayPalSdk payPalSdk;  // the Adaptee

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("[PayPalAdapter] Adapting payment request for PayPal...");

        // ADAPTATION: PayPal needs a nested "purchase_units" list
        List<Map<String, Object>> purchaseUnits = List.of(
                Map.of(
                        "reference_id", request.getOrderId(),
                        "description", request.getDescription(),
                        "amount", Map.of(
                                "currency_code", request.getCurrency().toUpperCase(),
                                // ADAPTATION: PayPal uses String amounts (not long cents)
                                "value", PaymentAmounts.formatted(request.getAmount())
                        )
                )
        );

        Map<String, Object> paypalResponse = payPalSdk.createOrder(
                "CAPTURE",
                purchaseUnits,
                request.getCustomerEmail()
        );

        return buildResponse(paypalResponse, request.getAmount(), request.getCurrency());
    }

    @Override
    public PaymentResponse refundPayment(String transactionId, double amount) {
        log.info("[PayPalAdapter] Adapting refund for PayPal...");

        // ADAPTATION: PayPal refund needs amount as a Map object
        Map<String, Object> amountMap = Map.of(
                "value", PaymentAmounts.formatted(amount),
                "currency_code", "USD"
        );

        Map<String, Object> refundResponse = payPalSdk.captureRefund(transactionId, amountMap);

        return PaymentResponse.builder()
                .transactionId((String) refundResponse.get("id"))
                .status(mapStatus((String) refundResponse.get("status")))
                .provider(getProviderName())
                .amount(amount)
                .currency("USD")
                .message("Refund processed via PayPal")
                .timestamp(System.currentTimeMillis())
                .rawProviderResponse(refundResponse)
                .build();
    }

    @Override
    public PaymentResponse getPaymentStatus(String transactionId) {
        log.info("[PayPalAdapter] Fetching PayPal order status...");

        Map<String, Object> orderResponse = payPalSdk.showOrderDetails(transactionId);
        return buildResponse(orderResponse, 0, "USD");
    }

    @Override
    public String getProviderName() {
        return "paypal";
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private PaymentResponse buildResponse(Map<String, Object> paypalResp, double amount, String currency) {
        return PaymentResponse.builder()
                .transactionId((String) paypalResp.get("id"))
                .status(mapStatus((String) paypalResp.getOrDefault("status", "UNKNOWN")))
                .provider(getProviderName())
                .amount(amount)
                .currency(currency)
                .message("Payment processed via PayPal")
                .timestamp(System.currentTimeMillis())
                .rawProviderResponse(paypalResp)
                .build();
    }

    /** PayPal → unified status mapping */
    private String mapStatus(String paypalStatus) {
        if (paypalStatus == null) return "UNKNOWN";
        return switch (paypalStatus) {
            case "COMPLETED" -> "SUCCESS";
            case "PENDING", "CREATED", "APPROVED" -> "PENDING";
            case "VOIDED", "DENIED"      -> "FAILED";
            default                       -> "UNKNOWN";
        };
    }
}
