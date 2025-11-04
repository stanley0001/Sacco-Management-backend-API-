package com.example.demo.customerManagement.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDTO {
    
    private Boolean success;
    private String transactionId;
    private String message;
    private BigDecimal amount;
    private String errorCode;
    private String errorMessage;
}
