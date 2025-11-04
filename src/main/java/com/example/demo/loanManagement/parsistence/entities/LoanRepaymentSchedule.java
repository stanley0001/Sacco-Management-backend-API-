package com.example.demo.loanManagement.parsistence.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loan_repayment_schedules")
public class LoanRepaymentSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "loan_account_id")
    private Long loanAccountId = 0L;
    
    @Column(name = "installment_number")
    private Integer installmentNumber = 1;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "principal_amount", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal principalAmount = BigDecimal.ZERO;
    
    @Column(name = "interest_amount", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal interestAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "paid_principal", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal paidPrincipal = BigDecimal.ZERO;
    
    @Column(name = "paid_interest", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal paidInterest = BigDecimal.ZERO;
    
    @Column(name = "total_paid", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal totalPaid = BigDecimal.ZERO;
    
    @Column(name = "outstanding_principal", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal outstandingPrincipal = BigDecimal.ZERO;
    
    @Column(name = "outstanding_interest", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal outstandingInterest = BigDecimal.ZERO;
    
    @Column(name = "total_outstanding", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal totalOutstanding = BigDecimal.ZERO;
    
    @Column(name = "penalty_amount", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal penaltyAmount = BigDecimal.ZERO;
    
    @Column(name = "paid_penalty", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal paidPenalty = BigDecimal.ZERO;
    
    @Column(name = "outstanding_penalty", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal outstandingPenalty = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(50) DEFAULT 'PENDING'")
    private ScheduleStatus status = ScheduleStatus.PENDING;
    
    @Column(name = "paid_date")
    private LocalDate paidDate;
    
    @Column(name = "payment_reference")
    private String paymentReference;
    
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
    
    private String createdBy;
    private String updatedBy;
    
    // Relationship to LoanAccount
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id", insertable = false, updatable = false)
    private LoanAccount loanAccount;
    
    public enum ScheduleStatus {
        PENDING,    // Not yet due
        CURRENT,    // Due now
        OVERDUE,    // Past due date
        PAID,       // Fully paid
        PARTIAL     // Partially paid
    }
    
    // Additional field for balance after payment (for backwards compatibility)
    @Column(name = "balance_after_payment", precision = 15, scale = 2, columnDefinition = "NUMERIC(15,2) DEFAULT 0")
    private BigDecimal balanceAfterPayment = BigDecimal.ZERO;
    
    // Convenience method for string-based status setting (renamed to avoid Lombok conflict)
    public void setStatusFromString(String statusString) {
        if (statusString != null) {
            try {
                this.status = ScheduleStatus.valueOf(statusString.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Default to PENDING if invalid status
                this.status = ScheduleStatus.PENDING;
            }
        }
    }
    
    public String getStatusString() {
        return status != null ? status.name() : "PENDING";
    }
    
    public void setBalanceAfterPayment(BigDecimal balanceAfterPayment) {
        this.balanceAfterPayment = balanceAfterPayment;
    }
    
    public BigDecimal getBalanceAfterPayment() {
        return balanceAfterPayment;
    }
    
    @PrePersist
    protected void onCreate() {
        // Initialize timestamps
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        
        // Initialize core fields with defaults if null
        if (loanAccountId == null) {
            loanAccountId = 0L;
        }
        if (installmentNumber == null) {
            installmentNumber = 1;
        }
        if (status == null) {
            status = ScheduleStatus.PENDING;
        }
        if (dueDate == null) {
            dueDate = LocalDate.now();
        }
        
        // Calculate and initialize all amounts
        calculateOutstandingAmounts();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateOutstandingAmounts();
    }
    
    // Business methods
    
    /**
     * Calculate outstanding amounts based on paid amounts
     */
    public void calculateOutstandingAmounts() {
        // Ensure all fields have default values
        if (principalAmount == null) principalAmount = BigDecimal.ZERO;
        if (interestAmount == null) interestAmount = BigDecimal.ZERO;
        if (paidPrincipal == null) paidPrincipal = BigDecimal.ZERO;
        if (paidInterest == null) paidInterest = BigDecimal.ZERO;
        if (penaltyAmount == null) penaltyAmount = BigDecimal.ZERO;
        if (paidPenalty == null) paidPenalty = BigDecimal.ZERO;
        
        outstandingPrincipal = principalAmount.subtract(paidPrincipal);
        outstandingInterest = interestAmount.subtract(paidInterest);
        totalOutstanding = outstandingPrincipal.add(outstandingInterest);
        
        if (penaltyAmount.compareTo(BigDecimal.ZERO) > 0) {
            outstandingPenalty = penaltyAmount.subtract(paidPenalty);
            totalOutstanding = totalOutstanding.add(outstandingPenalty);
        } else {
            outstandingPenalty = BigDecimal.ZERO;
        }
        
        // Calculate total amount if not set
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            totalAmount = principalAmount.add(interestAmount).add(penaltyAmount);
        }
        
        // Calculate total paid if not set
        if (totalPaid == null) {
            totalPaid = paidPrincipal.add(paidInterest).add(paidPenalty);
        }
    }
    
    /**
     * Check if installment is fully paid
     */
    public boolean isFullyPaid() {
        return totalOutstanding != null && totalOutstanding.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    /**
     * Check if installment is overdue
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && !isFullyPaid();
    }
    
    /**
     * Check if installment is current (due today or within grace period)
     */
    public boolean isCurrent() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(dueDate) && !isOverdue() && !isFullyPaid();
    }
    
    /**
     * Apply payment to this installment
     */
    public BigDecimal applyPayment(BigDecimal paymentAmount, String reference) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal remainingPayment = paymentAmount;
        
        // First pay penalty if any
        if (outstandingPenalty != null && outstandingPenalty.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal penaltyPayment = remainingPayment.min(outstandingPenalty);
            paidPenalty = paidPenalty.add(penaltyPayment);
            remainingPayment = remainingPayment.subtract(penaltyPayment);
        }
        
        // Then pay interest
        if (remainingPayment.compareTo(BigDecimal.ZERO) > 0 && 
            outstandingInterest.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal interestPayment = remainingPayment.min(outstandingInterest);
            paidInterest = paidInterest.add(interestPayment);
            remainingPayment = remainingPayment.subtract(interestPayment);
        }
        
        // Finally pay principal
        if (remainingPayment.compareTo(BigDecimal.ZERO) > 0 && 
            outstandingPrincipal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal principalPayment = remainingPayment.min(outstandingPrincipal);
            paidPrincipal = paidPrincipal.add(principalPayment);
            remainingPayment = remainingPayment.subtract(principalPayment);
        }
        
        // Update total paid and reference
        totalPaid = paidPrincipal.add(paidInterest);
        if (paidPenalty != null) {
            totalPaid = totalPaid.add(paidPenalty);
        }
        
        paymentReference = reference;
        
        // Update status
        updateStatus();
        
        // Recalculate outstanding amounts
        calculateOutstandingAmounts();
        
        // Return amount actually used from payment
        return paymentAmount.subtract(remainingPayment);
    }
    
    /**
     * Update status based on payment state
     */
    public void updateStatus() {
        if (isFullyPaid()) {
            status = ScheduleStatus.PAID;
            paidDate = LocalDate.now();
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            status = ScheduleStatus.PARTIAL;
        } else if (isOverdue()) {
            status = ScheduleStatus.OVERDUE;
        } else if (isCurrent()) {
            status = ScheduleStatus.CURRENT;
        } else {
            status = ScheduleStatus.PENDING;
        }
    }
    
    /**
     * Calculate penalty for overdue installment
     */
    public void calculatePenalty(BigDecimal penaltyRate, int overdueDays) {
        if (isOverdue() && penaltyRate != null && penaltyRate.compareTo(BigDecimal.ZERO) > 0) {
            // Calculate penalty as percentage of outstanding amount per day
            BigDecimal dailyPenaltyRate = penaltyRate.divide(BigDecimal.valueOf(100 * 365), 6, BigDecimal.ROUND_HALF_UP);
            BigDecimal calculatedPenalty = totalOutstanding.multiply(dailyPenaltyRate).multiply(BigDecimal.valueOf(overdueDays));
            
            // Update penalty amount if calculated penalty is higher
            if (penaltyAmount == null || calculatedPenalty.compareTo(penaltyAmount) > 0) {
                penaltyAmount = calculatedPenalty;
                calculateOutstandingAmounts();
            }
        }
    }
    
    /**
     * Get days overdue
     */
    public long getDaysOverdue() {
        if (isOverdue()) {
            return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }
        return 0;
    }
    
    /**
     * Get payment progress percentage
     */
    public BigDecimal getPaymentProgress() {
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalPaid.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }
}
