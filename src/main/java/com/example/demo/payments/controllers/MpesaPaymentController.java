package com.example.demo.payments.controllers;

import com.example.demo.payments.dto.*;
import com.example.demo.payments.services.MpesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mpesa")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "M-PESA Payments", description = "M-PESA STK Push and payment operations")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class MpesaPaymentController {
    
    private final MpesaService mpesaService;
    
    /**
     * Initiate STK Push for payment (Generic version)
     */
    @PostMapping("/stk-push/generic")
    @Operation(summary = "Initiate STK Push payment (Generic/Map input)")
    public ResponseEntity<?> initiateSTKPush(@RequestBody Map<String, Object> request) {
        log.info("Initiating STK Push for payment");
        
        try {
            // Build STK push request
            STKPushRequest stkRequest = new STKPushRequest();
            stkRequest.setPhoneNumber(String.valueOf(request.get("phoneNumber")));
            stkRequest.setAmount(new java.math.BigDecimal(String.valueOf(request.get("amount"))));
            stkRequest.setAccountReference(String.valueOf(request.get("accountReference")));
            stkRequest.setTransactionDesc(String.valueOf(request.getOrDefault("transactionDesc", "Payment")));
            
            // Set provider config if specified
            if (request.containsKey("providerConfigId")) {
                stkRequest.setProviderConfigId(Long.valueOf(String.valueOf(request.get("providerConfigId"))));
            }
            
            // Set customer and account IDs
            if (request.containsKey("customerId")) {
                stkRequest.setCustomerId(Long.valueOf(String.valueOf(request.get("customerId"))));
            }
            
            if (request.containsKey("accountId")) {
                stkRequest.setAccountId(Long.valueOf(String.valueOf(request.get("accountId"))));
            }
            
            // Initiate STK push
            STKPushResponse response = mpesaService.initiateSTKPush(stkRequest);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error initiating STK Push", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to initiate payment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Check STK Push payment status
     */
    @GetMapping("/status/{checkoutRequestId}")
    @Operation(summary = "Check STK Push payment status")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String checkoutRequestId) {
        log.info("Checking status for checkout request: {}", checkoutRequestId);
        
        try {
            MpesaDepositStatusResponse status = mpesaService.checkSTKPushStatus(checkoutRequestId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error checking payment status", e);
            return ResponseEntity.ok(Map.of(
                "ResultCode", "1037",
                "ResultDesc", "Timeout or error checking status"
            ));
        }
    }
    
    /**
     * M-PESA callback endpoint (Generic version) - DISABLED
     * Note: Callback handling is now managed by MpesaCallbackController
     * which has proper security exemptions for M-PESA webhooks
     */
    /*
    @PostMapping("/callback/generic")
    @Operation(summary = "M-PESA callback endpoint (Generic/Map input)")
    public ResponseEntity<?> handleCallback(@RequestBody Map<String, Object> callback) {
        log.info("Received M-PESA callback: {}", callback);
        
        try {
            mpesaService.processCallback(callback);
            return ResponseEntity.ok(Map.of(
                "ResultCode", "0",
                "ResultDesc", "Callback received successfully"
            ));
        } catch (Exception e) {
            log.error("Error processing callback", e);
            return ResponseEntity.ok(Map.of(
                "ResultCode", "1",
                "ResultDesc", "Error processing callback"
            ));
        }
    }
    */
}
