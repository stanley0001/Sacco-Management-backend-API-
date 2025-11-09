package com.example.demo.finance.payments.controllers;

import com.example.demo.finance.payments.dto.UniversalPaymentRequest;
import com.example.demo.finance.payments.dto.UniversalPaymentResponse;
import com.example.demo.finance.payments.dto.UniversalPaymentRequest;
import com.example.demo.finance.payments.dto.UniversalPaymentResponse;
import com.example.demo.finance.payments.services.UniversalPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/universal")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Universal Payment", description = "Unified payment processing with M-PESA and SMS integration")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class UniversalPaymentController {
    
    private final UniversalPaymentService universalPaymentService;
    
    /**
     * Process payment with automatic M-PESA/SMS integration
     */
    @PostMapping("/process")
    @Operation(summary = "Process payment with unified M-PESA and SMS integration")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_CREATE', 'PAYMENT_INITIATE', 'ADMIN_ACCESS')")
    public ResponseEntity<UniversalPaymentResponse> processPayment(@RequestBody UniversalPaymentRequest request) {
        log.info("Universal payment request: Customer={}, Amount={}, Method={}", 
            request.getCustomerId(), request.getAmount(), request.getPaymentMethod());
        
        try {
            UniversalPaymentResponse response = universalPaymentService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing universal payment", e);
            
            UniversalPaymentResponse errorResponse = UniversalPaymentResponse.builder()
                .success(false)
                .errorCode("PAYMENT_PROCESSING_ERROR")
                .errorMessage(e.getMessage())
                .customerMessage("Payment processing failed. Please try again or contact support.")
                .build();
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * Check payment status (calls M-PESA API - use sparingly)
     */
    @GetMapping("/status/{checkoutRequestId}")
    @Operation(summary = "Check payment status via M-PESA API")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'PAYMENT_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<UniversalPaymentResponse> checkPaymentStatus(@PathVariable String checkoutRequestId) {
        log.info("Checking payment status for checkout request: {}", checkoutRequestId);
        
        try {
            UniversalPaymentResponse response = universalPaymentService.checkPaymentStatus(checkoutRequestId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking payment status", e);
            
            UniversalPaymentResponse errorResponse = UniversalPaymentResponse.builder()
                .success(false)
                .checkoutRequestId(checkoutRequestId)
                .errorCode("STATUS_CHECK_ERROR")
                .errorMessage(e.getMessage())
                .customerMessage("Unable to check payment status. Please try again.")
                .build();
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Get transaction status from database (for frontend polling)
     */
    @GetMapping("/transaction-status/{checkoutRequestId}")
    @Operation(summary = "Get transaction status from database")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'PAYMENT_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<UniversalPaymentResponse> getTransactionStatus(@PathVariable String checkoutRequestId) {
        log.debug("Getting transaction status from database for: {}", checkoutRequestId);
        
        try {
            UniversalPaymentResponse response = universalPaymentService.getTransactionStatusFromDB(checkoutRequestId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting transaction status from DB: {}", e.getMessage());
            
            UniversalPaymentResponse errorResponse = UniversalPaymentResponse.builder()
                .success(false)
                .checkoutRequestId(checkoutRequestId)
                .errorCode("DB_STATUS_ERROR")
                .errorMessage(e.getMessage())
                .customerMessage("Unable to get transaction status.")
                .build();
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * Send payment reminder
     */
    @PostMapping("/reminder")
    @Operation(summary = "Send payment reminder SMS")
    @PreAuthorize("hasAnyAuthority('COMMUNICATION_SEND', 'ADMIN_ACCESS')")
    public ResponseEntity<?> sendPaymentReminder(@RequestBody Map<String, Object> request) {
        log.info("Sending payment reminder");
        
        try {
            String phoneNumber = (String) request.get("phoneNumber");
            String customerName = (String) request.get("customerName");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String dueDate = (String) request.get("dueDate");
            String accountType = (String) request.getOrDefault("accountType", "GENERAL");
            
            universalPaymentService.sendPaymentReminder(phoneNumber, customerName, amount, dueDate, accountType);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment reminder sent successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending payment reminder", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to send payment reminder: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Send overdue notification
     */
    @PostMapping("/overdue-notification")
    @Operation(summary = "Send overdue payment notification SMS")
    @PreAuthorize("hasAnyAuthority('COMMUNICATION_SEND', 'ADMIN_ACCESS')")
    public ResponseEntity<?> sendOverdueNotification(@RequestBody Map<String, Object> request) {
        log.info("Sending overdue payment notification");
        
        try {
            String phoneNumber = (String) request.get("phoneNumber");
            String customerName = (String) request.get("customerName");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Integer daysOverdue = Integer.valueOf(request.get("daysOverdue").toString());
            String accountType = (String) request.getOrDefault("accountType", "GENERAL");
            
            universalPaymentService.sendOverdueNotification(phoneNumber, customerName, amount, daysOverdue, accountType);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Overdue notification sent successfully"
            ));
        } catch (Exception e) {
            log.error("Error sending overdue notification", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to send overdue notification: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get customer M-PESA transactions
     */
    @GetMapping("/customer/{customerId}/transactions")
    @Operation(summary = "Get customer M-PESA transactions")
    @PreAuthorize("hasAnyAuthority('TRANSACTION_VIEW', 'canViewClients', 'ADMIN_ACCESS')")
    public ResponseEntity<?> getCustomerTransactions(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("Getting M-PESA transactions for customer: {}", customerId);
        
        try {
            return ResponseEntity.ok(universalPaymentService.getCustomerTransactions(customerId, page, size));
        } catch (Exception e) {
            log.error("Error getting customer transactions", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to get transactions: " + e.getMessage(),
                "transactions", java.util.Collections.emptyList(),
                "totalElements", 0
            ));
        }
    }
}
