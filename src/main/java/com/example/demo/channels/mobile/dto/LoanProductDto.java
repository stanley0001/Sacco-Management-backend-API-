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
public class LoanProductDto {
    private String productId;
    private String productCode;
    private String productName;
    private String description;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private String interestType; // FLAT_RATE, REDUCING_BALANCE, etc.
    private Integer minTerm;
    private Integer maxTerm;
    private String termUnit;
    private List<String> eligibilityCriteria;
    private BigDecimal processingFee;
    private BigDecimal processingFeeRate;
    private boolean topUpAllowed;
    private boolean earlyRepaymentAllowed;
    private BigDecimal earlyRepaymentPenalty;
    private boolean isActive;
}
