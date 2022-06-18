package com.example.demo.banking.parsitence.enitities;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class BankAccounts {
    @Id
    @Column(nullable = false,unique = true,updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,unique = true,updatable = false)
    private String bankAccount;
    private String accountDescription;
    private String accountType;
    private Double accountBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @OneToMany
    List<Transactions> transactions;
    @ManyToOne
    private Customer customer;

    public BankAccounts() {
    }

    public BankAccounts(String bankAccount, String accountDescription, String accountType, Double accountBalance, LocalDateTime createdAt, LocalDateTime updatedAt, List<Transactions> transactions, Customer customer) {
        this.bankAccount = bankAccount;
        this.accountDescription = accountDescription;
        this.accountType = accountType;
        this.accountBalance = accountBalance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.transactions = transactions;
        this.customer = customer;
    }

    public BankAccounts(Long id, String bankAccount, String accountDescription, String accountType, Double accountBalance, LocalDateTime createdAt, LocalDateTime updatedAt, List<Transactions> transactions, Customer customer) {
        this.id = id;
        this.bankAccount = bankAccount;
        this.accountDescription = accountDescription;
        this.accountType = accountType;
        this.accountBalance = accountBalance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.transactions = transactions;
        this.customer = customer;
    }
}
