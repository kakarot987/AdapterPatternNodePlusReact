package com.adapter.demo.service;

import com.adapter.demo.config.PaymentAdapterFactory;
import com.adapter.demo.model.PaymentRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class PaymentServiceValidationTest {

    private final PaymentAdapterFactory factory = mock(PaymentAdapterFactory.class);
    private final PaymentService service = new PaymentService(factory);

    private PaymentRequest valid() {
        return PaymentRequest.builder()
                .orderId("order-1")
                .provider("stripe")
                .currency("USD")
                .customerEmail("test@example.com")
                .description("Demo")
                .amount(10.50)
                .build();
    }

    @Test
    void rejectsMissingPaymentRequestWithoutCallingProvider() {
        assertThatThrownBy(() -> service.processPayment(null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(factory);
    }

    @Test
    void rejectsAmountsThatCannotBeRepresentedExactly() {
        PaymentRequest request = valid();
        request.setAmount(1.005);
        assertThatThrownBy(() -> service.processPayment(request))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(factory);
    }

    @Test
    void rejectsMissingSdkFieldsBeforeProviderLookup() {
        PaymentRequest missingDescription = valid();
        missingDescription.setDescription(null);
        assertThatThrownBy(() -> service.processPayment(missingDescription))
                .isInstanceOf(IllegalArgumentException.class);
        PaymentRequest missingOrder = valid();
        missingOrder.setOrderId(null);
        assertThatThrownBy(() -> service.processPayment(missingOrder))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(factory);
    }

    @Test
    void validatesRefundBeforeUsingProviderAdapter() {
        assertThatThrownBy(() -> service.refundPayment("stripe", "tx-1", Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.refundPayment("stripe", "", 10.0))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(factory);
    }
}
