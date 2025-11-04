package com.example.demo.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity to track loan rollovers
 * When a member pays interest and creates a new loan with principal balance
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "loan_rollovers")
public class LoanRollover {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "original_loan_id", nullable = false)
    private Long originalLoanId;
    
    @Column(name = "new_loan_id", nullable = false)
    private Long newLoanId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "original_principal")
    private Double originalPrincipal;
    
    @Column(name = "outstanding_principal")
    private Double outstandingPrincipal;
    
    @Column(name = "interest_paid")
    private Double interestPaid;
    
    @Column(name = "rollover_fee")
    private Double rolloverFee;
    
    @Column(name = "application_fee")
    private Double applicationFee; // 500 KES as per requirements
    
    @Column(name = "new_principal")
    private Double newPrincipal; // Outstanding + Application Fee
    
    @Column(name = "new_term")
    private Integer newTerm; // Same duration as original
    
    @Column(name = "rollover_date")
    private LocalDateTime rolloverDate;
    
    @Column(name = "original_due_date")
    private LocalDateTime originalDueDate;
    
    @Column(name = "new_due_date")
    private LocalDateTime newDueDate;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "status")
    private String status; // PENDING, APPROVED, COMPLETED, REJECTED
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        rolloverDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
