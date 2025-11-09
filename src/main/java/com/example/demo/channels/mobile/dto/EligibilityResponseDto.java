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
public class EligibilityResponseDto {
    private boolean eligible;
    private BigDecimal maxLoanAmount;
    private BigDecimal recommendedAmount;
    private String reason;
    private List<String> requirements;
    private List<String> missingRequirements;
    private BigDecimal existingLoanBalance;
    private Integer creditScore;
}
