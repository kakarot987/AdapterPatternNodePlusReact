package com.adapter.demo.adapter;

import com.adapter.demo.model.PaymentRequest;
import com.adapter.demo.model.PaymentResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RazorpayPaymentAdapterContractTest {

    private final RazorpaySdk sdk = mock(RazorpaySdk.class);
    private final RazorpayPaymentAdapter adapter = new RazorpayPaymentAdapter(sdk);

    private PaymentRequest request(double amount) {
        return PaymentRequest.builder().orderId("order-8").amount(amount)
                .currency("INR").customerEmail("buyer@example.com")
                .description("Demo purchase").provider("razorpay").build();
    }

    @Test
    void translatesMinorUnitsReceiptNotesAndCapturedStatus() {
        when(sdk.createPayment(anyInt(), anyString(), anyString(), anyMap()))
                .thenReturn(Map.of("id", "pay_123", "status", "captured"));

        PaymentResponse result = adapter.processPayment(request(12.34));

        verify(sdk).createPayment(eq(1234), eq("INR"), eq("receipt_order-8"), argThat(notes ->
                "buyer@example.com".equals(notes.get("email"))
                && "order-8".equals(notes.get("orderId"))
                && "Demo purchase".equals(notes.get("description"))));
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getProvider()).isEqualTo("razorpay");
    }

    @Test
    void rejectsAnAmountThatOverflowsTheProviderIntegerField() {
        assertThatThrownBy(() -> adapter.processPayment(request(30_000_000)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(sdk);
    }

    @Test
    void missingStatusMapsToUnknownAndRefundTranslatesPaise() {
        when(sdk.fetchPayment("pay_unknown")).thenReturn(Map.of("id", "pay_unknown"));
        assertThat(adapter.getPaymentStatus("pay_unknown").getStatus()).isEqualTo("UNKNOWN");

        when(sdk.createRefund("pay_123", 255))
                .thenReturn(Map.of("id", "rfnd_1", "status", "processed"));
        assertThat(adapter.refundPayment("pay_123", 2.55).getStatus()).isEqualTo("SUCCESS");
    }
}
