package com.example.demo.customerManagement.dto;

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
public class BankAccountDTO {
    
    private Long id;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private Boolean isActive;
    private String productCode;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
}
