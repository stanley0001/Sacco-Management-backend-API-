package com.example.demo.finance.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity to track interest waivers on loans
 * Used when customer pays early and future interest is waived
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "loan_waivers")
public class LoanWaiver {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "loan_id", nullable = false)
    private Long loanId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "waiver_type")
    private String waiverType; // EARLY_PAYMENT, PARTIAL_WAIVER, FULL_WAIVER, PENALTY_WAIVER
    
    @Column(name = "original_interest")
    private Double originalInterest;
    
    @Column(name = "waived_interest")
    private Double waivedInterest;
    
    @Column(name = "remaining_interest")
    private Double remainingInterest;
    
    @Column(name = "months_paid_early")
    private Integer monthsPaidEarly;
    
    @Column(name = "months_waived")
    private Integer monthsWaived; // e.g., months 4, 5, 6 if paid in 2 months out of 6
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "original_due_date")
    private LocalDateTime originalDueDate;
    
    @Column(name = "waiver_date")
    private LocalDateTime waiverDate;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "status")
    private String status; // PENDING, APPROVED, COMPLETED, REJECTED
    
    @Column(name = "auto_waived")
    @Builder.Default
    private Boolean autoWaived = false; // True if automatic based on product settings
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        waiverDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
