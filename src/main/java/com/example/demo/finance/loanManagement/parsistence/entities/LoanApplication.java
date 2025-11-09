package com.example.demo.finance.loanManagement.parsistence.entities;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.finance.loanManagement.parsistence.models.LoanBookUpload;
import lombok.*;

import jakarta.persistence.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplication {
    @Id
    @Column(updatable = false,unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;
    @Column(unique = true,updatable = false,nullable = false)
    private Long loanNumber;
    @Column(nullable = false)
    private String customerId;
    @Column(nullable = false)
    private String customerIdNumber;
    @Column(nullable = false)
    private String customerMobileNumber;
    @Column(nullable = false)
    private String loanAmount;
    @Column(nullable = false)
    private String creditLimit;
    private String disbursementType;
    @Column(nullable = false)
    private String destinationAccount;
    private String applicationStatus;
    @Column(nullable = false)
    private String productCode;
    private String loanTerm;
    private String loanInterest;
    private String installments;
    @Column(nullable = false)
    private LocalDateTime applicationTime;
    
    // Additional fields needed for disbursement service
    private Long productId; // Reference to Products table
    private Integer term; // Loan term in months
    private Double amount; // Loan amount as Double
    private LocalDateTime updatedAt;
    private String disbursementMethod;
    private String disbursementDestination;



    public LoanApplication(LoanBookUpload upload, Customer customer) {
        SecureRandom random = new SecureRandom();
        long randomFiveDigitNumber = 10000 + random.nextInt(90000);
        this.loanNumber= new Date().getTime() +randomFiveDigitNumber+customer.getId()+random.nextInt(100);
        this.applicationTime=LocalDateTime.now();
        this.applicationStatus="PROCESSED";
        this.productCode=upload.getProductName();
        this.destinationAccount=customer.getDocumentNumber();
        this.customerId=String.valueOf(customer.getId());
        this.customerIdNumber=customer.getDocumentNumber();
        this.customerMobileNumber=customer.getPhoneNumber();
        this.loanAmount=upload.getLoanAmount();
        this.creditLimit="UPLOAD";
    }
    
    // Custom methods for backward compatibility
    public String getStatus() {
        return this.applicationStatus;
    }
    
    public void setStatus(String status) {
        this.applicationStatus = status;
    }
    
    public Long getProductId() {
        return this.productId;
    }
    
    public Integer getTerm() {
        if (this.term != null) {
            return this.term;
        }
        // Try to parse from loanTerm string if available
        if (this.loanTerm != null) {
            try {
                return Integer.parseInt(this.loanTerm);
            } catch (NumberFormatException e) {
                return 12; // Default to 12 months
            }
        }
        return 12; // Default term
    }
    
    public Double getAmount() {
        if (this.amount != null) {
            return this.amount;
        }
        // Try to parse from loanAmount string if available
        if (this.loanAmount != null) {
            try {
                return Double.parseDouble(this.loanAmount.replaceAll(",", ""));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
    
    public Long getId() {
        return this.applicationId;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
    
    public void setDisbursementMethod(String disbursementMethod) {
        this.disbursementMethod = disbursementMethod;
    }
    
    public String getDisbursementMethod() {
        return this.disbursementMethod;
    }
    
    public void setDisbursementDestination(String disbursementDestination) {
        this.disbursementDestination = disbursementDestination;
    }
    
    public String getDisbursementDestination() {
        return this.disbursementDestination;
    }
    
    public String getCustomerId() {
        return this.customerId;
    }
}
