package com.example.demo.finance.loanManagement.dto;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response after creating/processing a loan application
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {
    
    private Boolean success;
    private String message;
    private List<String> warnings;
    
    // Application details
    private Long applicationId;
    private Long loanNumber;
    private String applicationStatus;
    private LocalDateTime applicationTime;
    
    // Next steps
    private String nextAction; // AWAITING_APPROVAL, READY_FOR_DISBURSEMENT, DISBURSED, etc.
    private String expectedDisbursementDate;
    
    // Loan calculation preview (if available)
    private Double principalAmount;
    private Double totalInterest;
    private Double totalRepayable;
    private Double monthlyInstallment;
    
    public static LoanApplicationResponse fromEntity(LoanApplication application) {
        return LoanApplicationResponse.builder()
            .success(true)
            .applicationId(application.getApplicationId())
            .loanNumber(application.getLoanNumber())
            .applicationStatus(application.getApplicationStatus())
            .applicationTime(application.getApplicationTime())
            .principalAmount(application.getAmount())
            .build();
    }
}
