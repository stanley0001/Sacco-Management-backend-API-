package com.example.demo.payments.services;

import com.example.demo.payments.dto.*;
import com.example.demo.payments.entities.MpesaTransaction;
import com.example.demo.payments.repositories.MpesaTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaService {
    
    private final MpesaAuthService authService;
    private final MpesaTransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${mpesa.api.url:https://sandbox.safaricom.co.ke}")
    private String apiUrl;
    
    @Value("${mpesa.shortcode:174379}")
    private String shortcode;
    
    @Value("${mpesa.passkey}")
    private String passkey;
    
    @Value("${mpesa.callback.url}")
    private String callbackUrl;
    
    @Value("${mpesa.initiator.name:testapi}")
    private String initiatorName;
    
    @Value("${mpesa.security.credential}")
    private String securityCredential;
    
    /**
     * Initiate STK Push (Lipa Na M-PESA Online)
     */
    public STKPushResponse initiateSTKPush(STKPushRequest request) {
        log.info("Initiating STK Push for phone: {}, amount: {}", 
            request.getPhoneNumber(), request.getAmount());
        
        try {
            // Generate timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            // Generate password
            String password = Base64.getEncoder().encodeToString(
                (shortcode + passkey + timestamp).getBytes()
            );
            
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("BusinessShortCode", shortcode);
            requestBody.put("Password", password);
            requestBody.put("Timestamp", timestamp);
            requestBody.put("TransactionType", "CustomerPayBillOnline");
            requestBody.put("Amount", request.getAmount().intValue());
            requestBody.put("PartyA", request.getFormattedPhoneNumber());
            requestBody.put("PartyB", shortcode);
            requestBody.put("PhoneNumber", request.getFormattedPhoneNumber());
            requestBody.put("CallBackURL", callbackUrl);
            requestBody.put("AccountReference", request.getAccountReference());
            requestBody.put("TransactionDesc", request.getTransactionDesc());
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authService.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make API call
            String url = apiUrl + "/mpesa/stkpush/v1/processrequest";
            log.debug("Calling M-PESA API: {}", url);
            
            ResponseEntity<STKPushResponse> response = restTemplate.postForEntity(url, entity, STKPushResponse.class);
            
            // Save transaction
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                STKPushResponse stkResponse = response.getBody();
                saveTransaction(request, stkResponse);
                log.info("STK Push initiated successfully: {}", stkResponse.getCheckoutRequestId());
                return stkResponse;
            }
            
            log.error("STK Push failed with status: {}", response.getStatusCode());
            throw new RuntimeException("STK Push failed: " + response.getStatusCode());
            
        } catch (Exception e) {
            log.error("Error initiating STK Push", e);
            throw new RuntimeException("Failed to initiate STK Push: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save transaction to database
     */
    private void saveTransaction(STKPushRequest request, STKPushResponse response) {
        MpesaTransaction transaction = new MpesaTransaction();
        transaction.setMerchantRequestId(response.getMerchantRequestId());
        transaction.setCheckoutRequestId(response.getCheckoutRequestId());
        transaction.setTransactionType(MpesaTransaction.TransactionType.STK_PUSH);
        transaction.setPhoneNumber(request.getFormattedPhoneNumber());
        transaction.setAmount(request.getAmount());
        transaction.setAccountReference(request.getAccountReference());
        transaction.setTransactionDesc(request.getTransactionDesc());
        transaction.setStatus(MpesaTransaction.TransactionStatus.PENDING);
        transaction.setCustomerId(request.getCustomerId());
        transaction.setLoanId(request.getLoanId());
        transaction.setSavingsAccountId(request.getSavingsAccountId());
        transaction.setCallbackReceived(false);
        transaction.setResultCode(response.getResponseCode());
        transaction.setResultDesc(response.getResponseDescription());
        
        transactionRepository.save(transaction);
        log.info("Transaction saved with ID: {}", transaction.getId());
    }
    
    /**
     * Query STK Push transaction status
     */
    public Map<String, Object> querySTKPushStatus(String checkoutRequestId) {
        log.info("Querying STK Push status for: {}", checkoutRequestId);
        
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String password = Base64.getEncoder().encodeToString(
                (shortcode + passkey + timestamp).getBytes()
            );
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("BusinessShortCode", shortcode);
            requestBody.put("Password", password);
            requestBody.put("Timestamp", timestamp);
            requestBody.put("CheckoutRequestID", checkoutRequestId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authService.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = apiUrl + "/mpesa/stkpushquery/v1/query";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error querying STK Push status", e);
            throw new RuntimeException("Failed to query transaction status", e);
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
        // Update loan account if loanId is present
        if (transaction.getLoanId() != null) {
            log.info("Processing loan payment for loan ID: {}", transaction.getLoanId());
            // TODO: Call loan service to record payment
            // loanRepaymentService.recordPayment(transaction.getLoanId(), transaction.getAmount(), transaction.getMpesaReceiptNumber());
        }
        
        // Update savings account if savingsAccountId is present
        if (transaction.getSavingsAccountId() != null) {
            log.info("Processing savings deposit for account ID: {}", transaction.getSavingsAccountId());
            // TODO: Call savings service to record deposit
            // savingsService.recordDeposit(transaction.getSavingsAccountId(), transaction.getAmount(), transaction.getMpesaReceiptNumber());
        }
        
        // TODO: Send SMS notification to customer
        // smsService.sendPaymentConfirmation(transaction.getPhoneNumber(), transaction.getAmount(), transaction.getMpesaReceiptNumber());
    }
    
    /**
     * Initiate B2C Payment (Business to Customer)
     */
    public Map<String, Object> initiateB2C(B2CRequest request) {
        log.info("Initiating B2C payment for phone: {}, amount: {}", 
            request.getPhoneNumber(), request.getAmount());
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("InitiatorName", initiatorName);
            requestBody.put("SecurityCredential", securityCredential);
            requestBody.put("CommandID", request.getCommandId() != null ? request.getCommandId() : "BusinessPayment");
            requestBody.put("Amount", request.getAmount().intValue());
            requestBody.put("PartyA", shortcode);
            requestBody.put("PartyB", request.getFormattedPhoneNumber());
            requestBody.put("Remarks", request.getRemarks());
            requestBody.put("QueueTimeOutURL", callbackUrl + "/timeout");
            requestBody.put("ResultURL", callbackUrl + "/result");
            requestBody.put("Occasion", request.getOccasion());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authService.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = apiUrl + "/mpesa/b2c/v1/paymentrequest";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            // Save B2C transaction
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                saveB2CTransaction(request, response.getBody());
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
    private void saveB2CTransaction(B2CRequest request, Map<String, Object> response) {
        MpesaTransaction transaction = new MpesaTransaction();
        transaction.setTransactionType(MpesaTransaction.TransactionType.B2C);
        transaction.setPhoneNumber(request.getFormattedPhoneNumber());
        transaction.setAmount(request.getAmount());
        transaction.setAccountReference(request.getOccasion());
        transaction.setTransactionDesc(request.getRemarks());
        transaction.setStatus(MpesaTransaction.TransactionStatus.PENDING);
        transaction.setCustomerId(request.getCustomerId());
        transaction.setCallbackReceived(false);
        
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
}
