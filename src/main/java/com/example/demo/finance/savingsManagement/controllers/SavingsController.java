package com.example.demo.finance.savingsManagement.controllers;

import com.example.demo.finance.payments.dto.UniversalPaymentRequest;
import com.example.demo.finance.payments.dto.UniversalPaymentResponse;
import com.example.demo.finance.payments.services.UniversalPaymentService;
import com.example.demo.finance.savingsManagement.persistence.entities.SavingsAccount;
import com.example.demo.finance.savingsManagement.persistence.entities.SavingsProduct;
import com.example.demo.finance.savingsManagement.persistence.entities.SavingsTransaction;
import com.example.demo.finance.savingsManagement.services.SavingsAccountService;
import com.example.demo.finance.savingsManagement.services.SavingsProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Savings Management", description = "APIs for managing savings accounts and transactions")
public class SavingsController {

    private final SavingsAccountService savingsAccountService;
    private final SavingsProductService savingsProductService;
    private final UniversalPaymentService universalPaymentService;

    // ============ SAVINGS ACCOUNTS ============
    
    @PostMapping("/accounts")
    @Operation(summary = "Create new savings account")
    public ResponseEntity<SavingsAccount> createAccount(@RequestBody SavingsAccount account) {
        SavingsAccount created = savingsAccountService.createSavingsAccount(account);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/accounts/{id}")
    @Operation(summary = "Get savings account by ID")
    public ResponseEntity<SavingsAccount> getAccount(@PathVariable Long id) {
        SavingsAccount account = savingsAccountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/accounts")
    @Operation(summary = "Get all savings accounts with pagination")
    public ResponseEntity<Page<SavingsAccount>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SavingsAccount> accounts = savingsAccountService.getAllAccounts(PageRequest.of(page, size));
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/customer/{customerId}")
    @Operation(summary = "Get all savings accounts for a customer")
    public ResponseEntity<List<SavingsAccount>> getCustomerAccounts(@PathVariable Long customerId) {
        List<SavingsAccount> accounts = savingsAccountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/accounts/{id}/status")
    @Operation(summary = "Update account status")
    public ResponseEntity<SavingsAccount> updateAccountStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        SavingsAccount account = savingsAccountService.updateAccountStatus(id, status);
        return ResponseEntity.ok(account);
    }

    // ============ TRANSACTIONS ============
    
    @PostMapping("/accounts/{accountId}/deposit")
    @Operation(summary = "Deposit to savings account with M-PESA integration")
    public ResponseEntity<?> deposit(
            @PathVariable Long accountId,
            @RequestBody DepositRequest request) {
        
        try {
            log.info("🔔 SavingsController deposit called: accountId={}, amount={}, paymentMethod={}, phoneNumber={}", 
                accountId, request.getAmount(), request.getPaymentMethod(), request.getPhoneNumber());
            
            // Check if this is an M-PESA payment
            if ("M-PESA".equalsIgnoreCase(request.getPaymentMethod()) || 
                "MPESA".equalsIgnoreCase(request.getPaymentMethod()) ||
                "M-PESA (STK Push)".equalsIgnoreCase(request.getPaymentMethod())) {
                
                // Get savings account to fetch customer details
                SavingsAccount account = savingsAccountService.getAccountById(accountId);
                
                // Create Universal Payment Request for M-PESA STK Push
                UniversalPaymentRequest mpesaRequest = UniversalPaymentRequest.builder()
                    .customerId(account.getCustomerId())
                    .savingsAccountId(accountId)
                    .amount(request.getAmount())
                    .phoneNumber(request.getPhoneNumber()) // Frontend should send this
                    .paymentMethod("MPESA")
                    .description(request.getDescription() != null ? request.getDescription() : "Savings Deposit")
                    .transactionType("DEPOSIT")
                    .initiatedBy(request.getPostedBy())
                    .sourceModule("ADMIN_PANEL")
                    .build();
                
                // Process M-PESA payment
                UniversalPaymentResponse mpesaResponse = universalPaymentService.processPayment(mpesaRequest);
                
                // Return M-PESA response (includes STK push details)
                return ResponseEntity.ok(java.util.Map.of(
                    "success", mpesaResponse.isSuccess(),
                    "message", mpesaResponse.isSuccess() ? 
                        "STK Push sent to " + request.getPhoneNumber() + ". Please complete the payment on your phone." :
                        "Failed to initiate M-PESA payment: " + mpesaResponse.getErrorMessage(),
                    "checkoutRequestId", mpesaResponse.getCheckoutRequestId() != null ? mpesaResponse.getCheckoutRequestId() : "",
                    "customerMessage", mpesaResponse.getCustomerMessage(),
                    "mpesaResponseCode", mpesaResponse.getResponseCode(),
                    "paymentMethod", "M-PESA STK Push"
                ));
                
            } else {
                // Regular manual deposit (Cash, Bank, etc.)
                SavingsTransaction transaction = savingsAccountService.deposit(
                        accountId,
                        request.getAmount(),
                        request.getPaymentMethod(),
                        request.getPaymentReference(),
                        request.getDescription(),
                        request.getPostedBy()
                );
                
                return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "message", "Deposit recorded successfully",
                    "transaction", transaction,
                    "paymentMethod", request.getPaymentMethod()
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of(
                "success", false,
                "message", "Deposit failed: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/deposits/mpesa/status/{checkoutRequestId}")
    @Operation(summary = "Check M-PESA deposit payment status")
    public ResponseEntity<?> checkMpesaDepositStatus(@PathVariable String checkoutRequestId) {
        try {
            UniversalPaymentResponse statusResponse = universalPaymentService.checkPaymentStatus(checkoutRequestId);
            
            return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "paymentStatus", statusResponse.getPaymentStatus() != null ? statusResponse.getPaymentStatus() : "UNKNOWN",
                "message", statusResponse.getCustomerMessage() != null ? statusResponse.getCustomerMessage() : "Status check completed",
                "checkoutRequestId", checkoutRequestId,
                "completed", statusResponse.isPaymentCompleted(),
                "mpesaResponseCode", statusResponse.getResponseCode() != null ? statusResponse.getResponseCode() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of(
                "success", false,
                "message", "Failed to check payment status: " + e.getMessage(),
                "checkoutRequestId", checkoutRequestId
            ));
        }
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    @Operation(summary = "Withdraw from savings account")
    public ResponseEntity<SavingsTransaction> withdraw(
            @PathVariable Long accountId,
            @RequestBody WithdrawalRequest request) {
        SavingsTransaction transaction = savingsAccountService.withdraw(
                accountId,
                request.getAmount(),
                request.getPaymentMethod(),
                request.getDescription(),
                request.getPostedBy()
        );
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(summary = "Get all transactions for a savings account")
    public ResponseEntity<Page<SavingsTransaction>> getAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SavingsTransaction> transactions = savingsAccountService.getAccountTransactionsPaged(
                accountId, PageRequest.of(page, size));
        return ResponseEntity.ok(transactions);
    }

    // ============ PRODUCTS ============
    
    @PostMapping("/products")
    @Operation(summary = "Create new savings product")
    public ResponseEntity<SavingsProduct> createProduct(@RequestBody SavingsProduct product) {
        SavingsProduct created = savingsProductService.createProduct(product);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update savings product")
    public ResponseEntity<SavingsProduct> updateProduct(
            @PathVariable Long id,
            @RequestBody SavingsProduct product) {
        SavingsProduct updated = savingsProductService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get savings product by ID")
    public ResponseEntity<SavingsProduct> getProduct(@PathVariable Long id) {
        SavingsProduct product = savingsProductService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/products")
    @Operation(summary = "Get all savings products")
    public ResponseEntity<List<SavingsProduct>> getAllProducts() {
        List<SavingsProduct> products = savingsProductService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/active")
    @Operation(summary = "Get active savings products")
    public ResponseEntity<List<SavingsProduct>> getActiveProducts() {
        List<SavingsProduct> products = savingsProductService.getActiveProducts();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/products/{id}/toggle-status")
    @Operation(summary = "Toggle product active status")
    public ResponseEntity<SavingsProduct> toggleProductStatus(@PathVariable Long id) {
        SavingsProduct product = savingsProductService.toggleProductStatus(id);
        return ResponseEntity.ok(product);
    }

    // ============ STATISTICS ============
    
    @GetMapping("/statistics")
    @Operation(summary = "Get savings statistics")
    public ResponseEntity<SavingsStatistics> getStatistics() {
        SavingsStatistics stats = new SavingsStatistics();
        stats.setTotalBalance(savingsAccountService.getTotalSavingsBalance());
        stats.setActiveAccounts(savingsAccountService.countActiveAccounts());
        return ResponseEntity.ok(stats);
    }

    // ============ DTOs ============
    
    @Data
    public static class DepositRequest {
        private BigDecimal amount;
        private String paymentMethod;
        private String paymentReference;
        private String description;
        private String postedBy;
        private String phoneNumber; // Required for M-PESA STK Push
    }

    @Data
    public static class WithdrawalRequest {
        private BigDecimal amount;
        private String paymentMethod;
        private String description;
        private String postedBy;
    }

    @Data
    public static class SavingsStatistics {
        private BigDecimal totalBalance;
        private Long activeAccounts;
    }
}
