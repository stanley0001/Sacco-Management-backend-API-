package com.example.demo.payments.services;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.banking.parsitence.enitities.Transactions;
import com.example.demo.banking.parsitence.repositories.BankAccountRepo;
import com.example.demo.banking.parsitence.repositories.PaymentTransactionRepo;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.serviceImplimentations.CustomerService;
import com.example.demo.loanManagement.parsistence.entities.SuspensePayments;
import com.example.demo.loanManagement.parsistence.repositories.SuspensePaymentRepo;
import com.example.demo.payments.entities.MpesaTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling deposits to Bank Accounts (ALPHA, SHARES, SAVINGS)
 * Follows the banking module logic for member account deposits
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankDepositService {

    private final BankAccountRepo bankAccountRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final CustomerService customerService;
    private final SuspensePaymentRepo suspensePaymentRepo;

    /**
     * Process deposit to bank account (ALPHA, SHARES, or SAVINGS)
     * If no account found, record as suspense
     */
    @Transactional
    public Transactions processDeposit(Long customerId, BigDecimal amount, String accountType, 
                                       String referenceNumber, String paymentMethod) {
        log.info("Processing bank deposit: Customer={}, Amount={}, AccountType={}", 
                customerId, amount, accountType);

        try {
            // Get customer
            Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
            if (customerOpt.isEmpty()) {
                log.warn("Customer {} not found. Recording as suspense.", customerId);
                recordSuspenseDeposit(customerId.toString(), amount, referenceNumber, "CUSTOMER_NOT_FOUND");
                throw new RuntimeException("Customer not found: " + customerId);
            }

            Customer customer = customerOpt.get();

            // Get customer bank accounts
            Optional<List<BankAccounts>> accountsOpt = bankAccountRepo.findByCustomer(customer);
            if (accountsOpt.isEmpty() || accountsOpt.get().isEmpty()) {
                log.warn("No bank accounts found for customer {}. Recording as suspense.", customerId);
                recordSuspenseDeposit(customer.getPhoneNumber(), amount, referenceNumber, "NO_BANK_ACCOUNTS");
                throw new RuntimeException("No bank accounts found for customer: " + customerId);
            }

            // Determine target account based on type, default to SAVINGS
            BankAccounts targetAccount = findAccountByType(accountsOpt.get(), accountType);
            if (targetAccount == null) {
                log.warn("Account type {} not found for customer {}. Using SAVINGS.", accountType, customerId);
                targetAccount = findAccountByType(accountsOpt.get(), "SAVINGS");
            }

            if (targetAccount == null) {
                log.error("No suitable account found for customer {}. Recording as suspense.", customerId);
                recordSuspenseDeposit(customer.getPhoneNumber(), amount, referenceNumber, "NO_SUITABLE_ACCOUNT");
                throw new RuntimeException("No suitable account found for deposit");
            }

            // Create and save transaction
            return saveDepositTransaction(targetAccount, amount, referenceNumber, paymentMethod);

        } catch (Exception e) {
            log.error("Error processing deposit: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Process M-PESA deposit to bank account
     * Simple deposit - no distribution logic
     */
    @Transactional
    public Transactions processMpesaDeposit(MpesaTransaction mpesaTransaction) {
        log.info("Processing M-PESA deposit for customer: {}", mpesaTransaction.getCustomerId());

        try {
            // Get customer
            Optional<Customer> customerOpt = customerService.findCustomerById(mpesaTransaction.getCustomerId());
            if (customerOpt.isEmpty()) {
                log.warn("Customer {} not found. Recording as suspense.", mpesaTransaction.getCustomerId());
                recordSuspenseDeposit(
                    mpesaTransaction.getPhoneNumber(),
                    mpesaTransaction.getAmount(),
                    mpesaTransaction.getMpesaReceiptNumber(),
                    "CUSTOMER_NOT_FOUND"
                );
                throw new RuntimeException("Customer not found: " + mpesaTransaction.getCustomerId());
            }

            Customer customer = customerOpt.get();

            // Get customer bank accounts
            Optional<List<BankAccounts>> accountsOpt = bankAccountRepo.findByCustomer(customer);
            if (accountsOpt.isEmpty() || accountsOpt.get().isEmpty()) {
                log.warn("No bank accounts found for customer {}. Recording as suspense.", customer.getId());
                recordSuspenseDeposit(
                    mpesaTransaction.getPhoneNumber(),
                    mpesaTransaction.getAmount(),
                    mpesaTransaction.getMpesaReceiptNumber(),
                    "NO_BANK_ACCOUNTS"
                );
                throw new RuntimeException("No bank accounts found for customer: " + customer.getId());
            }

            // Determine target account
            BankAccounts targetAccount = null;
            
            // If specific bank account ID provided, use it
            if (mpesaTransaction.getBankAccountId() != null) {
                targetAccount = bankAccountRepo.findById(mpesaTransaction.getBankAccountId())
                    .orElse(null);
            }
            
            // If no specific account, default to SAVINGS, then ALPHA, then first available
            if (targetAccount == null) {
                targetAccount = findAccountByType(accountsOpt.get(), "SAVINGS");
                if (targetAccount == null) {
                    targetAccount = findAccountByType(accountsOpt.get(), "ALPHA");
                }
                if (targetAccount == null) {
                    targetAccount = accountsOpt.get().get(0); // Use first available account
                }
            }

            if (targetAccount == null) {
                log.error("No suitable account found. Recording as suspense.");
                recordSuspenseDeposit(
                    mpesaTransaction.getPhoneNumber(),
                    mpesaTransaction.getAmount(),
                    mpesaTransaction.getMpesaReceiptNumber(),
                    "NO_SUITABLE_ACCOUNT"
                );
                throw new RuntimeException("No suitable account found for deposit");
            }

            // Create simple deposit transaction
            String receiptNumber = mpesaTransaction.getMpesaReceiptNumber();
            return saveDepositTransaction(targetAccount, mpesaTransaction.getAmount(), receiptNumber, "DEPOSIT");

        } catch (Exception e) {
            log.error("Error processing M-PESA deposit: {}", e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Find account by type from list
     */
    private BankAccounts findAccountByType(List<BankAccounts> accounts, String accountType) {
        if (accountType == null) {
            return null;
        }
        return accounts.stream()
            .filter(acc -> accountType.equalsIgnoreCase(acc.getAccountType()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Save deposit transaction and update account balance
     */
    private Transactions saveDepositTransaction(BankAccounts account, BigDecimal amount, 
                                                String referenceNumber, String transactionType) {
        double openingBalance = account.getAccountBalance() != null ? account.getAccountBalance() : 0.0;
        double depositAmount = amount.doubleValue();
        double closingBalance = openingBalance + depositAmount;

        Transactions transaction = new Transactions();
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setTransactionType(transactionType != null ? transactionType : "DEPOSIT");
        transaction.setAmount(depositAmount);
        transaction.setOpeningBalance(openingBalance);
        transaction.setClosingBalance(closingBalance);
        transaction.setOtherRef(referenceNumber);
        transaction.setBankAccount(account);

        Transactions saved = paymentTransactionRepo.save(transaction);

        // Update account balance
        account.setAccountBalance(closingBalance);
        account.setUpdatedAt(LocalDateTime.now());
        bankAccountRepo.save(account);

        log.info("Deposit saved: Account={}, Type={}, Amount={}, NewBalance={}", 
                account.getBankAccount(), account.getAccountType(), amount, closingBalance);

        return saved;
    }

    /**
     * Record failed deposit as suspense for later reconciliation
     */
    private void recordSuspenseDeposit(String phoneNumber, BigDecimal amount, 
                                      String referenceNumber, String exceptionType) {
        try {
            SuspensePayments suspense = new SuspensePayments();
            suspense.setAccountNumber(phoneNumber);
            suspense.setAmount(amount.toString());
            suspense.setStatus("NEW");
            suspense.setOtherRef(referenceNumber != null ? referenceNumber : "SUSP-" + System.currentTimeMillis());
            suspense.setExceptionType(exceptionType);
            suspense.setDestinationAccount("NOT_FOUND");
            suspense.setPaymentTime(LocalDateTime.now());

            suspensePaymentRepo.save(suspense);
            log.info("Suspense deposit created: {} - Amount: {} - Type: {}", 
                    suspense.getOtherRef(), suspense.getAmount(), exceptionType);

        } catch (Exception e) {
            log.error("Failed to create suspense deposit record: {}", e.getMessage(), e);
        }
    }

    /**
     * Get bank accounts for a customer
     */
    public List<BankAccounts> getCustomerBankAccounts(Long customerId) {
        Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Customer not found: " + customerId);
        }

        Optional<List<BankAccounts>> accountsOpt = bankAccountRepo.findByCustomer(customerOpt.get());
        return accountsOpt.orElse(List.of());
    }

    /**
     * Get specific bank account by customer and type
     */
    public BankAccounts getCustomerAccount(Long customerId, String accountType) {
        List<BankAccounts> accounts = getCustomerBankAccounts(customerId);
        return findAccountByType(accounts, accountType);
    }
}
