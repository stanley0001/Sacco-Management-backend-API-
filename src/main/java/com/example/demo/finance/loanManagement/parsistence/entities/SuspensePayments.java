package com.example.demo.finance.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SuspensePayments {
    @Id
    @Column(updatable = false,unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    @Column(nullable = false)
    private String accountNumber;
    @Column(nullable = false)
    private String status;
    @Column(unique = true)
    private String OtherRef;
    @Column(nullable = false)
    private String exceptionType;
    private String amount;
    private String utilisedBy;
    private String destinationAccount;
    private LocalDateTime paymentTime;

    public SuspensePayments() {
    }

    public SuspensePayments(Long paymentId) {
        this.paymentId = paymentId;
    }

    public SuspensePayments(String accountNumber, String status, String otherRef, String exceptionType, String amount, String utilisedBy, String destinationAccount, LocalDateTime paymentTime) {
        this.accountNumber = accountNumber;
        this.status = status;
        OtherRef = otherRef;
        this.exceptionType = exceptionType;
        this.amount = amount;
        this.utilisedBy = utilisedBy;
        this.destinationAccount = destinationAccount;
        this.paymentTime = paymentTime;
    }

    public SuspensePayments(Long paymentId, String accountNumber, String status, String otherRef, String exceptionType, String amount, String utilisedBy, String destinationAccount, LocalDateTime paymentTime) {
        this.paymentId = paymentId;
        this.accountNumber = accountNumber;
        this.status = status;
        OtherRef = otherRef;
        this.exceptionType = exceptionType;
        this.amount = amount;
        this.utilisedBy = utilisedBy;
        this.destinationAccount = destinationAccount;
        this.paymentTime = paymentTime;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOtherRef() {
        return OtherRef;
    }

    public void setOtherRef(String otherRef) {
        OtherRef = otherRef;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUtilisedBy() {
        return utilisedBy;
    }

    public void setUtilisedBy(String utilisedBy) {
        this.utilisedBy = utilisedBy;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    @Override
    public String toString() {
        return "SuspensePayments{" +
                "paymentId=" + paymentId +
                ", accountNumber='" + accountNumber + '\'' +
                ", status='" + status + '\'' +
                ", OtherRef='" + OtherRef + '\'' +
                ", exceptionType='" + exceptionType + '\'' +
                ", amount='" + amount + '\'' +
                ", utilisedBy='" + utilisedBy + '\'' +
                ", destinationAccount='" + destinationAccount + '\'' +
                ", paymentTime=" + paymentTime +
                '}';
    }
}
