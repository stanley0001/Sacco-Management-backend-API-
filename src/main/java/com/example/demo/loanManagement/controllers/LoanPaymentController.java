package com.example.demo.loanManagement.controllers;

import com.example.demo.loanManagement.parsistence.entities.loanTransactions;
import com.example.demo.loanManagement.services.LoanPaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for loan payment operations
 */
@RestController
@RequestMapping("/api/loans/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LoanPaymentController {
    
    private final LoanPaymentService loanPaymentService;
    
    /**
     * Process a loan payment
     * POST /api/loans/payments/process
     */
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request) {
        try {
            log.info("Processing payment request: {}", request);
            
            loanTransactions transaction = loanPaymentService.processLoanPayment(
                request.getLoanId(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getReferenceNumber()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment processed successfully");
            response.put("transaction", transaction);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing payment: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get payment summary for a loan
     * GET /api/loans/payments/summary/{loanId}
     */
    @GetMapping("/summary/{loanId}")
    public ResponseEntity<?> getPaymentSummary(@PathVariable Long loanId) {
        try {
            LoanPaymentService.PaymentSummary summary = loanPaymentService.getPaymentSummary(loanId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting payment summary: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get all payments for a loan
     * GET /api/loans/payments/loan/{loanRef}
     */
    @GetMapping("/loan/{loanRef}")
    public ResponseEntity<List<loanTransactions>> getLoanPayments(@PathVariable String loanRef) {
        try {
            List<loanTransactions> payments = loanPaymentService.getLoanPayments(loanRef);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error getting loan payments: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all payments for a customer
     * GET /api/loans/payments/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<loanTransactions>> getCustomerPayments(@PathVariable String customerId) {
        try {
            List<loanTransactions> payments = loanPaymentService.getCustomerPayments(customerId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error getting customer payments: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Payment request DTO
     */
    @Data
    public static class PaymentRequest {
        private Long loanId;
        private BigDecimal amount;
        private String paymentMethod;
        private String referenceNumber;
        private String description;
    }
}
