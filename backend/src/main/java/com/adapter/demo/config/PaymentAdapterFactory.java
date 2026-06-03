package com.adapter.demo.config;

import com.adapter.demo.port.PaymentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ============================================================
 *  ADAPTER FACTORY / REGISTRY
 * ============================================================
 * Collects all PaymentPort implementations from the Spring
 * context and provides a lookup by provider name.
 *
 * This is the "Context" in the classic Adapter/Strategy combo:
 * the client asks for an adapter by name at runtime, and the
 * factory returns the right one — the caller never needs to
 * know which concrete adapter is being used.
 *
 * Benefits:
 *  ✔  Open/Closed: add a new provider by adding a new Adapter
 *     class + zero changes here
 *  ✔  Single Responsibility: routing logic is centralised
 *  ✔  Testable: each adapter tested in isolation
 * ============================================================
 */
@Component
@RequiredArgsConstructor
public class PaymentAdapterFactory {

    private final List<PaymentPort> adapters;

    /** Lazy-built lookup map: "stripe" → StripePaymentAdapter, etc. */
    private Map<String, PaymentPort> adapterMap;

    public PaymentPort getAdapter(String providerName) {
        if (adapterMap == null) {
            adapterMap = adapters.stream()
                    .collect(Collectors.toMap(
                            a -> a.getProviderName().toLowerCase(),
                            Function.identity()
                    ));
        }

        PaymentPort adapter = adapterMap.get(providerName.toLowerCase());
        if (adapter == null) {
            throw new IllegalArgumentException(
                    "No payment adapter registered for provider: '" + providerName + "'. " +
                    "Available providers: " + adapterMap.keySet()
            );
        }
        return adapter;
    }

    public List<String> getAvailableProviders() {
        if (adapterMap == null) getAdapter("__init__");
        return List.copyOf(adapterMap.keySet());
    }
}
