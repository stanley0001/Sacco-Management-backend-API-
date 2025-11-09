package com.example.demo.finance.accounting.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Journal Entry Line - Individual debit/credit lines
 */
@Entity
@Table(name = "journal_entry_lines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private ChartOfAccounts account;

    @Column(nullable = false)
    private String accountCode;

    private String accountName; // Denormalized for performance
    
    private String reference; // Reference for this specific line

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type; // DEBIT or CREDIT

    @Column(nullable = false)
    private Double amount;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer lineNumber; // Order of line in entry

    // For tracking specific transactions
    private String customerId;
    private String loanAccountId;
    private String savingsAccountId;

    public enum EntryType {
        DEBIT,
        CREDIT
    }
}
