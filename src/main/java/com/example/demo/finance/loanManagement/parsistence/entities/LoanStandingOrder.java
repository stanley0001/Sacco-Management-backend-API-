package com.example.demo.finance.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Loan Standing Order
 * Automatic loan deduction from savings/account transactions
 */
@Entity
@Table(name = "loan_standing_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanStandingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long loanAccountId;

    @Column(length = 100)
    private String loanReference;

    @Column(nullable = false)
    private Long savingsAccountId; // Alpha account or specific account

    @Column(nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeductionType deductionType = DeductionType.FIXED_AMOUNT;

    // For FIXED_AMOUNT type
    @Column(precision = 15, scale = 2)
    private BigDecimal deductionAmount;

    // For PERCENTAGE type
    @Column(precision = 5, scale = 2)
    private BigDecimal deductionPercentage;

    // Minimum amount required in account before deduction
    @Column(precision = 15, scale = 2)
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    // Maximum amount to deduct per transaction
    @Column(precision = 15, scale = 2)
    private BigDecimal maximumDeduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerType triggerType = TriggerType.ON_DEPOSIT;

    @Column(nullable = false)
    private Boolean sendSmsNotification = true;

    @Column(nullable = false)
    private Boolean sendEmailNotification = false;

    // Total amount deducted through this standing order
    @Column(precision = 15, scale = 2)
    private BigDecimal totalDeducted = BigDecimal.ZERO;

    // Number of successful deductions
    @Column(nullable = false)
    private Integer deductionCount = 0;

    @Column
    private LocalDateTime lastDeductionDate;

    @Column(length = 200)
    private String createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 200)
    private String updatedBy;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum DeductionType {
        FIXED_AMOUNT,       // Deduct fixed amount (e.g., KES 5,000 per deposit)
        PERCENTAGE,         // Deduct percentage of deposit (e.g., 50%)
        FULL_INSTALLMENT,   // Deduct full next installment amount
        OUTSTANDING_BALANCE // Deduct entire outstanding balance
    }

    public enum TriggerType {
        ON_DEPOSIT,         // Trigger on any deposit
        ON_SALARY,          // Trigger only on salary deposits
        ON_MPESA,           // Trigger only on M-PESA deposits
        ON_ANY_CREDIT       // Trigger on any credit transaction
    }

    /**
     * Calculate deduction amount based on transaction amount
     */
    public BigDecimal calculateDeductionAmount(BigDecimal transactionAmount, BigDecimal accountBalance, BigDecimal loanOutstanding) {
        BigDecimal deduction = BigDecimal.ZERO;

        switch (deductionType) {
            case FIXED_AMOUNT:
                deduction = deductionAmount != null ? deductionAmount : BigDecimal.ZERO;
                break;

            case PERCENTAGE:
                if (deductionPercentage != null) {
                    deduction = transactionAmount.multiply(deductionPercentage).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                }
                break;

            case FULL_INSTALLMENT:
                // This would need to fetch next installment amount
                deduction = deductionAmount != null ? deductionAmount : BigDecimal.ZERO;
                break;

            case OUTSTANDING_BALANCE:
                deduction = loanOutstanding;
                break;
        }

        // Apply minimum balance check
        BigDecimal balanceAfterDeduction = accountBalance.subtract(deduction);
        if (minimumBalance != null && balanceAfterDeduction.compareTo(minimumBalance) < 0) {
            // Adjust deduction to maintain minimum balance
            deduction = accountBalance.subtract(minimumBalance);
            if (deduction.compareTo(BigDecimal.ZERO) < 0) {
                deduction = BigDecimal.ZERO;
            }
        }

        // Apply maximum deduction limit
        if (maximumDeduction != null && deduction.compareTo(maximumDeduction) > 0) {
            deduction = maximumDeduction;
        }

        // Don't deduct more than outstanding loan balance
        if (deduction.compareTo(loanOutstanding) > 0) {
            deduction = loanOutstanding;
        }

        return deduction;
    }

    /**
     * Record successful deduction
     */
    public void recordDeduction(BigDecimal amount) {
        this.totalDeducted = this.totalDeducted.add(amount);
        this.deductionCount++;
        this.lastDeductionDate = LocalDateTime.now();
    }
}
