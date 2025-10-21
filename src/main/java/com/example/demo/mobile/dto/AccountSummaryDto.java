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
public class AccountSummaryDto {
    private String accountId;
    private String accountNumber;
    private String accountType; // SAVINGS, FOSA, SHARES, LOAN
    private String accountName;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private String currency;
    private String status;
    private String lastTransactionDate;
}
