package com.example.demo.finance.accounting.services;

import com.example.demo.finance.accounting.entities.ChartOfAccounts;
import com.example.demo.finance.accounting.entities.JournalEntry;
import com.example.demo.finance.accounting.entities.JournalEntryLine;
import com.example.demo.finance.accounting.repositories.ChartOfAccountsRepo;
import com.example.demo.finance.accounting.repositories.JournalEntryRepo;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
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
 * Loan Accounting Integration Service
 * Creates journal entries for all loan transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanAccountingIntegrationService {

    private final JournalEntryRepo journalEntryRepository;
    private final ChartOfAccountsRepo chartOfAccountsRepository;

    // Standard Account Codes
    private static final String ACCOUNT_LOANS_RECEIVABLE = "1200"; // Asset
    private static final String ACCOUNT_CASH = "1000"; // Asset
    private static final String ACCOUNT_BANK = "1010"; // Asset
    private static final String ACCOUNT_MPESA = "1020"; // Asset
    private static final String ACCOUNT_INTEREST_INCOME = "4100"; // Revenue
    private static final String ACCOUNT_INTEREST_RECEIVABLE = "1210"; // Asset
    private static final String ACCOUNT_LOAN_PROCESSING_FEE_INCOME = "4110"; // Revenue
    private static final String ACCOUNT_PENALTY_INCOME = "4120"; // Revenue
    private static final String ACCOUNT_UNEARNED_INTEREST = "2300"; // Liability
    private static final String ACCOUNT_LOAN_LOSS_PROVISION = "1299"; // Contra Asset
    private static final String ACCOUNT_LOAN_LOSS_EXPENSE = "6100"; // Expense

    /**
     * Record loan disbursement
     * DR: Loans Receivable (Principal + Interest)
     * CR: Cash/Bank/M-PESA (Disbursed Amount)
     * CR: Unearned Interest (Interest Amount)
     * CR: Loan Processing Fee Income (if applicable)
     */
    @Transactional
    public JournalEntry recordLoanDisbursement(
        LoanAccount loanAccount,
        BigDecimal principalAmount,
        BigDecimal interestAmount,
        BigDecimal processingFee,
        String disbursementMethod, // CASH, BANK, MPESA
        String referenceNumber,
        String postedBy
    ) {
        try {
            log.info("Creating journal entry for loan disbursement: {}", loanAccount.getLoanref());

            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setEntryNumber(generateEntryNumber("LD"));
            journalEntry.setEntryDate(LocalDate.now());
            journalEntry.setTransactionDate(LocalDate.now());
            journalEntry.setJournalType(JournalEntry.JournalType.DISBURSEMENT);
            journalEntry.setDescription(String.format("Loan disbursement - %s - Customer: %s",
                loanAccount.getLoanref(), loanAccount.getCustomerId()));
            journalEntry.setReferenceNumber(referenceNumber);
            journalEntry.setSourceDocument("LOAN_DISBURSEMENT");
            journalEntry.setSourceId(loanAccount.getAccountId());
            journalEntry.setPostedBy(postedBy);
            journalEntry.setIsPosted(true);
            journalEntry.setPostedAt(LocalDateTime.now());

            List<JournalEntryLine> lines = new ArrayList<>();

            // DR: Loans Receivable (Principal + Interest for reducing balance method)
            BigDecimal totalReceivable = principalAmount.add(interestAmount);
            lines.add(createJournalLine(
                journalEntry,
                ACCOUNT_LOANS_RECEIVABLE,
                JournalEntryLine.EntryType.DEBIT,
                totalReceivable,
                "Loan disbursement - Principal and Interest receivable",
                loanAccount.getLoanref()
            ));

            // CR: Cash/Bank/M-PESA (Disbursed Amount - net of processing fee)
            String disbursementAccount = getDisbursementAccount(disbursementMethod);
            BigDecimal netDisbursement = principalAmount.subtract(processingFee != null ? processingFee : BigDecimal.ZERO);
            lines.add(createJournalLine(
                journalEntry,
                disbursementAccount,
                JournalEntryLine.EntryType.CREDIT,
                netDisbursement,
                "Loan disbursement to customer",
                referenceNumber
            ));

            // CR: Unearned Interest (Interest to be earned over loan period)
            lines.add(createJournalLine(
                journalEntry,
                ACCOUNT_UNEARNED_INTEREST,
                JournalEntryLine.EntryType.CREDIT,
                interestAmount,
                "Unearned interest on loan",
                loanAccount.getLoanref()
            ));

            // CR: Loan Processing Fee Income (if applicable)
            if (processingFee != null && processingFee.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(createJournalLine(
                    journalEntry,
                    ACCOUNT_LOAN_PROCESSING_FEE_INCOME,
                    JournalEntryLine.EntryType.CREDIT,
                    processingFee,
                    "Loan processing fee",
                    loanAccount.getLoanref()
                ));
            }

            journalEntry.setLines(lines);
            journalEntry.calculateTotals();

            JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
            log.info("✅ Loan disbursement journal entry created: {}", savedEntry.getEntryNumber());

            return savedEntry;

        } catch (Exception e) {
            log.error("Error creating loan disbursement journal entry", e);
            throw new RuntimeException("Failed to create accounting entry for loan disbursement", e);
        }
    }

    /**
     * Record loan repayment
     * DR: Cash/Bank/M-PESA (Payment Amount)
     * CR: Loans Receivable (Principal Portion)
     * DR: Unearned Interest
     * CR: Interest Income (Interest Portion)
     * CR: Penalty Income (if applicable)
     */
    @Transactional
    public JournalEntry recordLoanRepayment(
        LoanAccount loanAccount,
        BigDecimal paymentAmount,
        BigDecimal principalPortion,
        BigDecimal interestPortion,
        BigDecimal penaltyPortion,
        String paymentMethod, // CASH, BANK, MPESA
        String referenceNumber,
        String postedBy
    ) {
        try {
            log.info("Creating journal entry for loan repayment: {}, Amount: {}",
                loanAccount.getLoanref(), paymentAmount);

            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setEntryNumber(generateEntryNumber("LR"));
            journalEntry.setEntryDate(LocalDate.now());
            journalEntry.setTransactionDate(LocalDate.now());
            journalEntry.setJournalType(JournalEntry.JournalType.PAYMENT);
            journalEntry.setDescription(String.format("Loan repayment - %s - Customer: %s",
                loanAccount.getLoanref(), loanAccount.getCustomerId()));
            journalEntry.setReferenceNumber(referenceNumber);
            journalEntry.setSourceDocument("LOAN_REPAYMENT");
            journalEntry.setSourceId(loanAccount.getAccountId());
            journalEntry.setPostedBy(postedBy);
            journalEntry.setIsPosted(true);
            journalEntry.setPostedAt(LocalDateTime.now());

            List<JournalEntryLine> lines = new ArrayList<>();

            // DR: Cash/Bank/M-PESA (Payment Received)
            String paymentAccount = getDisbursementAccount(paymentMethod);
            lines.add(createJournalLine(
                journalEntry,
                paymentAccount,
                JournalEntryLine.EntryType.DEBIT,
                paymentAmount,
                "Loan repayment received",
                referenceNumber
            ));

            // CR: Loans Receivable (Principal Portion)
            if (principalPortion.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(createJournalLine(
                    journalEntry,
                    ACCOUNT_LOANS_RECEIVABLE,
                    JournalEntryLine.EntryType.CREDIT,
                    principalPortion,
                    "Principal repayment",
                    loanAccount.getLoanref()
                ));
            }

            // Interest Recognition (Transfer from Unearned to Earned)
            if (interestPortion.compareTo(BigDecimal.ZERO) > 0) {
                // DR: Unearned Interest
                lines.add(createJournalLine(
                    journalEntry,
                    ACCOUNT_UNEARNED_INTEREST,
                    JournalEntryLine.EntryType.DEBIT,
                    interestPortion,
                    "Interest earned on repayment",
                    loanAccount.getLoanref()
                ));

                // CR: Interest Income
                lines.add(createJournalLine(
                    journalEntry,
                    ACCOUNT_INTEREST_INCOME,
                    JournalEntryLine.EntryType.CREDIT,
                    interestPortion,
                    "Interest income earned",
                    loanAccount.getLoanref()
                ));
            }

            // CR: Penalty Income (if applicable)
            if (penaltyPortion != null && penaltyPortion.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(createJournalLine(
                    journalEntry,
                    ACCOUNT_PENALTY_INCOME,
                    JournalEntryLine.EntryType.CREDIT,
                    penaltyPortion,
                    "Penalty/Late payment fee",
                    loanAccount.getLoanref()
                ));
            }

            journalEntry.setLines(lines);
            journalEntry.calculateTotals();

            JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
            log.info("✅ Loan repayment journal entry created: {}", savedEntry.getEntryNumber());

            return savedEntry;

        } catch (Exception e) {
            log.error("Error creating loan repayment journal entry", e);
            throw new RuntimeException("Failed to create accounting entry for loan repayment", e);
        }
    }

    /**
     * Record loan write-off (bad debt)
     * DR: Loan Loss Expense
     * CR: Loans Receivable
     * CR: Loan Loss Provision (if exists)
     */
    @Transactional
    public JournalEntry recordLoanWriteOff(
        LoanAccount loanAccount,
        BigDecimal writeOffAmount,
        BigDecimal provisionAmount,
        String reason,
        String postedBy
    ) {
        try {
            log.info("Creating journal entry for loan write-off: {}, Amount: {}",
                loanAccount.getLoanref(), writeOffAmount);

            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setEntryNumber(generateEntryNumber("LW"));
            journalEntry.setEntryDate(LocalDate.now());
            journalEntry.setTransactionDate(LocalDate.now());
            journalEntry.setJournalType(JournalEntry.JournalType.ADJUSTMENT);
            journalEntry.setDescription(String.format("Loan write-off - %s - Reason: %s",
                loanAccount.getLoanref(), reason));
            journalEntry.setReferenceNumber(loanAccount.getLoanref());
            journalEntry.setSourceDocument("LOAN_WRITE_OFF");
            journalEntry.setSourceId(loanAccount.getAccountId());
            journalEntry.setPostedBy(postedBy);
            journalEntry.setIsPosted(true);
            journalEntry.setPostedAt(LocalDateTime.now());

            List<JournalEntryLine> lines = new ArrayList<>();

            // DR: Loan Loss Expense
            BigDecimal expenseAmount = writeOffAmount.subtract(provisionAmount);
            if (expenseAmount.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(createJournalLine(
                    journalEntry,
                    ACCOUNT_LOAN_LOSS_EXPENSE,
                    JournalEntryLine.EntryType.DEBIT,
                    expenseAmount,
                    "Bad debt expense",
                    loanAccount.getLoanref()
                ));
            }

            // DR: Loan Loss Provision (use existing provision)
            if (provisionAmount.compareTo(BigDecimal.ZERO) > 0) {
                lines.add(createJournalLine(
                    journalEntry,
                    ACCOUNT_LOAN_LOSS_PROVISION,
                    JournalEntryLine.EntryType.DEBIT,
                    provisionAmount,
                    "Provision utilized for write-off",
                    loanAccount.getLoanref()
                ));
            }

            // CR: Loans Receivable
            lines.add(createJournalLine(
                journalEntry,
                ACCOUNT_LOANS_RECEIVABLE,
                JournalEntryLine.EntryType.CREDIT,
                writeOffAmount,
                "Loan written off",
                loanAccount.getLoanref()
            ));

            journalEntry.setLines(lines);
            journalEntry.calculateTotals();

            JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
            log.info("✅ Loan write-off journal entry created: {}", savedEntry.getEntryNumber());

            return savedEntry;

        } catch (Exception e) {
            log.error("Error creating loan write-off journal entry", e);
            throw new RuntimeException("Failed to create accounting entry for loan write-off", e);
        }
    }

    /**
     * Create journal entry line
     */
    private JournalEntryLine createJournalLine(
        JournalEntry journalEntry,
        String accountCode,
        JournalEntryLine.EntryType type,
        BigDecimal amount,
        String description,
        String reference
    ) {
        // Find or create account
        ChartOfAccounts account = chartOfAccountsRepository.findByAccountCode(accountCode)
            .orElseGet(() -> createDefaultAccount(accountCode));

        JournalEntryLine line = new JournalEntryLine();
        line.setJournalEntry(journalEntry);
        line.setAccount(account);
        line.setType(type);
        line.setAmount(amount.doubleValue());
        line.setDescription(description);
        line.setReference(reference);

        return line;
    }

    /**
     * Get disbursement/payment account based on method
     */
    private String getDisbursementAccount(String method) {
        if (method == null) return ACCOUNT_CASH;
        
        return switch (method.toUpperCase()) {
            case "MPESA", "M-PESA", "MOBILE_MONEY" -> ACCOUNT_MPESA;
            case "BANK", "BANK_TRANSFER", "CHEQUE" -> ACCOUNT_BANK;
            default -> ACCOUNT_CASH;
        };
    }

    /**
     * Generate unique entry number
     */
    private String generateEntryNumber(String prefix) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp.substring(timestamp.length() - 10);
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
            case ACCOUNT_LOANS_RECEIVABLE -> "Loans Receivable";
            case ACCOUNT_CASH -> "Cash";
            case ACCOUNT_BANK -> "Bank Account";
            case ACCOUNT_MPESA -> "M-PESA Account";
            case ACCOUNT_INTEREST_INCOME -> "Interest Income";
            case ACCOUNT_INTEREST_RECEIVABLE -> "Interest Receivable";
            case ACCOUNT_LOAN_PROCESSING_FEE_INCOME -> "Loan Processing Fee Income";
            case ACCOUNT_PENALTY_INCOME -> "Penalty Income";
            case ACCOUNT_UNEARNED_INTEREST -> "Unearned Interest";
            case ACCOUNT_LOAN_LOSS_PROVISION -> "Loan Loss Provision";
            case ACCOUNT_LOAN_LOSS_EXPENSE -> "Loan Loss Expense";
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
