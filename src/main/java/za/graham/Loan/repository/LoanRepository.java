package za.graham.Loan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.graham.Loan.model.Loan;

public interface LoanRepository extends JpaRepository<Loan, String> {
}
