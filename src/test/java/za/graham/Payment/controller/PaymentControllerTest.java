package za.graham.Payment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.graham.Loan.exception.LoanAlreadySettledException;
import za.graham.Loan.exception.LoanNotFoundException;
import za.graham.Loan.model.Loan;
import za.graham.Payment.exception.PaymentExceedsOutstandingException;
import za.graham.Payment.model.Payment;
import za.graham.Payment.service.PaymentService;
import za.graham.common.exception.InvalidDataException;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void postLoanPayment_withPayment() throws Exception {
        Loan loan = Loan.builder()
                .loanId("LOAN_001")
                .loanAmount(1000.0)
                .term(12)
                .status(Loan.Status.ACTIVE)
                .createdDate(Instant.now())
                .build();
        Payment payment = Payment.builder()
                .paymentId("PAYMENT_001")
                .loan(loan)
                .paymentAmount(500.0)
                .paymentDate(Instant.now())
                .build();

        when(paymentService.processLoanPayment("LOAN_001", 500.0)).thenReturn(payment);

        mockMvc.perform(post("/payments")
                        .param("loanId", "LOAN_001")
                        .param("paymentAmount", "500.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("PAYMENT_001"))
                .andExpect(jsonPath("$.paymentAmount").value(500.0));
    }

    @Test
    void postLoanPayment_whenPaymentExceedsOutstanding() throws Exception {
        when(paymentService.processLoanPayment("LOAN_001", 9999.0))
                .thenThrow(new PaymentExceedsOutstandingException("Payment exceeds outstanding"));

        mockMvc.perform(post("/payments")
                        .param("loanId", "LOAN_001")
                        .param("paymentAmount", "9999.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PaymentExceedsOutstandingException"))
                .andExpect(jsonPath("$.message").value("Payment exceeds outstanding"));
    }

    @Test
    void postLoanPayment_whenLoanAlreadySettled() throws Exception {
        when(paymentService.processLoanPayment("LOAN_001", 100.0))
                .thenThrow(new LoanAlreadySettledException("Loan is already settled"));

        mockMvc.perform(post("/payments")
                        .param("loanId", "LOAN_001")
                        .param("paymentAmount", "100.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("LoanAlreadySettledException"))
                .andExpect(jsonPath("$.message").value("Loan is already settled"));
    }

    @Test
    void postLoanPayment_whenLoanNotFound() throws Exception {
        when(paymentService.processLoanPayment("LOAN_MISSING", 100.0))
                .thenThrow(new LoanNotFoundException("Loan not found"));

        mockMvc.perform(post("/payments")
                        .param("loanId", "LOAN_MISSING")
                        .param("paymentAmount", "100.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("LoanNotFoundException"))
                .andExpect(jsonPath("$.message").value("Loan not found"));
    }

    @Test
    void postLoanPayment_whenPaymentInvalid() throws Exception {
        when(paymentService.processLoanPayment("LOAN_002", -100.0))
                .thenThrow(new InvalidDataException("Payment amount invalid"));

        mockMvc.perform(post("/payments")
                        .param("loanId", "LOAN_002")
                        .param("paymentAmount", "-100.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("InvalidDataException"))
                .andExpect(jsonPath("$.message").value("Payment amount invalid"));
    }
}