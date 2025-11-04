package com.example.demo.mpesa.entities;

/*
 * DUPLICATE FILE - DISABLED
 * 
 * This file was causing a bean definition conflict with:
 * com.example.demo.payments.entities.MpesaTransaction
 * 
 * The active entity is in the payments package.
 * This file has been commented out to prevent Spring Boot startup errors.
 * 
 * DO NOT UNCOMMENT - Delete this file or keep it as documentation.
 */

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

/* DISABLED - Original code below:

package com.example.demo.mpesa.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity to track MPESA transactions including callbacks, STK push, and payments
 */
// @Entity
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Table(name = "mpesa_transactions")
@Builder
public class MpesaTransaction_DISABLED_DUPLICATE {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // STK_PUSH, CALLBACK, PAYBILL, WITHDRAW, DEPOSIT
    
    @Column(name = "mpesa_receipt_number", unique = true)
    private String mpesaReceiptNumber;
    
    @Column(name = "transaction_id", unique = true)
    private String transactionId;
    
    @Column(name = "checkout_request_id")
    private String checkoutRequestId; // For STK push tracking
    
    @Column(name = "merchant_request_id")
    private String merchantRequestId;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "account_reference")
    private String accountReference; // Customer ID or Account Number
    
    @Column(name = "amount")
    private Double amount;
    
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    
    @Column(name = "status")
    private String status; // PENDING, COMPLETED, FAILED, REVERSED
    
    @Column(name = "result_code")
    private String resultCode;
    
    @Column(name = "result_desc")
    private String resultDesc;
    
    @Column(name = "paybill_number")
    private String paybillNumber;
    
    @Column(name = "sender_name")
    private String senderName;
    
    @Column(name = "mpesa_balance")
    private Double mpesaBalance;
    
    @Column(name = "callback_received")
    @Builder.Default
    private Boolean callbackReceived = false;
    
    @Column(name = "callback_data", length = 2000)
    private String callbackData; // Store full callback JSON
    
    @Column(name = "purpose")
    private String purpose; // LOAN_APPLICATION, LOAN_REPAYMENT, DEPOSIT, WITHDRAWAL
    
    @Column(name = "reference_id")
    private Long referenceId; // Link to Loan, Deposit, or other entities
    
    @Column(name = "processed")
    @Builder.Default
    private Boolean processed = false;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
