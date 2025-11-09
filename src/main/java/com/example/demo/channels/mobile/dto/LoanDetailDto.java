package com.example.demo.channels.mobile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetailDto {
    private String loanId;
    private String loanNumber;
    private String productCode;
    private String productName;
    private BigDecimal principalAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalRepayable;
    private BigDecimal paidAmount;
    private BigDecimal outstandingBalance;
    private BigDecimal interestRate;
    private String interestType;
    private Integer loanTerm;
    private String termUnit; // DAYS, MONTHS, YEARS
    private String status;
    private String applicationDate;
    private String approvalDate;
    private String disbursementDate;
    private String maturityDate;
    private BigDecimal penaltyAmount;
    private Integer daysOverdue;
    private String nextPaymentDate;
    private BigDecimal nextPaymentAmount;
    private Integer installmentsPaid;
    private Integer totalInstallments;
    private List<RepaymentScheduleDto> repaymentSchedule;
}
