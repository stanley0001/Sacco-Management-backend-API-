package com.example.demo.finance.payments.services;

import com.example.demo.erp.communication.sms.SmsService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.finance.payments.entities.MpesaTransaction;
import com.example.demo.finance.payments.entities.TransactionRequest;
import com.example.demo.finance.payments.repositories.MpesaTransactionRepository;
import com.example.demo.finance.payments.repositories.TransactionRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * C2B Payment Processing Service
 * Handles Safaricom M-PESA C2B (Customer to Business) payment callbacks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class C2BPaymentProcessingService {

    private final CustomerRepository customerRepository;
    private final LoanAccountRepo loanAccountRepo;
    private final MpesaTransactionRepository mpesaTransactionRepository;
    private final TransactionRequestRepository transactionRequestRepository;
    private final TransactionApprovalService transactionApprovalService;
    private final SmsService smsService;

    /**
     * Validate C2B payment before processing
     * Returns true if payment should be accepted
     */
    public boolean validateC2BPayment(Map<String, Object> payload) {
        try {
            String billRefNumber = (String) payload.get("BillRefNumber");
            String msisdn = (String) payload.get("MSISDN");
            Object transAmountObj = payload.get("TransAmount");
            
            if (billRefNumber == null || billRefNumber.isBlank()) {
                log.warn("C2B validation failed: No bill reference number provided");
                return true; // Still accept, will go to suspense
            }
            
            if (msisdn == null || msisdn.isBlank()) {
                log.warn("C2B validation failed: No MSISDN provided");
                return false;
            }
            
            // Parse amount
            Double transAmount = transAmountObj instanceof Number 
                ? ((Number) transAmountObj).doubleValue() 
                : Double.parseDouble(transAmountObj.toString());
                
            if (transAmount == null || transAmount <= 0) {
                log.warn("C2B validation failed: Invalid amount: {}", transAmount);
                return false;
            }
            
            // Check if bill reference is a loan reference
            if (billRefNumber.startsWith("LOAN-") || billRefNumber.matches("^\\d+$")) {
                Optional<LoanAccount> loanAccount = loanAccountRepo.findByLoanref(billRefNumber);
                if (loanAccount.isEmpty()) {
                    loanAccount = loanAccountRepo.findByOtherRef(billRefNumber);
                }
                
                if (loanAccount.isPresent()) {
                    log.info("✅ C2B validation passed - Valid loan reference: {}", billRefNumber);
                    return true;
                }
            }
            
            // Check if customer exists by phone number
            String cleanMsisdn = cleanPhoneNumber(msisdn);
            Optional<Customer> customer = customerRepository.findByPhoneNumber(cleanMsisdn);
            
            if (customer.isPresent()) {
                log.info("✅ C2B validation passed - Valid customer phone: {}", cleanMsisdn);
                return true;
            }
            
            // Accept payment even if customer not found - will go to suspense for reconciliation
            log.warn("C2B validation - Customer not found for phone: {}, accepting to suspense", cleanMsisdn);
            return true;
            
        } catch (Exception e) {
            log.error("Error validating C2B payment", e);
            return false; // Reject on validation error
        }
    }

    /**
     * Process C2B payment confirmation
     */
    @Transactional
    public void processC2BPayment(Map<String, Object> payload) {
        try {
            // Extract payment details
            String transId = (String) payload.get("TransID");
            String transTime = (String) payload.get("TransTime");
            Object transAmountObj = payload.get("TransAmount");
            String billRefNumber = (String) payload.get("BillRefNumber");
            String msisdn = (String) payload.get("MSISDN");
            String firstName = (String) payload.get("FirstName");
            String lastName = (String) payload.get("LastName");
            String businessShortCode = (String) payload.get("BusinessShortCode");
            
            // Parse amount
            Double transAmount = transAmountObj instanceof Number 
                ? ((Number) transAmountObj).doubleValue() 
                : Double.parseDouble(transAmountObj.toString());
            
            BigDecimal amount = BigDecimal.valueOf(transAmount);
            String cleanMsisdn = cleanPhoneNumber(msisdn);
            String customerName = String.format("%s %s", firstName != null ? firstName : "", lastName != null ? lastName : "").trim();
            
            log.info("Processing C2B payment: TransID={}, Amount={}, BillRef={}, MSISDN={}", 
                transId, transAmount, billRefNumber, cleanMsisdn);
            
            // Check if transaction already processed
            Optional<MpesaTransaction> existingTransaction = mpesaTransactionRepository.findByMpesaReceiptNumber(transId);
            if (existingTransaction.isPresent()) {
                log.warn("C2B payment already processed: TransID={}", transId);
                return;
            }
            
            // Find customer
            Optional<Customer> customerOpt = findCustomerByPhoneOrBillRef(cleanMsisdn, billRefNumber);
            Long customerId = customerOpt.map(Customer::getId).orElse(null);
            
            // Determine if this is a loan repayment
            Long loanId = null;
            String loanReference = null;
            if (billRefNumber != null && !billRefNumber.isBlank()) {
                Optional<LoanAccount> loanAccount = findLoanByReference(billRefNumber);
                if (loanAccount.isPresent()) {
                    loanId = loanAccount.get().getAccountId();
                    loanReference = loanAccount.get().getLoanref();
                    if (customerId == null) {
                        // Get customer from loan
                        customerId = Long.valueOf(loanAccount.get().getCustomerId());
                    }
                }
            }
            
            // Create M-PESA transaction record
            MpesaTransaction mpesaTransaction = new MpesaTransaction();
            mpesaTransaction.setTransactionType(MpesaTransaction.TransactionType.C2B_PAYMENT);
            mpesaTransaction.setAmount(amount);
            mpesaTransaction.setPhoneNumber(cleanMsisdn);
            mpesaTransaction.setMpesaReceiptNumber(transId);
            mpesaTransaction.setTransactionDate(parseTransactionTime(transTime));
            mpesaTransaction.setStatus(MpesaTransaction.TransactionStatus.SUCCESS);
            mpesaTransaction.setResultCode("0");
            mpesaTransaction.setResultDesc("Payment received successfully");
            mpesaTransaction.setCustomerId(customerId);
            mpesaTransaction.setLoanId(loanId);
            mpesaTransaction.setAccountReference(billRefNumber);
            mpesaTransaction.setTransactionDesc(String.format("C2B payment from %s", customerName));
            mpesaTransaction.setCreatedAt(LocalDateTime.now());
            mpesaTransaction.setCallbackReceived(true);
            
            mpesaTransaction = mpesaTransactionRepository.save(mpesaTransaction);
            log.info("✅ M-PESA transaction saved: ID={}, Receipt={}", mpesaTransaction.getId(), transId);
            
            // Create transaction request for processing
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setType(TransactionRequest.TransactionType.DEPOSIT);
            transactionRequest.setTransactionCategory(loanId != null 
                ? TransactionRequest.TransactionCategory.LOAN_REPAYMENT 
                : TransactionRequest.TransactionCategory.SAVINGS_DEPOSIT);
            transactionRequest.setCustomerId(customerId);
            transactionRequest.setCustomerName(customerName);
            transactionRequest.setPhoneNumber(cleanMsisdn);
            transactionRequest.setAmount(amount);
            transactionRequest.setStatus(TransactionRequest.RequestStatus.SUCCESS);
            transactionRequest.setPaymentMethod(TransactionRequest.PaymentMethodType.MPESA);
            transactionRequest.setPaymentChannel(TransactionRequest.PaymentChannel.C2B);
            transactionRequest.setDescription(String.format("C2B payment - %s", billRefNumber != null ? billRefNumber : "General"));
            transactionRequest.setReferenceNumber(transId);
            transactionRequest.setInitiatedBy("MPESA_C2B");
            transactionRequest.setInitiatedAt(LocalDateTime.now());
            transactionRequest.setProcessedAt(LocalDateTime.now());
            transactionRequest.setPostedToAccount(false);
            transactionRequest.setLoanId(loanId);
            transactionRequest.setLoanReference(loanReference);
            transactionRequest.setMpesaTransactionId(mpesaTransaction.getId());
            
            transactionRequest = transactionRequestRepository.save(transactionRequest);
            log.info("✅ Transaction request created: ID={}", transactionRequest.getId());
            
            // Link M-PESA transaction to request
            mpesaTransaction.setTransactionRequestId(transactionRequest.getId());
            mpesaTransactionRepository.save(mpesaTransaction);
            
            // Auto-approve and post the transaction
            try {
                transactionApprovalService.approveTransaction(
                    transactionRequest.getId(), 
                    "MPESA_C2B_AUTO", 
                    transId
                );
                log.info("✅ C2B payment auto-approved and posted: TransID={}, Amount={}", transId, transAmount);
                
                // Send SMS confirmation
                sendPaymentConfirmationSms(cleanMsisdn, customerName, transAmount, transId, loanReference != null);
                
            } catch (Exception e) {
                log.error("Error auto-approving C2B payment: TransID={}, Error: {}", transId, e.getMessage());
                // Payment is saved but needs manual approval
            }
            
        } catch (Exception e) {
            log.error("Error processing C2B payment", e);
            throw new RuntimeException("Failed to process C2B payment: " + e.getMessage(), e);
        }
    }

    /**
     * Find customer by phone number or bill reference
     */
    private Optional<Customer> findCustomerByPhoneOrBillRef(String phoneNumber, String billRefNumber) {
        // Try phone number first
        Optional<Customer> customer = customerRepository.findByPhoneNumber(phoneNumber);
        
        // If not found and bill ref looks like a customer ID, try that
        if (customer.isEmpty() && billRefNumber != null && billRefNumber.matches("^\\d+$")) {
            try {
                Long customerId = Long.parseLong(billRefNumber);
                customer = customerRepository.findById(customerId);
            } catch (NumberFormatException e) {
                // Not a valid ID
            }
        }
        
        return customer;
    }

    /**
     * Find loan by reference number
     */
    private Optional<LoanAccount> findLoanByReference(String reference) {
        if (reference == null || reference.isBlank()) {
            return Optional.empty();
        }
        
        // Try direct loan reference
        Optional<LoanAccount> loan = loanAccountRepo.findByLoanref(reference);
        if (loan.isPresent()) {
            return loan;
        }
        
        // Try other reference
        loan = loanAccountRepo.findByOtherRef(reference);
        if (loan.isPresent()) {
            return loan;
        }
        
        // Try with LOAN- prefix
        if (!reference.startsWith("LOAN-")) {
            loan = loanAccountRepo.findByLoanref("LOAN-" + reference);
            if (loan.isPresent()) {
                return loan;
            }
        }
        
        return Optional.empty();
    }

    /**
     * Parse M-PESA transaction time
     * Format: YYYYMMDDHHmmss (e.g., 20191122063845)
     */
    private LocalDateTime parseTransactionTime(String transTime) {
        try {
            if (transTime != null && transTime.length() == 14) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                return LocalDateTime.parse(transTime, formatter);
            }
        } catch (Exception e) {
            log.warn("Failed to parse transaction time: {}, using current time", transTime);
        }
        return LocalDateTime.now();
    }

    /**
     * Clean and format phone number
     */
    private String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        // Remove any spaces, dashes, or special characters
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Ensure it starts with country code
        if (cleaned.startsWith("0")) {
            cleaned = "254" + cleaned.substring(1);
        } else if (!cleaned.startsWith("254") && !cleaned.startsWith("+254")) {
            cleaned = "254" + cleaned;
        }
        
        // Remove + if present
        cleaned = cleaned.replace("+", "");
        
        return cleaned;
    }

    /**
     * Send payment confirmation SMS
     */
    private void sendPaymentConfirmationSms(String phoneNumber, String customerName, Double amount, String receiptNumber, boolean isLoanPayment) {
        try {
            String messageType = isLoanPayment ? "loan repayment" : "deposit";
            String message = String.format(
                "Dear %s, we have received your %s of KES %,.2f via M-PESA. Receipt No: %s. Thank you for banking with us.",
                customerName.isBlank() ? "Customer" : customerName,
                messageType,
                amount,
                receiptNumber
            );
            
            smsService.sendSms(phoneNumber, message);
            log.info("✅ Payment confirmation SMS sent to {}", phoneNumber);
            
        } catch (Exception e) {
            log.error("Failed to send payment confirmation SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }
}
