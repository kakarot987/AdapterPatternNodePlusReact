package com.adapter.demo.controller;

import com.adapter.demo.model.PaymentRequest;
import com.adapter.demo.model.PaymentResponse;
import com.adapter.demo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ============================================================
 *  REST CONTROLLER — Payment API
 * ============================================================
 * Exposes a unified payment API. The client (frontend/postman)
 * picks a provider by name; the adapter pattern handles the rest.
 * ============================================================
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/payments/process
     * Process a payment using the specified provider.
     *
     * Example body:
     * {
     *   "orderId": "order_001",
     *   "amount": 99.99,
     *   "currency": "USD",
     *   "customerEmail": "user@example.com",
     *   "description": "Premium subscription",
     *   "provider": "stripe"   ← swap to "paypal" or "razorpay" — no other change needed!
     * }
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/payments/{provider}/refund/{transactionId}?amount=50.00
     */
    @PostMapping("/{provider}/refund/{transactionId}")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable String provider,
            @PathVariable String transactionId,
            @RequestParam double amount
    ) {
        PaymentResponse response = paymentService.refundPayment(provider, transactionId, amount);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/payments/{provider}/status/{transactionId}
     */
    @GetMapping("/{provider}/status/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @PathVariable String provider,
            @PathVariable String transactionId
    ) {
        PaymentResponse response = paymentService.getPaymentStatus(provider, transactionId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/payments/providers
     * Returns the list of available payment providers.
     */
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getProviders() {
        List<String> providers = paymentService.getAvailableProviders();
        return ResponseEntity.ok(Map.of(
                "providers", providers,
                "count", providers.size()
        ));
    }
}
