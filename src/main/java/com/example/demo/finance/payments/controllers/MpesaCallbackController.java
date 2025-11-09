package com.example.demo.finance.payments.controllers;

import com.example.demo.finance.payments.services.MpesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mpesa/callback")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "M-PESA Callbacks", description = "Security-exempt M-PESA callback endpoints")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class MpesaCallbackController {

    private final MpesaService mpesaService;

    // Constants to avoid duplication
    private static final String RESULT_CODE = "ResultCode";
    private static final String RESULT_DESC = "ResultDesc";
    private static final String SUCCESS_DESC = "Success";
    private static final String ACCEPTED_DESC = "Accepted";

    /**
     * M-PESA STK Push Callback - Security Exempt
     * This endpoint processes callbacks from M-PESA after STK Push completion
     */
    @PostMapping("/stk-push")
    @Operation(summary = "M-PESA STK Push callback (Security Exempt)")
    public ResponseEntity<Map<String, Object>> stkPushCallback(@RequestBody Map<String, Object> payload) {
        log.info("🔔 M-PESA STK Push Callback received: {}", payload);
        
        try {
            // Process the callback using MpesaService
            mpesaService.processCallback(payload);
            
            log.info("✅ STK Push callback processed successfully");
            
            // M-PESA expects a simple acknowledgment with ResultCode 0 for success
            return ResponseEntity.ok(Map.of(
                RESULT_CODE, 0,
                RESULT_DESC, SUCCESS_DESC
            ));
            
        } catch (Exception e) {
            log.error("❌ Error processing M-PESA STK Push callback", e);
            
            // Still return success to M-PESA to avoid retries
            // The error handling is done internally by MpesaService
            return ResponseEntity.ok(Map.of(
                RESULT_CODE, 0,
                RESULT_DESC, ACCEPTED_DESC
            ));
        }
    }

    /**
     * M-PESA B2C Callback - Security Exempt
     */
    @PostMapping("/b2c")
    @Operation(summary = "M-PESA B2C callback (Security Exempt)")
    public ResponseEntity<?> b2cCallback(@RequestBody Map<String, Object> payload) {
        log.info("🔔 M-PESA B2C Callback received: {}", payload);
        
        try {
            // Extract B2C callback data
            Map<String, Object> result = (Map<String, Object>) payload.get("Result");
            if (result != null) {
                Integer resultCode = (Integer) result.get("ResultCode");
                String resultDesc = (String) result.get("ResultDesc");
                String transactionId = (String) result.get("TransactionID");
                
                log.info("B2C Callback - TransactionID: {}, ResultCode: {}, ResultDesc: {}", 
                    transactionId, resultCode, resultDesc);
                
                // TODO: Process B2C callback (for disbursements)
            }
            
            return ResponseEntity.ok(Map.of(
                "ResultCode", 0,
                "ResultDesc", "Success"
            ));
            
        } catch (Exception e) {
            log.error("Error processing M-PESA B2C callback", e);
            
            return ResponseEntity.ok(Map.of(
                "ResultCode", 0,
                "ResultDesc", "Accepted"
            ));
        }
    }

    /**
     * M-PESA Transaction Status Callback - Security Exempt
     */
    @PostMapping("/transaction-status")
    @Operation(summary = "M-PESA transaction status callback (Security Exempt)")
    public ResponseEntity<?> transactionStatusCallback(@RequestBody Map<String, Object> payload) {
        log.info("🔔 M-PESA Transaction Status Callback received: {}", payload);
        
        try {
            // Process transaction status callback
            return ResponseEntity.ok(Map.of(
                "ResultCode", 0,
                "ResultDesc", "Success"
            ));
            
        } catch (Exception e) {
            log.error("Error processing M-PESA transaction status callback", e);
            
            return ResponseEntity.ok(Map.of(
                "ResultCode", 0,
                "ResultDesc", "Accepted"
            ));
        }
    }

    /**
     * Generic M-PESA Callback Handler - Security Exempt
     */
    @PostMapping("/generic")
    @Operation(summary = "Generic M-PESA callback handler (Security Exempt)")
    public ResponseEntity<?> genericCallback(@RequestBody Map<String, Object> payload) {
        log.info("🔔 Generic M-PESA Callback received: {}", payload);
        
        return ResponseEntity.ok(Map.of(
            "ResultCode", 0,
            "ResultDesc", "Success"
        ));
    }

    /**
     * Health check endpoint for M-PESA callback URL validation
     */
    @GetMapping("/health")
    @Operation(summary = "Health check for callback URLs")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "timestamp", java.time.LocalDateTime.now(),
            "service", "M-PESA Callback Service"
        ));
    }
}
