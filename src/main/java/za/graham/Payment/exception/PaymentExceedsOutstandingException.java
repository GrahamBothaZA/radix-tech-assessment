package za.graham.Payment.exception;

/**
 * Exception used to handle payment amounts exceeding the outstanding balance on a loan.
 */
public class PaymentExceedsOutstandingException extends RuntimeException {

    public PaymentExceedsOutstandingException() {
        super();
    }

    public PaymentExceedsOutstandingException(String message) {
        super(message);
    }

    public PaymentExceedsOutstandingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentExceedsOutstandingException(Throwable cause) {
        super(cause);
    }

}
