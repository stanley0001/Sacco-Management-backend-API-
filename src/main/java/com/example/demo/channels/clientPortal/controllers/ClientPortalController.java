package com.example.demo.channels.clientPortal.controllers;

import com.example.demo.channels.clientPortal.services.ClientPortalService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import com.example.demo.finance.loanManagement.dto.LoanApplicationResponse;
import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client Portal Controller
 * Provides all self-service operations for customers via web portal
 * Uses centralized ClientPortalService (same service used by Mobile and USSD)
 */
@RestController
@RequestMapping("/api/client-portal")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClientPortalController {
    
    private final ClientPortalService clientPortalService;
    
    /**
     * Get customer dashboard summary
     */
    @GetMapping("/dashboard/{customerId}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable String customerId) {
        try {
            Map<String, Object> dashboard = clientPortalService.getDashboardSummary(customerId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error getting dashboard", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get all customer loans
     */
    @GetMapping("/loans/{customerId}")
    public ResponseEntity<List<Map<String, Object>>> getLoans(@PathVariable String customerId) {
        try {
            List<Map<String, Object>> loans = clientPortalService.getCustomerLoans(customerId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            log.error("Error getting loans", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get loan details with schedule
     */
    @GetMapping("/loans/{customerId}/{loanId}")
    public ResponseEntity<Map<String, Object>> getLoanDetails(
            @PathVariable String customerId,
            @PathVariable Long loanId) {
        try {
            Map<String, Object> details = clientPortalService.getLoanDetails(loanId, customerId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Error getting loan details", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get all loan applications
     */
    @GetMapping("/applications/{customerId}")
    public ResponseEntity<List<LoanApplication>> getApplications(@PathVariable String customerId) {
        try {
            List<LoanApplication> applications = clientPortalService.getCustomerApplications(customerId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("Error getting applications", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Apply for a loan
     */
    @PostMapping("/apply-loan")
    public ResponseEntity<Map<String, Object>> applyForLoan(
            @RequestParam String customerId,
            @RequestParam String productCode,
            @RequestParam Double amount,
            @RequestParam Integer term,
            @RequestParam(required = false) String purpose) {
        try {
            LoanApplicationResponse response = clientPortalService.applyForLoan(
                customerId, productCode, amount, term, 
                purpose != null ? purpose : "Personal loan");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response.getSuccess());
            result.put("message", response.getMessage());
            result.put("applicationId", response.getApplicationId());
            result.put("loanNumber", response.getLoanNumber());
            result.put("applicationStatus", response.getApplicationStatus());
            result.put("nextAction", response.getNextAction());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error applying for loan", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get available loan products
     */
    @GetMapping("/products/{customerId}")
    public ResponseEntity<List<Map<String, Object>>> getProducts(@PathVariable String customerId) {
        try {
            List<Map<String, Object>> products = clientPortalService.getAvailableProducts(customerId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting products", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Make a loan payment
     */
    @PostMapping("/make-payment")
    public ResponseEntity<Map<String, Object>> makePayment(
            @RequestParam String customerId,
            @RequestParam Long loanId,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod,
            @RequestParam String reference) {
        try {
            loanTransactions transaction = clientPortalService.makePayment(
                customerId, loanId, amount, paymentMethod, reference);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Payment processed successfully");
            result.put("transaction", transaction);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error processing payment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get transaction history
     */
    @GetMapping("/transactions/{customerId}")
    public ResponseEntity<List<loanTransactions>> getTransactions(
            @PathVariable String customerId,
            @RequestParam(required = false) Integer limit) {
        try {
            List<loanTransactions> transactions = clientPortalService.getTransactionHistory(
                customerId, limit);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error getting transactions", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get account statement
     */
    @GetMapping("/statement/{customerId}")
    public ResponseEntity<Map<String, Object>> getStatement(
            @PathVariable String customerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> statement = clientPortalService.getAccountStatement(
                customerId, startDate, endDate);
            return ResponseEntity.ok(statement);
        } catch (Exception e) {
            log.error("Error generating statement", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Update customer profile
     */
    @PutMapping("/profile/{customerId}")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable String customerId,
            @RequestBody Map<String, String> updates) {
        try {
            Customer customer = clientPortalService.updateProfile(customerId, updates);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Profile updated successfully");
            result.put("customer", customer);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error updating profile", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get bank accounts (ALPHA, Shares, Deposits)
     */
    @GetMapping("/accounts/{customerId}")
    public ResponseEntity<List<BankAccounts>> getBankAccounts(@PathVariable String customerId) {
        try {
            List<BankAccounts> accounts = clientPortalService.getBankAccounts(customerId);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            log.error("Error getting bank accounts", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Check loan eligibility
     */
    @GetMapping("/check-eligibility/{customerId}")
    public ResponseEntity<Map<String, Object>> checkEligibility(
            @PathVariable String customerId,
            @RequestParam String productCode,
            @RequestParam Double amount) {
        try {
            Map<String, Object> eligibility = clientPortalService.checkLoanEligibility(
                customerId, productCode, amount);
            return ResponseEntity.ok(eligibility);
        } catch (Exception e) {
            log.error("Error checking eligibility", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get customer notifications
     */
    @GetMapping("/notifications/{customerId}")
    public ResponseEntity<List<Map<String, Object>>> getNotifications(@PathVariable String customerId) {
        try {
            List<Map<String, Object>> notifications = clientPortalService.getNotifications(customerId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error getting notifications", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
