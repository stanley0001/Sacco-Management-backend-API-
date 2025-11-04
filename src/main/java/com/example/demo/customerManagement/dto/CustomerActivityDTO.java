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
public class CustomerActivityDTO {
    
    private Long id;
    private String activityType; // LOAN_DISBURSED, LOAN_APPLICATION, SUBSCRIPTION, PAYMENT, DEPOSIT, WITHDRAWAL
    private LocalDateTime activityDate;
    private String title;
    private String description;
    private String icon;
    private String color; // success, info, warning, danger
    
    private BigDecimal amount;
    private String currency;
    private String reference;
    private String status;
    
    // Related entity information
    private Long loanId;
    private Long accountId;
    private Long subscriptionId;
    private String productCode;
    
    // User information
    private String initiatedBy;
    private String processedBy;
    
    // Additional metadata
    private String module; // LOAN_MANAGEMENT, BANKING, SUBSCRIPTION, etc.
    private String entityType; // LOAN, ACCOUNT, SUBSCRIPTION, etc.
    private Long entityId;
}
