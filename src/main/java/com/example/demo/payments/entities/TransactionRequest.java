package com.example.demo.payments.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // DEPOSIT, WITHDRAWAL, LOAN_DISBURSEMENT
    
    @Column(nullable = false)
    private Long customerId;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status; // INITIATED, PROCESSING, SUCCESS, FAILED, POSTED_TO_ACCOUNT
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethodType paymentMethod; // MPESA, BANK, CASH
    
    @Column(name = "reference_number")
    private String referenceNumber; // M-PESA receipt, bank reference, etc.
    
    @Column(name = "mpesa_transaction_id")
    private Long mpesaTransactionId;
    
    @Column(name = "loan_id")
    private Long loanId; // For loan-related transactions
    
    @Column(name = "savings_account_id")
    private Long savingsAccountId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "service_provider_response", columnDefinition = "TEXT")
    private String serviceProviderResponse;
    
    @Column(name = "posted_to_account")
    private Boolean postedToAccount = false;
    
    @Column(name = "utilized_for_loan")
    private Boolean utilizedForLoan = false;
    
    @Column(name = "utilized_loan_id")
    private Long utilizedLoanId;
    
    @Column(name = "initiated_by")
    private String initiatedBy; // User who initiated
    
    @Column(name = "processed_by")
    private String processedBy; // User who processed
    
    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "posted_at")
    private LocalDateTime postedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (initiatedAt == null) {
            initiatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        LOAN_DISBURSEMENT,
        LOAN_REPAYMENT,
        TRANSFER
    }
    
    public enum RequestStatus {
        INITIATED,
        PROCESSING,
        SUCCESS,
        FAILED,
        POSTED_TO_ACCOUNT,
        CANCELLED
    }
    
    public enum PaymentMethodType {
        MPESA,
        BANK_TRANSFER,
        CASH,
        CHEQUE,
        AIRTEL_MONEY,
        TKASH
    }
}
