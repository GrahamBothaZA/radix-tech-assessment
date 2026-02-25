package za.graham.Payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.graham.Loan.exception.LoanAlreadySettledException;
import za.graham.Loan.exception.LoanNotFoundException;
import za.graham.Loan.model.Loan;
import za.graham.Loan.service.LoanService;
import za.graham.Payment.exception.PaymentExceedsOutstandingException;
import za.graham.Payment.model.Payment;
import za.graham.Payment.repository.PaymentRepository;
import za.graham.common.generator.UniqueIdGenerator;
import za.graham.common.exception.InvalidDataException;

import java.time.Instant;

/**
 * Service layer responsible for processing loan payment business logic,
 * including outstanding balance validation.
 */
@Service
public class PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    LoanService loanService;

    /**
     * Processes a payment against the specified loan.
     * Looks up the loan, validates that the payment does not exceed the remaining
     * outstanding balance, then persists and returns the payment record.
     *
     * @param loanId the unique identifier of the loan being paid
     * @param paymentAmount the amount to pay
     * @return the persisted {Payment} entity
     * @throws LoanNotFoundException if no loan exists with the given ID
     * @throws PaymentExceedsOutstandingException if the payment amount exceeds the outstanding balance
     */
    public Payment processLoanPayment(final String loanId, final Double paymentAmount) throws LoanNotFoundException,
            PaymentExceedsOutstandingException, LoanAlreadySettledException {

        if (paymentAmount <= 0) {
            throw new InvalidDataException("Payment amount cannot be zero or less");
        }

        Loan loan = loanService.getLoan(loanId);

        if (loan.getStatus().equals(Loan.Status.SETTLED)) {
            throw new LoanAlreadySettledException("Loan is already in a settled status");
        }

        Payment payment = Payment.builder()
                .paymentId(UniqueIdGenerator.generateUniqueId("PAYMENT"))
                .paymentAmount(paymentAmount)
                .loan(loan)
                .paymentDate(Instant.now())
                .build();

        Double outstandingLoanAmount = getOutstandingLoanPaymentAmount(loan);

        if (paymentAmount > outstandingLoanAmount) {
            throw new PaymentExceedsOutstandingException(String.format("Payment exceeds outstanding paymentAmount {outstandingLoanAmount=%.2f, paymentAmount=%.2f}", outstandingLoanAmount, paymentAmount));
        } else if (paymentAmount.equals(outstandingLoanAmount)) {
            loan.setStatus(Loan.Status.SETTLED);
        }

        return paymentRepository.save(payment);
    }

    /**
     * Calculates the outstanding balance on a loan by subtracting the sum of all
     * existing payments from the original loan amount.
     *
     * @param loan the loan to evaluate
     * @return the remaining outstanding amount
     */
    private double getOutstandingLoanPaymentAmount(final Loan loan) {
        return loan.getLoanAmount() - paymentRepository.findByLoan(loan).stream()
                .filter(p -> p.getPaymentAmount() != null)
                .mapToDouble(Payment::getPaymentAmount)
                .sum();
    }
}
