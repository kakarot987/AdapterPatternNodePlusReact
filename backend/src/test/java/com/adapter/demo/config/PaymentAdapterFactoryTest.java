package com.adapter.demo.config;

import com.adapter.demo.port.PaymentPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentAdapterFactoryTest {

    @Test
    void listsProvidersBeforeAnyAdapterLookup() {
        PaymentAdapterFactory factory = new PaymentAdapterFactory(List.of(
                adapterNamed("stripe"), adapterNamed("paypal"), adapterNamed("razorpay")
        ));

        assertThat(factory.getAvailableProviders())
                .containsExactly("paypal", "razorpay", "stripe");
        assertThat(factory.getAvailableProviders()).hasSize(3);
    }

    @Test
    void resolvesProviderNamesCaseInsensitively() {
        PaymentPort stripe = adapterNamed("stripe");
        PaymentAdapterFactory factory = new PaymentAdapterFactory(List.of(stripe));

        assertThat(factory.getAdapter("STRIPE")).isSameAs(stripe);
    }

    @Test
    void rejectsMissingAndUnknownProvidersWithUsefulErrors() {
        PaymentAdapterFactory factory = new PaymentAdapterFactory(List.of(adapterNamed("stripe")));

        assertThatThrownBy(() -> factory.getAdapter(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be specified");
        assertThatThrownBy(() -> factory.getAdapter("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stripe");
    }

    @Test
    void preventsDuplicateProviderRegistrations() {
        assertThatThrownBy(() -> new PaymentAdapterFactory(List.of(
                adapterNamed("stripe"), adapterNamed("STRIPE")
        ))).isInstanceOf(IllegalStateException.class);
    }

    private static PaymentPort adapterNamed(String name) {
        PaymentPort port = mock(PaymentPort.class);
        when(port.getProviderName()).thenReturn(name);
        return port;
    }
}
