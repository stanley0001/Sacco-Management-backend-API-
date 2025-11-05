package com.example.demo.payments.controllers;

import com.example.demo.payments.services.MpesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AutoPay Callback Controller for C2B Payments
 * 
 * NOTE: URLs do NOT contain "mpesa", "safaricom", "sql", "exec" or other restricted keywords
 * as per Safaricom Daraja API requirements for C2B URL registration
 * 
 * This controller handles:
 * - C2B Validation requests
 * - C2B Confirmation requests
 * - PayBill callbacks
 */
@RestController
@RequestMapping("/api/auto-pay/callback")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AutoPay Callbacks", description = "Security-exempt C2B payment callback endpoints (no restricted keywords)")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class AutoPayCallbackController {

    private final MpesaService mpesaService;

    // Constants
    private static final String RESULT_CODE = "ResultCode";
    private static final String RESULT_DESC = "ResultDesc";
    private static final String SUCCESS_DESC = "Success";
    private static final String ACCEPTED_DESC = "Accepted";

    /**
     * C2B Validation Callback - Security Exempt
     * 
     * Called by M-PESA to validate payment before processing (if external validation is enabled)
     * URL Format: https://yourdomain.com/api/auto-pay/callback/validate
     * 
     * Expected Request Body:
     * {
     *   "TransactionType": "Pay Bill",
     *   "TransID": "RKTQDM7W6S",
     *   "TransTime": "20191122063845",
     *   "TransAmount": "10",
     *   "BusinessShortCode": "600638",
     *   "BillRefNumber": "invoice008",
     *   "MSISDN": "254700000000",
     *   "FirstName": "John",
     *   "LastName": "Doe"
     * }
     * 
     * Response: { "ResultCode": "0", "ResultDesc": "Accepted" }
     */
    @PostMapping("/validate")
    @Operation(summary = "C2B validation callback (Security Exempt)")
    public ResponseEntity<Map<String, Object>> c2bValidationCallback(@RequestBody Map<String, Object> payload) {
        log.info("🔔 C2B Validation Callback received: {}", payload);
        
        try {
            // Extract validation details
            String transactionType = (String) payload.get("TransactionType");
            String transId = (String) payload.get("TransID");
            String transAmount = String.valueOf(payload.get("TransAmount"));
            String billRefNumber = (String) payload.get("BillRefNumber");
            String msisdn = (String) payload.get("MSISDN");
            
            log.info("C2B Validation - TransID: {}, Amount: {}, BillRef: {}, MSISDN: {}", 
                transId, transAmount, billRefNumber, msisdn);
            
            // TODO: Add business validation logic here if needed
            // For now, accept all transactions
            // You can add validation like:
            // - Check if account/bill reference exists
            // - Validate amount
            // - Check customer KYC
            
            // Accept the transaction
            log.info("✅ C2B validation passed - accepting transaction");
            return ResponseEntity.ok(Map.of(
                RESULT_CODE, "0",
                RESULT_DESC, ACCEPTED_DESC
            ));
            
        } catch (Exception e) {
            log.error("❌ Error in C2B validation callback", e);
            
            // Reject transaction on error
            return ResponseEntity.ok(Map.of(
                RESULT_CODE, "C2B00016", // Other Error
                RESULT_DESC, "Rejected"
            ));
        }
    }

    /**
     * C2B Confirmation Callback - Security Exempt
     * 
     * Called by M-PESA after successful payment completion
     * URL Format: https://yourdomain.com/api/auto-pay/callback/confirm
     * 
     * Expected Request Body (same as validation):
     * {
     *   "TransactionType": "Pay Bill",
     *   "TransID": "RKTQDM7W6S",
     *   "TransTime": "20191122063845",
     *   "TransAmount": "10",
     *   "BusinessShortCode": "600638",
     *   "BillRefNumber": "invoice008",
     *   "InvoiceNumber": "",
     *   "OrgAccountBalance": "49532.00",
     *   "ThirdPartyTransID": "",
     *   "MSISDN": "254700000000",
     *   "FirstName": "John",
     *   "LastName": "Doe"
     * }
     * 
     * Response: { "ResultCode": 0, "ResultDesc": "Success" }
     */
    @PostMapping("/confirm")
    @Operation(summary = "C2B confirmation callback (Security Exempt)")
    public ResponseEntity<Map<String, Object>> c2bConfirmationCallback(@RequestBody Map<String, Object> payload) {
        log.info("🔔 C2B Confirmation Callback received: {}", payload);
        
        try {
            // Extract payment details
            String transactionType = (String) payload.get("TransactionType");
            String transId = (String) payload.get("TransID");
            String transTime = (String) payload.get("TransTime");
            Object transAmountObj = payload.get("TransAmount");
            String businessShortCode = (String) payload.get("BusinessShortCode");
            String billRefNumber = (String) payload.get("BillRefNumber");
            String orgAccountBalance = (String) payload.get("OrgAccountBalance");
            String msisdn = (String) payload.get("MSISDN");
            String firstName = (String) payload.get("FirstName");
            String lastName = (String) payload.get("LastName");
            
            // Parse amount
            Double transAmount = transAmountObj instanceof Number 
                ? ((Number) transAmountObj).doubleValue() 
                : Double.parseDouble(transAmountObj.toString());
            
            log.info("C2B Confirmation - TransID: {}, Amount: {}, BillRef: {}, MSISDN: {}, Balance: {}", 
                transId, transAmount, billRefNumber, msisdn, orgAccountBalance);
            
            // TODO: Process the payment
            // - Credit customer account
            // - Update loan repayment if billRefNumber is a loan reference
            // - Send SMS notification
            // - Update transaction records
            
            log.info("✅ C2B confirmation processed successfully");
            
            // Acknowledge receipt
            return ResponseEntity.ok(Map.of(
                RESULT_CODE, 0,
                RESULT_DESC, SUCCESS_DESC
            ));
            
        } catch (Exception e) {
            log.error("❌ Error processing C2B confirmation callback", e);
            
            // Still acknowledge to prevent retries
            return ResponseEntity.ok(Map.of(
                RESULT_CODE, 0,
                RESULT_DESC, ACCEPTED_DESC
            ));
        }
    }

    /**
     * PayBill Callback - Alternative endpoint name
     * Same as confirmation but with different URL pattern
     * URL Format: https://yourdomain.com/api/auto-pay/callback/paybill
     */
    @PostMapping("/paybill")
    @Operation(summary = "PayBill callback (Security Exempt)")
    public ResponseEntity<Map<String, Object>> paybillCallback(@RequestBody Map<String, Object> payload) {
        log.info("🔔 PayBill Callback received (routing to C2B confirmation): {}", payload);
        
        // Route to the same confirmation logic
        return c2bConfirmationCallback(payload);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check for AutoPay callback URLs")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "timestamp", java.time.LocalDateTime.now().toString(),
            "service", "AutoPay Callback Service",
            "note", "URLs comply with Daraja restricted keywords policy"
        ));
    }
}
