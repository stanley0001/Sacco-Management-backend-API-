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
public class TransactionResponseDto {
    private boolean success;
    private String transactionId;
    private String transactionRef;
    private String message;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String transactionDate;
    private String receiptNumber;
}
