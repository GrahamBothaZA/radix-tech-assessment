package za.graham.Loan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.graham.common.exception.InvalidDataException;
import za.graham.Loan.exception.LoanNotFoundException;
import za.graham.Loan.model.Loan;
import za.graham.common.generator.UniqueIdGenerator;
import za.graham.Loan.repository.LoanRepository;

import java.time.Instant;

/**
 * Service layer responsible for loan creation and retrieval business logic.
 */
@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    @Autowired
    LoanRepository loanRepository;

    /**
     * Creates a new loan with an auto-generated ID, sets its status to ACTIVE,
     * and persists it to the database.
     *
     * @param amount the main loan amount
     * @param term the repayment term in months
     * @return the persisted {Loan} entity
     * @throws InvalidDataException if the loan has an invalid amount or term
     */
    public Loan createLoan(final Double amount, final Integer term) throws InvalidDataException {

        if (amount <= 0) {
            throw new InvalidDataException("Loan amount cannot be zero or less");
        }

        if (term <= 0) {
            throw new InvalidDataException("Loan term cannot be zero or less");
        }

        Loan loan = Loan.builder()
                .loanId(UniqueIdGenerator.generateUniqueId("LOAN"))
                .loanAmount(amount)
                .term(term)
                .status(Loan.Status.ACTIVE)
                .createdDate(Instant.now())
                .build();

        return loanRepository.save(loan);
    }

    /**
     * Retrieves a loan by its unique ID.
     *
     * @param loanId the unique identifier of the loan
     * @return the matching {Loan} entity
     * @throws LoanNotFoundException if no loan exists with the given ID
     */
    public Loan getLoan(final String loanId) throws LoanNotFoundException {
        Loan loan = loanRepository.findById(loanId).orElse(null);

        if (loan == null) {
            throw new LoanNotFoundException(String.format("Loan not found {loanId: %s}", loanId));
        }

        return loan;
    }
}
