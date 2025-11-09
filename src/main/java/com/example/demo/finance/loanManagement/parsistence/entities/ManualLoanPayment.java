package com.example.demo.finance.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "manual_loan_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualLoanPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "loan_account_id", nullable = false)
    private Long loanAccountId;
    
    @Column(name = "loan_reference", length = 50)
    private String loanReference;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "customer_name", length = 200)
    private String customerName;
    
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod; // BANK_TRANSFER, CASH, CHEQUE
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Column(name = "bank_branch", length = 100)
    private String bankBranch;
    
    @Column(name = "depositor_name", length = 200)
    private String depositorName;
    
    @Column(name = "depositor_id_number", length = 50)
    private String depositorIdNumber;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status; // PENDING, APPROVED, REJECTED
    
    @Column(name = "submitted_by", length = 100)
    private String submittedBy;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "approved_by", length = 100)
    private String approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approval_comments", length = 500)
    private String approvalComments;
    
    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}
