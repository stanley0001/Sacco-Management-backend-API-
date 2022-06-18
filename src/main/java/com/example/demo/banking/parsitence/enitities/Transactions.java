package com.example.demo.loanManagement.parsistence.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class loanTransactions  {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    private String transactionType;
    private String loanRef;
    private String otherRef;
    private String otherResponses;
    private String initialBalance;
    private String finalBalance;
    private String accountNumber;
    private LocalDateTime transactionTime;

    public loanTransactions() {
    }

    public loanTransactions(Long transactionId) {
        this.transactionId = transactionId;
    }

    public loanTransactions(String transactionType, String loanRef, String otherRef, String otherResponses, String initialBalance, String finalBalance, String accountNumber, LocalDateTime transactionTime) {
        this.transactionType = transactionType;
        this.loanRef = loanRef;
        this.otherRef = otherRef;
        this.otherResponses = otherResponses;
        this.initialBalance = initialBalance;
        this.finalBalance = finalBalance;
        this.accountNumber = accountNumber;
        this.transactionTime = transactionTime;
    }

    public loanTransactions(Long transactionId, String transactionType, String loanRef, String otherRef, String otherResponses, String initialBalance, String finalBalance, String accountNumber, LocalDateTime transactionTime) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.loanRef = loanRef;
        this.otherRef = otherRef;
        this.otherResponses = otherResponses;
        this.initialBalance = initialBalance;
        this.finalBalance = finalBalance;
        this.accountNumber = accountNumber;
        this.transactionTime = transactionTime;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getLoanRef() {
        return loanRef;
    }

    public void setLoanRef(String loanRef) {
        this.loanRef = loanRef;
    }

    public String getOtherRef() {
        return otherRef;
    }

    public void setOtherRef(String otherRef) {
        this.otherRef = otherRef;
    }

    public String getOtherResponses() {
        return otherResponses;
    }

    public void setOtherResponses(String otherResponses) {
        this.otherResponses = otherResponses;
    }

    public String getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(String initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getFinalBalance() {
        return finalBalance;
    }

    public void setFinalBalance(String finalBalance) {
        this.finalBalance = finalBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    @Override
    public String toString() {
        return "loanTransactions{" +
                "transactionId=" + transactionId +
                ", transactionType='" + transactionType + '\'' +
                ", loanRef='" + loanRef + '\'' +
                ", otherRef='" + otherRef + '\'' +
                ", otherResponses='" + otherResponses + '\'' +
                ", initialBalance='" + initialBalance + '\'' +
                ", finalBalance='" + finalBalance + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", transactionTime=" + transactionTime +
                '}';
    }
}
