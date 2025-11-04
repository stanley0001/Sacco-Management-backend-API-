package com.example.demo.loanManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanBookUploadDTO {
    
    // Customer Information
    private String customerId;
    private String customerName;
    private String phoneNumber;
    private String email;
    
    // Loan Details
    private String loanId; // External loan ID from upload (used as account number)
    private String productCode;
    private String productName;
    private Double principal;
    private Double interestRate;
    private Integer term; // in months
    private LocalDate disbursementDate;
    private String status; // ACTIVE, CLOSED, DEFAULTED, WRITTEN_OFF
    
    // Current Status
    private Double outstandingBalance;
    private Double totalPaid;
    private Integer paymentsMade;
    private LocalDate lastPaymentDate;
    
    // Optional Fields
    private String collateralType;
    private String collateralValue;
    private String guarantorName;
    private String guarantorPhone;
    private String loanPurpose;
    private String branchCode;
    private String loanOfficer;
    
    // Validation Results
    private Integer rowNumber;
    private Boolean isValid;
    private String errorMessage;
    private String warningMessage; // Non-blocking warnings
    
    // Processing Results
    private Boolean isProcessed;
    private Long loanAccountId;
    private String loanReference;
}
