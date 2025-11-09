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
public class CustomerBalancesDTO {
    
    private BigDecimal totalSavings;
    private BigDecimal totalLoansOutstanding;
    private BigDecimal totalCreditLimit;
    private BigDecimal availableCredit;
    private BigDecimal netWorth;
    private LocalDateTime lastUpdated;
}
