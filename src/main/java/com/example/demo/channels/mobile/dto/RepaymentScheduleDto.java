package com.example.demo.channels.mobile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentScheduleDto {
    private Integer installmentNumber;
    private String dueDate;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalPayment;
    private BigDecimal balanceAfterPayment;
    private String status; // PENDING, PAID, OVERDUE, PARTIALLY_PAID
    private BigDecimal amountPaid;
    private BigDecimal amountDue;
    private String paymentDate;
}
