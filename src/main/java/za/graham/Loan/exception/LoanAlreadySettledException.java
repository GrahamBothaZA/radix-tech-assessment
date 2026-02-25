package za.graham.Loan.exception;

/**
 * Exception used when attempting to make a payment on a loan that is already in a settled status
 */
public class LoanAlreadySettledException extends RuntimeException {

    public LoanAlreadySettledException() {
        super();
    }

    public LoanAlreadySettledException(String message) {
        super(message);
    }

    public LoanAlreadySettledException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanAlreadySettledException(Throwable cause) {
        super(cause);
    }

}
