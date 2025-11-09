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
public class PaymentCalculationRequest {
    
    private BigDecimal amount;
    private String paymentType; // FULL, PARTIAL, INTEREST_ONLY
    private LocalDateTime effectiveDate;
}
