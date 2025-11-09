package com.example.demo.erp.customerManagement.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDTO {
    
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private String description;
    private String reason;
}
