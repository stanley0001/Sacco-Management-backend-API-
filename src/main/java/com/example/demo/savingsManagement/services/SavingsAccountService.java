package com.example.demo.savingsManagement.services;

import com.example.demo.savingsManagement.persistence.entities.SavingsAccount;
import com.example.demo.savingsManagement.persistence.entities.SavingsProduct;
import com.example.demo.savingsManagement.persistence.entities.SavingsTransaction;
import com.example.demo.savingsManagement.persistence.repositories.SavingsAccountRepository;
import com.example.demo.savingsManagement.persistence.repositories.SavingsProductRepository;
import com.example.demo.savingsManagement.persistence.repositories.SavingsTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsAccountService {

    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsTransactionRepository transactionRepository;
    private final SavingsProductRepository productRepository;

    @Transactional
    public SavingsAccount createSavingsAccount(SavingsAccount account) {
        log.info("Creating savings account for customer: {}", account.getCustomerId());
        
        // Validate product exists
        SavingsProduct product = productRepository.findByCode(account.getProductCode())
                .orElseThrow(() -> new RuntimeException("Savings product not found: " + account.getProductCode()));
        
        if (!product.getIsActive()) {
            throw new RuntimeException("Savings product is not active: " + account.getProductCode());
        }
        
        // Generate unique account number
        account.setAccountNumber(generateAccountNumber());
        account.setProductName(product.getName());
        account.setInterestRate(product.getInterestRate());
        account.setMinimumBalance(product.getMinimumBalance());
        account.setStatus("ACTIVE");
        account.setOpenedDate(LocalDateTime.now());
        
        return savingsAccountRepository.save(account);
    }

    @Transactional
    public SavingsTransaction deposit(Long accountId, BigDecimal amount, String paymentMethod, 
                                      String paymentReference, String description, String postedBy) {
        log.info("Processing deposit of {} to account: {}", amount, accountId);
        
        SavingsAccount account = savingsAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Savings account not found: " + accountId));
        
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active. Status: " + account.getStatus());
        }
        
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal newBalance = balanceBefore.add(amount);
        
        account.setBalance(newBalance);
        account.setAvailableBalance(newBalance);
        account.setLastTransactionDate(LocalDateTime.now());
        savingsAccountRepository.save(account);
        
        SavingsTransaction transaction = new SavingsTransaction();
        transaction.setSavingsAccountId(accountId);
        transaction.setTransactionRef(generateTransactionRef());
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(newBalance);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setPaymentReference(paymentReference);
        transaction.setDescription(description);
        transaction.setPostedBy(postedBy);
        transaction.setStatus("COMPLETED");
        
        return transactionRepository.save(transaction);
    }

    @Transactional
    public SavingsTransaction withdraw(Long accountId, BigDecimal amount, String paymentMethod,
                                       String description, String postedBy) {
        log.info("Processing withdrawal of {} from account: {}", amount, accountId);
        
        SavingsAccount account = savingsAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Savings account not found: " + accountId));
        
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active. Status: " + account.getStatus());
        }
        
        SavingsProduct product = productRepository.findByCode(account.getProductCode())
                .orElseThrow(() -> new RuntimeException("Savings product not found"));
        
        if (!product.getAllowsWithdrawals()) {
            throw new RuntimeException("Withdrawals not allowed for this product");
        }
        
        // Check withdrawal limits
        if (product.getMaxWithdrawalsPerMonth() != null) {
            LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
            Integer withdrawalsThisMonth = transactionRepository.countWithdrawalsSince(accountId, startOfMonth);
            if (withdrawalsThisMonth >= product.getMaxWithdrawalsPerMonth()) {
                throw new RuntimeException("Maximum withdrawals per month exceeded");
            }
        }
        
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal totalDeduction = amount.add(product.getWithdrawalFee());
        BigDecimal newBalance = balanceBefore.subtract(totalDeduction);
        
        if (newBalance.compareTo(account.getMinimumBalance()) < 0) {
            throw new RuntimeException("Insufficient balance. Minimum balance requirement: " + account.getMinimumBalance());
        }
        
        account.setBalance(newBalance);
        account.setAvailableBalance(newBalance);
        account.setLastTransactionDate(LocalDateTime.now());
        savingsAccountRepository.save(account);
        
        SavingsTransaction transaction = new SavingsTransaction();
        transaction.setSavingsAccountId(accountId);
        transaction.setTransactionRef(generateTransactionRef());
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(newBalance);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setDescription(description);
        transaction.setPostedBy(postedBy);
        transaction.setStatus("COMPLETED");
        
        return transactionRepository.save(transaction);
    }

    public SavingsAccount getAccountById(Long accountId) {
        return savingsAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Savings account not found: " + accountId));
    }

    public List<SavingsAccount> getAccountsByCustomerId(Long customerId) {
        return savingsAccountRepository.findByCustomerId(customerId);
    }

    public Page<SavingsAccount> getAllAccounts(Pageable pageable) {
        return savingsAccountRepository.findAll(pageable);
    }

    public List<SavingsTransaction> getAccountTransactions(Long accountId) {
        return transactionRepository.findBySavingsAccountId(accountId);
    }

    public Page<SavingsTransaction> getAccountTransactionsPaged(Long accountId, Pageable pageable) {
        return transactionRepository.findBySavingsAccountId(accountId, pageable);
    }

    @Transactional
    public SavingsAccount updateAccountStatus(Long accountId, String status) {
        SavingsAccount account = getAccountById(accountId);
        account.setStatus(status);
        if ("CLOSED".equals(status)) {
            account.setClosedDate(LocalDateTime.now());
        }
        return savingsAccountRepository.save(account);
    }

    private String generateAccountNumber() {
        return "SAV" + System.currentTimeMillis();
    }

    private String generateTransactionRef() {
        return "TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public BigDecimal getTotalSavingsBalance() {
        BigDecimal total = savingsAccountRepository.getTotalSavingsBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    public Long countActiveAccounts() {
        Long count = savingsAccountRepository.countActiveSavingsAccounts();
        return count != null ? count : 0L;
    }
}
