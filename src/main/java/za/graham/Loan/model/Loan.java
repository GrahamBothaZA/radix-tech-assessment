package za.graham.Loan.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity representing a loan.
 */
@Entity
@Table(name = "loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    protected String loanId;

    private Double loanAmount;

    private Integer term;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant createdDate;

    public enum Status {
        ACTIVE,
        SETTLED
    }
}
