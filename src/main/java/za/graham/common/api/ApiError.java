package za.graham.common.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

/**
 * This class is used to standardize error responses across the application,
 * providing the error type, a descriptive message, and the timestamp when
 * the error occurred.
 */
@Getter
public class ApiError {
    private final String error;
    private final String message;
    private final Instant timestamp;

    public ApiError(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now();
    }

    /**
     * Factory method that creates a complete {ResponseEntity} containing an {ApiError}
     * with the given HTTP status and exception information.
     *
     * @param status the HTTP status code to return (e.g. 400, 404, 500)
     * @param ex the exception that caused the error
     * @return a {ResponseEntity} ready to be returned from a controller method
     */
    public static ResponseEntity<ApiError> apiErrorResponseEntity(HttpStatus status, Exception ex) {
        return ResponseEntity.status(status)
                .body(new ApiError(
                        ex.getClass().getSimpleName(),
                        ex.getMessage()));
    }
}
