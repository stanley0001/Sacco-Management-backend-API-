package com.example.demo.loanManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning enriched loan account data with customer and product information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountResponseDto {
    // Loan Account fields
    private Long id;
    private String accountNumber;
    private Long applicationId;
    private Float principalAmount;
    private Float payableAmount;
    private Float amountPaid;
    private Float balance;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private String status;
    private Integer term; // installments
    
    // Customer fields
    private String customerId;
    private String customerName;
    private String phoneNumber;
    
    // Product fields
    private String productCode;
    private String productName;
    private Double interestRate;
}
