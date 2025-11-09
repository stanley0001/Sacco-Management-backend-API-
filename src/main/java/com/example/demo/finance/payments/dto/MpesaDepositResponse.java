package com.example.demo.finance.payments.dto;

import com.example.demo.finance.payments.entities.MpesaTransaction;
import com.example.demo.finance.payments.entities.TransactionRequest;
import com.example.demo.finance.payments.entities.MpesaTransaction;
import com.example.demo.finance.payments.entities.TransactionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpesaDepositResponse {

    private String merchantRequestId;
    private String checkoutRequestId;
    private String responseCode;
    private String responseDescription;
    private String customerMessage;
    private Long transactionRequestId;
    private Long mpesaTransactionId;
    private TransactionRequest.RequestStatus requestStatus;
    private MpesaTransaction.TransactionStatus transactionStatus;
}
