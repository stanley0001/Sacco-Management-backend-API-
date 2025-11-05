package com.example.demo.loanManagement.services;

import com.example.demo.accounting.entities.ChartOfAccounts;
import com.example.demo.accounting.entities.JournalEntry;
import com.example.demo.accounting.entities.JournalEntryLine;
import com.example.demo.accounting.repositories.ChartOfAccountsRepo;
import com.example.demo.accounting.services.AccountingService;
import com.example.demo.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.loanManagement.parsistence.entities.loanTransactions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loan Accounting Service
 * Posts loan transactions to accounting module using double-entry bookkeeping
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanAccountingService {

    private final AccountingService accountingService;
    private final ChartOfAccountsRepo chartOfAccountsRepo;

    // Account Codes (must match Chart of Accounts)
    private static final String LOANS_RECEIVABLE_CODE = "1200";
    private static final String CASH_ACCOUNT_CODE = "1010";
    private static final String BANK_ACCOUNT_CODE = "1020";
    private static final String MPESA_ACCOUNT_CODE = "1030";
    private static final String INTEREST_INCOME_CODE = "4100";
    private static final String BAD_DEBT_EXPENSE_CODE = "5200";

    /**
     * Post loan disbursement to accounting
     */
    @Transactional
    public JournalEntry postLoanDisbursement(LoanAccount loanAccount, String disbursementMethod, String disbursedBy) {
        log.info("Posting loan disbursement to accounting for loan ID: {}", loanAccount.getAccountId());

        try {
            // Convert BigDecimal to Double for JournalEntry
            Double principalAmount = loanAccount.getPrincipalAmount() != null 
                ? loanAccount.getPrincipalAmount().doubleValue() 
                : loanAccount.getAmount().doubleValue();

            JournalEntry entry = new JournalEntry();
            entry.setJournalType(JournalEntry.JournalType.LOAN_DISBURSEMENT);
            entry.setTransactionDate(LocalDate.now());
            entry.setDescription("Loan Disbursed - Loan #" + loanAccount.getAccountId() + " via " + disbursementMethod);
            entry.setReference("LOAN-DISB-" + loanAccount.getAccountId());

            List<JournalEntryLine> lines = new ArrayList<>();
            AtomicInteger lineNum = new AtomicInteger(1);

            // DEBIT: Loans Receivable
            JournalEntryLine debitLoans = JournalEntryLine.builder()
                .accountCode(LOANS_RECEIVABLE_CODE)
                .accountName(getAccountName(LOANS_RECEIVABLE_CODE))
                .type(JournalEntryLine.EntryType.DEBIT)
                .amount(principalAmount)
                .description("Loan disbursed")
                .lineNumber(lineNum.getAndIncrement())
                .loanAccountId(String.valueOf(loanAccount.getAccountId()))
                .build();
            lines.add(debitLoans);

            // CREDIT: Cash/Bank/M-PESA Account
            String paymentAccountCode = getCashAccountCode(disbursementMethod);
            JournalEntryLine creditCash = JournalEntryLine.builder()
                .accountCode(paymentAccountCode)
                .accountName(getAccountName(paymentAccountCode))
                .type(JournalEntryLine.EntryType.CREDIT)
                .amount(principalAmount)
                .description("Loan disbursement via " + disbursementMethod)
                .lineNumber(lineNum.getAndIncrement())
                .build();
            lines.add(creditCash);

            entry.setLines(lines);

            // Save and post journal entry
            JournalEntry savedEntry = accountingService.createJournalEntry(entry, disbursedBy);
            accountingService.postJournalEntry(savedEntry.getId(), disbursedBy);

            log.info("✅ Loan disbursement posted successfully. Journal ID: {}", savedEntry.getId());
            return savedEntry;

        } catch (Exception e) {
            log.error("❌ Failed to post loan disbursement to accounting", e);
            throw new RuntimeException("Failed to post loan disbursement: " + e.getMessage(), e);
        }
    }

    /**
     * Post loan repayment to accounting
     */
    @Transactional
    public JournalEntry postLoanRepayment(loanTransactions transaction, String postedBy) {
        log.info("Posting loan repayment to accounting for transaction ID: {}", transaction.getTransactionId());

        try {
            Double totalAmount = transaction.getAmount().doubleValue();
            // Note: loanTransactions doesn't have separate principal/interest fields
            // This is a simplified version - you'll need to calculate split based on schedule
            Double principalPortion = totalAmount * 0.8; // Example: 80% principal
            Double interestPortion = totalAmount * 0.2; // Example: 20% interest

            JournalEntry entry = new JournalEntry();
            entry.setJournalType(JournalEntry.JournalType.LOAN_REPAYMENT);
            entry.setTransactionDate(LocalDate.now());
            entry.setDescription("Loan Repayment - Receipt #" + transaction.getOtherRef());
            entry.setReference("LOAN-PMT-" + transaction.getTransactionId());

            List<JournalEntryLine> lines = new ArrayList<>();
            AtomicInteger lineNum = new AtomicInteger(1);

            // DEBIT: Cash/Bank/M-PESA Account
            String paymentAccountCode = getCashAccountCode(transaction.getTransactionType());
            JournalEntryLine debitCash = JournalEntryLine.builder()
                .accountCode(paymentAccountCode)
                .accountName(getAccountName(paymentAccountCode))
                .type(JournalEntryLine.EntryType.DEBIT)
                .amount(totalAmount)
                .description("Loan payment received via " + transaction.getTransactionType())
                .lineNumber(lineNum.getAndIncrement())
                .build();
            lines.add(debitCash);

            // CREDIT: Loans Receivable (Principal)
            if (principalPortion != null && principalPortion > 0) {
                JournalEntryLine creditPrincipal = JournalEntryLine.builder()
                    .accountCode(LOANS_RECEIVABLE_CODE)
                    .accountName(getAccountName(LOANS_RECEIVABLE_CODE))
                    .type(JournalEntryLine.EntryType.CREDIT)
                    .amount(principalPortion)
                    .description("Principal repayment")
                    .lineNumber(lineNum.getAndIncrement())
                    .loanAccountId(String.valueOf(transaction.getAccountId()))
                    .build();
                lines.add(creditPrincipal);
            }

            // CREDIT: Interest Income
            if (interestPortion != null && interestPortion > 0) {
                JournalEntryLine creditInterest = JournalEntryLine.builder()
                    .accountCode(INTEREST_INCOME_CODE)
                    .accountName(getAccountName(INTEREST_INCOME_CODE))
                    .type(JournalEntryLine.EntryType.CREDIT)
                    .amount(interestPortion)
                    .description("Interest income earned")
                    .lineNumber(lineNum.getAndIncrement())
                    .build();
                lines.add(creditInterest);
            }

            entry.setLines(lines);

            // Save and post journal entry
            JournalEntry savedEntry = accountingService.createJournalEntry(entry, postedBy);
            accountingService.postJournalEntry(savedEntry.getId(), postedBy);

            log.info("✅ Loan repayment posted successfully. Journal ID: {}", savedEntry.getId());
            return savedEntry;

        } catch (Exception e) {
            log.error("❌ Failed to post loan repayment to accounting", e);
            throw new RuntimeException("Failed to post loan repayment: " + e.getMessage(), e);
        }
    }

    /**
     * Post loan write-off to accounting
     */
    @Transactional
    public JournalEntry postLoanWriteOff(LoanAccount loanAccount, Double writeOffAmount, 
                                        String reason, String postedBy) {
        log.info("Posting loan write-off to accounting for loan ID: {}", loanAccount.getAccountId());

        try {
            JournalEntry entry = new JournalEntry();
            entry.setJournalType(JournalEntry.JournalType.ADJUSTMENT);
            entry.setTransactionDate(LocalDate.now());
            entry.setDescription("Loan Write-off - Loan #" + loanAccount.getAccountId() + " - " + reason);
            entry.setReference("LOAN-WO-" + loanAccount.getAccountId());

            List<JournalEntryLine> lines = new ArrayList<>();
            AtomicInteger lineNum = new AtomicInteger(1);

            // DEBIT: Bad Debt Expense
            JournalEntryLine debitBadDebt = JournalEntryLine.builder()
                .accountCode(BAD_DEBT_EXPENSE_CODE)
                .accountName(getAccountName(BAD_DEBT_EXPENSE_CODE))
                .type(JournalEntryLine.EntryType.DEBIT)
                .amount(writeOffAmount)
                .description("Bad debt write-off")
                .lineNumber(lineNum.getAndIncrement())
                .loanAccountId(String.valueOf(loanAccount.getAccountId()))
                .build();
            lines.add(debitBadDebt);

            // CREDIT: Loans Receivable
            JournalEntryLine creditLoans = JournalEntryLine.builder()
                .accountCode(LOANS_RECEIVABLE_CODE)
                .accountName(getAccountName(LOANS_RECEIVABLE_CODE))
                .type(JournalEntryLine.EntryType.CREDIT)
                .amount(writeOffAmount)
                .description("Loan written off")
                .lineNumber(lineNum.getAndIncrement())
                .loanAccountId(String.valueOf(loanAccount.getAccountId()))
                .build();
            lines.add(creditLoans);

            entry.setLines(lines);

            // Save and post journal entry
            JournalEntry savedEntry = accountingService.createJournalEntry(entry, postedBy);
            accountingService.postJournalEntry(savedEntry.getId(), postedBy);

            log.info("✅ Loan write-off posted successfully. Journal ID: {}", savedEntry.getId());
            return savedEntry;

        } catch (Exception e) {
            log.error("❌ Failed to post loan write-off to accounting", e);
            throw new RuntimeException("Failed to post loan write-off: " + e.getMessage(), e);
        }
    }

    /**
     * Helper: Get cash account code based on payment method
     */
    private String getCashAccountCode(String paymentMethod) {
        if (paymentMethod == null) {
            return CASH_ACCOUNT_CODE;
        }

        switch (paymentMethod.toUpperCase()) {
            case "MPESA":
            case "M-PESA":
                return MPESA_ACCOUNT_CODE;
            case "BANK":
            case "BANK_TRANSFER":
            case "CHEQUE":
                return BANK_ACCOUNT_CODE;
            case "CASH":
            default:
                return CASH_ACCOUNT_CODE;
        }
    }

    /**
     * Helper: Get account name from chart of accounts
     */
    private String getAccountName(String accountCode) {
        return chartOfAccountsRepo.findByAccountCode(accountCode)
            .map(ChartOfAccounts::getAccountName)
            .orElse("Account " + accountCode);
    }
}
