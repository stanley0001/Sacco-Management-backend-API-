package com.example.demo.erp.customerManagement.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentScheduleDTO {
    
    private Integer installmentNumber;
    private LocalDateTime dueDate;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalAmount;
    private BigDecimal outstandingBalance;
    private String status; // PENDING, PAID, OVERDUE
    private LocalDateTime paidDate;
    private BigDecimal paidAmount;
}
