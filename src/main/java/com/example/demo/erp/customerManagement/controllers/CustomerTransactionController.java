package com.example.demo.erp.customerManagement.controllers;

import com.example.demo.finance.payments.entities.MpesaTransaction;
import com.example.demo.finance.payments.repositories.MpesaTransactionRepository;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.services.CustomerS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

/**
 * Controller for customer-specific transaction operations
 * Provides filtered views of deposits, withdrawals, and transaction history
 */
@RestController
@RequestMapping("/api/customers/{customerId}/transactions")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
@Tag(name = "Customer Transactions", description = "Customer transaction history and filtering")
@Slf4j
public class CustomerTransactionController {

    private final MpesaTransactionRepository mpesaTransactionRepository;
    private final CustomerS customerService;

    public CustomerTransactionController(
            MpesaTransactionRepository mpesaTransactionRepository,
            CustomerS customerService
    ) {
        this.mpesaTransactionRepository = mpesaTransactionRepository;
        this.customerService = customerService;
    }

    /**
     * Get all transactions for a customer
     */
    @GetMapping
    @Operation(summary = "Get all transactions for a customer")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'CUSTOMER_READ', 'ADMIN_ACCESS')")
    public ResponseEntity<?> getCustomerTransactions(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found"));
            }

            Customer customer = customerOpt.get();
            
            // Get all transactions for customer ordered by most recent
            List<MpesaTransaction> allTransactions = mpesaTransactionRepository
                    .findRecentTransactionsByCustomer(customerId);
            
            // Apply pagination manually
            int start = page * size;
            int end = Math.min(start + size, allTransactions.size());
            List<MpesaTransaction> paginatedTransactions = allTransactions.subList(
                    Math.min(start, allTransactions.size()), 
                    Math.min(end, allTransactions.size())
            );

            return ResponseEntity.ok(Map.of(
                    "transactions", paginatedTransactions,
                    "totalElements", allTransactions.size(),
                    "totalPages", (int) Math.ceil((double) allTransactions.size() / size),
                    "currentPage", page
            ));
        } catch (Exception e) {
            log.error("Error fetching transactions for customer {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch transactions"));
        }
    }

    /**
     * Get customer deposits only
     */
    @GetMapping("/deposits")
    @Operation(summary = "Get customer deposit transactions")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'CUSTOMER_READ', 'ADMIN_ACCESS')")
    public ResponseEntity<?> getCustomerDeposits(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found"));
            }

            Customer customer = customerOpt.get();
            
            // Get all transactions for customer
            List<MpesaTransaction> allTransactions = mpesaTransactionRepository
                    .findRecentTransactionsByCustomer(customerId);

            // Filter for successful DEPOSIT transactions
            List<MpesaTransaction> deposits = allTransactions.stream()
                    .filter(t -> t.getTransactionType() == MpesaTransaction.TransactionType.DEPOSIT && 
                                 t.getStatus() == MpesaTransaction.TransactionStatus.SUCCESS)
                    .limit(limit)
                    .collect(Collectors.toList());

            // Calculate total deposits
            double totalDeposits = deposits.stream()
                    .mapToDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                    .sum();

            return ResponseEntity.ok(Map.of(
                    "deposits", deposits,
                    "totalDeposits", totalDeposits,
                    "count", deposits.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching deposits for customer {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch deposits"));
        }
    }

    /**
     * Get customer withdrawals only
     */
    @GetMapping("/withdrawals")
    @Operation(summary = "Get customer withdrawal transactions")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'CUSTOMER_READ', 'ADMIN_ACCESS')")
    public ResponseEntity<?> getCustomerWithdrawals(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found"));
            }

            Customer customer = customerOpt.get();
            
            // Get all transactions for customer
            List<MpesaTransaction> allTransactions = mpesaTransactionRepository
                    .findRecentTransactionsByCustomer(customerId);

            // Filter for successful WITHDRAWAL transactions
            List<MpesaTransaction> withdrawals = allTransactions.stream()
                    .filter(t -> t.getTransactionType() == MpesaTransaction.TransactionType.WITHDRAWAL && 
                                 t.getStatus() == MpesaTransaction.TransactionStatus.SUCCESS)
                    .limit(limit)
                    .collect(Collectors.toList());

            // Calculate total withdrawals
            double totalWithdrawals = withdrawals.stream()
                    .mapToDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                    .sum();

            return ResponseEntity.ok(Map.of(
                    "withdrawals", withdrawals,
                    "totalWithdrawals", totalWithdrawals,
                    "count", withdrawals.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching withdrawals for customer {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch withdrawals"));
        }
    }

    /**
     * Get transaction summary for customer
     */
    @GetMapping("/summary")
    @Operation(summary = "Get customer transaction summary")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'CUSTOMER_READ', 'ADMIN_ACCESS')")
    public ResponseEntity<?> getTransactionSummary(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "30") int days
    ) {
        try {
            Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found"));
            }

            Customer customer = customerOpt.get();
            
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            LocalDateTime now = LocalDateTime.now();
            
            // Get transactions within date range
            List<MpesaTransaction> recentTransactions = mpesaTransactionRepository
                    .findByCustomerIdAndCreatedAtBetween(customerId, cutoffDate, now)
                    .stream()
                    .filter(t -> t.getStatus() == MpesaTransaction.TransactionStatus.SUCCESS)
                    .collect(Collectors.toList());

            double totalDeposits = recentTransactions.stream()
                    .filter(t -> t.getTransactionType() == MpesaTransaction.TransactionType.DEPOSIT)
                    .mapToDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                    .sum();

            double totalWithdrawals = recentTransactions.stream()
                    .filter(t -> t.getTransactionType() == MpesaTransaction.TransactionType.WITHDRAWAL)
                    .mapToDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0.0)
                    .sum();

            int depositCount = (int) recentTransactions.stream()
                    .filter(t -> t.getTransactionType() == MpesaTransaction.TransactionType.DEPOSIT)
                    .count();

            int withdrawalCount = (int) recentTransactions.stream()
                    .filter(t -> t.getTransactionType() == MpesaTransaction.TransactionType.WITHDRAWAL)
                    .count();

            return ResponseEntity.ok(Map.of(
                    "period", days + " days",
                    "totalDeposits", totalDeposits,
                    "totalWithdrawals", totalWithdrawals,
                    "depositCount", depositCount,
                    "withdrawalCount", withdrawalCount,
                    "netFlow", totalDeposits - totalWithdrawals,
                    "totalTransactions", recentTransactions.size()
            ));
        } catch (Exception e) {
            log.error("Error generating transaction summary for customer {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate summary"));
        }
    }
}
