package com.example.demo.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
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
    @Column(nullable = false)
    private Float accountBalance;
    private  LocalDateTime startDate;
    private  LocalDateTime dueDate;
    @Column(nullable = false)
    private  String status;
    @Column(nullable = false)
    private  String customerId;
    @Column(nullable = false)
    private  String loanref;
    public LoanAccount() {
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

    public String getLoanref() {
        return loanref;
    }

    public void setLoanref(String loanref) {
        this.loanref = loanref;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getOtherRef() {
        return OtherRef;
    }

    public void setOtherRef(String otherRef) {
        OtherRef = otherRef;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Float getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(Float payableAmount) {
        this.payableAmount = payableAmount;
    }

    public Float getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(Float accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
