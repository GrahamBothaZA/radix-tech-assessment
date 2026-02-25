package za.graham.Loan.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.graham.Loan.exception.LoanNotFoundException;
import za.graham.Loan.model.Loan;
import za.graham.Loan.repository.LoanRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanService loanService;

    @Test
    void createLoan_savesAndReturnsLoan() {
        Loan savedLoan = Loan.builder()
                .loanId("LOAN_123")
                .loanAmount(5000.0)
                .term(12)
                .status(Loan.Status.ACTIVE)
                .build();

        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        Loan result = loanService.createLoan(5000.0, 12);

        assertNotNull(result);
        assertEquals("LOAN_123", result.getLoanId());
        assertEquals(5000.0, result.getLoanAmount());
        assertEquals(12, result.getTerm());
        assertEquals(Loan.Status.ACTIVE, result.getStatus());
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void getLoan_whenFound() {
        Loan loan = Loan.builder()
                .loanId("LOAN_001")
                .loanAmount(1000.0)
                .term(6)
                .status(Loan.Status.ACTIVE)
                .build();

        when(loanRepository.findById("LOAN_001")).thenReturn(Optional.of(loan));

        Loan result = loanService.getLoan("LOAN_001");

        assertNotNull(result);
        assertEquals("LOAN_001", result.getLoanId());
        assertEquals(1000.0, result.getLoanAmount());
    }

    @Test
    void getLoan_whenNotFound() {
        when(loanRepository.findById("LOAN_MISSING")).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> loanService.getLoan("LOAN_MISSING"));
    }
}