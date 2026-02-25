package za.graham.Loan.controller;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.graham.Loan.exception.LoanNotFoundException;
import za.graham.Loan.model.Loan;
import org.slf4j.Logger;
import za.graham.Loan.service.LoanService;
import za.graham.common.api.ApiError;
import za.graham.common.exception.InvalidDataException;

/**
 * REST controller that exposes endpoints for creating and retrieving loans.
 */
@RestController
@RequestMapping("/loans")
public class LoanController {

    private static final Logger log = LoggerFactory.getLogger(LoanController.class);

    @Autowired
    LoanService loanService;

    /**
     * Retrieves a loan by its unique ID.
     *
     * @param loanId the unique identifier of the loan
     * @return 200 OK: with the loan, 404 Not Found: if no loan exists with the given ID
     */
    @GetMapping
    public ResponseEntity<?> getLoan(@RequestParam String loanId) {
        Loan loan;

        try {
            loan = loanService.getLoan(loanId);
        } catch (LoanNotFoundException ex) {
            return ApiError.apiErrorResponseEntity(HttpStatus.NOT_FOUND, ex);
        }

        return new ResponseEntity<>(loan, HttpStatus.OK);
    }

    /**
     * Creates a new loan with the given amount and repayment term.
     *
     * @param loanAmount the principal amount of the loan
     * @param term the repayment term in months
     * @return 201 Created: with the newly created loan, 400 Bad request: if the loan data is invalid
     */
    @PostMapping
    public ResponseEntity<?> postLoan(@RequestParam Double loanAmount, @RequestParam Integer term) {
        Loan createdLoan;
        try {
            createdLoan = loanService.createLoan(loanAmount, term);
            log.info("Loan processed: {{}}", createdLoan);
        } catch (InvalidDataException ex) {
            return ApiError.apiErrorResponseEntity(HttpStatus.BAD_REQUEST, ex);
        }

        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }
}
