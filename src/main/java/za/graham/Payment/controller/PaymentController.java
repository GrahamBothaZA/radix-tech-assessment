package za.graham.Payment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.graham.Loan.exception.LoanAlreadySettledException;
import za.graham.Loan.exception.LoanNotFoundException;
import za.graham.Payment.exception.PaymentExceedsOutstandingException;
import za.graham.Payment.model.Payment;
import za.graham.Payment.service.PaymentService;
import za.graham.common.api.ApiError;
import za.graham.common.exception.InvalidDataException;

/**
 * REST controller that exposes endpoints for submitting loan payments.
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    PaymentService paymentService;

    /**
     * Processes a payment against an existing loan.
     *
     * @param loanId the unique identifier of the loan being paid
     * @param paymentAmount the amount being paid
     * @return 201 Created: with the recorded payment
     * <p>
     * 400 Bad Request: if the payment amount exceeds the outstanding
     * loan balance or payment is posted with invalid data,
     * <p>
     * 409 Conflict: if the loan has already been settled,
     * <p>
     * 404 Not found: if no loan exists with the given ID
     */
    @PostMapping
    public ResponseEntity<?> postLoanPayment(@RequestParam String loanId, @RequestParam Double paymentAmount) {
        Payment loanPayment;
        try {
            loanPayment = paymentService.processLoanPayment(loanId, paymentAmount);
            log.info("Payment processed: {{}}", loanPayment);

            return new ResponseEntity<>(loanPayment, HttpStatus.CREATED);
        } catch (PaymentExceedsOutstandingException | InvalidDataException ex) {
            return ApiError.apiErrorResponseEntity(HttpStatus.BAD_REQUEST, ex);
        } catch (LoanAlreadySettledException ex) {
            return ApiError.apiErrorResponseEntity(HttpStatus.CONFLICT, ex);
        } catch (LoanNotFoundException ex) {
            return ApiError.apiErrorResponseEntity(HttpStatus.NOT_FOUND, ex);
        }
    }
}
