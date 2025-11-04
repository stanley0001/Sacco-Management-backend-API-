package com.example.demo.payments.dto;

import com.example.demo.payments.entities.MpesaTransaction;
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
public class MpesaDepositStatusResponse {

    private String merchantRequestId;
    private String checkoutRequestId;
    private String resultCode;
    private String resultDesc;
    private MpesaTransaction.TransactionStatus transactionStatus;
    private TransactionRequest.RequestStatus requestStatus;
    private Long transactionRequestId;
    private Long mpesaTransactionId;
    private String transactionId;
    private BigDecimal amount;
    private String phoneNumber;
}
