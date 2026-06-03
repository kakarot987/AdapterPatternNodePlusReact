package com.adapter.demo;

import com.adapter.demo.adapter.StripeSdk;
import com.adapter.demo.adapter.StripePaymentAdapter;
import com.adapter.demo.model.PaymentRequest;
import com.adapter.demo.model.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripePaymentAdapterTest {

    @Mock
    private StripeSdk stripeSdk;

    @InjectMocks
    private StripePaymentAdapter stripeAdapter;

    private PaymentRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = PaymentRequest.builder()
                .orderId("order_test_001")
                .amount(99.99)
                .currency("USD")
                .customerEmail("test@example.com")
                .description("Test payment")
                .provider("stripe")
                .build();
    }

    @Test
    void shouldConvertDollarsToCentsWhenCallingStripeSdk() {
        // Arrange
        when(stripeSdk.createCharge(anyLong(), anyString(), anyString(), anyString(), any()))
                .thenReturn(Map.of("id", "ch_123", "status", "succeeded"));

        // Act
        stripeAdapter.processPayment(sampleRequest);

        // Assert: adapter must convert $99.99 → 9999 cents
        verify(stripeSdk).createCharge(eq(9999L), eq("USD"), eq("test@example.com"), any(), any());
    }

    @Test
    void shouldMapStripeSucceededToUnifiedSuccess() {
        // Arrange
        when(stripeSdk.createCharge(anyLong(), anyString(), anyString(), anyString(), any()))
                .thenReturn(Map.of("id", "ch_abc", "status", "succeeded"));

        // Act
        PaymentResponse response = stripeAdapter.processPayment(sampleRequest);

        // Assert: "succeeded" → "SUCCESS"
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getProvider()).isEqualTo("stripe");
        assertThat(response.getTransactionId()).isEqualTo("ch_abc");
    }

    @Test
    void shouldMapStripeFailedToUnifiedFailed() {
        when(stripeSdk.createCharge(anyLong(), anyString(), anyString(), anyString(), any()))
                .thenReturn(Map.of("id", "ch_fail", "status", "failed"));

        PaymentResponse response = stripeAdapter.processPayment(sampleRequest);

        assertThat(response.getStatus()).isEqualTo("FAILED");
    }
}
