package com.example.demo.accounting.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * General Ledger - Tracks all account transactions and balances
 */
@Entity
@Table(name = "general_ledger")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountCode;

    private String accountName;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false)
    private String reference; // Journal entry number

    private String description;

    @Column(nullable = false)
    private Double debit = 0.0;

    @Column(nullable = false)
    private Double credit = 0.0;

    @Column(nullable = false)
    private Double balance;

    private String journalEntryId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String createdBy;
}
