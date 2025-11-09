package com.example.demo.finance.loanManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Command for booking/creating a loan account from an approved application
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanBookingCommand {
    
    // Application reference
    private Long applicationId;
    
    // Disbursement details
    private String disbursementMethod; // SACCO_ACCOUNT, MPESA_B2C, BANK_TRANSFER, CASH, CHEQUE
    private String disbursementReference;
    private String disbursedBy;
    private LocalDate disbursementDate; // For backdated loans, null for current
    
    // Destination details (based on method)
    private String phoneNumber; // For MPESA
    private String bankAccountNumber; // For BANK_TRANSFER
    private String bankName;
    private String bankBranch;
    private String recipientName; // For CASH/CHEQUE
    private String recipientIdNumber;
    
    // Optional
    private String comments;
    
    // Flags
    private Boolean skipDisbursement; // For uploads that are already disbursed
    private Boolean postToAccounting; // Default true
}
