package com.example.demo.finance.loanManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for loan disbursement requests with multiple disbursement methods
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDisbursementRequest {
    
    @NotNull(message = "Application ID is required")
    private Long applicationId;
    
    @NotNull(message = "Disbursement method is required")
    private DisbursementMethod disbursementMethod;
    
    // For M-PESA B2C disbursements
    private String phoneNumber;
    
    // For Bank Account transfers
    private String bankAccountNumber;
    private String bankName;
    private String bankBranch;
    
    // For manual/cash disbursements
    private String recipientName;
    private String recipientIdNumber;
    
    // Common fields
    private String disbursementReference;
    private String comments;
    private String disbursedBy;
    
    /**
     * Disbursement methods supported
     */
    public enum DisbursementMethod {
        SACCO_ACCOUNT("Credit to SACCO Account"),
        MPESA_B2C("M-PESA B2C Transfer"),
        BANK_TRANSFER("Bank Account Transfer"),
        CASH_MANUAL("Cash Disbursement (Manual)"),
        CHEQUE("Cheque Payment");
        
        private final String description;
        
        DisbursementMethod(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Validate request based on disbursement method
     */
    public boolean isValid() {
        if (applicationId == null || disbursementMethod == null) {
            return false;
        }
        
        return switch (disbursementMethod) {
            case MPESA_B2C -> phoneNumber != null && !phoneNumber.isEmpty();
            case BANK_TRANSFER -> bankAccountNumber != null && !bankAccountNumber.isEmpty() &&
                    bankName != null && !bankName.isEmpty();
            case CASH_MANUAL, CHEQUE -> recipientName != null && !recipientName.isEmpty();
            case SACCO_ACCOUNT -> true; // No additional validation needed
        };
    }
    
    /**
     * Get destination string for logging/display
     */
    public String getDestinationString() {
        return switch (disbursementMethod) {
            case MPESA_B2C -> "M-PESA: " + phoneNumber;
            case BANK_TRANSFER -> bankName + " - " + bankAccountNumber;
            case CASH_MANUAL -> "Cash to: " + recipientName;
            case CHEQUE -> "Cheque for: " + recipientName;
            case SACCO_ACCOUNT -> "SACCO Account";
        };
    }
}
