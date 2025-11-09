package com.example.demo.erp.customerManagement.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    
    private Long id;
    private BigDecimal amount;
    private String transactionType;
    private LocalDateTime date;
    private String reference;
    private String productCode;
    private String status;
    private String description;
    private String paymentMethod;
    private String channel;
    private String initiatedBy;
}
