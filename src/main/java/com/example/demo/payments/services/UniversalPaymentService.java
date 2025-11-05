package com.example.demo.payments.services;

import com.example.demo.payments.dto.DepositRequestCommand;
import com.example.demo.payments.dto.STKPushRequest;
import com.example.demo.payments.dto.STKPushResponse;
import com.example.demo.payments.dto.UniversalPaymentRequest;
import com.example.demo.payments.dto.UniversalPaymentResponse;
import com.example.demo.payments.entities.MpesaTransaction;
import com.example.demo.payments.entities.TransactionRequest;
import com.example.demo.payments.repositories.MpesaTransactionRepository;
import com.example.demo.savingsManagement.services.SavingsAccountService;
import com.example.demo.savingsManagement.persistence.entities.SavingsTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Universal Payment Service for handling payments across the entire platform
 * Provides consistent M-PESA STK Push and SMS notification integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UniversalPaymentService {

    private final MpesaService mpesaService;
    private final TransactionRequestService transactionRequestService;
    private final com.example.demo.sms.SmsService smsService;
    private final SavingsAccountService savingsAccountService;
    private final MpesaTransactionRepository mpesaTransactionRepository;

    /**
     * Process payment with automatic M-PESA integration and SMS notifications
     * This method can be used across the platform for consistent payment handling
     */
    public UniversalPaymentResponse processPayment(UniversalPaymentRequest request) {
        log.info("Processing universal payment: Customer={}, Amount={}, Method={}, Type={}", 
            request.getCustomerId(), request.getAmount(), request.getPaymentMethod(), request.getTransactionType());

        try {
            // Determine payment method and process accordingly
            if ("MPESA".equalsIgnoreCase(request.getPaymentMethod())) {
                return processMpesaPayment(request);
            } else {
                return processManualPayment(request);
            }
        } catch (Exception e) {
            log.error("Error processing universal payment", e);
            
            // Send error SMS notification
            try {
                String errorMessage = String.format(
                    "Payment processing failed for KES %,.2f. Error: %s. Please contact support if needed. HelaSuite",
                    request.getAmount(), e.getMessage()
                );
                smsService.sendSms(request.getPhoneNumber(), errorMessage);
            } catch (Exception smsEx) {
                log.error("Failed to send error notification SMS", smsEx);
            }
            
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Process M-PESA payment with STK Push
     */
    private UniversalPaymentResponse processMpesaPayment(UniversalPaymentRequest request) {
        log.info("Initiating M-PESA STK Push for customer: {}", request.getCustomerId());

        String checkoutRequestId = null;
        TransactionRequest savedRequest = null;

        try {
            TransactionRequest.TransactionType transactionType = resolveTransactionType(request);
            TransactionRequest.TransactionCategory transactionCategory = resolveTransactionCategory(request);

            DepositRequestCommand command = DepositRequestCommand.builder()
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .phoneNumber(request.getPhoneNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .initiatedBy(request.getInitiatedBy())
                .savingsAccountId(request.getSavingsAccountId())
                .targetAccountId(request.getTargetAccountId())
                .loanId(request.getLoanId())
                .loanReference(request.getLoanReference())
                .paymentMethod(TransactionRequest.PaymentMethodType.MPESA)
                .transactionType(transactionType)
                .transactionCategory(transactionCategory)
                .paymentChannel(TransactionRequest.PaymentChannel.MPESA)
                .initialStatus(TransactionRequest.RequestStatus.PROCESSING)
                .providerConfigId(request.getProviderConfigId())
                .referenceNumber(request.getReferenceNumber())
                .build();

            savedRequest = transactionRequestService.createDepositRequest(command);

            STKPushRequest stkRequest = STKPushRequest.builder()
                .phoneNumber(request.getPhoneNumber())
                .amount(request.getAmount())
                .accountReference(buildAccountReference(request))
                .transactionDesc(buildTransactionDescription(request))
                .customerId(request.getCustomerId())
                .accountId(request.getTargetAccountId() != null ? request.getTargetAccountId() : request.getSavingsAccountId())
                .loanId(request.getLoanId())
                .savingsAccountId(request.getSavingsAccountId())
                .bankAccountId(request.getTargetAccountId())
                .providerConfigId(request.getProviderConfigId())
                .providerCode(request.getProviderCode())
                .transactionRequestId(savedRequest.getId())
                .build();

            STKPushResponse stkResponse = mpesaService.initiateSTKPush(stkRequest);
            checkoutRequestId = stkResponse.getCheckoutRequestId();

            MpesaTransaction mpesaTransaction = mpesaTransactionRepository
                .findByCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() -> new RuntimeException("M-PESA transaction not persisted"));

            if (stkRequest.getBankAccountId() != null) {
                mpesaTransaction.setBankAccountId(stkRequest.getBankAccountId());
            }
            mpesaTransaction.setTransactionRequestId(savedRequest.getId());
            mpesaTransactionRepository.save(mpesaTransaction);

            TransactionRequest linkedRequest = transactionRequestService.linkMpesaTransaction(savedRequest.getId(), mpesaTransaction.getId());

            if (request.getSavingsAccountId() != null && "DEPOSIT".equalsIgnoreCase(request.getTransactionType())) {
                createPendingDepositAsync(request, checkoutRequestId);
            }

            return UniversalPaymentResponse.builder()
                .success(true)
                .paymentMethod("MPESA")
                .checkoutRequestId(checkoutRequestId)
                .merchantRequestId(stkResponse.getMerchantRequestId())
                .responseCode(stkResponse.getResponseCode())
                .responseDescription(stkResponse.getResponseDescription())
                .customerMessage("M-PESA STK Push sent to your phone! Enter your PIN to complete the payment.")
                .requiresStatusCheck(true)
                .transactionRequestId(linkedRequest.getId())
                .build();

        } catch (Exception e) {
            log.error("Error initiating M-PESA STK Push: {}", e.getMessage());

            if (savedRequest != null) {
                transactionRequestService.updateStatus(
                    savedRequest.getId(),
                    TransactionRequest.RequestStatus.FAILED,
                    "MPESA_STK_INIT",
                    e.getMessage()
                );
            }

            sendMpesaFailureSms(request.getPhoneNumber(), request.getAmount(), checkoutRequestId);

            throw new RuntimeException("Failed to initiate M-PESA payment: " + e.getMessage(), e);
        }
    }

    private TransactionRequest.TransactionType resolveTransactionType(UniversalPaymentRequest request) {
        if (request.getTransactionType() != null) {
            try {
                return TransactionRequest.TransactionType.valueOf(request.getTransactionType().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                log.warn("Unknown transaction type '{}', defaulting to DEPOSIT", request.getTransactionType());
            }
        }
        return request.getLoanId() != null
            ? TransactionRequest.TransactionType.LOAN_REPAYMENT
            : TransactionRequest.TransactionType.DEPOSIT;
    }

    private TransactionRequest.TransactionCategory resolveTransactionCategory(UniversalPaymentRequest request) {
        if (request.getLoanId() != null
            || "LOAN_REPAYMENT".equalsIgnoreCase(request.getTransactionType())) {
            return TransactionRequest.TransactionCategory.LOAN_REPAYMENT;
        }
        return TransactionRequest.TransactionCategory.SAVINGS_DEPOSIT;
    }

    /**
     * Process manual payment (Cash, Bank, Cheque, etc.)
     */
    private UniversalPaymentResponse processManualPayment(UniversalPaymentRequest request) {
        log.info("Processing manual payment for customer: {}", request.getCustomerId());

        // Create transaction request for manual processing/approval
        TransactionRequest transactionRequest = transactionRequestService.createDepositRequest(
            request.getCustomerId(),
            request.getCustomerName(),
            request.getPhoneNumber(),
            request.getAmount(),
            request.getDescription(),
            request.getInitiatedBy(),
            request.getSavingsAccountId(),
            request.getTargetAccountId(),
            TransactionRequest.PaymentMethodType.valueOf(request.getPaymentMethod())
        );

        // Send confirmation SMS for manual payment
        String message = String.format(
            "Payment request of KES %,.2f submitted successfully. Reference: %s. " +
            "Your payment is being processed and will be credited upon verification. HelaSuite",
            request.getAmount(), transactionRequest.getId()
        );
        smsService.sendSms(request.getPhoneNumber(), message);

        return UniversalPaymentResponse.builder()
            .success(true)
            .paymentMethod(request.getPaymentMethod())
            .transactionRequestId(transactionRequest.getId())
            .referenceNumber(transactionRequest.getId().toString())
            .responseDescription("Manual payment request submitted successfully")
            .customerMessage("Payment request submitted. You will be notified once processed.")
            .requiresStatusCheck(false)
            .build();
    }


    /**
     * Check payment status for M-PESA transactions
     */
    public UniversalPaymentResponse checkPaymentStatus(String checkoutRequestId) {
        log.info("Checking payment status for checkout request: {}", checkoutRequestId);
        
        try {
            // Use MpesaService to check the status
            var statusResponse = mpesaService.checkSTKPushStatus(checkoutRequestId);
            
            boolean isCompleted = "0".equals(statusResponse.getResultCode());
            boolean isCancelled = "1032".equals(statusResponse.getResultCode());
            boolean isFailed = "1".equals(statusResponse.getResultCode()) && !"1037".equals(statusResponse.getResultCode());
            
            return UniversalPaymentResponse.builder()
                .success(true)
                .paymentMethod("MPESA")
                .checkoutRequestId(checkoutRequestId)
                .merchantRequestId(statusResponse.getMerchantRequestId())
                .responseCode(statusResponse.getResultCode())
                .responseDescription(statusResponse.getResultDesc())
                .transactionId(statusResponse.getTransactionId())
                .amount(statusResponse.getAmount())
                .phoneNumber(statusResponse.getPhoneNumber())
                .paymentCompleted(isCompleted)
                .completed(isCompleted) // For backward compatibility
                .cancelled(isCancelled)
                .failed(isFailed)
                .customerMessage(buildStatusMessage(statusResponse))
                .build();
                
        } catch (Exception e) {
            log.error("Error checking payment status for checkout: {}", checkoutRequestId, e);
            
            return UniversalPaymentResponse.builder()
                .success(false)
                .checkoutRequestId(checkoutRequestId)
                .errorCode("STATUS_CHECK_ERROR")
                .errorMessage(e.getMessage())
                .customerMessage("Unable to check payment status. Please try again.")
                .build();
        }
    }

    /**
     * Get transaction status from database (for frontend polling)
     */
    public UniversalPaymentResponse getTransactionStatusFromDB(String checkoutRequestId) {
        log.debug("Getting transaction status from database for: {}", checkoutRequestId);
        
        try {
            // Get transaction from database
            Optional<MpesaTransaction> transactionOpt = mpesaTransactionRepository.findByCheckoutRequestId(checkoutRequestId);
            
            if (transactionOpt.isEmpty()) {
                return UniversalPaymentResponse.builder()
                    .success(false)
                    .checkoutRequestId(checkoutRequestId)
                    .errorCode("TRANSACTION_NOT_FOUND")
                    .customerMessage("Transaction not found")
                    .build();
            }
            
            MpesaTransaction transaction = transactionOpt.get();
            MpesaTransaction.TransactionStatus status = transaction.getStatus();
            
            boolean isCompleted = status == MpesaTransaction.TransactionStatus.SUCCESS;
            boolean isCancelled = status == MpesaTransaction.TransactionStatus.CANCELLED;
            boolean isFailed = status == MpesaTransaction.TransactionStatus.FAILED;
            
            return UniversalPaymentResponse.builder()
                .success(true)
                .paymentMethod("MPESA")
                .checkoutRequestId(checkoutRequestId)
                .merchantRequestId(transaction.getMerchantRequestId())
                .responseCode(transaction.getResultCode())
                .responseDescription(transaction.getResultDesc())
                .transactionId(transaction.getMpesaReceiptNumber())
                .amount(transaction.getAmount())
                .phoneNumber(transaction.getPhoneNumber())
                .paymentCompleted(isCompleted)
                .completed(isCompleted) // For backward compatibility
                .cancelled(isCancelled)
                .failed(isFailed)
                .customerMessage(buildDatabaseStatusMessage(transaction))
                .build();
                
        } catch (Exception e) {
            log.error("Error getting transaction status from DB for: {}", checkoutRequestId, e);
            
            return UniversalPaymentResponse.builder()
                .success(false)
                .checkoutRequestId(checkoutRequestId)
                .errorCode("DB_ERROR")
                .errorMessage(e.getMessage())
                .customerMessage("Unable to get transaction status")
                .build();
        }
    }
    
    /**
     * Build user-friendly status message from database transaction
     */
    private String buildDatabaseStatusMessage(MpesaTransaction transaction) {
        MpesaTransaction.TransactionStatus status = transaction.getStatus();
        
        switch (status) {
            case SUCCESS:
                return "Payment completed successfully. Receipt: " + transaction.getMpesaReceiptNumber();
            case CANCELLED:
                return "Payment was cancelled. Please try again when ready.";
            case FAILED:
                return "Payment failed: " + (transaction.getResultDesc() != null ? transaction.getResultDesc() : "Please try again");
            case PENDING:
                return "Payment is still being processed. Please wait...";
            default:
                return "Payment status: " + status.toString();
        }
    }
    
    /**
     * Build user-friendly status message
     */
    private String buildStatusMessage(com.example.demo.payments.dto.MpesaDepositStatusResponse statusResponse) {
        String resultCode = statusResponse.getResultCode();
        
        if ("0".equals(resultCode)) {
            return "Payment completed successfully. Receipt: " + statusResponse.getTransactionId();
        } else if ("1032".equals(resultCode)) {
            return "Payment was cancelled. Please try again when ready.";
        } else if ("1".equals(resultCode)) {
            return "Payment failed: " + statusResponse.getResultDesc();
        } else if ("1037".equals(resultCode)) {
            return "Payment is still pending. Please check your phone or try again.";
        } else {
            return "Payment status: " + statusResponse.getResultDesc();
        }
    }

    /**
     * Send payment reminder SMS
     */
    public void sendPaymentReminder(String phoneNumber, String customerName, BigDecimal amount, String dueDate, String accountType) {
        String message;
        if ("LOAN".equalsIgnoreCase(accountType)) {
            smsService.sendPaymentReminderSms(phoneNumber, customerName, amount, dueDate);
        } else {
            message = String.format(
                "Dear %s, this is a reminder about your %s payment of KES %,.2f due on %s. " +
                "Please ensure timely payment. HelaSuite.",
                customerName, accountType, amount, dueDate
            );
            smsService.sendSms(phoneNumber, message);
        }
    }

    /**
     * Send overdue payment notification
     */
    public void sendOverdueNotification(String phoneNumber, String customerName, BigDecimal amount, int daysOverdue, String accountType) {
        if ("LOAN".equalsIgnoreCase(accountType)) {
            smsService.sendOverduePaymentSms(phoneNumber, customerName, amount, daysOverdue);
        } else {
            String message = String.format(
                "Dear %s, your %s payment of KES %,.2f is %d days overdue. " +
                "Please make payment immediately to avoid service interruption. HelaSuite.",
                customerName, accountType, amount, daysOverdue
            );
            smsService.sendSms(phoneNumber, message);
        }
    }

    /**
     * Build account reference for M-PESA transactions
     */
    private String buildAccountReference(UniversalPaymentRequest request) {
        if (request.getLoanId() != null) {
            return "LOAN-" + request.getLoanId();
        }
        if (request.getSavingsAccountId() != null) {
            return "SAV-" + request.getSavingsAccountId();
        }
        if (request.getCustomerId() != null) {
            return "CUST-" + request.getCustomerId();
        }
        return "GENERAL-" + System.currentTimeMillis();
    }

    /**
     * Build transaction description for M-PESA transactions
     */
    private String buildTransactionDescription(UniversalPaymentRequest request) {
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            return request.getDescription();
        }
        
        if (request.getLoanId() != null) {
            return "Loan Repayment";
        }
        if (request.getSavingsAccountId() != null) {
            return "Savings Deposit";
        }
        if ("DEPOSIT".equalsIgnoreCase(request.getTransactionType())) {
            return "Account Deposit";
        }
        if ("LOAN_REPAYMENT".equalsIgnoreCase(request.getTransactionType())) {
            return "Loan Payment";
        }
        
        return "Payment via HelaSuite";
    }
    
    /**
     * Process payment request from Map (for bulk processing compatibility)
     */
    public UniversalPaymentResponse processPaymentRequest(Map<String, Object> paymentData) {
        try {
            // Convert Map to UniversalPaymentRequest
            UniversalPaymentRequest request = new UniversalPaymentRequest();
            
            if (paymentData.get("customerId") != null) {
                request.setCustomerId(Long.valueOf(paymentData.get("customerId").toString()));
            }
            if (paymentData.get("amount") != null) {
                request.setAmount(new BigDecimal(paymentData.get("amount").toString()));
            }
            if (paymentData.get("phoneNumber") != null) {
                request.setPhoneNumber(paymentData.get("phoneNumber").toString());
            }
            if (paymentData.get("paymentMethod") != null) {
                request.setPaymentMethod(paymentData.get("paymentMethod").toString());
            } else {
                request.setPaymentMethod("MPESA"); // Default to M-PESA
            }
            if (paymentData.get("transactionType") != null) {
                request.setTransactionType(paymentData.get("transactionType").toString());
            } else {
                request.setTransactionType("DEPOSIT"); // Default to deposit
            }
            if (paymentData.get("description") != null) {
                request.setDescription(paymentData.get("description").toString());
            }
            
            return processPayment(request);
            
        } catch (Exception e) {
            log.error("Error processing payment request from Map: {}", e.getMessage());
            UniversalPaymentResponse response = new UniversalPaymentResponse();
            response.setSuccess(false);
            response.setResponseDescription("Payment processing failed: " + e.getMessage());
            response.setCustomerMessage("Payment failed. Please try again.");
            return response;
        }
    }
    
    /**
     * Send M-PESA failure SMS with paybill alternative
     */
    private void sendMpesaFailureSms(String phoneNumber, BigDecimal amount, String checkoutRequestId) {
        try {
            String paybillNumber = "400200"; // Default paybill - should be configurable
            String accountNumber = checkoutRequestId != null ? checkoutRequestId.substring(0, Math.min(8, checkoutRequestId.length())) : "ACC" + System.currentTimeMillis();
            
            String failureMessage = String.format(
                "M-PESA payment of KES %,.2f failed. Alternative: Use Paybill %s, Account Number: %s. Or try STK push again. HelaSuite",
                amount, paybillNumber, accountNumber
            );
            
            smsService.sendSms(phoneNumber, failureMessage);
            log.info("Sent M-PESA failure SMS to: {} with paybill option", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send M-PESA failure SMS: {}", e.getMessage());
        }
    }
    
    /**
     * Send M-PESA success confirmation SMS
     */
    public void sendMpesaSuccessSms(String phoneNumber, BigDecimal amount, String receiptNumber, BigDecimal newBalance) {
        try {
            String successMessage = String.format(
                "Payment Confirmed! KES %,.2f received via M-PESA. Receipt: %s. New Balance: KES %,.2f. Thank you! HelaSuite",
                amount, receiptNumber != null ? receiptNumber : "Pending", newBalance
            );
            
            smsService.sendSms(phoneNumber, successMessage);
            log.info("Sent M-PESA success SMS to: {} for amount: {}", phoneNumber, amount);
        } catch (Exception e) {
            log.error("Failed to send M-PESA success SMS: {}", e.getMessage());
        }
    }

    /**
     * Create pending deposit record asynchronously to avoid transaction rollback issues
     */
    private void createPendingDepositAsync(UniversalPaymentRequest request, String checkoutRequestId) {
        // Run in a separate thread to completely isolate from main transaction
        new Thread(() -> {
            try {
                log.info("Creating pending deposit record of {} for account: {}", 
                    request.getAmount(), request.getSavingsAccountId());
                    
                savingsAccountService.createPendingDeposit(
                    request.getSavingsAccountId(),
                    request.getAmount(),
                    "M-PESA STK Push",
                    checkoutRequestId,
                    String.format("M-PESA deposit pending - Checkout ID: %s", checkoutRequestId),
                    request.getInitiatedBy() != null ? request.getInitiatedBy() : "MPESA_SYSTEM"
                );
                
                log.info("Successfully created pending deposit record for checkout: {}", checkoutRequestId);
            } catch (Exception e) {
                log.warn("Could not create pending deposit record (non-critical): {}", e.getMessage());
                // This is non-critical - the STK push will still work and be tracked via M-PESA callbacks
            }
        }).start();
    }

    /**
     * Get customer M-PESA transactions
     */
    public Map<String, Object> getCustomerTransactions(Long customerId, int page, int size) {
        log.info("Getting M-PESA transactions for customer: {}", customerId);
        
        try {
            // This would integrate with your transaction repository
            // For now, return structure that frontend expects
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("transactions", java.util.Collections.emptyList());
            response.put("totalElements", 0);
            response.put("totalPages", 0);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("message", "M-PESA transaction history - integrate with MpesaTransaction repository");
            
            return response;
        } catch (Exception e) {
            log.error("Error getting customer transactions", e);
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get transactions: " + e.getMessage());
            errorResponse.put("transactions", java.util.Collections.emptyList());
            errorResponse.put("totalElements", 0);
            return errorResponse;
        }
    }
}
