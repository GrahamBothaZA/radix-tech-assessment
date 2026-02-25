package za.graham.Loan.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.graham.Loan.exception.LoanNotFoundException;
import za.graham.Loan.model.Loan;
import za.graham.Loan.service.LoanService;
import za.graham.common.exception.InvalidDataException;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    @Test
    void getLoan_whenLoanExists() throws Exception {
        Loan loan = Loan.builder()
                .loanId("LOAN_001")
                .loanAmount(5000.0)
                .term(12)
                .status(Loan.Status.ACTIVE)
                .createdDate(Instant.now())
                .build();

        when(loanService.getLoan("LOAN_001")).thenReturn(loan);

        mockMvc.perform(get("/loans").param("loanId", "LOAN_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value("LOAN_001"))
                .andExpect(jsonPath("$.loanAmount").value(5000.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getLoan_whenLoanNotFound() throws Exception {
        when(loanService.getLoan("LOAN_MISSING")).thenThrow(new LoanNotFoundException("Loan not found"));

        mockMvc.perform(get("/loans").param("loanId", "LOAN_MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("LoanNotFoundException"))
                .andExpect(jsonPath("$.message").value("Loan not found"));
    }

    @Test
    void postLoan_withValidLoan() throws Exception {
        Loan createdLoan = Loan.builder()
                .loanId("LOAN_002")
                .loanAmount(2000.0)
                .term(6)
                .status(Loan.Status.ACTIVE)
                .createdDate(Instant.now())
                .build();

        when(loanService.createLoan(2000.0, 6)).thenReturn(createdLoan);

        mockMvc.perform(post("/loans")
                        .param("loanAmount", "2000.0")
                        .param("term", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanId").value("LOAN_002"))
                .andExpect(jsonPath("$.loanAmount").value(2000.0));
    }

    @Test
    void postLoan_withInvalidAmount() throws Exception {
        when(loanService.createLoan(-20.0, 6)).thenThrow(new InvalidDataException("Loan amount invalid"));

        mockMvc.perform(post("/loans")
                        .param("loanAmount", "-20.0")
                        .param("term", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("InvalidDataException"))
                .andExpect(jsonPath("$.message").value("Loan amount invalid"));
    }

    @Test
    void postLoan_withInvalidTerm() throws Exception {
        when(loanService.createLoan(20.0, -6)).thenThrow(new InvalidDataException("Loan term invalid"));

        mockMvc.perform(post("/loans")
                        .param("loanAmount", "20.0")
                        .param("term", "-6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("InvalidDataException"))
                .andExpect(jsonPath("$.message").value("Loan term invalid"));
    }
}