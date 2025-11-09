package com.example.demo.finance.loanManagement.dto;

import lombok.Data;

@Data
public class CreditLimitUpdateDto {
    private Long subscriptionId;
    private Integer creditLimit;
    private Boolean override;  // If true, marks as manually overridden
    private String calculationRule;  // Optional rule for automatic calculation
}
