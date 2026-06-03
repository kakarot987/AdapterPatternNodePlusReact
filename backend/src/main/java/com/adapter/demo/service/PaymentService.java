package com.adapter.demo.service;

import com.adapter.demo.config.PaymentAdapterFactory;
import com.adapter.demo.model.PaymentRequest;
import com.adapter.demo.model.PaymentResponse;
import com.adapter.demo.port.PaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ============================================================
 *  SERVICE — Payment Service
 * ============================================================
 * Pure business logic. Knows nothing about Stripe, PayPal,
 * or Razorpay. Depends ONLY on the PaymentPort abstraction.
 *
 * This is the key benefit of the Adapter Pattern:
 *  ✔  Service is completely decoupled from gateway specifics
 *  ✔  Swapping providers requires zero service changes
 *  ✔  Business rules (validation, logging) live here cleanly
 * ============================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentAdapterFactory adapterFactory;

    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment: orderId={}, provider={}, amount={}",
                request.getOrderId(), request.getProvider(), request.getAmount());

        validateRequest(request);

        // Resolve the correct adapter — service doesn't care which one
        PaymentPort adapter = adapterFactory.getAdapter(request.getProvider());

        PaymentResponse response = adapter.processPayment(request);
        log.info("Payment completed: txId={}, status={}", response.getTransactionId(), response.getStatus());

        return response;
    }

    public PaymentResponse refundPayment(String provider, String transactionId, double amount) {
        log.info("Processing refund: txId={}, provider={}, amount={}", transactionId, provider, amount);

        PaymentPort adapter = adapterFactory.getAdapter(provider);
        return adapter.refundPayment(transactionId, amount);
    }

    public PaymentResponse getPaymentStatus(String provider, String transactionId) {
        log.info("Fetching payment status: txId={}, provider={}", transactionId, provider);

        PaymentPort adapter = adapterFactory.getAdapter(provider);
        return adapter.getPaymentStatus(transactionId);
    }

    public List<String> getAvailableProviders() {
        return adapterFactory.getAvailableProviders();
    }

    private void validateRequest(PaymentRequest request) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (request.getProvider() == null || request.getProvider().isBlank()) {
            throw new IllegalArgumentException("Payment provider must be specified");
        }
    }
}
