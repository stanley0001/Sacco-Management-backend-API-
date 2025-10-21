package com.example.demo.mobile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSummaryDto {
    private String loanId;
    private String loanNumber;
    private String productName;
    private String productCode;
    private BigDecimal principalAmount;
    private BigDecimal outstandingBalance;
    private BigDecimal totalRepayable;
    private BigDecimal paidAmount;
    private BigDecimal interestRate;
    private String status; // ACTIVE, COMPLETED, DEFAULTED, WRITTEN_OFF
    private String disbursementDate;
    private String maturityDate;
    private String nextPaymentDate;
    private BigDecimal nextPaymentAmount;
    private Integer installmentsPaid;
    private Integer totalInstallments;
    private Integer daysOverdue;
}
