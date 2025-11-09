package com.example.demo.finance.loanManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Unified command for processing loan payments from any source
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCommand {
    
    // Payment source tracking
    private PaymentSource source;
    private String sourceReference; // Transaction ID from source
    
    // Loan reference
    private Long loanId;
    private String loanReference;
    
    // Payment details
    private BigDecimal amount;
    private String paymentMethod; // MPESA, BANK, CASH, CHEQUE, MANUAL
    private String referenceNumber;
    
    // For MPESA payments
    private String mpesaReceiptNumber;
    private String phoneNumber;
    
    // For manual/approval flows
    private String approvedBy;
    private String comments;
    
    // Optional allocation (for future enhancement)
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal penaltyAmount;
    
    public enum PaymentSource {
        MPESA_CALLBACK,
        MPESA_STK,
        MANUAL_APPROVAL,
        MOBILE_APP,
        ADMIN_UI,
        BANK_DEPOSIT,
        CASH_DEPOSIT,
        API
    }
}
