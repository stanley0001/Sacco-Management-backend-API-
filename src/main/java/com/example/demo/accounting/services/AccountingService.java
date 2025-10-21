package com.example.demo.accounting.services;

import com.example.demo.accounting.entities.ChartOfAccounts;
import com.example.demo.accounting.entities.JournalEntry;
import com.example.demo.accounting.entities.JournalEntryLine;
import com.example.demo.accounting.repositories.ChartOfAccountsRepo;
import com.example.demo.accounting.repositories.JournalEntryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AccountingService {

    private final ChartOfAccountsRepo chartOfAccountsRepo;
    private final JournalEntryRepo journalEntryRepo;

    // ========== Chart of Accounts ==========

    @Transactional
    public ChartOfAccounts createAccount(ChartOfAccounts account) {
        if (chartOfAccountsRepo.existsByAccountCode(account.getAccountCode())) {
            throw new RuntimeException("Account code already exists: " + account.getAccountCode());
        }
        return chartOfAccountsRepo.save(account);
    }

    @Transactional
    public ChartOfAccounts updateAccount(Long id, ChartOfAccounts account) {
        ChartOfAccounts existing = chartOfAccountsRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
        
        if (existing.getIsSystemAccount()) {
            throw new RuntimeException("System accounts cannot be modified");
        }

        existing.setAccountName(account.getAccountName());
        existing.setDescription(account.getDescription());
        existing.setIsActive(account.getIsActive());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return chartOfAccountsRepo.save(existing);
    }

    public List<ChartOfAccounts> getAllAccounts() {
        return chartOfAccountsRepo.findAll();
    }

    public List<ChartOfAccounts> getActiveAccounts() {
        return chartOfAccountsRepo.findByIsActiveTrue();
    }

    public Optional<ChartOfAccounts> getAccountByCode(String accountCode) {
        return chartOfAccountsRepo.findByAccountCode(accountCode);
    }

    public List<ChartOfAccounts> getAccountsByType(ChartOfAccounts.AccountType type) {
        return chartOfAccountsRepo.findByAccountType(type);
    }

    // ========== Journal Entries ==========

    @Transactional
    public JournalEntry createJournalEntry(JournalEntry entry, String createdBy) {
        // Generate journal number if not provided
        if (entry.getJournalNumber() == null || entry.getJournalNumber().isEmpty()) {
            entry.setJournalNumber(generateJournalNumber(entry.getJournalType()));
        }

        entry.setCreatedBy(createdBy);
        entry.setStatus(JournalEntry.JournalStatus.DRAFT);
        
        // Set journal entry reference for lines
        for (JournalEntryLine line : entry.getLines()) {
            line.setJournalEntry(entry);
            
            // Get account name
            Optional<ChartOfAccounts> account = chartOfAccountsRepo.findByAccountCode(line.getAccountCode());
            account.ifPresent(chartOfAccounts -> line.setAccountName(chartOfAccounts.getAccountName()));
        }

        entry.calculateTotals();

        if (!entry.getIsBalanced()) {
            throw new RuntimeException("Journal entry is not balanced. Debits: " + 
                entry.getTotalDebit() + ", Credits: " + entry.getTotalCredit());
        }

        return journalEntryRepo.save(entry);
    }

    @Transactional
    public JournalEntry postJournalEntry(Long entryId, String postedBy) {
        JournalEntry entry = journalEntryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Journal entry not found: " + entryId));

        if (entry.getStatus() != JournalEntry.JournalStatus.DRAFT) {
            throw new RuntimeException("Only draft entries can be posted");
        }

        if (!entry.getIsBalanced()) {
            throw new RuntimeException("Cannot post unbalanced entry");
        }

        // Update account balances
        for (JournalEntryLine line : entry.getLines()) {
            updateAccountBalance(line);
        }

        entry.setStatus(JournalEntry.JournalStatus.POSTED);
        entry.setPostedBy(postedBy);
        entry.setPostedAt(LocalDateTime.now());

        return journalEntryRepo.save(entry);
    }

    @Transactional
    public JournalEntry approveJournalEntry(Long entryId, String approvedBy) {
        JournalEntry entry = journalEntryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Journal entry not found: " + entryId));

        if (entry.getStatus() != JournalEntry.JournalStatus.POSTED) {
            throw new RuntimeException("Only posted entries can be approved");
        }

        entry.setStatus(JournalEntry.JournalStatus.APPROVED);
        entry.setApprovedBy(approvedBy);
        entry.setApprovedAt(LocalDateTime.now());

        return journalEntryRepo.save(entry);
    }

    @Transactional
    public JournalEntry reverseJournalEntry(Long entryId, String reason, String reversedBy) {
        JournalEntry originalEntry = journalEntryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Journal entry not found: " + entryId));

        if (originalEntry.getStatus() != JournalEntry.JournalStatus.POSTED &&
            originalEntry.getStatus() != JournalEntry.JournalStatus.APPROVED) {
            throw new RuntimeException("Only posted or approved entries can be reversed");
        }

        // Create reversal entry
        JournalEntry reversalEntry = JournalEntry.builder()
                .journalNumber(generateJournalNumber(JournalEntry.JournalType.ADJUSTMENT))
                .transactionDate(LocalDate.now())
                .description("REVERSAL: " + originalEntry.getDescription() + " - Reason: " + reason)
                .reference("REV-" + originalEntry.getJournalNumber())
                .journalType(JournalEntry.JournalType.ADJUSTMENT)
                .status(JournalEntry.JournalStatus.POSTED)
                .createdBy(reversedBy)
                .postedBy(reversedBy)
                .postedAt(LocalDateTime.now())
                .build();

        // Reverse all lines (swap debit and credit)
        for (JournalEntryLine originalLine : originalEntry.getLines()) {
            JournalEntryLine reversalLine = JournalEntryLine.builder()
                    .journalEntry(reversalEntry)
                    .accountCode(originalLine.getAccountCode())
                    .accountName(originalLine.getAccountName())
                    .type(originalLine.getType() == JournalEntryLine.EntryType.DEBIT ? 
                          JournalEntryLine.EntryType.CREDIT : JournalEntryLine.EntryType.DEBIT)
                    .amount(originalLine.getAmount())
                    .description("Reversal of " + originalLine.getDescription())
                    .lineNumber(originalLine.getLineNumber())
                    .build();
            reversalEntry.getLines().add(reversalLine);
        }

        reversalEntry.calculateTotals();

        // Update account balances for reversal
        for (JournalEntryLine line : reversalEntry.getLines()) {
            updateAccountBalance(line);
        }

        // Mark original as reversed
        originalEntry.setStatus(JournalEntry.JournalStatus.REVERSED);
        originalEntry.setNotes((originalEntry.getNotes() != null ? originalEntry.getNotes() : "") + 
                              "\nReversed on " + LocalDateTime.now() + " by " + reversedBy + 
                              ". Reason: " + reason);
        journalEntryRepo.save(originalEntry);

        return journalEntryRepo.save(reversalEntry);
    }

    public List<JournalEntry> getJournalEntriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return journalEntryRepo.findByTransactionDateBetween(startDate, endDate);
    }

    public List<JournalEntry> getJournalEntriesByStatus(JournalEntry.JournalStatus status) {
        return journalEntryRepo.findByStatus(status);
    }

    public Optional<JournalEntry> getJournalEntryById(Long id) {
        return journalEntryRepo.findById(id);
    }

    // ========== Helper Methods ==========

    private void updateAccountBalance(JournalEntryLine line) {
        Optional<ChartOfAccounts> accountOpt = chartOfAccountsRepo.findByAccountCode(line.getAccountCode());
        
        accountOpt.ifPresent(account -> {
            double currentBalance = account.getCurrentBalance();
            
            // Update balance based on account normal balance and transaction type
            if (account.getNormalBalance() == ChartOfAccounts.NormalBalance.DEBIT) {
                if (line.getType() == JournalEntryLine.EntryType.DEBIT) {
                    account.setCurrentBalance(currentBalance + line.getAmount());
                } else {
                    account.setCurrentBalance(currentBalance - line.getAmount());
                }
            } else { // CREDIT normal balance
                if (line.getType() == JournalEntryLine.EntryType.CREDIT) {
                    account.setCurrentBalance(currentBalance + line.getAmount());
                } else {
                    account.setCurrentBalance(currentBalance - line.getAmount());
                }
            }
            
            chartOfAccountsRepo.save(account);
            log.info("Updated balance for account {}: {}", account.getAccountCode(), account.getCurrentBalance());
        });
    }

    private String generateJournalNumber(JournalEntry.JournalType type) {
        String prefix = switch (type) {
            case GENERAL -> "GJ";
            case SALES -> "SJ";
            case PURCHASES -> "PJ";
            case CASH_RECEIPTS -> "CR";
            case CASH_PAYMENTS -> "CP";
            case LOAN_DISBURSEMENT -> "LD";
            case LOAN_REPAYMENT -> "LR";
            case DEPOSIT -> "DE";
            case WITHDRAWAL -> "WD";
            case ADJUSTMENT -> "AJ";
            case CLOSING -> "CJ";
        };
        
        return prefix + "-" + System.currentTimeMillis();
    }

    /**
     * Initialize standard chart of accounts for a SACCO
     */
    @Transactional
    public void initializeStandardChartOfAccounts(String createdBy) {
        if (chartOfAccountsRepo.count() > 0) {
            log.info("Chart of accounts already initialized");
            return;
        }

        log.info("Initializing standard chart of accounts...");

        // Assets
        createStandardAccount("1000", "Assets", ChartOfAccounts.AccountType.ASSET, 
                             ChartOfAccounts.AccountCategory.CURRENT_ASSET, null, 
                             ChartOfAccounts.NormalBalance.DEBIT, true, createdBy);
        createStandardAccount("1010", "Cash", ChartOfAccounts.AccountType.ASSET, 
                             ChartOfAccounts.AccountCategory.CURRENT_ASSET, "1000", 
                             ChartOfAccounts.NormalBalance.DEBIT, true, createdBy);
        createStandardAccount("1020", "Bank Accounts", ChartOfAccounts.AccountType.ASSET, 
                             ChartOfAccounts.AccountCategory.CURRENT_ASSET, "1000", 
                             ChartOfAccounts.NormalBalance.DEBIT, true, createdBy);
        createStandardAccount("1030", "Loans Receivable", ChartOfAccounts.AccountType.ASSET, 
                             ChartOfAccounts.AccountCategory.CURRENT_ASSET, "1000", 
                             ChartOfAccounts.NormalBalance.DEBIT, true, createdBy);
        createStandardAccount("1040", "Interest Receivable", ChartOfAccounts.AccountType.ASSET, 
                             ChartOfAccounts.AccountCategory.CURRENT_ASSET, "1000", 
                             ChartOfAccounts.NormalBalance.DEBIT, true, createdBy);
        createStandardAccount("1050", "Fixed Assets", ChartOfAccounts.AccountType.ASSET, 
                             ChartOfAccounts.AccountCategory.FIXED_ASSET, "1000", 
                             ChartOfAccounts.NormalBalance.DEBIT, false, createdBy);

        // Liabilities
        createStandardAccount("2000", "Liabilities", ChartOfAccounts.AccountType.LIABILITY, 
                             ChartOfAccounts.AccountCategory.CURRENT_LIABILITY, null, 
                             ChartOfAccounts.NormalBalance.CREDIT, true, createdBy);
        createStandardAccount("2010", "Member Deposits", ChartOfAccounts.AccountType.LIABILITY, 
                             ChartOfAccounts.AccountCategory.CURRENT_LIABILITY, "2000", 
                             ChartOfAccounts.NormalBalance.CREDIT, true, createdBy);
        createStandardAccount("2020", "Member Savings", ChartOfAccounts.AccountType.LIABILITY, 
                             ChartOfAccounts.AccountCategory.CURRENT_LIABILITY, "2000", 
                             ChartOfAccounts.NormalBalance.CREDIT, true, createdBy);

        // Equity
        createStandardAccount("3000", "Equity", ChartOfAccounts.AccountType.EQUITY, 
                             ChartOfAccounts.AccountCategory.CAPITAL, null, 
                             ChartOfAccounts.NormalBalance.CREDIT, true, createdBy);
        createStandardAccount("3010", "Share Capital", ChartOfAccounts.AccountType.EQUITY, 
                             ChartOfAccounts.AccountCategory.CAPITAL, "3000", 
                             ChartOfAccounts.NormalBalance.CREDIT, true, createdBy);
        createStandardAccount("3020", "Retained Earnings", ChartOfAccounts.AccountType.EQUITY, 
                             ChartOfAccounts.AccountCategory.RETAINED_EARNINGS, "3000", 
                             ChartOfAccounts.NormalBalance.CREDIT, true, createdBy);

        // Revenue
        createStandardAccount("4000", "Revenue", ChartOfAccounts.AccountType.REVENUE, 
                             ChartOfAccounts.AccountCategory.OPERATING_REVENUE, null, 
                             ChartOfAccounts.NormalBalance.CREDIT, true, createdBy);
        createStandardAccount("4010", "Interest Income", ChartOfAccounts.AccountType.REVENUE, 
                             ChartOfAccounts.AccountCategory.OPERATING_REVENUE, "4000", 
                             ChartOfAccounts.NormalBalance.CREDIT, true, createdBy);
        createStandardAccount("4020", "Fee Income", ChartOfAccounts.AccountType.REVENUE, 
                             ChartOfAccounts.AccountCategory.OPERATING_REVENUE, "4000", 
                             ChartOfAccounts.NormalBalance.CREDIT, true, createdBy);

        // Expenses
        createStandardAccount("5000", "Expenses", ChartOfAccounts.AccountType.EXPENSE, 
                             ChartOfAccounts.AccountCategory.OPERATING_EXPENSE, null, 
                             ChartOfAccounts.NormalBalance.DEBIT, true, createdBy);
        createStandardAccount("5010", "Interest Expense", ChartOfAccounts.AccountType.EXPENSE, 
                             ChartOfAccounts.AccountCategory.FINANCIAL_EXPENSE, "5000", 
                             ChartOfAccounts.NormalBalance.DEBIT, true, createdBy);
        createStandardAccount("5020", "Administrative Expenses", ChartOfAccounts.AccountType.EXPENSE, 
                             ChartOfAccounts.AccountCategory.ADMINISTRATIVE_EXPENSE, "5000", 
                             ChartOfAccounts.NormalBalance.DEBIT, false, createdBy);

        log.info("Standard chart of accounts initialized successfully");
    }

    private void createStandardAccount(String code, String name, ChartOfAccounts.AccountType type,
                                       ChartOfAccounts.AccountCategory category, String parent,
                                       ChartOfAccounts.NormalBalance normalBalance,
                                       boolean isSystem, String createdBy) {
        ChartOfAccounts account = ChartOfAccounts.builder()
                .accountCode(code)
                .accountName(name)
                .accountType(type)
                .accountCategory(category)
                .parentAccountCode(parent)
                .normalBalance(normalBalance)
                .isActive(true)
                .isSystemAccount(isSystem)
                .currentBalance(0.0)
                .createdBy(createdBy)
                .build();
        
        chartOfAccountsRepo.save(account);
    }
}
