package com.example.demo.loanManagement.parsistence.entities;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Disbursements  {
    @Id
    @Column(updatable = false,unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long disbursmentId;
    @Column(nullable = false)
    private Long accountId;
    @Column(nullable = false)
    private String status;
    @Column(unique = true)
    private String OtherRef;
    private String amount;
    private String response;
    private String destinationAccount;
    private LocalDateTime disbursementStart;
    private LocalDateTime disbursementEnd;

    public Disbursements() {
    }

    public Disbursements(Long disbursmentId) {
        this.disbursmentId = disbursmentId;
    }

    public Disbursements(Long accountId, String status, String otherRef, String amount, String response, String destinationAccount, LocalDateTime disbursementStart, LocalDateTime disbursementEnd) {
        this.accountId = accountId;
        this.status = status;
        OtherRef = otherRef;
        this.amount = amount;
        this.response = response;
        this.destinationAccount = destinationAccount;
        this.disbursementStart = disbursementStart;
        this.disbursementEnd = disbursementEnd;
    }

    public Disbursements(Long disbursmentId, Long accountId, String status, String otherRef, String amount, String response, String destinationAccount, LocalDateTime disbursementStart, LocalDateTime disbursementEnd) {
        this.disbursmentId = disbursmentId;
        this.accountId = accountId;
        this.status = status;
        OtherRef = otherRef;
        this.amount = amount;
        this.response = response;
        this.destinationAccount = destinationAccount;
        this.disbursementStart = disbursementStart;
        this.disbursementEnd = disbursementEnd;
    }

    public Long getDisbursmentId() {
        return disbursmentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDisbursmentId(Long disbursmentId) {
        this.disbursmentId = disbursmentId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public LocalDateTime getDisbursementStart() {
        return disbursementStart;
    }

    public void setDisbursementStart(LocalDateTime disbursementStart) {
        this.disbursementStart = disbursementStart;
    }

    public LocalDateTime getDisbursementEnd() {
        return disbursementEnd;
    }

    public void setDisbursementEnd(LocalDateTime disbursementEnd) {
        this.disbursementEnd = disbursementEnd;
    }

    @Override
    public String toString() {
        return "Disbursements{" +
                "disbursmentId=" + disbursmentId +
                ", accountId=" + accountId +
                ", status='" + status + '\'' +
                ", OtherRef='" + OtherRef + '\'' +
                ", amount='" + amount + '\'' +
                ", response='" + response + '\'' +
                ", destinationAccount='" + destinationAccount + '\'' +
                ", disbursementStart=" + disbursementStart +
                ", disbursementEnd=" + disbursementEnd +
                '}';
    }
}
