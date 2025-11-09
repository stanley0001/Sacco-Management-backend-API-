package com.example.demo.finance.payments.services;

import com.example.demo.erp.communication.sms.SmsService;
import com.example.demo.finance.banking.parsitence.enitities.Transactions;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.services.LoanPaymentService;
import com.example.demo.finance.loanManagement.services.PaymentProcessingHub;
import com.example.demo.finance.payments.entities.TransactionRequest;
import com.example.demo.finance.payments.repositories.TransactionRequestRepository;
import com.example.demo.finance.savingsManagement.persistence.entities.SavingsAccount;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.finance.payments.dto.*;
import com.example.demo.finance.payments.dto.C2BRegisterUrlRequest;
import com.example.demo.finance.payments.entities.MpesaConfig;
import com.example.demo.finance.payments.entities.MpesaTransaction;
import com.example.demo.finance.payments.repositories.MpesaTransactionRepository;
import com.example.demo.finance.savingsManagement.services.SavingsAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaService {
    
    // Circuit breaker pattern for M-PESA API resilience
    private final java.util.concurrent.atomic.AtomicInteger failureCount = new java.util.concurrent.atomic.AtomicInteger(0);
    private final java.util.concurrent.atomic.AtomicLong lastFailureTime = new java.util.concurrent.atomic.AtomicLong(0);
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;
    private static final long CIRCUIT_BREAKER_TIMEOUT = 5 * 60 * 1000; // 5 minutes
    
    // Performance monitoring
    private final java.util.concurrent.atomic.AtomicLong totalTransactions = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong successfulTransactions = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicReference<Double> averageResponseTime = new java.util.concurrent.atomic.AtomicReference<>(0.0);
    
    private final MpesaAuthService authService;
    private final MpesaConfigService mpesaConfigService;
    private final MpesaTransactionRepository transactionRepository;
    private final TransactionRequestRepository transactionRequestRepository;
    private final TransactionApprovalService transactionApprovalService;
    private final SavingsAccountService savingsAccountService;
    private final BankDepositService bankDepositService;
    private final LoanAccountRepo loanAccountRepo;
    private final LoanPaymentService loanPaymentService;
    private final PaymentProcessingHub paymentProcessingHub;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SmsService smsService;
    
    /**
     * Initiate STK Push (Lipa Na M-PESA Online)
     */
    public STKPushResponse initiateSTKPush(STKPushRequest request) {
        log.info("Initiating STK Push for phone: {}, amount: {}",
            request.getPhoneNumber(), request.getAmount());

        MpesaTransaction pendingTransaction = null;

        try {
            MpesaConfig config = mpesaConfigService.getActiveConfiguration(
                request.getProviderConfigId(), request.getProviderCode());

            String formattedPhoneNumber = request.getFormattedPhoneNumber();
            pendingTransaction = createPendingTransactionLog(request, config, formattedPhoneNumber);

            // Generate timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // Generate password
            String password = Base64.getEncoder().encodeToString(
                (config.getShortcode() + config.getPasskey() + timestamp).getBytes()
            );

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("BusinessShortCode", config.getShortcode());
            requestBody.put("Password", password);
            requestBody.put("Timestamp", timestamp);
            requestBody.put("TransactionType", "CustomerPayBillOnline");
            requestBody.put("Amount", request.getAmount().intValue());
            requestBody.put("PartyA", formattedPhoneNumber);
            requestBody.put("PartyB", config.getShortcode());
            requestBody.put("PhoneNumber", formattedPhoneNumber);
            // Use the specific STK callback URL if available, otherwise fall back to default
            String callbackUrl = config.getStkCallbackUrl() != null ? config.getStkCallbackUrl() : config.getCallbackUrl();
            requestBody.put("CallBackURL", callbackUrl);
            log.info("Using STK callback URL: {}", callbackUrl);
            requestBody.put("AccountReference", request.getAccountReference());
            requestBody.put("TransactionDesc", request.getTransactionDesc());

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authService.getAccessToken(
                request.getProviderConfigId(), request.getProviderCode()));
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make API call
            String url = config.getApiUrl() + "/mpesa/stkpush/v1/processrequest";
            log.debug("Calling M-PESA API: {}", url);

            ResponseEntity<STKPushResponse> response = restTemplate.postForEntity(url, entity, STKPushResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                STKPushResponse stkResponse = response.getBody();
                updateTransactionWithResponse(pendingTransaction, request, stkResponse, formattedPhoneNumber, config);
                log.info("STK Push initiated successfully: {}", stkResponse.getCheckoutRequestId());
                return stkResponse;
            }

            String errorMessage = "STK Push failed: " + response.getStatusCode();
            log.error(errorMessage);
            markTransactionFailed(pendingTransaction, errorMessage, String.valueOf(response.getStatusCodeValue()));
            throw new RuntimeException(errorMessage);

        } catch (Exception e) {
            if (pendingTransaction != null && pendingTransaction.getStatus() == MpesaTransaction.TransactionStatus.PENDING) {
                markTransactionFailed(pendingTransaction, e.getMessage(), null);
            }
            log.error("Error initiating STK Push", e);
            throw new RuntimeException("Failed to initiate STK Push: " + e.getMessage(), e);
        }
    }

    private BigDecimal resolveLoanBalance(Long loanId) {
        try {
            LoanAccount loanAccount = loanAccountRepo.findById(loanId).orElse(null);
            if (loanAccount == null || loanAccount.getAccountBalance() == null) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(loanAccount.getAccountBalance())
                .setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            log.warn("Unable to resolve loan balance for loan {}: {}", loanId, ex.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private SavingsAccount resolveSavingsAccount(Long savingsAccountId) {
        try {
            return savingsAccountService.getAccountById(savingsAccountId);
        } catch (Exception ex) {
            log.warn("Unable to resolve savings account {}: {}", savingsAccountId, ex.getMessage());
            return null;
        }
    }
    
    /**
     * Save transaction to database
     */
    private MpesaTransaction createPendingTransactionLog(STKPushRequest request, MpesaConfig config, String formattedPhoneNumber) {
        MpesaTransaction transaction = new MpesaTransaction();
        transaction.setMerchantRequestId("PENDING-" + UUID.randomUUID());
        transaction.setCheckoutRequestId("PENDING-" + UUID.randomUUID());
        transaction.setTransactionType(MpesaTransaction.TransactionType.STK_PUSH);
        transaction.setPhoneNumber(formattedPhoneNumber);
        transaction.setAmount(request.getAmount());
        transaction.setAccountReference(request.getAccountReference());
        transaction.setTransactionDesc(request.getTransactionDesc());
        transaction.setStatus(MpesaTransaction.TransactionStatus.PENDING);
        transaction.setCustomerId(request.getCustomerId());
        transaction.setLoanId(request.getLoanId());
        transaction.setSavingsAccountId(request.getSavingsAccountId());
        transaction.setProviderConfigId(config.getId());
        transaction.setProviderCode(config.getConfigName());
        transaction.setCallbackReceived(false);
        transaction.setResultDesc("Awaiting STK Push response");
        transaction.setTransactionRequestId(request.getTransactionRequestId());
        transaction.setBankAccountId(request.getBankAccountId());

        MpesaTransaction savedTransaction = transactionRepository.save(transaction);
        log.info("Pending M-PESA transaction saved with ID: {}", savedTransaction.getId());
        return savedTransaction;
    }

    private void updateTransactionWithResponse(MpesaTransaction transaction, STKPushRequest request,
                                               STKPushResponse response, String formattedPhoneNumber,
                                               MpesaConfig config) {
        if (response.getMerchantRequestId() != null && !response.getMerchantRequestId().isBlank()) {
            transaction.setMerchantRequestId(response.getMerchantRequestId());
        }
        if (response.getCheckoutRequestId() != null && !response.getCheckoutRequestId().isBlank()) {
            transaction.setCheckoutRequestId(response.getCheckoutRequestId());
        }

        transaction.setPhoneNumber(formattedPhoneNumber);
        transaction.setAmount(request.getAmount());
        transaction.setAccountReference(request.getAccountReference());
        transaction.setTransactionDesc(request.getTransactionDesc());
        transaction.setResultCode(response.getResponseCode());
        transaction.setResultDesc(response.getResponseDescription());
        transaction.setStatus(MpesaTransaction.TransactionStatus.PENDING);
        transaction.setProviderConfigId(config.getId());
        transaction.setProviderCode(config.getConfigName());
        transaction.setTransactionRequestId(request.getTransactionRequestId());

        transactionRepository.save(transaction);
        log.info("M-PESA transaction updated with checkout ID: {}", transaction.getCheckoutRequestId());
    }

    private void markTransactionFailed(MpesaTransaction transaction, String errorMessage, String resultCode) {
        transaction.setStatus(MpesaTransaction.TransactionStatus.FAILED);
        if (resultCode != null) {
            transaction.setResultCode(resultCode);
        } else {
            transaction.setResultCode("FAILED");
        }
        transaction.setResultDesc(errorMessage != null ? errorMessage : "STK Push initiation failed");
        transactionRepository.save(transaction);
        log.info("M-PESA transaction {} marked as FAILED", transaction.getId());
    }

    private void handleFailedPayment(MpesaTransaction transaction, String failureReason) {
        String resolvedReason = (failureReason != null && !failureReason.isBlank())
            ? failureReason
            : "Payment failed";

        try {
            transaction.setResultDesc(resolvedReason);
            transactionRepository.save(transaction);

            if (transaction.getTransactionRequestId() != null) {
                transactionRequestRepository.findById(transaction.getTransactionRequestId()).ifPresent(request -> {
                    TransactionRequest.RequestStatus status =
                        transaction.getStatus() == MpesaTransaction.TransactionStatus.CANCELLED
                            ? TransactionRequest.RequestStatus.CANCELLED
                            : TransactionRequest.RequestStatus.FAILED;

                    request.setStatus(status);
                    request.setFailureReason(resolvedReason);
                    request.setServiceProviderResponse(resolvedReason);
                    request.setProcessedAt(LocalDateTime.now());
                    request.setProcessedBy("MPESA_STATUS_CHECK");

                    transactionRequestRepository.save(request);
                });
            }

            if (transaction.getPhoneNumber() != null && transaction.getAmount() != null) {
                String failureMessage = String.format(
                    "M-PESA payment of KES %,.2f was not completed. Reason: %s. If funds were deducted, please contact support.",
                    transaction.getAmount(),
                    resolvedReason
                );
                smsService.sendSms(transaction.getPhoneNumber(), failureMessage);
            }

            log.info("Handled failed MPESA transaction {} with status {}", transaction.getId(), transaction.getStatus());

        } catch (Exception ex) {
            log.error("Error while handling failed MPESA transaction {}", transaction.getId(), ex);
        }
    }
    
    /**
     * Query STK Push transaction status
     */
    public Map<String, Object> querySTKPushStatus(String checkoutRequestId) {
        log.info("Querying STK Push status for: {}", checkoutRequestId);
        
        try {
            MpesaTransaction transaction = transactionRepository.findByCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

            MpesaConfig config = mpesaConfigService.getActiveConfiguration(
                transaction.getProviderConfigId(), transaction.getProviderCode());

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String password = Base64.getEncoder().encodeToString(
                (config.getShortcode() + config.getPasskey() + timestamp).getBytes()
            );
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("BusinessShortCode", config.getShortcode());
            requestBody.put("Password", password);
            requestBody.put("Timestamp", timestamp);
            requestBody.put("CheckoutRequestID", checkoutRequestId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authService.getAccessToken(
                transaction.getProviderConfigId(), transaction.getProviderCode()));
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = config.getApiUrl() + "/mpesa/stkpushquery/v1/query";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error querying STK Push status", e);
            throw new RuntimeException("Failed to query transaction status", e);
        }
    }
    
    /**
     * Check STK Push Status
     */
    public MpesaDepositStatusResponse checkSTKPushStatus(String checkoutRequestId) {
        log.info("Checking STK push status for checkout request: {}", checkoutRequestId);
        
        try {
            // Query Safaricom directly for real-time status
            Map<String, Object> stkQueryResponse = querySTKPushStatus(checkoutRequestId);
            
            // Extract values from Safaricom response
            String resultCode = String.valueOf(stkQueryResponse.get("ResultCode"));
            String resultDesc = (String) stkQueryResponse.get("ResultDesc");
            String merchantRequestId = (String) stkQueryResponse.get("MerchantRequestID");
            
            // Also get local transaction for additional info
            MpesaTransaction transaction = transactionRepository.findByCheckoutRequestId(checkoutRequestId)
                .orElse(null);
            
            if (transaction != null) {
                boolean alreadyProcessed = Boolean.TRUE.equals(transaction.getCallbackReceived());
                transaction.setResultCode(resultCode);
                transaction.setResultDesc(resultDesc);

                if ("0".equals(resultCode)) {
                    transaction.setStatus(MpesaTransaction.TransactionStatus.SUCCESS);
                    if (stkQueryResponse.containsKey("MpesaReceiptNumber")) {
                        transaction.setMpesaReceiptNumber((String) stkQueryResponse.get("MpesaReceiptNumber"));
                    }
                    transaction.setCallbackReceived(true);
                    transactionRepository.save(transaction);

                    if (!alreadyProcessed) {
                        log.debug("Processing successful MPESA transaction {} via status query", transaction.getId());
                        processSuccessfulPayment(transaction);
                    }

                } else if ("1032".equals(resultCode)) {
                    transaction.setStatus(MpesaTransaction.TransactionStatus.CANCELLED);
                    transaction.setCallbackReceived(true);
                    transactionRepository.save(transaction);

                    if (!alreadyProcessed) {
                        log.debug("Handling cancelled MPESA transaction {} via status query", transaction.getId());
                        handleFailedPayment(transaction, resultDesc != null ? resultDesc : "Payment cancelled by user");
                    }

                } else if (!"1037".equals(resultCode)) { // Not timeout, so it's failed
                    transaction.setStatus(MpesaTransaction.TransactionStatus.FAILED);
                    transaction.setCallbackReceived(true);
                    transactionRepository.save(transaction);

                    if (!alreadyProcessed) {
                        log.debug("Handling failed MPESA transaction {} via status query", transaction.getId());
                        handleFailedPayment(transaction, resultDesc != null ? resultDesc : "Payment failed");
                    }

                } else {
                    transactionRepository.save(transaction);
                }
            }
            
            return MpesaDepositStatusResponse.builder()
                .checkoutRequestId(checkoutRequestId)
                .merchantRequestId(merchantRequestId)
                .resultCode(resultCode)
                .resultDesc(resultDesc)
                .amount(transaction != null ? transaction.getAmount() : null)
                .phoneNumber(transaction != null ? transaction.getPhoneNumber() : null)
                .transactionId(transaction != null ? transaction.getMpesaReceiptNumber() : null)
                .build();
                
        } catch (Exception e) {
            log.error("Error checking STK push status", e);
            
            // Handle rate limiting specifically
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                log.warn("Rate limit hit for checkout request: {}, returning pending status", checkoutRequestId);
                return MpesaDepositStatusResponse.builder()
                    .checkoutRequestId(checkoutRequestId)
                    .resultCode("1032")  // Request cancelled by user (will be updated by callback)
                    .resultDesc("Query rate limited - waiting for callback")
                    .build();
            }
            
            return MpesaDepositStatusResponse.builder()
                .checkoutRequestId(checkoutRequestId)
                .resultCode("1")
                .resultDesc("Error checking transaction status: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Process M-PESA callback from raw Map
     */
    public void processCallback(Map<String, Object> callbackData) {
        try {
            // Convert Map to MpesaCallbackResponse
            MpesaCallbackResponse callback = objectMapper.convertValue(callbackData, MpesaCallbackResponse.class);
            processCallback(callback);
        } catch (Exception e) {
            log.error("Error converting callback data to MpesaCallbackResponse", e);
            // Try to extract key fields manually
            processRawCallback(callbackData);
        }
    }
    
    /**
     * Process raw callback data
     */
    private void processRawCallback(Map<String, Object> callbackData) {
        try {
            // Extract nested structure
            Map<String, Object> body = (Map<String, Object>) callbackData.get("Body");
            if (body == null) {
                body = callbackData; // Maybe the data is already at root level
            }
            
            Map<String, Object> stkCallback = (Map<String, Object>) body.get("stkCallback");
            if (stkCallback != null) {
                String merchantRequestId = String.valueOf(stkCallback.get("MerchantRequestID"));
                String checkoutRequestId = String.valueOf(stkCallback.get("CheckoutRequestID"));
                Integer resultCode = (Integer) stkCallback.get("ResultCode");
                String resultDesc = String.valueOf(stkCallback.get("ResultDesc"));
                
                // Fetch transaction from database
                MpesaTransaction transaction = transactionRepository
                    .findByMerchantRequestId(merchantRequestId)
                    .orElseGet(() -> transactionRepository.findByCheckoutRequestId(checkoutRequestId)
                        .orElseThrow(() -> new RuntimeException("Transaction not found: " + merchantRequestId)));
                
                // Save the updated transaction
                transactionRepository.save(transaction);
            }
        } catch (Exception e) {
            log.error("Error processing raw callback data", e);
        }
    }
    
    /**
     * Process callback metadata
     */
    private void processCallbackMetadata(MpesaTransaction transaction, Map<String, Object> metadata) {
        try {
            List<Map<String, Object>> items = (List<Map<String, Object>>) metadata.get("Item");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    String name = String.valueOf(item.get("Name"));
                    Object value = item.get("Value");
                    
                    if ("MpesaReceiptNumber".equals(name)) {
                        transaction.setMpesaReceiptNumber(String.valueOf(value));
                    } else if ("Amount".equals(name) && value != null) {
                        transaction.setAmount(new BigDecimal(String.valueOf(value)));
                    } else if ("PhoneNumber".equals(name)) {
                        transaction.setPhoneNumber(String.valueOf(value));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing callback metadata", e);
        }
    }
    
    /**
     * Process M-PESA callback
     */
    public void processCallback(MpesaCallbackResponse callback) {
        log.info("Processing M-PESA callback");
        
        try {
            String merchantRequestId = callback.getBody().getStkCallback().getMerchantRequestId();
            
            MpesaTransaction transaction = transactionRepository
                .findByMerchantRequestId(merchantRequestId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + merchantRequestId));
            
            Integer resultCode = callback.getBody().getStkCallback().getResultCode();
            transaction.setCallbackReceived(true);
            transaction.setResultCode(String.valueOf(resultCode));
            transaction.setResultDesc(callback.getBody().getStkCallback().getResultDesc());
            
            // Store full callback response
            transaction.setCallbackResponse(objectMapper.writeValueAsString(callback));
            
            if (resultCode == 0) {
                // Success
                transaction.setStatus(MpesaTransaction.TransactionStatus.SUCCESS);
                
                // Extract payment details from callback metadata
                var metadata = callback.getBody().getStkCallback().getCallbackMetadata();
                if (metadata != null && metadata.getItem() != null) {
                    for (var item : metadata.getItem()) {
                        if ("MpesaReceiptNumber".equals(item.getName())) {
                            transaction.setMpesaReceiptNumber(String.valueOf(item.getValue()));
                            log.info("M-PESA Receipt: {}", item.getValue());
                        } else if ("TransactionDate".equals(item.getName())) {
                            // Parse transaction date (format: 20211201143000)
                            String dateStr = String.valueOf(item.getValue());
                            if (dateStr != null && dateStr.length() == 14) {
                                LocalDateTime txnDate = LocalDateTime.parse(dateStr, 
                                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                                transaction.setTransactionDate(txnDate);
                            }
                        } else if ("Amount".equals(item.getName())) {
                            // Verify amount matches
                            log.info("Amount received: {}", item.getValue());
                        }
                    }
                }
                
                // Process successful payment (update loan/savings account)
                processSuccessfulPayment(transaction);
                log.info("Payment processed successfully: {}", transaction.getMpesaReceiptNumber());
                
            } else if (resultCode == 1032) {
                // User cancelled
                transaction.setStatus(MpesaTransaction.TransactionStatus.CANCELLED);
                log.info("Payment cancelled by user");
            } else {
                // Failed
                transaction.setStatus(MpesaTransaction.TransactionStatus.FAILED);
                log.warn("Payment failed with code: {}, desc: {}", resultCode, transaction.getResultDesc());
            }
            
            transactionRepository.save(transaction);
            
        } catch (Exception e) {
            log.error("Error processing M-PESA callback", e);
            throw new RuntimeException("Failed to process callback", e);
        }
    }
    
    /**
     * Process successful payment - update related entities
     */
    private void processSuccessfulPayment(MpesaTransaction transaction) {
        try {
            // Track if payment was already processed to avoid double-processing
            boolean depositProcessed = false;

            // Handle loan repayment - now using centralized PaymentProcessingHub
            if (transaction.getLoanId() != null) {
                // Process loan payment through centralized hub
                com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions loanTxn =
                    paymentProcessingHub.processMpesaPayment(
                        transaction.getLoanId(),
                        transaction.getAmount().doubleValue(),
                        transaction.getMpesaReceiptNumber(),
                        transaction.getPhoneNumber()
                    );
                
                // Get updated balance after payment
                BigDecimal remainingBalance = resolveLoanBalance(transaction.getLoanId());

                log.info("Loan repayment recorded for loan ID: {} via MPESA checkout {}. Remaining balance: {}",
                    transaction.getLoanId(), transaction.getCheckoutRequestId(), remainingBalance);

                smsService.sendPaymentConfirmationSms(
                    transaction.getPhoneNumber(),
                    transaction.getAmount(),
                    transaction.getMpesaReceiptNumber(),
                    remainingBalance
                );

                log.info("M-PESA loan repayment processed successfully: Amount={}, Receipt={}, Loan ID={}, BalanceAfter={}",
                    transaction.getAmount(), transaction.getMpesaReceiptNumber(), transaction.getLoanId(), remainingBalance);
                
                depositProcessed = true;
            }

            // Handle bank account deposits (ALPHA, SHARES, SAVINGS from banking module)
            else if (transaction.getBankAccountId() != null || (transaction.getCustomerId() != null && transaction.getLoanId() == null && transaction.getSavingsAccountId() == null)) {
                try {
                    Transactions bankTxn =
                        bankDepositService.processMpesaDeposit(transaction);
                    
                    log.info("Bank deposit processed via MPESA checkout {}",
                        transaction.getCheckoutRequestId());

                    // Get updated balance from the transaction
                    BigDecimal newBalance = BigDecimal.valueOf(bankTxn.getClosingBalance());
                    String accountNumber = bankTxn.getBankAccount().getBankAccount();

                    smsService.sendDepositConfirmationSms(
                        transaction.getPhoneNumber(),
                        transaction.getAmount(),
                        accountNumber,
                        newBalance
                    );

                    log.info("M-PESA bank deposit processed successfully: Amount={}, Receipt={}, Account={}, BalanceAfter={}",
                        transaction.getAmount(), transaction.getMpesaReceiptNumber(), accountNumber, newBalance);
                    
                    depositProcessed = true;
                } catch (Exception e) {
                    log.error("Error processing bank deposit: {}. Suspense payment recorded.", e.getMessage());
                    // Suspense payment already recorded by BankDepositService
                }
            }
            // Handle new savings account deposits (if using SavingsAccount module)
            else if (transaction.getSavingsAccountId() != null) {
                SavingsAccount savingsAccount = resolveSavingsAccount(transaction.getSavingsAccountId());
                BigDecimal newBalance = savingsAccount != null ? savingsAccount.getBalance() : BigDecimal.ZERO;
                String accountNumber = savingsAccount != null ? savingsAccount.getAccountNumber() : transaction.getSavingsAccountId().toString();

                log.info("Savings deposit recorded for account {} via MPESA checkout {}. New balance: {}",
                    accountNumber, transaction.getCheckoutRequestId(), newBalance);

                smsService.sendDepositConfirmationSms(
                    transaction.getPhoneNumber(),
                    transaction.getAmount(),
                    accountNumber,
                    newBalance
                );

                log.info("M-PESA deposit processed successfully: Amount={}, Receipt={}, Account={}, BalanceAfter={}",
                    transaction.getAmount(), transaction.getMpesaReceiptNumber(), accountNumber, newBalance);
                
                depositProcessed = true;
            }
            
            // Auto-post to transaction request ONLY if payment hasn't been processed by specific handlers
            // This prevents double-processing of deposits
            TransactionRequest postedRequest = null;
            if (transaction.getTransactionRequestId() != null && !depositProcessed) {
                log.info("Auto-posting M-PESA transaction to transaction request: {}", transaction.getTransactionRequestId());
                postedRequest = transactionApprovalService.autoPostSuccessfulMpesa(transaction);
            }
            
            // Send general successful payment notification if no specific account and not already processed
            if (transaction.getLoanId() == null && !depositProcessed) {
                smsService.sendPaymentConfirmationSms(
                    transaction.getPhoneNumber(),
                    transaction.getAmount(),
                    transaction.getMpesaReceiptNumber(),
                    BigDecimal.ZERO
                );

                log.info("M-PESA general payment processed successfully: Amount={}, Receipt={}",
                    transaction.getAmount(), transaction.getMpesaReceiptNumber());
            }

            if (postedRequest != null) {
                log.info("Transaction request {} updated to status {} after MPESA success",
                    postedRequest.getId(), postedRequest.getStatus());

                // Update transaction request with final status
                postedRequest.setServiceProviderResponse("Payment completed successfully via M-PESA");
            }
        } catch (Exception ex) {
            log.error("Error auto-posting successful MPESA transaction {}", transaction.getId(), ex);

            // Send error notification SMS
            try {
                smsService.sendSms(transaction.getPhoneNumber(),
                    String.format("Payment processing error. Amount: KES %,.2f. Please contact support if funds were deducted. Receipt: %s",
                        transaction.getAmount(), transaction.getMpesaReceiptNumber()));
            } catch (Exception smsEx) {
                log.error("Failed to send error notification SMS", smsEx);
            }

            throw new RuntimeException("Failed to post MPESA payment: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Initiate B2C Payment (Business to Customer)
     */
    public Map<String, Object> initiateB2C(B2CRequest request) {
        log.info("Initiating B2C payment for phone: {}, amount: {}", 
            request.getPhoneNumber(), request.getAmount());
        
        try {
            MpesaConfig config = mpesaConfigService.getActiveConfiguration(
                request.getProviderConfigId(), request.getProviderCode());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("InitiatorName", config.getInitiatorName());
            requestBody.put("SecurityCredential", config.getSecurityCredential());
            requestBody.put("CommandID", request.getCommandId() != null ? request.getCommandId() : "BusinessPayment");
            requestBody.put("Amount", request.getAmount().intValue());
            requestBody.put("PartyA", config.getShortcode());
            requestBody.put("PartyB", request.getFormattedPhoneNumber());
            requestBody.put("Remarks", request.getRemarks());
            requestBody.put("QueueTimeOutURL", config.getTimeoutUrl());
            requestBody.put("ResultURL", config.getResultUrl());
            requestBody.put("Occasion", request.getOccasion());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authService.getAccessToken(
                request.getProviderConfigId(), request.getProviderCode()));
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = config.getApiUrl() + "/mpesa/b2c/v1/paymentrequest";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            // Save B2C transaction
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                saveB2CTransaction(request, response.getBody(), config);
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error initiating B2C payment", e);
            throw new RuntimeException("Failed to initiate B2C payment", e);
        }
    }
    
    /**
     * Save B2C transaction
     */
    private void saveB2CTransaction(B2CRequest request, Map<String, Object> response, MpesaConfig config) {
        MpesaTransaction transaction = new MpesaTransaction();
        transaction.setTransactionType(MpesaTransaction.TransactionType.B2C);
        transaction.setPhoneNumber(request.getFormattedPhoneNumber());
        transaction.setAmount(request.getAmount());
        transaction.setAccountReference(request.getOccasion());
        transaction.setTransactionDesc(request.getRemarks());
        transaction.setStatus(MpesaTransaction.TransactionStatus.PENDING);
        transaction.setCustomerId(request.getCustomerId());
        transaction.setCallbackReceived(false);
        transaction.setProviderConfigId(config.getId());
        transaction.setProviderCode(config.getConfigName());
        
        if (response.containsKey("ConversationID")) {
            transaction.setMerchantRequestId((String) response.get("ConversationID"));
        }
        if (response.containsKey("OriginatorConversationID")) {
            transaction.setCheckoutRequestId((String) response.get("OriginatorConversationID"));
        }
        
        transactionRepository.save(transaction);
        log.info("B2C transaction saved with ID: {}", transaction.getId());
    }
    
    /**
     * Get transaction by checkout request ID
     */
    public MpesaTransaction getTransactionByCheckoutRequestId(String checkoutRequestId) {
        return transactionRepository.findByCheckoutRequestId(checkoutRequestId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
    
    /**
     * Get customer transactions
     */
    public java.util.List<MpesaTransaction> getCustomerTransactions(Long customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }

    /**
     * Get dynamic callback URL based on transaction type
     */
    public String getDynamicCallbackUrl(MpesaConfig config, String transactionType, String fallbackUrl) {
        String dynamicUrl = null;
        
        switch (transactionType.toUpperCase()) {
            case "STK_PUSH":
                dynamicUrl = config.getStkCallbackUrl();
                break;
            case "PAYBILL":
                dynamicUrl = config.getPaybillCallbackUrl();
                break;
            case "B2C":
                dynamicUrl = config.getB2cCallbackUrl();
                break;
            case "VALIDATION":
                dynamicUrl = config.getValidationUrl();
                break;
            case "CONFIRMATION":
                dynamicUrl = config.getConfirmationUrl();
                break;
            case "STATUS":
                dynamicUrl = config.getStatusCallbackUrl();
                break;
            default:
                dynamicUrl = config.getCallbackUrl(); // Use default callback URL
        }
        
        // Return dynamic URL if available, otherwise use fallback
        return (dynamicUrl != null && !dynamicUrl.trim().isEmpty()) ? dynamicUrl : fallbackUrl;
    }

    /**
     * Auto-generate callback URLs based on base URL
     */
    public void autoGenerateCallbackUrls(MpesaConfig config, String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return;
        }
        
        // Remove trailing slash
        baseUrl = baseUrl.replaceAll("/$", "");
        
        // Generate URLs for different transaction types
        config.setStkCallbackUrl(baseUrl + "/api/mpesa/callback/stk-push");
        config.setPaybillCallbackUrl(baseUrl + "/api/mpesa/callback/paybill");
        config.setB2cCallbackUrl(baseUrl + "/api/mpesa/callback/b2c");
        config.setValidationUrl(baseUrl + "/api/mpesa/callback/validation");
        config.setConfirmationUrl(baseUrl + "/api/mpesa/callback/confirmation");
        config.setStatusCallbackUrl(baseUrl + "/api/mpesa/callback/transaction-status");
    }
    
    /**
     * Register C2B URLs with Safaricom Daraja API
     * This registers the ValidationURL and ConfirmationURL for C2B transactions
     * 
     * @param configId M-PESA configuration ID
     * @param providerCode Provider code/config name
     * @param shortcode Paybill/Till number
     * @param validationUrl URL for validation requests
     * @param confirmationUrl URL for confirmation callbacks
     * @return true if registration successful, false otherwise
     */
    public boolean registerC2BUrls(Long configId, String providerCode, String shortcode, 
                                   String validationUrl, String confirmationUrl) {
        try {
            log.info("Registering C2B URLs with Daraja API for shortcode: {}", shortcode);
            log.info("ValidationURL: {}", validationUrl);
            log.info("ConfirmationURL: {}", confirmationUrl);
            
            // Get M-PESA configuration (same pattern as STK push, B2C)
            MpesaConfig config = mpesaConfigService.getActiveConfiguration(configId, providerCode);
            
            // Get OAuth Bearer token (same as STK push)
            String accessToken = authService.getAccessToken(configId, providerCode);
            
            if (accessToken == null || accessToken.trim().isEmpty()) {
                log.error("❌ Failed to obtain access token for C2B URL registration");
                return false;
            }
            
            log.info("Using Bearer token for C2B registration: {}***", accessToken.substring(0, Math.min(20, accessToken.length())));
            
            // Prepare request body using DTO with @JsonProperty annotations
            // This ensures exact field names (ShortCode, ResponseType, etc.) are sent to M-PESA
            C2BRegisterUrlRequest requestBody = C2BRegisterUrlRequest.builder()
                .shortCode(shortcode)
                .responseType("Completed")
                .confirmationURL(confirmationUrl)
                .validationURL(validationUrl)
                .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Build request entity
            HttpEntity<C2BRegisterUrlRequest> requestEntity = new HttpEntity<>(requestBody, headers);
            
            String apiBaseUrl = config.getApiUrl();
            String registerUrlEndpoint = apiBaseUrl + "/mpesa/c2b/v1/registerurl";
            
            log.info("Calling Daraja C2B Register URL: {}", registerUrlEndpoint);
            log.info("Request: {}", requestBody);
            log.debug("Authorization: Bearer {}***", accessToken.substring(0, Math.min(30, accessToken.length())));
            
            // Make API call using injected restTemplate (same pattern as STK push, B2C)
            ResponseEntity<Map> response = restTemplate.exchange(
                registerUrlEndpoint,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            // Log response
            log.info("C2B URL Registration Response Status: {}", response.getStatusCode());
            log.info("C2B URL Registration Response Body: {}", response.getBody());
            
            // Check response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                // Daraja API returns different response formats
                // Success indicators: ResponseDescription or ResponseCode
                String responseDescription = (String) responseBody.get("ResponseDescription");
                String responseCode = (String) responseBody.get("ResponseCode");
                
                boolean success = false;
                if (responseCode != null && "0".equals(responseCode)) {
                    // Success response code
                    success = true;
                } else if (responseDescription != null && 
                          (responseDescription.toLowerCase().contains("success") ||
                           responseDescription.toLowerCase().contains("registered"))) {
                    // Success description
                    success = true;
                }
                
                if (success) {
                    log.info("✅ C2B URLs registered successfully with M-PESA");
                    log.info("Response: {}", responseDescription != null ? responseDescription : responseBody);
                    
                    // Update configuration with registered URLs
                    config.setValidationUrl(validationUrl);
                    config.setConfirmationUrl(confirmationUrl);
                    config.setPaybillCallbackUrl(confirmationUrl);
                    // Configuration will be saved automatically after this method returns
                    log.info("Configuration updated with registered URLs");
                    
                    return true;
                } else {
                    log.error("❌ C2B URL registration failed. Response: {}", responseBody);
                    return false;
                }
            } else {
                log.error("❌ C2B URL registration failed with status: {}", response.getStatusCode());
                return false;
            }
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("❌ M-PESA API Client Error during C2B registration: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            log.error("Error details: {}", e.getMessage());
            return false;
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("❌ M-PESA API Server Error during C2B registration: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("❌ Unexpected error during C2B URL registration", e);
            log.error("Error message: {}", e.getMessage());
            return false;
        }
    }
}
