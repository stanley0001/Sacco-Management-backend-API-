package com.example.demo.finance.payments.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for manual payment records (CASH, BANK_TRANSFER, CHEQUE)
 */
@Entity
@Table(name = "manual_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Target information
    @Column(name = "target_type", nullable = false)
    private String targetType; // LOAN_REPAYMENT, ACCOUNT_DEPOSIT, etc.
    
    @Column(name = "target_id")
    private Long targetId;
    
    @Column(name = "target_reference")
    private String targetReference;
    
    // Payment details
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // CASH, BANK_TRANSFER, CHEQUE
    
    @Column(name = "reference_number")
    private String referenceNumber;
    
    @Column(name = "payment_date")
    private LocalDate paymentDate;
    
    // Cheque details
    @Column(name = "cheque_number")
    private String chequeNumber;
    
    @Column(name = "cheque_bank")
    private String chequeBank;
    
    @Column(name = "cheque_date")
    private LocalDate chequeDate;
    
    // Bank transfer details
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "bank_branch")
    private String bankBranch;
    
    @Column(name = "bank_account_number")
    private String bankAccountNumber;
    
    @Column(name = "sender_name")
    private String senderName;
    
    // Approval workflow
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING_APPROVAL;
    
    @Column(name = "requires_approval")
    private Boolean requiresApproval = false;
    
    @Column(name = "received_by")
    private String receivedBy;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approval_comments")
    private String approvalComments;
    
    @Column(name = "rejected_by")
    private String rejectedBy;
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    // Accounting integration
    @Column(name = "debit_account_id")
    private Long debitAccountId; // Source account (Cash/Bank)
    
    @Column(name = "credit_account_id")
    private Long creditAccountId; // Destination account
    
    @Column(name = "post_to_accounting")
    private Boolean postToAccounting = true;
    
    @Column(name = "posted_to_accounting")
    private Boolean postedToAccounting = false;
    
    @Column(name = "posted_at")
    private LocalDateTime postedAt;
    
    @Column(name = "posting_error")
    private String postingError;
    
    // Transaction reference
    @Column(name = "transaction_reference")
    private String transactionReference;
    
    // Comments
    @Column(name = "comments", length = 1000)
    private String comments;
    
    // Error handling
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.PENDING_APPROVAL;
        }
        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum PaymentStatus {
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        POSTED,
        FAILED
    }
}
