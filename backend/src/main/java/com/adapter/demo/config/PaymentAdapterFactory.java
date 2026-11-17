package com.adapter.demo.config;

import com.adapter.demo.port.PaymentPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of provider-specific adapters.
 *
 * Spring injects all PaymentPort implementations. Building the immutable
 * lookup during construction avoids lazy initialization races and makes
 * provider discovery work before any payment request is processed.
 */
@Component
public class PaymentAdapterFactory {

    private final Map<String, PaymentPort> adapterMap;

    public PaymentAdapterFactory(List<PaymentPort> adapters) {
        this.adapterMap = adapters.stream()
                .collect(Collectors.toUnmodifiableMap(
                        adapter -> adapter.getProviderName().toLowerCase(Locale.ROOT),
                        Function.identity()
                ));
    }

    public PaymentPort getAdapter(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("Payment provider must be specified");
        }

        PaymentPort adapter = adapterMap.get(providerName.toLowerCase(Locale.ROOT));
        if (adapter == null) {
            throw new IllegalArgumentException(
                    "No payment adapter registered for provider: '" + providerName + "'. " +
                    "Available providers: " + getAvailableProviders()
            );
        }
        return adapter;
    }

    public List<String> getAvailableProviders() {
        return adapterMap.keySet().stream().sorted().toList();
    }
}
