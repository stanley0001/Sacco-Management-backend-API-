package com.example.demo.finance.payments.controllers;

import com.example.demo.erp.communication.sms.SmsService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.erp.customerManagement.serviceImplimentations.CustomerService;
import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import com.example.demo.finance.banking.parsitence.enitities.Transactions;
import com.example.demo.finance.banking.parsitence.repositories.BankAccountRepo;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.SuspensePayments;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.finance.loanManagement.services.LoanPaymentService;
import com.example.demo.finance.loanManagement.services.PaymentService;
import com.example.demo.finance.payments.services.BankDepositService;
import com.example.demo.finance.payments.services.MpesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final CustomerService customerService;
    private final BankAccountRepo bankAccountRepo;
    private final LoanAccountRepo loanAccountRepo;
    private final BankDepositService bankDepositService;
    private final LoanPaymentService loanPaymentService;
    private final SmsService smsService;
    private final PaymentService paymentService;

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
            String transId = (String) payload.get("TransID");
            Object transAmountObj = payload.get("TransAmount");
            String billRefNumber = (String) payload.get("BillRefNumber");
            String orgAccountBalance = (String) payload.get("OrgAccountBalance");
            String msisdn = (String) payload.get("MSISDN");
            
            // Parse amount
            Double transAmount = transAmountObj instanceof Number 
                ? ((Number) transAmountObj).doubleValue() 
                : Double.parseDouble(transAmountObj.toString());
            
            log.info("C2B Confirmation - TransID: {}, Amount: {}, BillRef: {}, MSISDN: {}, Balance: {}", 
                transId, transAmount, billRefNumber, msisdn, orgAccountBalance);
            
            // Process the payment
            this.processDeposit(billRefNumber, transAmount, msisdn, transId);

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

    /**
     * Handle suspense payments for unmatched transactions
     */
    private void handleSuspense(String documentNumber, BigDecimal amount, String phoneNumber, String receiptNumber, String message) {
        log.warn(message);
        sendPaymentErrorSms(phoneNumber, amount, message);
        
        SuspensePayments suspensePayments = new SuspensePayments();
        suspensePayments.setPaymentTime(LocalDateTime.now());
        suspensePayments.setDestinationAccount(documentNumber);
        suspensePayments.setAmount(String.valueOf(amount));
        suspensePayments.setStatus("NEW");
        suspensePayments.setOtherRef(receiptNumber);
        suspensePayments.setAccountNumber(phoneNumber);
        suspensePayments.setExceptionType(message);
        paymentService.saveSuspensePayment(suspensePayments);
    }

    /**
     * Process deposit to bank accounts or loan repayments
     */
    private void processDeposit(String accountRef, Double totalAmount, String phoneNumber, String receiptNumber) {
        BigDecimal amount = BigDecimal.valueOf(totalAmount);
        
        try {
            // Find customer by document number
            ClientInfo customerOpt = customerService.findByDocumentNumber(accountRef);
            if (customerOpt == null) {
                // Try finding by phone number
                Optional<Customer> customerByPhone = customerService.findByPhone(phoneNumber);
                if (customerByPhone.isPresent()) {
                    customerOpt = customerService.findById(customerByPhone.get().getId());
                }
            }

            if (customerOpt == null) {
                // Check bank account
                BankAccounts bankAccount = bankAccountRepo.findByBankAccount(accountRef);
                if (bankAccount != null) {
                    Transactions transaction = bankDepositService.processDeposit(
                            bankAccount.getCustomer().getId(),
                            amount,
                            bankAccount.getAccountType(),
                            receiptNumber,
                            "PAYBILL"
                    );
                    phoneNumber = bankAccount.getCustomer().getPhoneNumber();

                    // Send SMS confirmation
                    BigDecimal newBalance = BigDecimal.valueOf(transaction.getClosingBalance());
                    smsService.sendDepositConfirmationSms(
                            phoneNumber,
                            amount,
                            bankAccount.getBankAccount(),
                            newBalance
                    );

                    log.info("PayBill deposit processed: Customer={}, Account={}, Amount={}, Receipt={}",
                            bankAccount.getCustomer().getFirstName(), bankAccount.getBankAccount(), amount, receiptNumber);
                    return;
                }
                
                // Check loan
                Optional<LoanAccount> loanAccount = loanAccountRepo.findByLoanref(accountRef);
                if (loanAccount.isEmpty()) {
                    loanAccount = loanAccountRepo.findByOtherRef(accountRef);
                }
                
                if (loanAccount.isPresent()) {
                    LoanAccount loan = loanAccount.get();
                    Optional<Customer> customerOptional = customerService.findCustomerById(Long.parseLong(loan.getCustomerId()));
                    if (customerOptional.isEmpty()) {
                        // Try finding by document number from repository
                        customerOptional = customerService.findByPhone(phoneNumber);
                    }
                    if (customerOptional.isPresent()) {
                        phoneNumber = customerOptional.get().getPhoneNumber();
                    }
                    
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
                    return;
                }
                
                handleSuspense(accountRef, amount, phoneNumber, receiptNumber, "Customer not found by the given id");
                return;
            }

            Customer customer = customerOpt.getClient();
            phoneNumber = customer.getPhoneNumber();

            // Get customer bank accounts
            Optional<List<BankAccounts>> accountsOpt = bankAccountRepo.findByCustomer(customer);

            if (accountsOpt.isEmpty() || accountsOpt.get().isEmpty()) {
                // Check for pending loans
                LoanAccount loan = getPayableLoanAccount(customer);
                if (loan != null) {
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
                    return;
                }

                handleSuspense(accountRef, amount, phoneNumber, receiptNumber, "Account not found by the given id");
                return;
            }

            // Find SAVINGS account (or default to first account)
            BankAccounts targetAccount = accountsOpt.get().stream()
                    .filter(acc -> "ALPHA".equalsIgnoreCase(acc.getAccountType()))
                    .findFirst()
                    .orElse(accountsOpt.get().get(0));

            // Create deposit transaction
            Transactions transaction = bankDepositService.processDeposit(
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
     * Get a loan account that requires payment for the customer
     */
    private LoanAccount getPayableLoanAccount(Customer customer) {
        try {
            List<LoanAccount> loans = loanAccountRepo.findByCustomerId(customer.getId().toString());
            // Return the first active loan with outstanding balance
            return loans.stream()
                    .filter(loan -> "ACTIVE".equals(loan.getStatus()) && loan.getAccountBalance() != null && loan.getAccountBalance() > 0)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error finding payable loan account for customer {}", customer.getId(), e);
            return null;
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
}
