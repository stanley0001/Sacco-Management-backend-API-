package com.example.demo.finance.loanManagement.services;

import com.example.demo.finance.accounting.entities.JournalEntry;
import com.example.demo.finance.accounting.entities.JournalEntryLine;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepository;
import com.example.demo.finance.accounting.services.AccountingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service for processing loan waivers
 * Handles interest waivers, penalty waivers, and partial principal waivers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanWaiverService {
    
    private final LoanAccountRepository loanAccountRepository;
    private final AccountingService accountingService;
    
    /**
     * Waive interest on a loan
     */
    @Transactional
    public LoanAccount waiveInterest(Long loanId, BigDecimal waiverAmount, String approvedBy, String reason) {
        log.info("Processing interest waiver for loan: {}, amount: {}", loanId, waiverAmount);
        
        LoanAccount loan = loanAccountRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        
        // Validate waiver amount
        if (waiverAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Waiver amount must be greater than zero");
        }
        
        if (waiverAmount.compareTo(loan.getOutstandingInterest()) > 0) {
            throw new IllegalArgumentException("Waiver amount cannot exceed outstanding interest");
        }
        
        // Update loan balances
        BigDecimal newOutstandingInterest = loan.getOutstandingInterest().subtract(waiverAmount);
        loan.setOutstandingInterest(newOutstandingInterest);
        
        BigDecimal newTotalOutstanding = loan.getTotalOutstanding().subtract(waiverAmount);
        loan.setTotalOutstanding(newTotalOutstanding);
        
        loan.setUpdatedAt(LocalDateTime.now());
        
        LoanAccount updated = loanAccountRepository.save(loan);
        
        // Post to accounting (waiver as expense/write-off)
        try {
            postWaiverToAccounting(loan, waiverAmount, "INTEREST_WAIVER", approvedBy, reason);
        } catch (Exception e) {
            log.error("Failed to post waiver to accounting", e);
            // Don't fail the waiver, just log the error
        }
        
        log.info("Interest waiver processed successfully for loan: {}", loanId);
        return updated;
    }
    
    /**
     * Waive penalty on a loan
     */
    @Transactional
    public LoanAccount waivePenalty(Long loanId, BigDecimal waiverAmount, String approvedBy, String reason) {
        log.info("Processing penalty waiver for loan: {}, amount: {}", loanId, waiverAmount);
        
        LoanAccount loan = loanAccountRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        
        // Validate waiver amount
        if (waiverAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Waiver amount must be greater than zero");
        }
        
        // Update loan balances (reduce total outstanding)
        BigDecimal newTotalOutstanding = loan.getTotalOutstanding().subtract(waiverAmount);
        loan.setTotalOutstanding(newTotalOutstanding);
        loan.setUpdatedAt(LocalDateTime.now());
        
        LoanAccount updated = loanAccountRepository.save(loan);
        
        // Post to accounting
        try {
            postWaiverToAccounting(loan, waiverAmount, "PENALTY_WAIVER", approvedBy, reason);
        } catch (Exception e) {
            log.error("Failed to post waiver to accounting", e);
        }
        
        log.info("Penalty waiver processed successfully for loan: {}", loanId);
        return updated;
    }
    
    /**
     * Waive part of principal (partial write-off)
     */
    @Transactional
    public LoanAccount waivePrincipal(Long loanId, BigDecimal waiverAmount, String approvedBy, String reason) {
        log.info("Processing principal waiver for loan: {}, amount: {}", loanId, waiverAmount);
        
        LoanAccount loan = loanAccountRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        
        // Validate waiver amount
        if (waiverAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Waiver amount must be greater than zero");
        }
        
        if (waiverAmount.compareTo(loan.getOutstandingPrincipal()) > 0) {
            throw new IllegalArgumentException("Waiver amount cannot exceed outstanding principal");
        }
        
        // Update loan balances
        BigDecimal newOutstandingPrincipal = loan.getOutstandingPrincipal().subtract(waiverAmount);
        loan.setOutstandingPrincipal(newOutstandingPrincipal);
        
        BigDecimal newTotalOutstanding = loan.getTotalOutstanding().subtract(waiverAmount);
        loan.setTotalOutstanding(newTotalOutstanding);
        
        loan.setUpdatedAt(LocalDateTime.now());
        
        LoanAccount updated = loanAccountRepository.save(loan);
        
        // Post to accounting (as bad debt write-off)
        try {
            postWaiverToAccounting(loan, waiverAmount, "PRINCIPAL_WAIVER", approvedBy, reason);
        } catch (Exception e) {
            log.error("Failed to post waiver to accounting", e);
        }
        
        log.info("Principal waiver processed successfully for loan: {}", loanId);
        return updated;
    }
    
    /**
     * Full waiver - write off entire loan
     */
    @Transactional
    public LoanAccount waiveFull(Long loanId, String approvedBy, String reason) {
        log.info("Processing full waiver for loan: {}", loanId);
        
        LoanAccount loan = loanAccountRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        
        BigDecimal totalWaiver = loan.getTotalOutstanding();
        
        // Zero out all balances
        loan.setOutstandingPrincipal(BigDecimal.ZERO);
        loan.setOutstandingInterest(BigDecimal.ZERO);
        loan.setTotalOutstanding(BigDecimal.ZERO);
        loan.setStatus("WRITTEN_OFF");
        loan.setUpdatedAt(LocalDateTime.now());
        
        LoanAccount updated = loanAccountRepository.save(loan);
        
        // Post to accounting
        try {
            postWaiverToAccounting(loan, totalWaiver, "FULL_WAIVER", approvedBy, reason);
        } catch (Exception e) {
            log.error("Failed to post waiver to accounting", e);
        }
        
        log.info("Full waiver processed successfully for loan: {}", loanId);
        return updated;
    }
    
    /**
     * Post waiver to accounting as expense/write-off
     */
    private void postWaiverToAccounting(LoanAccount loan, BigDecimal amount, String waiverType, 
                                       String approvedBy, String reason) {
        // Create journal entry for waiver
        JournalEntry journalEntry =
            JournalEntry.builder()
                .description(String.format("Loan Waiver - %s - Loan: %s - Reason: %s", 
                    waiverType, loan.getLoanReference(), reason))
                .journalType(JournalEntry.JournalType.ADJUSTMENT)
                .reference(loan.getLoanReference())
                .transactionDate(java.time.LocalDate.now())
                .createdBy(approvedBy)
                .build();
        
        // Create journal lines
        java.util.List<JournalEntryLine> lines = new java.util.ArrayList<>();
        
        // Debit: Bad Debt Expense / Waiver Expense
        JournalEntryLine debitLine =
            new JournalEntryLine();
        debitLine.setJournalEntry(journalEntry);
        debitLine.setAccountCode("5100"); // Bad Debt Expense account (configurable)
        debitLine.setAccountName("Bad Debt Expense");
        debitLine.setType(JournalEntryLine.EntryType.DEBIT);
        debitLine.setAmount(amount.doubleValue());
        debitLine.setDescription("Loan waiver - " + waiverType);
        debitLine.setLineNumber(1);
        debitLine.setLoanAccountId(loan.getId().toString());
        lines.add(debitLine);
        
        // Credit: Loans Receivable
        JournalEntryLine creditLine =
            new JournalEntryLine();
        creditLine.setJournalEntry(journalEntry);
        creditLine.setAccountCode("1200"); // Loans Receivable account (configurable)
        creditLine.setAccountName("Loans Receivable");
        creditLine.setType(JournalEntryLine.EntryType.CREDIT);
        creditLine.setAmount(amount.doubleValue());
        creditLine.setDescription("Loan waiver - " + waiverType);
        creditLine.setLineNumber(2);
        creditLine.setLoanAccountId(loan.getId().toString());
        lines.add(creditLine);
        
        journalEntry.setLines(lines);
        
        // Post to accounting
        accountingService.createJournalEntry(journalEntry, approvedBy);
        
        log.info("Waiver posted to accounting: loan={}, amount={}, type={}", 
            loan.getId(), amount, waiverType);
    }
}
