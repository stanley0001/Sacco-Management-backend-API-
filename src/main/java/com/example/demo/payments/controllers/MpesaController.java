package com.example.demo.payments.controllers;

import com.example.demo.payments.dto.*;
import com.example.demo.payments.entities.MpesaTransaction;
import com.example.demo.payments.services.MpesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mpesa")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "M-PESA Integration", description = "M-PESA Daraja API endpoints for payments")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class MpesaController {
    
    private final MpesaService mpesaService;
    
    /**
     * Initiate STK Push payment
     */
    @PostMapping("/stk-push")
    @Operation(summary = "Initiate STK Push payment (Lipa Na M-PESA)")
    @PreAuthorize("hasAuthority('PAYMENT_INITIATE')")
    public ResponseEntity<STKPushResponse> initiateSTKPush(
        @RequestBody @Parameter(description = "STK Push request details") STKPushRequest request
    ) {
        log.info("API: Initiating STK Push for phone: {}, amount: {}", 
            request.getPhoneNumber(), request.getAmount());
        
        try {
            STKPushResponse response = mpesaService.initiateSTKPush(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in STK Push API", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Query STK Push transaction status
     */
    @GetMapping("/query/{checkoutRequestId}")
    @Operation(summary = "Query STK Push transaction status")
    @PreAuthorize("hasAnyAuthority('PAYMENT_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<Map<String, Object>> queryStatus(
        @PathVariable @Parameter(description = "Checkout Request ID") String checkoutRequestId
    ) {
        log.info("API: Querying status for: {}", checkoutRequestId);
        
        try {
            Map<String, Object> status = mpesaService.querySTKPushStatus(checkoutRequestId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error querying transaction status", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get transaction details
     */
    @GetMapping("/transaction/{checkoutRequestId}")
    @Operation(summary = "Get transaction details from database")
    @PreAuthorize("hasAnyAuthority('PAYMENT_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<MpesaTransaction> getTransaction(
        @PathVariable @Parameter(description = "Checkout Request ID") String checkoutRequestId
    ) {
        log.info("API: Getting transaction: {}", checkoutRequestId);
        
        try {
            MpesaTransaction transaction = mpesaService.getTransactionByCheckoutRequestId(checkoutRequestId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Error getting transaction", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get customer transactions
     */
    @GetMapping("/customer/{customerId}/transactions")
    @Operation(summary = "Get all transactions for a customer")
    @PreAuthorize("hasAnyAuthority('PAYMENT_VIEW', 'CUSTOMER_READ', 'ADMIN_ACCESS')")
    public ResponseEntity<List<MpesaTransaction>> getCustomerTransactions(
        @PathVariable @Parameter(description = "Customer ID") Long customerId
    ) {
        log.info("API: Getting transactions for customer: {}", customerId);
        
        try {
            List<MpesaTransaction> transactions = mpesaService.getCustomerTransactions(customerId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error getting customer transactions", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * M-PESA callback endpoint
     */
    @PostMapping("/callback")
    @Operation(summary = "M-PESA callback endpoint (called by Safaricom)")
    public ResponseEntity<Map<String, String>> mpesaCallback(
        @RequestBody @Parameter(description = "M-PESA callback payload") MpesaCallbackResponse callback
    ) {
        log.info("API: Received M-PESA callback");
        log.debug("Callback payload: {}", callback);
        
        try {
            mpesaService.processCallback(callback);
            return ResponseEntity.ok(Map.of(
                "ResultCode", "0",
                "ResultDesc", "Callback received and processed successfully"
            ));
        } catch (Exception e) {
            log.error("Error processing callback", e);
            // Still return 200 to M-PESA to prevent retries
            return ResponseEntity.ok(Map.of(
                "ResultCode", "0",
                "ResultDesc", "Callback received"
            ));
        }
    }
    
    /**
     * M-PESA timeout endpoint
     */
    @PostMapping("/timeout")
    @Operation(summary = "M-PESA timeout endpoint")
    public ResponseEntity<Map<String, String>> mpesaTimeout(
        @RequestBody @Parameter(description = "M-PESA timeout payload") Map<String, Object> timeout
    ) {
        log.info("API: Received M-PESA timeout");
        log.debug("Timeout payload: {}", timeout);
        
        return ResponseEntity.ok(Map.of(
            "ResultCode", "0",
            "ResultDesc", "Timeout received"
        ));
    }
    
    /**
     * M-PESA result endpoint (for B2C, C2B)
     */
    @PostMapping("/result")
    @Operation(summary = "M-PESA result endpoint")
    public ResponseEntity<Map<String, String>> mpesaResult(
        @RequestBody @Parameter(description = "M-PESA result payload") Map<String, Object> result
    ) {
        log.info("API: Received M-PESA result");
        log.debug("Result payload: {}", result);
        
        return ResponseEntity.ok(Map.of(
            "ResultCode", "0",
            "ResultDesc", "Result received"
        ));
    }
    
    /**
     * Initiate B2C payment
     */
    @PostMapping("/b2c")
    @Operation(summary = "Initiate B2C payment (Business to Customer)")
    @PreAuthorize("hasAuthority('PAYMENT_DISBURSE')")
    public ResponseEntity<Map<String, Object>> initiateB2C(
        @RequestBody @Parameter(description = "B2C payment request") B2CRequest request
    ) {
        log.info("API: Initiating B2C payment for phone: {}, amount: {}", 
            request.getPhoneNumber(), request.getAmount());
        
        try {
            Map<String, Object> response = mpesaService.initiateB2C(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error initiating B2C payment", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
