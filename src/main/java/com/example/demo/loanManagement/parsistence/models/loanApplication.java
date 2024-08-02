package com.example.demo.loanManagement.parsistence.models;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private String installments;
    @Column(nullable = false)
    private LocalDateTime applicationTime;
}
