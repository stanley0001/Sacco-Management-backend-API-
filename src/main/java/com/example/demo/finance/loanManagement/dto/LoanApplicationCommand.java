package com.example.demo.finance.loanManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Unified command for creating loan applications from any source
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationCommand {
    
    // Source tracking
    private ApplicationSource source; // UPLOAD, ADMIN_UI, MOBILE_APP, CLIENT_PROFILE, API
    private String sourceReference; // Reference ID from source system
    
    // Customer information
    private Long customerId; // Database ID
    private String customerIdNumber; // National ID or document number
    private String customerMobileNumber;
    private String customerExternalId; // External system ID (for uploads)
    
    // Loan details
    private String productCode;
    private Long productId; // Optional, will be resolved if not provided
    private BigDecimal loanAmount;
    private Integer term; // in months
    private Double interestRate; // Optional, will use product rate if not provided
    
    // Disbursement details
    private String disbursementType; // MPESA, BANK, CASH, SACCO_ACCOUNT
    private String destinationAccount; // Phone number, bank account, etc.
    private LocalDate disbursementDate; // For backdated uploads
    
    // Upload-specific fields
    private Boolean isUpload; // Flag for uploaded loans
    private String uploadStatus; // ACTIVE, CLOSED, DEFAULTED, etc.
    private Double outstandingBalance; // For backdated loans
    private Double totalPaid; // For backdated loans
    private Integer paymentsMade;
    private LocalDate lastPaymentDate;
    
    // Optional fields
    private String loanPurpose;
    private String guarantorName;
    private String guarantorPhone;
    private String collateralType;
    private String collateralValue;
    private String branchCode;
    private String loanOfficer;
    private String comments;
    
    // Request metadata
    private String requestedBy; // User who created the application
    private String installments; // Optional installment count
    
    public enum ApplicationSource {
        UPLOAD,
        ADMIN_UI,
        MOBILE_APP,
        CLIENT_PROFILE,
        CLIENT_PORTAL, API
    }
}
