package com.example.demo.customerManagement.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsDTO {
    
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalLoansApplied;
    private BigDecimal totalLoansPaid;
    private BigDecimal averageBalance;
    private BigDecimal currentBalance;
    
    private Integer creditScore;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private LocalDate nextPaymentDue;
    private Long membershipDuration; // in days
    
    private Integer totalTransactions;
    private Integer activeLoans;
    private Integer completedLoans;
    private Integer activeSubscriptions;
    
    private BigDecimal totalOutstanding;
    private BigDecimal monthlyRepayment;
    private BigDecimal availableCredit;
    
    // Performance metrics
    private Double paymentSuccessRate;
    private Integer overduePayments;
    private Integer onTimePayments;
    
    // Growth metrics
    private BigDecimal balanceGrowth; // percentage
    private BigDecimal transactionGrowth; // percentage
    
    private LocalDate lastTransactionDate;
    private LocalDate lastLoginDate;
    private String customerStatus;
}
