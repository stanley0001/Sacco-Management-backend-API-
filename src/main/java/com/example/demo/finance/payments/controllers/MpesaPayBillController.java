package com.example.demo.finance.payments.controllers;

import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import com.example.demo.finance.banking.parsitence.enitities.Transactions;
import com.example.demo.finance.banking.parsitence.repositories.BankAccountRepo;
import com.example.demo.erp.communication.sms.SmsService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.erp.customerManagement.serviceImplimentations.CustomerService;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.services.LoanPaymentService;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.finance.payments.dto.PayBillC2BRequest;
import com.example.demo.finance.payments.services.BankDepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling M-PESA PayBill C2B callbacks
 * Uses document number as account reference
 * Integrates with BPS module callback URL generation
 */
@RestController
@RequestMapping("/api/mpesa/callback")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class MpesaPayBillController {

    private final CustomerService customerService;
    private final BankAccountRepo bankAccountRepo;
    private final LoanAccountRepo loanAccountRepo;
    private final BankDepositService bankDepositService;
    private final LoanPaymentService loanPaymentService;
    private final SmsService smsService;

    /**
     * Handle PayBill C2B Confirmation Callback from M-PESA
     * POST /api/mpesa/callback/paybill
     * This is called by M-PESA when payment is confirmed
     */
    @PostMapping("/paybill")
    public ResponseEntity<Map<String, Object>> handlePayBillCallback(@RequestBody PayBillC2BRequest request) {
        log.info("🔔 PayBill C2B Confirmation received: TransID={}, BillRefNumber={}, Amount={}", 
                request.getTransID(), request.getBillRefNumber(), request.getTransAmount());

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate request
            if (request.getTransID() == null || request.getBillRefNumber() == null || request.getTransAmount() == null) {
                log.error("Invalid PayBill request: Missing required fields");
                response.put("ResultCode", "C2B00011");
                response.put("ResultDesc", "Invalid request - missing required fields");
                return ResponseEntity.badRequest().body(response);
            }

            String documentNumber = request.getBillRefNumber();
            BigDecimal amount = new BigDecimal(request.getTransAmount());
            String phoneNumber = request.getMSISDN();
            String receiptNumber = request.getTransID();

            log.info("Processing PayBill payment: Document={}, Amount={}, Phone={}", 
                    documentNumber, amount, phoneNumber);

            // Check if this is a loan repayment (format: LOAN-xxx or loan number)
            if (documentNumber.toUpperCase().startsWith("LOAN-") || documentNumber.matches("^[0-9]+$")) {
                processLoanRepayment(documentNumber, amount, phoneNumber, receiptNumber);
            } else {
                // Otherwise, treat as deposit to bank accounts
                processDeposit(documentNumber, amount, phoneNumber, receiptNumber);
            }

            response.put("ResultCode", "0");
            response.put("ResultDesc", "Success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing PayBill callback", e);
            response.put("ResultCode", "C2B00012");
            response.put("ResultDesc", "System error: " + e.getMessage());
            return ResponseEntity.ok(response); // Return 200 to acknowledge receipt
        }
    }

    /**
     * Process loan repayment
     */
    private void processLoanRepayment(String reference, BigDecimal amount, String phoneNumber, String receiptNumber) {
        try {
            // Find loan by reference or loan number
            Optional<LoanAccount> loanOpt;
            
            if (reference.toUpperCase().startsWith("LOAN-")) {
                String loanRef = reference.substring(5); // Remove "LOAN-" prefix
                loanOpt = loanAccountRepo.findByLoanref(loanRef);
            } else {
                // Try as loan number
                loanOpt = loanAccountRepo.findByLoanref(reference);
            }

            if (loanOpt.isEmpty()) {
                log.warn("Loan not found for reference: {}", reference);
                sendPaymentErrorSms(phoneNumber, amount, "Loan not found: " + reference);
                return;
            }

            LoanAccount loan = loanOpt.get();
            
            // Process loan payment
            loanPaymentService.processLoanPayment(
                    loan.getAccountId(),
                    amount,
                    "PAYBILL",
                    receiptNumber
            );

            // Send SMS confirmation
            BigDecimal remainingBalance = BigDecimal.valueOf(loan.getAccountBalance());
            smsService.sendPaymentConfirmationSms(
                    phoneNumber,
                    amount,
                    receiptNumber,
                    remainingBalance
            );

            log.info("PayBill loan repayment processed: Loan={}, Amount={}, Receipt={}", 
                    loan.getLoanref(), amount, receiptNumber);

        } catch (Exception e) {
            log.error("Error processing PayBill loan repayment", e);
            sendPaymentErrorSms(phoneNumber, amount, "Payment processing error");
        }
    }

    /**
     * Process deposit to bank accounts using document number
     */
    private void processDeposit(String documentNumber, BigDecimal amount, String phoneNumber, String receiptNumber) {
        try {
            // Find customer by document number
            ClientInfo customerOpt = customerService.findByDocumentNumber(documentNumber);

            if (customerOpt==null) {
                log.warn("Customer not found for document number: {}", documentNumber);
                sendPaymentErrorSms(phoneNumber, amount, "Account not found. Please contact support. Ref: " + receiptNumber);
                return;
            }

            Customer customer = customerOpt.getClient();
            
            // Get customer bank accounts
            Optional<List<BankAccounts>> accountsOpt = bankAccountRepo.findByCustomer(customer);
            
            if (accountsOpt.isEmpty() || accountsOpt.get().isEmpty()) {
                log.warn("No bank accounts found for customer: {}", customer.getId());
                sendPaymentErrorSms(phoneNumber, amount, "No accounts found. Please contact support. Ref: " + receiptNumber);
                return;
            }

            // Find SAVINGS account (or default to first account)
            BankAccounts targetAccount = accountsOpt.get().stream()
                    .filter(acc -> "SAVINGS".equalsIgnoreCase(acc.getAccountType()))
                    .findFirst()
                    .orElse(accountsOpt.get().get(0));

            // Create deposit transaction
            Transactions transaction =
                    bankDepositService.processDeposit(
                            customer.getId(),
                            amount,
                            targetAccount.getAccountType(),
                            receiptNumber,
                            "PAYBILL"
                    );

            // Send SMS confirmation
            BigDecimal newBalance = BigDecimal.valueOf(transaction.getClosingBalance());
            smsService.sendDepositConfirmationSms(
                    phoneNumber,
                    amount,
                    targetAccount.getBankAccount(),
                    newBalance
            );

            log.info("PayBill deposit processed: Customer={}, Account={}, Amount={}, Receipt={}", 
                    customer.getFirstName(), targetAccount.getBankAccount(), amount, receiptNumber);

        } catch (Exception e) {
            log.error("Error processing PayBill deposit", e);
            sendPaymentErrorSms(phoneNumber, amount, "Payment processing error. Ref: " + receiptNumber);
        }
    }

    /**
     * Send error SMS
     */
    private void sendPaymentErrorSms(String phoneNumber, BigDecimal amount, String error) {
        try {
            String message = String.format(
                    "Payment of KES %,.2f received but could not be processed. Reason: %s. Please contact support.",
                    amount, error
            );
            smsService.sendSms(phoneNumber, message);
        } catch (Exception e) {
            log.error("Failed to send error SMS", e);
        }
    }

    /**
     * Validation endpoint for M-PESA C2B
     * POST /api/mpesa/callback/validation
     * M-PESA calls this before confirming payment to validate the transaction
     */
    @PostMapping("/validation")
    public ResponseEntity<Map<String, Object>> validatePayBill(@RequestBody(required = false) PayBillC2BRequest request) {
        log.info("🔍 PayBill Validation request: {}", request != null ? request.getBillRefNumber() : "empty");
        
        Map<String, Object> response = new HashMap<>();
        
        // You can add validation logic here if needed
        // For now, accept all transactions
        response.put("ResultCode", "0");
        response.put("ResultDesc", "Accepted");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Alternative confirmation endpoint
     * POST /api/mpesa/callback/confirmation
     * Some M-PESA setups use separate validation and confirmation URLs
     */
    @PostMapping("/confirmation")
    public ResponseEntity<Map<String, Object>> handleConfirmation(@RequestBody PayBillC2BRequest request) {
        log.info("🔔 PayBill Confirmation (alt endpoint): TransID={}", request.getTransID());
        // Route to same handler as /paybill
        return handlePayBillCallback(request);
    }

    /**
     * Test endpoint to simulate PayBill payment
     * POST /api/mpesa/callback/test-paybill
     */
    @PostMapping("/test-paybill")
    public ResponseEntity<Map<String, Object>> testPayBill(@RequestBody Map<String, String> testData) {
        log.info("🧪 Testing PayBill payment: {}", testData);

        PayBillC2BRequest request = PayBillC2BRequest.builder()
                .TransactionType("Pay Bill")
                .TransID("TEST" + System.currentTimeMillis())
                .TransTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .TransAmount(testData.getOrDefault("amount", "1000"))
                .BusinessShortCode("400200")
                .BillRefNumber(testData.get("accountNumber")) // Document number
                .MSISDN(testData.getOrDefault("phoneNumber", "254712345678"))
                .FirstName("Test")
                .LastName("Customer")
                .build();

        return handlePayBillCallback(request);
    }
    
    /**
     * Health check endpoint for PayBill
     * GET /api/mpesa/callback/health-paybill
     */
    @GetMapping("/health-paybill")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "M-PESA PayBill Callback");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("endpoints", java.util.Map.of(
            "validation", "/api/mpesa/callback/validation",
            "confirmation", "/api/mpesa/callback/confirmation",
            "paybill", "/api/mpesa/callback/paybill"
        ));
        return ResponseEntity.ok(response);
    }
}
