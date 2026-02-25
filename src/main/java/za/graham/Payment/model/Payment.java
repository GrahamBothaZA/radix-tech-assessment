package za.graham.Payment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.graham.Loan.model.Loan;

import java.time.Instant;

/**
 * JPA entity representing a payment made against a loan.
 */
@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    private String paymentId;

    @ManyToOne
    @JoinColumn(name = "loanId")
    private Loan loan;

    private Double paymentAmount;

    private Instant paymentDate;
}
