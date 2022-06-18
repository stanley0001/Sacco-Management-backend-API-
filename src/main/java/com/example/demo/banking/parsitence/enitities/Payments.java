package com.example.demo.loanManagement.parsistence.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Payments  {
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
    private String amount;
    private String loanNumber;
    private String destinationAccount;
    private LocalDateTime paymentTime;

    public Payments() {
    }

    public Payments(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Payments(String accountNumber, String status, String otherRef, String amount, String loanNumber, String destinationAccount, LocalDateTime paymentTime) {
        this.accountNumber = accountNumber;
        this.status = status;
        OtherRef = otherRef;
        this.amount = amount;
        this.loanNumber = loanNumber;
        this.destinationAccount = destinationAccount;
        this.paymentTime = paymentTime;
    }

    public Payments(Long paymentId, String accountNumber, String status, String otherRef, String amount, String loanNumber, String destinationAccount, LocalDateTime paymentTime) {
        this.paymentId = paymentId;
        this.accountNumber = accountNumber;
        this.status = status;
        OtherRef = otherRef;
        this.amount = amount;
        this.loanNumber = loanNumber;
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
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
        return "Payments{" +
                "paymentId=" + paymentId +
                ", accountNumber='" + accountNumber + '\'' +
                ", status='" + status + '\'' +
                ", OtherRef='" + OtherRef + '\'' +
                ", amount='" + amount + '\'' +
                ", loanNumber='" + loanNumber + '\'' +
                ", destinationAccount='" + destinationAccount + '\'' +
                ", paymentTime=" + paymentTime +
                '}';
    }
}
