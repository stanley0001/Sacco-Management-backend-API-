package com.example.demo.finance.accounting.services;

import com.example.demo.finance.accounting.entities.ChartOfAccounts;
import com.example.demo.finance.accounting.entities.JournalEntry;
import com.example.demo.finance.accounting.entities.JournalEntryLine;
import com.example.demo.finance.accounting.repositories.ChartOfAccountsRepo;
import com.example.demo.finance.accounting.repositories.JournalEntryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Deposit & Savings Accounting Integration Service
 * Creates journal entries for deposits, withdrawals, and transfers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepositAccountingIntegrationService {

    private final JournalEntryRepo journalEntryRepository;
    private final ChartOfAccountsRepo chartOfAccountsRepository;

    // Standard Account Codes
    private static final String ACCOUNT_CASH = "1000"; // Asset
    private static final String ACCOUNT_BANK = "1010"; // Asset  
    private static final String ACCOUNT_MPESA = "1020"; // Asset
    private static final String ACCOUNT_CUSTOMER_DEPOSITS = "2100"; // Liability
    private static final String ACCOUNT_SAVINGS_DEPOSITS = "2110"; // Liability
    private static final String ACCOUNT_FIXED_DEPOSITS = "2120"; // Liability
    private static final String ACCOUNT_INTEREST_EXPENSE = "5100"; // Expense
    private static final String ACCOUNT_INTEREST_PAYABLE = "2200"; // Liability

    /**
     * Record customer deposit (Cash/Bank/M-PESA)
     * DR: Cash/Bank/M-PESA (Asset increases)
     * CR: Customer Deposits (Liability increases)
     */
    @Transactional
    public JournalEntry recordCustomerDeposit(
        Long customerId,
        Long accountId,
        BigDecimal amount,
        String depositMethod, // CASH, BANK, MPESA
        String accountType, // SAVINGS, CURRENT, FIXED
        String referenceNumber,
        String description,
        String postedBy
    ) {
        try {
            log.info("Creating journal entry for customer deposit: Customer {}, Amount: {}",
                customerId, amount);

            JournalEntry journalEntry = createJournalEntry(
                "DEP",
                "Customer deposit - " + accountType + " - Customer: " + customerId + " - " + description,
                referenceNumber,
                "CUSTOMER_DEPOSIT",
                accountId,
                postedBy
            );

            List<JournalEntryLine> lines = new ArrayList<>();

            // DR: Cash/Bank/M-PESA (Money received)
            String cashAccount = getCashAccount(depositMethod);
            lines.add(createLine(
                journalEntry,
                cashAccount,
                JournalEntryLine.EntryType.DEBIT,
                amount,
                "Deposit received via " + depositMethod,
                referenceNumber
            ));

            // CR: Customer Deposits (Liability to customer)
            String depositAccount = getDepositAccount(accountType);
            lines.add(createLine(
                journalEntry,
                depositAccount,
                JournalEntryLine.EntryType.CREDIT,
                amount,
                "Customer " + customerId + " deposit",
                String.valueOf(accountId)
            ));

            journalEntry.setLines(lines);
            journalEntry.calculateTotals();

            JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
            log.info("✅ Customer deposit journal entry created: {}", savedEntry.getId());

            return savedEntry;

        } catch (Exception e) {
            log.error("Error creating customer deposit journal entry", e);
            throw new RuntimeException("Failed to create accounting entry for deposit", e);
        }
    }

    /**
     * Record customer withdrawal
     * DR: Customer Deposits (Liability decreases)
     * CR: Cash/Bank/M-PESA (Asset decreases)
     */
    @Transactional
    public JournalEntry recordCustomerWithdrawal(
        Long customerId,
        Long accountId,
        BigDecimal amount,
        String withdrawalMethod, // CASH, BANK, MPESA
        String accountType,
        String referenceNumber,
        String description,
        String postedBy
    ) {
        try {
            log.info("Creating journal entry for customer withdrawal: Customer {}, Amount: {}",
                customerId, amount);

            JournalEntry journalEntry = createJournalEntry(
                "WDR",
                "Customer withdrawal - " + accountType + " - Customer: " + customerId + " - " + description,
                referenceNumber,
                "CUSTOMER_WITHDRAWAL",
                accountId,
                postedBy
            );

            List<JournalEntryLine> lines = new ArrayList<>();

            // DR: Customer Deposits (Reduce liability)
            String depositAccount = getDepositAccount(accountType);
            lines.add(createLine(
                journalEntry,
                depositAccount,
                JournalEntryLine.EntryType.DEBIT,
                amount,
                "Customer " + customerId + " withdrawal",
                String.valueOf(accountId)
            ));

            // CR: Cash/Bank/M-PESA (Money paid out)
            String cashAccount = getCashAccount(withdrawalMethod);
            lines.add(createLine(
                journalEntry,
                cashAccount,
                JournalEntryLine.EntryType.CREDIT,
                amount,
                "Withdrawal paid via " + withdrawalMethod,
                referenceNumber
            ));

            journalEntry.setLines(lines);
            journalEntry.calculateTotals();

            JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
            log.info("✅ Customer withdrawal journal entry created: {}", savedEntry.getId());

            return savedEntry;

        } catch (Exception e) {
            log.error("Error creating customer withdrawal journal entry", e);
            throw new RuntimeException("Failed to create accounting entry for withdrawal", e);
        }
    }

    /**
     * Record account transfer (between customer accounts)
     * DR: From Account Deposits
     * CR: To Account Deposits
     */
    @Transactional
    public JournalEntry recordAccountTransfer(
        Long fromCustomerId,
        Long fromAccountId,
        Long toCustomerId,
        Long toAccountId,
        BigDecimal amount,
        String referenceNumber,
        String description,
        String postedBy
    ) {
        try {
            log.info("Creating journal entry for account transfer: From {} to {}, Amount: {}",
                fromAccountId, toAccountId, amount);

            JournalEntry journalEntry = createJournalEntry(
                "TRF",
                "Account transfer - From: " + fromAccountId + " To: " + toAccountId + " - " + description,
                referenceNumber,
                "ACCOUNT_TRANSFER",
                fromAccountId,
                postedBy
            );

            List<JournalEntryLine> lines = new ArrayList<>();

            // DR: From Account (Reduce liability to sender)
            lines.add(createLine(
                journalEntry,
                ACCOUNT_CUSTOMER_DEPOSITS,
                JournalEntryLine.EntryType.DEBIT,
                amount,
                "Transfer from customer " + fromCustomerId,
                String.valueOf(fromAccountId)
            ));

            // CR: To Account (Increase liability to receiver)
            lines.add(createLine(
                journalEntry,
                ACCOUNT_CUSTOMER_DEPOSITS,
                JournalEntryLine.EntryType.CREDIT,
                amount,
                "Transfer to customer " + toCustomerId,
                String.valueOf(toAccountId)
            ));

            journalEntry.setLines(lines);
            journalEntry.calculateTotals();

            JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
            log.info("✅ Account transfer journal entry created: {}", savedEntry.getId());

            return savedEntry;

        } catch (Exception e) {
            log.error("Error creating account transfer journal entry", e);
            throw new RuntimeException("Failed to create accounting entry for transfer", e);
        }
    }

    /**
     * Record interest payment to customer
     * DR: Interest Expense
     * CR: Customer Deposits (or Interest Payable)
     */
    @Transactional
    public JournalEntry recordInterestPayment(
        Long customerId,
        Long accountId,
        BigDecimal interestAmount,
        String period, // e.g., "January 2024"
        String postedBy
    ) {
        try {
            log.info("Creating journal entry for interest payment: Customer {}, Amount: {}",
                customerId, interestAmount);

            JournalEntry journalEntry = createJournalEntry(
                "INT",
                "Interest payment - " + period + " - Customer: " + customerId,
                "INT-" + accountId + "-" + period,
                "INTEREST_PAYMENT",
                accountId,
                postedBy
            );

            List<JournalEntryLine> lines = new ArrayList<>();

            // DR: Interest Expense
            lines.add(createLine(
                journalEntry,
                ACCOUNT_INTEREST_EXPENSE,
                JournalEntryLine.EntryType.DEBIT,
                interestAmount,
                "Interest expense for " + period,
                String.valueOf(accountId)
            ));

            // CR: Customer Deposits (Credit their account)
            lines.add(createLine(
                journalEntry,
                ACCOUNT_CUSTOMER_DEPOSITS,
                JournalEntryLine.EntryType.CREDIT,
                interestAmount,
                "Interest credited to customer " + customerId,
                String.valueOf(accountId)
            ));

            journalEntry.setLines(lines);
            journalEntry.calculateTotals();

            JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
            log.info("✅ Interest payment journal entry created: {}", savedEntry.getId());

            return savedEntry;

        } catch (Exception e) {
            log.error("Error creating interest payment journal entry", e);
            throw new RuntimeException("Failed to create accounting entry for interest payment", e);
        }
    }

    /**
     * Create journal entry template
     */
    private JournalEntry createJournalEntry(
        String prefix,
        String description,
        String reference,
        String sourceDoc,
        Long sourceId,
        String postedBy
    ) {
        JournalEntry entry = new JournalEntry();
        entry.setDescription(description);
        entry.setJournalType(JournalEntry.JournalType.GENERAL);
        entry.setTransactionDate(LocalDate.now());
        entry.setPostedBy(postedBy);
        return entry;
    }

    /**
     * Create journal entry line
     */
    private JournalEntryLine createLine(
        JournalEntry entry,
        String accountCode,
        JournalEntryLine.EntryType type,
        BigDecimal amount,
        String description,
        String reference
    ) {
        ChartOfAccounts account = chartOfAccountsRepository.findByAccountCode(accountCode)
            .orElseGet(() -> createDefaultAccount(accountCode));

        JournalEntryLine line = new JournalEntryLine();
        line.setJournalEntry(entry);
        line.setType(type);
        line.setAmount(amount.doubleValue());
        line.setDescription(description);
        return line;
    }

    /**
     * Get cash/bank account based on method
     */
    private String getCashAccount(String method) {
        if (method == null) return ACCOUNT_CASH;
        
        return switch (method.toUpperCase()) {
            case "MPESA", "M-PESA", "MOBILE_MONEY" -> ACCOUNT_MPESA;
            case "BANK", "BANK_TRANSFER", "CHEQUE" -> ACCOUNT_BANK;
            default -> ACCOUNT_CASH;
        };
    }

    /**
     * Get deposit account based on type
     */
    private String getDepositAccount(String accountType) {
        if (accountType == null) return ACCOUNT_CUSTOMER_DEPOSITS;
        
        return switch (accountType.toUpperCase()) {
            case "SAVINGS" -> ACCOUNT_SAVINGS_DEPOSITS;
            case "FIXED", "FIXED_DEPOSIT", "FD" -> ACCOUNT_FIXED_DEPOSITS;
            default -> ACCOUNT_CUSTOMER_DEPOSITS;
        };
    }

    /**
     * Create default account if not exists
     */
    private ChartOfAccounts createDefaultAccount(String accountCode) {
        log.warn("Account not found, creating default: {}", accountCode);
        
        ChartOfAccounts account = new ChartOfAccounts();
        account.setAccountCode(accountCode);
        account.setAccountName(getDefaultAccountName(accountCode));
        account.setAccountType(getDefaultAccountType(accountCode));
        account.setIsActive(true);
        account.setCreatedAt(LocalDateTime.now());
        
        return chartOfAccountsRepository.save(account);
    }

    private String getDefaultAccountName(String code) {
        return switch (code) {
            case ACCOUNT_CASH -> "Cash";
            case ACCOUNT_BANK -> "Bank Account";
            case ACCOUNT_MPESA -> "M-PESA Account";
            case ACCOUNT_CUSTOMER_DEPOSITS -> "Customer Deposits";
            case ACCOUNT_SAVINGS_DEPOSITS -> "Savings Deposits";
            case ACCOUNT_FIXED_DEPOSITS -> "Fixed Deposits";
            case ACCOUNT_INTEREST_EXPENSE -> "Interest Expense";
            case ACCOUNT_INTEREST_PAYABLE -> "Interest Payable";
            default -> "Account " + code;
        };
    }

    private ChartOfAccounts.AccountType getDefaultAccountType(String code) {
        if (code.startsWith("1")) return ChartOfAccounts.AccountType.ASSET;
        if (code.startsWith("2")) return ChartOfAccounts.AccountType.LIABILITY;
        if (code.startsWith("3")) return ChartOfAccounts.AccountType.EQUITY;
        if (code.startsWith("4")) return ChartOfAccounts.AccountType.REVENUE;
        if (code.startsWith("5") || code.startsWith("6")) return ChartOfAccounts.AccountType.EXPENSE;
        return ChartOfAccounts.AccountType.ASSET;
    }
}
