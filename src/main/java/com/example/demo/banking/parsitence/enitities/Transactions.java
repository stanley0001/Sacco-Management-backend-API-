package com.example.demo.banking.parsitence.enitities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Transactions {
    @Id
    @Column(nullable = false,unique = true,updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    private String transactionType;
    private String otherRef;
    private Double amount;
    private Double openingBalance;
    private Double closingBalance;
    private LocalDateTime transactionTime;
    @ManyToOne
    private BankAccounts bankAccount;
    public Transactions() {
    }

    public Transactions(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Transactions(String transactionType, String otherRef, Double openingBalance, Double closingBalance, LocalDateTime transactionTime) {
        this.transactionType = transactionType;
        this.otherRef = otherRef;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.transactionTime = transactionTime;
    }

    public Transactions(Long transactionId, String transactionType, String otherRef, Double openingBalance, Double closingBalance, LocalDateTime transactionTime) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.otherRef = otherRef;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.transactionTime = transactionTime;
    }

    @Override
    public String toString() {
        return "Transactions{" +
                "transactionId=" + transactionId +
                ", transactionType='" + transactionType + '\'' +
                ", otherRef='" + otherRef + '\'' +
                ", openingBalance='" + openingBalance + '\'' +
                ", closingBalance='" + closingBalance + '\'' +
                ", transactionTime=" + transactionTime +
                '}';
    }
}
