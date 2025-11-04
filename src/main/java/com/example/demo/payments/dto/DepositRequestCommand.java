package com.example.demo.payments.dto;

import com.example.demo.payments.entities.TransactionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequestCommand {

    private Long customerId;
    private String customerName;
    private String phoneNumber;
    private BigDecimal amount;
    private String description;
    private String initiatedBy;
    private Long savingsAccountId;
    private Long loanId;
    private String loanReference;
    private Long targetAccountId;
    private TransactionRequest.PaymentMethodType paymentMethod;
    private TransactionRequest.TransactionCategory transactionCategory;
    private TransactionRequest.TransactionType transactionType;
    private TransactionRequest.PaymentChannel paymentChannel;
    private TransactionRequest.RequestStatus initialStatus;
    private Long providerConfigId;
    private String referenceNumber;
}
