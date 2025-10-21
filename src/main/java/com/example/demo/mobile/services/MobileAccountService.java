package com.example.demo.mobile.services;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.mobile.dto.*;
import com.example.demo.savingsManagement.persistence.entities.SavingsAccount;
import com.example.demo.savingsManagement.persistence.repositories.SavingsAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileAccountService {

    private final SavingsAccountRepository savingsAccountRepository;
    private final CustomerRepository customerRepository;

    public List<AccountSummaryDto> getMemberAccounts(String memberId) {
        log.info("Fetching accounts for member: {}", memberId);
        
        List<SavingsAccount> savingsAccounts = savingsAccountRepository.findByCustomerId(Long.valueOf(memberId));
        
        return savingsAccounts.stream()
                .map(this::convertToAccountSummary)
                .collect(Collectors.toList());
    }

    public BalanceDto getAccountBalance(String accountId, String memberId) {
        log.info("Fetching balance for account: {}", accountId);
        
        SavingsAccount account = savingsAccountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Verify ownership
        if (!account.getCustomerId().equals(Long.valueOf(memberId))) {
            throw new RuntimeException("Unauthorized access to account");
        }
        
        return BalanceDto.builder()
                .accountId(account.getId().toString())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .currentBalance(account.getBalance())
                .availableBalance(account.getBalance())
                .pendingDebits(BigDecimal.ZERO)
                .pendingCredits(BigDecimal.ZERO)
                .currency("KES")
                .asOfDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    public Page<TransactionDto> getAccountStatement(String accountId, String memberId, 
                                                    String startDate, String endDate, Pageable pageable) {
        log.info("Fetching statement for account: {}", accountId);
        
        SavingsAccount account = savingsAccountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getCustomerId().equals(Long.valueOf(memberId))) {
            throw new RuntimeException("Unauthorized access");
        }
        
        List<TransactionDto> transactions = generateMockTransactions(accountId);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), transactions.size());
        
        return new PageImpl<>(transactions.subList(start, end), pageable, transactions.size());
    }

    public List<TransactionDto> getMiniStatement(String accountId, String memberId) {
        log.info("Fetching mini statement for account: {}", accountId);
        
        SavingsAccount account = savingsAccountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getCustomerId().equals(Long.valueOf(memberId))) {
            throw new RuntimeException("Unauthorized access");
        }
        
        List<TransactionDto> transactions = generateMockTransactions(accountId);
        return transactions.stream().limit(5).collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponseDto makeDeposit(String accountId, String memberId, DepositRequest request) {
        log.info("Processing deposit for account: {}, amount: {}", accountId, request.getAmount());
        
        Customer customer = customerRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (!verifyPin(request.getPin(), customer.getPinHash())) {
            throw new RuntimeException("Invalid PIN");
        }
        
        SavingsAccount account = savingsAccountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getCustomerId().equals(Long.valueOf(memberId))) {
            throw new RuntimeException("Unauthorized access");
        }
        
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        savingsAccountRepository.save(account);
        
        String transactionRef = "DEP" + System.currentTimeMillis();
        
        log.info("Deposit successful. New balance: {}", newBalance);
        
        return TransactionResponseDto.builder()
                .success(true)
                .transactionId(UUID.randomUUID().toString())
                .transactionRef(transactionRef)
                .message("Deposit successful")
                .amount(request.getAmount())
                .newBalance(newBalance)
                .transactionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .receiptNumber(transactionRef)
                .build();
    }

    @Transactional
    public TransactionResponseDto makeWithdrawal(String accountId, String memberId, WithdrawalRequest request) {
        log.info("Processing withdrawal for account: {}, amount: {}", accountId, request.getAmount());
        
        Customer customer = customerRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (!verifyPin(request.getPin(), customer.getPinHash())) {
            throw new RuntimeException("Invalid PIN");
        }
        
        SavingsAccount account = savingsAccountRepository.findById(Long.valueOf(accountId))
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getCustomerId().equals(Long.valueOf(memberId))) {
            throw new RuntimeException("Unauthorized access");
        }
        
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        savingsAccountRepository.save(account);
        
        String transactionRef = "WDR" + System.currentTimeMillis();
        
        log.info("Withdrawal successful. New balance: {}", newBalance);
        
        return TransactionResponseDto.builder()
                .success(true)
                .transactionId(UUID.randomUUID().toString())
                .transactionRef(transactionRef)
                .message("Withdrawal successful")
                .amount(request.getAmount())
                .newBalance(newBalance)
                .transactionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .receiptNumber(transactionRef)
                .build();
    }

    @Transactional
    public TransactionResponseDto transferFunds(String memberId, TransferRequest request) {
        log.info("Processing transfer from {} to {}, amount: {}", 
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        
        Customer customer = customerRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (!verifyPin(request.getPin(), customer.getPinHash())) {
            throw new RuntimeException("Invalid PIN");
        }
        
        SavingsAccount fromAccount = savingsAccountRepository.findById(Long.valueOf(request.getFromAccountId()))
                .orElseThrow(() -> new RuntimeException("Source account not found"));
        
        if (!fromAccount.getCustomerId().equals(Long.valueOf(memberId))) {
            throw new RuntimeException("Unauthorized access to source account");
        }
        
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        SavingsAccount toAccount = savingsAccountRepository.findById(Long.valueOf(request.getToAccountId()))
                .orElseThrow(() -> new RuntimeException("Destination account not found"));
        
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        
        savingsAccountRepository.save(fromAccount);
        savingsAccountRepository.save(toAccount);
        
        String transactionRef = "TRF" + System.currentTimeMillis();
        
        log.info("Transfer successful. New balance: {}", fromAccount.getBalance());
        
        return TransactionResponseDto.builder()
                .success(true)
                .transactionId(UUID.randomUUID().toString())
                .transactionRef(transactionRef)
                .message("Transfer successful")
                .amount(request.getAmount())
                .newBalance(fromAccount.getBalance())
                .transactionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .receiptNumber(transactionRef)
                .build();
    }

    private AccountSummaryDto convertToAccountSummary(SavingsAccount account) {
        return AccountSummaryDto.builder()
                .accountId(account.getId().toString())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .accountName(account.getAccountType() + " Account")
                .balance(account.getBalance())
                .availableBalance(account.getBalance())
                .currency("KES")
                .status(account.getStatus())
                .lastTransactionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build();
    }

    private List<TransactionDto> generateMockTransactions(String accountId) {
        List<TransactionDto> transactions = new ArrayList<>();
        BigDecimal balance = BigDecimal.valueOf(45000);
        
        for (int i = 0; i < 10; i++) {
            BigDecimal amount = BigDecimal.valueOf((i + 1) * 1000);
            balance = balance.add(amount);
            
            transactions.add(TransactionDto.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .transactionDate(LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .transactionType(i % 2 == 0 ? "DEPOSIT" : "WITHDRAWAL")
                    .description("Transaction " + (i + 1))
                    .amount(amount)
                    .debitCredit(i % 2 == 0 ? "CR" : "DR")
                    .balanceBefore(balance.subtract(amount))
                    .balanceAfter(balance)
                    .reference("TXN" + System.currentTimeMillis() + i)
                    .status("COMPLETED")
                    .channel("MOBILE")
                    .build());
        }
        
        return transactions;
    }

    private boolean verifyPin(String pin, String hashedPin) {
        if (hashedPin == null) return false;
        return BCrypt.checkpw(pin, hashedPin);
    }
}
