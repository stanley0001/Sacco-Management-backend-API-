package com.example.demo.finance.payments.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mpesa_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpesaTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String merchantRequestId;
    
    @Column(nullable = false)
    private String checkoutRequestId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType; // STK_PUSH, C2B, B2C
    
    @Column(nullable = false)
    private String phoneNumber;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "account_reference")
    private String accountReference; // Loan ID, Customer ID, etc.
    
    @Column(name = "transaction_desc")
    private String transactionDesc;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status; // PENDING, SUCCESS, FAILED, CANCELLED
    
    @Column(name = "mpesa_receipt_number")
    private String mpesaReceiptNumber;
    
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    
    @Column(name = "callback_received")
    private Boolean callbackReceived = false;
    
    @Column(name = "callback_response", columnDefinition = "TEXT")
    private String callbackResponse;
    
    @Column(name = "result_code")
    private String resultCode;
    
    @Column(name = "result_desc")
    private String resultDesc;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // For linking to specific entities
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "loan_id")
    private Long loanId;
    
    @Column(name = "savings_account_id")
    private Long savingsAccountId;

    @Column(name = "bank_account_id")
    private Long bankAccountId;
    
    @Column(name = "transaction_request_id")
    private Long transactionRequestId;

    @Column(name = "provider_config_id")
    private Long providerConfigId;

    @Column(name = "provider_code")
    private String providerCode;

    @Column(name = "initiated_by")
    private String initiatedBy; // User who initiated the transaction
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum TransactionType {
        STK_PUSH,
        C2B,
        B2C,
        BALANCE_QUERY,
        C2B_PAYMENT, REVERSAL
    }
    
    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED,
        CANCELLED,
        TIMEOUT
    }
}
