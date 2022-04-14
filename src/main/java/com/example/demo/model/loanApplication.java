package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class loanApplication {
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
    @Column(nullable = false)
    private LocalDateTime applicationTime;

    public loanApplication() {
    }

    public loanApplication(Long applicationId) {
        this.applicationId = applicationId;
    }

    public loanApplication(Long loanNumber, String customerId, String customerIdNumber, String customerMobileNumber, String loanAmount, String creditLimit, String disbursementType, String destinationAccount, String applicationStatus, String productCode, String loanTerm, String loanInterest, LocalDateTime applicationTime) {
        this.loanNumber = loanNumber;
        this.customerId = customerId;
        this.customerIdNumber = customerIdNumber;
        this.customerMobileNumber = customerMobileNumber;
        this.loanAmount = loanAmount;
        this.creditLimit = creditLimit;
        this.disbursementType = disbursementType;
        this.destinationAccount = destinationAccount;
        this.applicationStatus = applicationStatus;
        this.productCode = productCode;
        this.loanTerm = loanTerm;
        this.loanInterest = loanInterest;
        this.applicationTime = applicationTime;
    }

    public loanApplication(Long applicationId, Long loanNumber, String customerId, String customerIdNumber, String customerMobileNumber, String loanAmount, String creditLimit, String disbursementType, String destinationAccount, String applicationStatus, String productCode, String loanTerm, String loanInterest, LocalDateTime applicationTime) {
        this.applicationId = applicationId;
        this.loanNumber = loanNumber;
        this.customerId = customerId;
        this.customerIdNumber = customerIdNumber;
        this.customerMobileNumber = customerMobileNumber;
        this.loanAmount = loanAmount;
        this.creditLimit = creditLimit;
        this.disbursementType = disbursementType;
        this.destinationAccount = destinationAccount;
        this.applicationStatus = applicationStatus;
        this.productCode = productCode;
        this.loanTerm = loanTerm;
        this.loanInterest = loanInterest;
        this.applicationTime = applicationTime;
    }


    @Override
    public String toString() {
        return "loanApplication{" +
                "applicationId=" + applicationId +
                ", loanNumber=" + loanNumber +
                ", customerId='" + customerId + '\'' +
                ", customerIdNumber='" + customerIdNumber + '\'' +
                ", customerMobileNumber='" + customerMobileNumber + '\'' +
                ", loanAmount='" + loanAmount + '\'' +
                ", creditLimit='" + creditLimit + '\'' +
                ", disbursementType='" + disbursementType + '\'' +
                ", destinationAccount='" + destinationAccount + '\'' +
                ", applicationStatus='" + applicationStatus + '\'' +
                ", productCode='" + productCode + '\'' +
                ", loanTerm='" + loanTerm + '\'' +
                ", loanInterest='" + loanInterest + '\'' +
                ", applicationTime=" + applicationTime +
                '}';
    }


}
