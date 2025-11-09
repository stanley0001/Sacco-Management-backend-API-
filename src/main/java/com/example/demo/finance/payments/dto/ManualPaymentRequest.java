package com.example.demo.finance.payments.dto;

import com.example.demo.finance.payments.entities.TransactionRequest;
import com.example.demo.finance.payments.entities.TransactionRequest;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ManualPaymentRequest {
    private Long customerId;
    private String customerName;
    private String phoneNumber;
    private BigDecimal amount;
    private TransactionRequest.PaymentMethodType paymentMethod; // CASH, CHEQUE, BANK_TRANSFER
    private TransactionRequest.TransactionType transactionType; // DEPOSIT, LOAN_REPAYMENT
    private TransactionRequest.TransactionCategory transactionCategory; // SAVINGS_DEPOSIT, LOAN_REPAYMENT
    private Long targetAccountId; // Bank account ID
    private Long loanId; // For loan repayments
    private Long savingsAccountId; // For savings deposits
    private String referenceNumber; // Receipt number, cheque number, etc.
    private String description;
}
