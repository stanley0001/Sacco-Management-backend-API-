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
public class TransactionDto {
    private String transactionId;
    private String transactionDate;
    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER, LOAN_DISBURSEMENT, LOAN_REPAYMENT
    private String description;
    private BigDecimal amount;
    private String debitCredit; // DR or CR
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String reference;
    private String status;
    private String channel; // MOBILE, USSD, MPESA, BANK
}
