package com.example.demo.finance.loanManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command for manual payment processing with accounting integration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualPaymentCommand {
    
    // Payment target
    private PaymentTarget target; // LOAN_REPAYMENT, ACCOUNT_DEPOSIT
    private Long targetId; // Loan ID or Account ID
    private String targetReference; // Loan ref or Account number
    
    // Payment details
    private BigDecimal amount;
    private String paymentMethod; // CASH, BANK_TRANSFER, CHEQUE, etc.
    private String referenceNumber;
    private LocalDate paymentDate;
    
    // For cheques
    private String chequeNumber;
    private String chequeBank;
    private LocalDate chequeDate;
    
    // For bank transfers
    private String bankName;
    private String bankBranch;
    private String bankAccountNumber;
    private String senderName;
    
    // Approval and accounting
    private String receivedBy;
    private String approvedBy;
    private String comments;
    
    // Accounting integration
    private Long debitAccountId; // Where money is coming FROM (Bank/Cash account)
    private Long creditAccountId; // Where money is going TO (Loan/Customer account)
    
    private boolean postToAccounting; // Default true
    private boolean requireApproval; // Default false for cash, true for cheques
    
    public enum PaymentTarget {
        LOAN_REPAYMENT,
        ACCOUNT_DEPOSIT,
        SHARE_DEPOSIT,
        SAVINGS_DEPOSIT
    }
}
