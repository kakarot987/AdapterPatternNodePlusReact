package com.adapter.demo.controller;

import com.adapter.demo.model.PaymentResponse;
import com.adapter.demo.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerContractTest {
    @Autowired private MockMvc mvc;
    @MockBean private PaymentService payments;

    @Test
    void listsProvidersWithCount() throws Exception {
        when(payments.getAvailableProviders()).thenReturn(List.of("paypal", "razorpay", "stripe"));

        mvc.perform(get("/api/payments/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.providers[0]").value("paypal"));
    }

    @Test
    void processesAUnifiedPaymentResponse() throws Exception {
        when(payments.processPayment(any())).thenReturn(PaymentResponse.builder()
                .transactionId("ch_123").provider("stripe").status("SUCCESS")
                .currency("USD").amount(12.5).message("Simulated payment")
                .timestamp(123L).build());

        mvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                  {"provider":"stripe","orderId":"o1","amount":12.5,
                   "currency":"USD","customerEmail":"buyer@example.com",
                   "description":"Test purchase"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("ch_123"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void returns400ForInvalidPaymentAndMalformedJson() throws Exception {
        when(payments.processPayment(any()))
                .thenThrow(new IllegalArgumentException("Payment amount must be positive"));

        mvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Payment amount must be positive"));

        mvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON request"));

        mvc.perform(post("/api/payments/stripe/refund/tx-1?amount=not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid or missing request parameter"));
    }

    @Test
    void doesNotExposeUnexpectedExceptionDetailsToClients() throws Exception {
        when(payments.processPayment(any())).thenThrow(new IllegalStateException("secret internal implementation detail"));

        mvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }
}
