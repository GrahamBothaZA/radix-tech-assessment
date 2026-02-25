package za.graham.Payment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.graham.Loan.exception.LoanAlreadySettledException;
import za.graham.Loan.exception.LoanNotFoundException;
import za.graham.Loan.model.Loan;
import za.graham.Loan.service.LoanService;
import za.graham.Payment.exception.PaymentExceedsOutstandingException;
import za.graham.Payment.model.Payment;
import za.graham.Payment.repository.PaymentRepository;
import za.graham.common.exception.InvalidDataException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private LoanService loanService;

    @InjectMocks
    private PaymentService paymentService;

    private Loan buildActiveLoan(String loanId, double amount) {
        return Loan.builder()
                .loanId(loanId)
                .loanAmount(amount)
                .term(12)
                .status(Loan.Status.ACTIVE)
                .createdDate(Instant.now())
                .build();
    }

    private Loan buildSettledLoan(String loanId, double amount) {
        return Loan.builder()
                .loanId(loanId)
                .loanAmount(amount)
                .term(12)
                .status(Loan.Status.SETTLED)
                .createdDate(Instant.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // processLoanPayment — input validation
    // -------------------------------------------------------------------------

    /** Validation: a negative payment amount must be rejected before the loan is even fetched. */
    @Test
    void processLoanPayment_throwsInvalidDataException_whenAmountIsNegative() {
        assertThrows(InvalidDataException.class,
                () -> paymentService.processLoanPayment("LOAN_001", -50.0));

        verifyNoInteractions(loanService);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    /** Validation: a zero payment amount must be rejected before the loan is even fetched. */
    @Test
    void processLoanPayment_throwsInvalidDataException_whenAmountIsZero() {
        assertThrows(InvalidDataException.class,
                () -> paymentService.processLoanPayment("LOAN_001", 0.0));

        verifyNoInteractions(loanService);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    // -------------------------------------------------------------------------
    // processLoanPayment — loan state checks
    // -------------------------------------------------------------------------

    /** The loan ID does not exist; LoanNotFoundException must propagate to the caller. */
    @Test
    void processLoanPayment_throwsLoanNotFoundException_whenLoanMissing() throws LoanNotFoundException {
        when(loanService.getLoan("LOAN_MISSING")).thenThrow(new LoanNotFoundException("Loan not found"));

        assertThrows(LoanNotFoundException.class,
                () -> paymentService.processLoanPayment("LOAN_MISSING", 500.0));
    }

    /** A payment against an already-settled loan must be rejected with LoanAlreadySettledException. */
    @Test
    void processLoanPayment_throwsLoanAlreadySettledException_whenLoanIsSettled() throws LoanNotFoundException {
        Loan settledLoan = buildSettledLoan("LOAN_001", 1000.0);

        when(loanService.getLoan("LOAN_001")).thenReturn(settledLoan);

        assertThrows(LoanAlreadySettledException.class,
                () -> paymentService.processLoanPayment("LOAN_001", 100.0));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    // -------------------------------------------------------------------------
    // processLoanPayment — outstanding balance checks
    // -------------------------------------------------------------------------

    /** A payment larger than the outstanding balance must be rejected. */
    @Test
    void processLoanPayment_throwsPaymentExceedsOutstandingException_whenPaymentTooLarge() throws LoanNotFoundException {
        Loan loan = buildActiveLoan("LOAN_001", 1000.0);

        when(loanService.getLoan("LOAN_001")).thenReturn(loan);
        when(paymentRepository.findByLoan(loan)).thenReturn(Collections.emptyList());

        assertThrows(PaymentExceedsOutstandingException.class,
                () -> paymentService.processLoanPayment("LOAN_001", 1500.0));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    /**
     * Existing payments reduce the outstanding balance. Here, a R600 prior payment
     * leaves R400 outstanding, so a R500 payment must be rejected.
     */
    @Test
    void processLoanPayment_accountsForExistingPayments_inOutstandingCalculation() throws LoanNotFoundException {
        Loan loan = buildActiveLoan("LOAN_001", 1000.0);
        Payment existingPayment = Payment.builder()
                .paymentId("P1")
                .loan(loan)
                .paymentAmount(600.0)
                .paymentDate(Instant.now())
                .build();

        when(loanService.getLoan("LOAN_001")).thenReturn(loan);
        when(paymentRepository.findByLoan(loan)).thenReturn(List.of(existingPayment));

        // Outstanding = 1000 - 600 = 400. Paying 500 should throw.
        assertThrows(PaymentExceedsOutstandingException.class,
                () -> paymentService.processLoanPayment("LOAN_001", 500.0));
    }

    // -------------------------------------------------------------------------
    // processLoanPayment — happy paths
    // -------------------------------------------------------------------------

    /** Happy path: a valid partial payment is saved and returned. */
    @Test
    void processLoanPayment_success_returnsPayment() throws LoanNotFoundException, PaymentExceedsOutstandingException {
        Loan loan = buildActiveLoan("LOAN_001", 1000.0);
        Payment savedPayment = Payment.builder()
                .paymentId("PAYMENT_ABC")
                .loan(loan)
                .paymentAmount(500.0)
                .paymentDate(Instant.now())
                .build();

        when(loanService.getLoan("LOAN_001")).thenReturn(loan);
        when(paymentRepository.findByLoan(loan)).thenReturn(Collections.emptyList());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        Payment result = paymentService.processLoanPayment("LOAN_001", 500.0);

        assertNotNull(result);
        assertEquals("PAYMENT_ABC", result.getPaymentId());
        assertEquals(500.0, result.getPaymentAmount());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    /**
     * When a payment exactly matches the outstanding balance the loan status is set to
     * SETTLED in memory and the payment is persisted successfully.
     */
    @Test
    void processLoanPayment_settlesLoan_whenPaymentEqualsOutstanding() throws LoanNotFoundException,
            PaymentExceedsOutstandingException, LoanAlreadySettledException {
        Loan loan = buildActiveLoan("LOAN_001", 1000.0);
        Payment savedPayment = Payment.builder()
                .paymentId("PAYMENT_FINAL")
                .loan(loan)
                .paymentAmount(1000.0)
                .paymentDate(Instant.now())
                .build();

        when(loanService.getLoan("LOAN_001")).thenReturn(loan);
        when(paymentRepository.findByLoan(loan)).thenReturn(Collections.emptyList());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        Payment result = paymentService.processLoanPayment("LOAN_001", 1000.0);

        assertNotNull(result);
        assertEquals("PAYMENT_FINAL", result.getPaymentId());
        assertEquals(Loan.Status.SETTLED, loan.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}