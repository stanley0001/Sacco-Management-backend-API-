package com.example.demo.loanManagement.parsistence.entities;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.loanManagement.parsistence.models.LoanBookUpload;
import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

@Entity
@Data
public class LoanAccount {


    @Id
    @Column(updatable = false,unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;
    @Column(unique = true,updatable = false,nullable = false)
    private Long applicationId;
    @Column(unique = true)
    private String OtherRef;
    @Column(nullable = false)
    private Float amount;
    @Column(nullable = false)
    private Float payableAmount;
    private Float amountPaid;
    private Float accountBalance;
    private  LocalDateTime startDate;
    private  LocalDateTime dueDate;
    @Column(nullable = false)
    private  String status;
    @Column(nullable = false)
    private  String customerId;
    @Column(nullable = false)
    private  String loanref;
    private Integer installments;
    
    // Additional fields for enhanced loan management
    private Long productId;
    // private Long customerId;
    private java.math.BigDecimal principalAmount;
    private java.math.BigDecimal interestRate;
    private Integer term;
    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal outstandingPrincipal;
    private java.math.BigDecimal outstandingInterest;
    private java.math.BigDecimal totalOutstanding;
    private String loanReference;
    private LocalDate disbursementDate;
    private LocalDate maturityDate;
    private LocalDate nextPaymentDate;
    private String disbursedBy;
    private String disbursementReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public LoanAccount() {
    }
    
    // Getter and setter for term
    public Integer getTerm() {
        return term;
    }
    
    public void setTerm(Integer term) {
        this.term = term;
    }
    
    // Additional getters and setters
    public Long getId() {
        return accountId;
    }
    
    public void setId(Long id) {
        this.accountId = id;
    }

    public LoanAccount(Long accountId) {
        this.accountId = accountId;
    }

    public LoanAccount(Long applicationId, String otherRef, Float amount, Float payableAmount, Float accountBalance, LocalDateTime startDate, LocalDateTime dueDate, String status, String customerId, String loanref) {
        this.applicationId = applicationId;
        OtherRef = otherRef;
        this.amount = amount;
        this.payableAmount = payableAmount;
        this.accountBalance = accountBalance;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = status;
        this.customerId = customerId;
        this.loanref = loanref;
    }

    public LoanAccount(Long accountId, Long applicationId, String otherRef, Float amount, Float payableAmount, Float accountBalance, LocalDateTime startDate, LocalDateTime dueDate, String status, String customerId, String loanref) {
        this.accountId = accountId;
        this.applicationId = applicationId;
        OtherRef = otherRef;
        this.amount = amount;
        this.payableAmount = payableAmount;
        this.accountBalance = accountBalance;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = status;
        this.customerId = customerId;
        this.loanref = loanref;
    }

    public LoanAccount(LoanBookUpload upload, LoanApplication loanApplication, Customer customer) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH);
        this.applicationId = loanApplication.getApplicationId();
        this.OtherRef = upload.getLoanRef();
        this.amount = Float.valueOf(upload.getLoanAmount());
        this.payableAmount = Float.parseFloat(upload.getLoanAmount())+Float.parseFloat(upload.getInterest());
        this.accountBalance = Float.valueOf(upload.getBalance());
        this.startDate = LocalDate.parse(upload.getDueDate(), formatter).atStartOfDay();
        this.dueDate = LocalDate.parse(upload.getDueDate(), formatter).atTime(23,59);
        this.status = upload.getLoanStatus();
        this.customerId = String.valueOf(customer.getId());
        this.loanref = upload.getLoanRef();
        this.installments=upload.getInstallments();
    }

    public Float getAmountPaid() {
        return this.payableAmount-this.accountBalance;
    }

    @Override
    public String toString() {
        return "LoanAccount{" +
                "accountId=" + accountId +
                ", applicationId=" + applicationId +
                ", OtherRef='" + OtherRef + '\'' +
                ", amount=" + amount +
                ", payableAmount=" + payableAmount +
                ", accountBalance=" + accountBalance +
                ", startDate=" + startDate +
                ", dueDate=" + dueDate +
                ", status='" + status + '\'' +
                ", customerId='" + customerId + '\'' +
                ", loanref='" + loanref + '\'' +
                '}';
    }
}
