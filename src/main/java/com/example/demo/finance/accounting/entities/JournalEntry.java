package com.example.demo.finance.accounting.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Journal Entry - Records financial transactions
 */
@Entity
@Table(name = "journal_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String journalNumber;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private String reference; // Invoice number, receipt number, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JournalType journalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JournalStatus status = JournalStatus.DRAFT;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JournalEntryLine> lines = new ArrayList<>();

    private Double totalDebit = 0.0;
    private Double totalCredit = 0.0;

    @Column(nullable = false)
    private Boolean isBalanced = false;

    private String postedBy;
    private LocalDateTime postedAt;

    private String approvedBy;
    private LocalDateTime approvedAt;

    private String createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 100)
    private String entryNumber; // Unique entry number (e.g., LD-1234567890)

    @Column
    private LocalDate entryDate;

    @Column(length = 100)
    private String referenceNumber; // External reference (receipt, transaction ID)

    @Column(length = 100)
    private String sourceDocument; // Source document type (e.g., LOAN_DISBURSEMENT)

    @Column
    private Long sourceId; // Source entity ID (e.g., loanAccountId)

    @Column
    private Boolean isPosted = false;

    public enum JournalType {
        GENERAL,           // General journal entries
        SALES,             // Sales transactions
        PURCHASES,         // Purchase transactions
        CASH_RECEIPTS,     // Cash received
        CASH_PAYMENTS,     // Cash paid
        LOAN_DISBURSEMENT, // Loan disbursement
        LOAN_REPAYMENT,    // Loan repayment
        DEPOSIT,           // Member deposits
        WITHDRAWAL,        // Member withdrawals
        ADJUSTMENT,        // Adjusting entries
        PAYMENT,           // Payment entries
        DISBURSEMENT,      // Disbursement entries
        CLOSING            // Closing entries
    }

    public enum JournalStatus {
        DRAFT,      // Created but not posted
        POSTED,     // Posted to ledger
        APPROVED,   // Approved by authorized person
        REVERSED    // Reversed/Cancelled
    }

    // Helper methods
    public void addLine(JournalEntryLine line) {
        if (lines == null) {
            lines = new ArrayList<>();
        }
        lines.add(line);
        line.setJournalEntry(this);
    }

    /**
     * Calculate total debits and credits from lines
     */
    public void calculateTotals() {
        double debitTotal = 0.0;
        double creditTotal = 0.0;
        
        if (lines != null) {
            for (JournalEntryLine line : lines) {
                if (line.getType() == JournalEntryLine.EntryType.DEBIT) {
                    debitTotal += line.getAmount();
                } else {
                    creditTotal += line.getAmount();
                }
            }
        }
        
        this.totalDebit = debitTotal;
        this.totalCredit = creditTotal;
        this.isBalanced = Math.abs(debitTotal - creditTotal) < 0.01; // Allow for rounding
    }
}
