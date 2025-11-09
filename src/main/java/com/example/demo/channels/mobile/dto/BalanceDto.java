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
public class BalanceDto {
    private String accountId;
    private String accountNumber;
    private String accountType;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private BigDecimal pendingDebits;
    private BigDecimal pendingCredits;
    private String currency;
    private String asOfDate;
}
