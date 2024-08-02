package com.example.demo.banking.parsitence.enitities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    @JoinColumn(name = "bank_account_id")
    private BankAccounts bankAccount;
}
