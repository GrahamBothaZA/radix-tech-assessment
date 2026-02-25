package za.graham.Loan.exception;

/**
 * Exception used when a requested loan cannot be found in the database
 */
public class LoanNotFoundException extends RuntimeException {

    public LoanNotFoundException() {
        super();
    }

    public LoanNotFoundException(String message) {
        super(message);
    }

    public LoanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanNotFoundException(Throwable cause) {
        super(cause);
    }

}
