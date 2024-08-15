package com.example.demo.loanManagement.parsistence.entities;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.loanManagement.parsistence.models.LoanBookUpload;
import lombok.*;

import javax.persistence.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

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
}
