package za.graham.Payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.graham.Loan.model.Loan;
import za.graham.Payment.model.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findByLoan(Loan loan);

}
