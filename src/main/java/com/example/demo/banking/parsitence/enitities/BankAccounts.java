package com.example.demo.banking.parsitence.enitities;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    /*@OneToMany(fetch = FetchType.LAZY)
            @JoinColumn(columnDefinition = "transactions_transaction_id")
    List<Transactions> transactions; */
    @ManyToOne
    private Customer customer;

}
