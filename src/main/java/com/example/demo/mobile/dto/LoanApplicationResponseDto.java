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
public class LoanApplicationResponseDto {
    private boolean success;
    private String applicationId;
    private String applicationNumber;
    private String message;
    private String status; // SUBMITTED, PENDING, APPROVED, REJECTED
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private String productName;
    private String applicationDate;
    private String expectedDisbursementDate;
}
