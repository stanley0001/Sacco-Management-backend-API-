package com.example.demo.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PaymentRequest  {
    @Id
    @Column(updatable = false,unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;
    @Column(nullable = false)
    private String accountNumber;
    @Column(nullable = false)
    private String status;
    @Column(unique = true)
    private String OtherRef;
    private String amount;
    private String productCode;
    private String destinationAccount;
    private LocalDateTime paymentTime;

    public PaymentRequest() {
    }

    public PaymentRequest(String accountNumber, String status, String otherRef, String amount, String productCode, String destinationAccount, LocalDateTime paymentTime) {
        this.accountNumber = accountNumber;
        this.status = status;
        OtherRef = otherRef;
        this.amount = amount;
        this.productCode = productCode;
        this.destinationAccount = destinationAccount;
        this.paymentTime = paymentTime;
    }

    public PaymentRequest(Long requestId, String accountNumber, String status, String otherRef, String amount, String productCode, String destinationAccount, LocalDateTime paymentTime) {
        this.requestId = requestId;
        this.accountNumber = accountNumber;
        this.status = status;
        OtherRef = otherRef;
        this.amount = amount;
        this.productCode = productCode;
        this.destinationAccount = destinationAccount;
        this.paymentTime = paymentTime;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
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

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
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
        return "PaymentRequest{" +
                "requestId=" + requestId +
                ", accountNumber='" + accountNumber + '\'' +
                ", status='" + status + '\'' +
                ", OtherRef='" + OtherRef + '\'' +
                ", amount='" + amount + '\'' +
                ", productCode='" + productCode + '\'' +
                ", destinationAccount='" + destinationAccount + '\'' +
                ", paymentTime=" + paymentTime +
                '}';
    }
}
