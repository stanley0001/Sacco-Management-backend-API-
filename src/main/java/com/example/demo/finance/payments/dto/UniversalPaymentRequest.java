package com.example.demo.finance.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversalPaymentRequest {
    
    // Customer Information
    private Long customerId;
    private String customerName;
    private String phoneNumber;
    
    // Payment Details
    private BigDecimal amount;
    private String paymentMethod; // MPESA, CASH, BANK, CHEQUE, EFT
    private String description;
    private String transactionType; // DEPOSIT, LOAN_REPAYMENT, WITHDRAWAL, TRANSFER
    private String initiatedBy;
    
    // Account Information
    private Long savingsAccountId;
    private Long targetAccountId;
    private Long loanId;
    private String loanReference;
    
    // M-PESA Configuration (optional)
    private Long providerConfigId;
    private String providerCode;
    
    // Additional Metadata
    private String referenceNumber;
    private String sourceModule; // CLIENT_PROFILE, MOBILE_APP, ADMIN_PANEL, etc.
}
