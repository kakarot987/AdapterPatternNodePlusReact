package com.adapter.demo.adapter;

import com.adapter.demo.model.PaymentRequest;
import com.adapter.demo.model.PaymentResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PayPalPaymentAdapterContractTest {

    private final PayPalSdk sdk = mock(PayPalSdk.class);
    private final PayPalPaymentAdapter adapter = new PayPalPaymentAdapter(sdk);

    private PaymentRequest request() {
        return PaymentRequest.builder()
                .orderId("order-9").amount(19.95).currency("USD")
                .customerEmail("buyer@example.com").description("Demo purchase")
                .provider("paypal").build();
    }

    @Test
    void translatesPurchaseUnitsAndCompletedStatus() {
        when(sdk.createOrder(eq("CAPTURE"), anyList(), eq("buyer@example.com")))
                .thenReturn(Map.of("id", "PAY-123", "status", "COMPLETED"));

        PaymentResponse result = adapter.processPayment(request());

        verify(sdk).createOrder(eq("CAPTURE"), argThat(units -> {
            Map<String, Object> unit = units.get(0);
            @SuppressWarnings("unchecked")
            Map<String, String> money = (Map<String, String>) unit.get("amount");
            return "order-9".equals(unit.get("reference_id"))
                    && "Demo purchase".equals(unit.get("description"))
                    && "USD".equals(money.get("currency_code"))
                    && "19.95".equals(money.get("value"));
        }), eq("buyer@example.com"));
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getTransactionId()).isEqualTo("PAY-123");
        assertThat(result.getProvider()).isEqualTo("paypal");
    }

    @Test
    void approvedOrderIsPendingUntilCaptured() {
        when(sdk.createOrder(anyString(), anyList(), anyString()))
                .thenReturn(Map.of("id", "PAY-pending", "status", "APPROVED"));

        assertThat(adapter.processPayment(request()).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void missingProviderStatusMapsToUnknown() {
        when(sdk.showOrderDetails("PAY-unknown")).thenReturn(Map.of("id", "PAY-unknown"));

        assertThat(adapter.getPaymentStatus("PAY-unknown").getStatus()).isEqualTo("UNKNOWN");
    }

    @Test
    void refundsUseTwoDecimalCurrencyStrings() {
        when(sdk.captureRefund(eq("PAY-123"), anyMap()))
                .thenReturn(Map.of("id", "REF-123", "status", "COMPLETED"));

        PaymentResponse result = adapter.refundPayment("PAY-123", 2.5);
        verify(sdk).captureRefund("PAY-123", Map.of("value", "2.50", "currency_code", "USD"));
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
    }
}
