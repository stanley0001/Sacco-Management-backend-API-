package com.example.demo.sms;

import com.example.demo.communication.parsitence.models.Email;
import com.example.demo.communication.parsitence.repositories.emailRepo;
import com.example.demo.sms.dto.SmsSendRequest;
import com.example.demo.sms.dto.SmsSendResponse;
import com.example.demo.sms.dto.SmsTestRequest;
import com.example.demo.sms.dto.SmsTestResponse;
import com.example.demo.sms.entities.SmsConfig;
import com.example.demo.sms.repositories.SmsConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final RestTemplate restTemplate;
    private final SmsConfigRepository smsConfigRepository;
    private final emailRepo emailRepo;

    private static final String DEFAULT_TEXTSMS_BASE_URL = "https://sms.textsms.co.ke";

    /**
     * Convenience method for sending SMS with phone number and message
     */
    public void sendSmsTest(String phoneNumber, String message) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent message to {}", phoneNumber);
            return;
        }

        SmsConfig config = resolveActiveConfig();
        if (config == null) {
            log.warn("No active SMS configuration found. Unable to send SMS to {}", phoneNumber);
            return;
        }

        try {
            String formattedPhone = formatPhoneNumber(phoneNumber);
            sendSmsWithConfig(config, formattedPhone, message);
            log.info("SMS sent successfully to {}", formattedPhone);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }

    /**
     * Send loan approval SMS
     */
    public void sendLoanApprovalSms(String phoneNumber, String customerName, BigDecimal loanAmount, String loanRef) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent loan approval to {}", phoneNumber);
            return;
        }

        String message = String.format(
            "Dear %s, your loan application of KES %,.2f has been APPROVED. " +
            "Loan Ref: %s. The amount will be disbursed shortly. " +
            "Thank you for choosing HelaSuite.",
            customerName, loanAmount, loanRef
        );

        sendSms(phoneNumber, message);
    }

    /**
     * Send loan rejection SMS
     */
    public void sendLoanRejectionSms(String phoneNumber, String customerName, String reason) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent loan rejection to {}", phoneNumber);
            return;
        }

        String message = String.format(
            "Dear %s, we regret to inform you that your loan application has been declined. %s " +
            "For assistance, contact our support team. HelaSuite.",
            customerName, reason != null ? "Reason: " + reason + "." : ""
        );

        sendSms(phoneNumber, message);
    }

    /**
     * Send payment confirmation SMS
     */
    public void sendPaymentConfirmationSms(String phoneNumber, BigDecimal amount, String receiptNumber, BigDecimal balance) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent payment confirmation to {}", phoneNumber);
            return;
        }

        String message = String.format(
            "Payment of KES %,.2f received. Receipt: %s. " +
            "Remaining balance: KES %,.2f. " +
            "Thank you for your payment. HelaSuite.",
            amount, receiptNumber, balance
        );

        sendSms(phoneNumber, message);
    }

    /**
     * Send deposit confirmation SMS
     */
    public void sendDepositConfirmationSms(String phoneNumber, BigDecimal amount, String accountNumber, BigDecimal newBalance) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent deposit confirmation to {}", phoneNumber);
            return;
        }

        String message = String.format(
            "Deposit of KES %,.2f received to account %s. " +
            "New balance: KES %,.2f. " +
            "Thank you for banking with HelaSuite.",
            amount, accountNumber, newBalance
        );

        sendSms(phoneNumber, message);
    }

    /**
     * Send withdrawal confirmation SMS
     */
    public void sendWithdrawalConfirmationSms(String phoneNumber, BigDecimal amount, String accountNumber, BigDecimal newBalance) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent withdrawal confirmation to {}", phoneNumber);
            return;
        }

        String message = String.format(
            "Withdrawal of KES %,.2f from account %s successful. " +
            "New balance: KES %,.2f. " +
            "Thank you for banking with HelaSuite.",
            amount, accountNumber, newBalance
        );

        sendSms(phoneNumber, message);
    }

    /**
     * Send loan disbursement SMS
     */
    public void sendLoanDisbursementSms(String phoneNumber, String customerName, BigDecimal amount, String loanRef) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent disbursement notification to {}", phoneNumber);
            return;
        }

        String message = String.format(
            "Dear %s, KES %,.2f has been disbursed to your account. " +
            "Loan Ref: %s. " +
            "Thank you for choosing HelaSuite.",
            customerName, amount, loanRef
        );

        sendSms(phoneNumber, message);
    }

    /**
     * Send payment reminder SMS
     */
    public void sendPaymentReminderSms(String phoneNumber, String customerName, BigDecimal amount, String dueDate) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent payment reminder to {}", phoneNumber);
            return;
        }

        String message = String.format(
            "Dear %s, this is a reminder that your loan payment of KES %,.2f is due on %s. " +
            "Please ensure timely payment to avoid penalties. HelaSuite.",
            customerName, amount, dueDate
        );

        sendSms(phoneNumber, message);
    }

    /**
     * Send overdue payment SMS
     */
    public void sendOverduePaymentSms(String phoneNumber, String customerName, BigDecimal amount, int daysOverdue) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent overdue notification to {}", phoneNumber);
            return;
        }

        String message = String.format(
            "Dear %s, your loan payment of KES %,.2f is %d days overdue. " +
            "Please make payment immediately to avoid further action. HelaSuite.",
            customerName, amount, daysOverdue
        );

        sendSms(phoneNumber, message);
    }

    /**
     * Send M-PESA STK push initiated SMS
     */
    public void sendStkPushInitiatedSms(String phoneNumber, BigDecimal amount) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent STK push notification to {}", phoneNumber);
            return;
        }

        String message = String.format(
            "Please check your phone for the M-PESA prompt to complete payment of KES %,.2f. " +
            "Enter your PIN to confirm. HelaSuite.",
            amount
        );

        sendSms(phoneNumber, message);
    }

    /**
     * Manually send SMS via API
     */
    public SmsSendResponse sendSms(SmsSendRequest request) {
        SmsSendResponse.SmsSendResponseBuilder builder = SmsSendResponse.builder()
            .phoneNumber(request.getEffectivePhoneNumber())
            .configId(request.getConfigId())
            .sentAt(LocalDateTime.now());

        log.info("SMS send request - phoneNumber: '{}', message length: {}, configId: {}", 
            request.getEffectivePhoneNumber(), 
            request.getEffectiveMessage() != null ? request.getEffectiveMessage().length() : 0,
            request.getConfigId());

        try {
            // Validate required fields
            String phoneNumber = request.getEffectivePhoneNumber();
            String message = request.getEffectiveMessage();

            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return builder.success(false).message("Phone number is required").errorCode("MISSING_PHONE").build();
            }

            // Enhanced phone number validation for masked numbers
            if (phoneNumber.contains("*")) {
                log.warn("Received masked phone number: '{}'. Attempting to extract usable parts.", phoneNumber);
                
                // Try to extract a usable phone number from masked input
                String extractedPhone = extractPhoneFromMasked(phoneNumber);
                if (extractedPhone != null && !extractedPhone.trim().isEmpty()) {
                    log.info("Extracted usable phone number from masked input: '{}'", extractedPhone);
                    phoneNumber = extractedPhone;
                } else {
                    return builder.success(false).message("Phone number appears to be masked and cannot be processed. Please provide the full phone number.").errorCode("MASKED_PHONE").build();
                }
            }

            if (message == null || message.trim().isEmpty()) {
                return builder.success(false).message("Message is required").errorCode("MISSING_MESSAGE").build();
            }

            // Format phone number
            String formattedPhone = formatPhoneNumber(phoneNumber);

            // Resolve configuration
            SmsConfig config;
            if (request.getConfigId() != null) {
                config = smsConfigRepository.findById(request.getConfigId()).orElse(null);
                if (config == null) {
                    return builder.success(false).message("SMS configuration not found").errorCode("CONFIG_NOT_FOUND").build();
                }
            } else {
                config = resolveActiveConfig();
                if (config == null) {
                    return builder.success(false).message("No active SMS configuration found").errorCode("NO_ACTIVE_CONFIG").build();
                }
            }

            // Send SMS using the core method
            sendSmsWithConfig(config, formattedPhone, message);

            return builder.success(true).message("SMS sent successfully").build();

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", request.getEffectivePhoneNumber(), e.getMessage(), e);
            return builder.success(false).message("SMS sending failed: " + e.getMessage()).errorCode("SMS_SEND_ERROR").build();
        }
    }

    /**
     * Core SMS sending method
     */
    public void sendSms(String phoneNumber, String message) {
        try {
            log.info("Sending SMS to {}: {}", phoneNumber, message);

            // Format phone number (ensure it starts with country code)
            String formattedPhone = formatPhoneNumber(phoneNumber);

            SmsConfig config = resolveActiveConfig();
            if (config == null) {
                log.warn("No active SMS configuration found. Message not sent to {}", formattedPhone);
                return;
            }

//            switch (config.getProviderType()) {
//                case AFRICAS_TALKING -> sendViaAfricasTalking(config, formattedPhone, message);
//                case TEXT_SMS -> sendViaTextSmsSingle(config, formattedPhone, message);
//                case CUSTOM_GET -> sendViaCustomGet(config, formattedPhone, message);
//                default -> log.warn("Unsupported SMS provider {}", config.getProviderType());
//            }
            sendSmsWithConfig(config,formattedPhone,message);

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage(), e);
            // Don't throw exception to avoid disrupting the main flow
        }
    }

    /**
     * Send SMS via Africa's Talking API
     */
    private void sendViaAfricasTalking(SmsConfig config, String phoneNumber, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apiKey", config.getApiKey());
            headers.set("Accept", "application/json");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String senderId = config.getSenderId() != null ? config.getSenderId() : "HELASUITE";
            String body = String.format("username=%s&to=%s&message=%s&from=%s",
                config.getUsername(), phoneNumber, message, senderId);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            String apiUrl = resolveAfricasTalkingUrl(config);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                log.info("SMS sent successfully to {}", phoneNumber);
            } else {
                log.error("SMS sending failed with status: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error sending SMS via Africa's Talking", e);
        }
    }

    private void sendViaTextSmsSingle(SmsConfig config, String phoneNumber, String message) {
        try {
            String url = buildTextSmsUrl(config, "/api/services/sendsms/");

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("apikey", config.getApiKey())
                .queryParam("partnerID", config.getPartnerId())
                .queryParam("message", message)
                .queryParam("shortcode", config.getShortcode())
                .queryParam("mobile", phoneNumber);
            String resultUrl=builder.toUriString();
//            log.info("SMS resulting url {}", resultUrl);
            restTemplate.getForEntity(resultUrl, String.class);
            log.info("SMS sent via TextSMS to {}", phoneNumber);
        } catch (Exception e) {
            log.error("Error sending SMS via TextSMS", e);
        }
    }

    private void sendViaCustomGet(SmsConfig config, String phoneNumber, String message) {
        try {
            String url = buildTextSmsUrl(config, "");
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("to", phoneNumber)
                .queryParam("message", message);
            restTemplate.getForEntity(builder.toUriString(), String.class);
        } catch (Exception e) {
            log.error("Error sending SMS via custom GET provider", e);
        }
    }

    /**
     * Format phone number to include country code
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }

        // Remove any spaces or special characters
        phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");

        // If starts with 0, replace with +254 (Kenya)
        if (phoneNumber.startsWith("0")) {
            phoneNumber = "+254" + phoneNumber.substring(1);
        }
        // If starts with 254, add +
        else if (phoneNumber.startsWith("254")) {
            phoneNumber = "+" + phoneNumber;
        }
        // If starts with 7 or 1 (likely Kenyan number without prefix)
        else if (phoneNumber.startsWith("7") || phoneNumber.startsWith("1")) {
            phoneNumber = "+254" + phoneNumber;
        }

        return phoneNumber;
    }

    /**
     * Try to extract a usable phone number from masked input
     */
    private String extractPhoneFromMasked(String maskedPhone) {
        if (maskedPhone == null || maskedPhone.trim().isEmpty()) {
            return null;
        }

        try {
            // Common patterns for masked phone numbers
            // Pattern 1: +254***123***456 or 254***123***456
            // Pattern 2: 0***123***456 or 0*********456
            // Pattern 3: *****123456 (extract last digits)
            
            String cleaned = maskedPhone.replaceAll("\\s", ""); // Remove spaces
            log.debug("Attempting to extract phone from: '{}'", cleaned);
            
            // If it starts with country code and has reasonable length
            if (cleaned.startsWith("+254") && cleaned.length() >= 10) {
                // Extract all digits after +254
                String digits = cleaned.substring(4).replaceAll("[^0-9]", "");
                if (digits.length() >= 8) { // At least 8 digits for a valid number
                    return "+254" + digits;
                }
            }
            
            // If it starts with 254 and has reasonable length
            if (cleaned.startsWith("254") && cleaned.length() >= 9) {
                String digits = cleaned.substring(3).replaceAll("[^0-9]", "");
                if (digits.length() >= 8) {
                    return "+254" + digits;
                }
            }
            
            // If it starts with 0 (Kenyan local format)
            if (cleaned.startsWith("0") && cleaned.length() >= 9) {
                String digits = cleaned.substring(1).replaceAll("[^0-9]", "");
                if (digits.length() >= 8) {
                    return "+254" + digits;
                }
            }
            
            // Try to extract any sequence of 9+ digits
            String allDigits = cleaned.replaceAll("[^0-9]", "");
            if (allDigits.length() >= 9) {
                // If we have 9 digits, assume Kenyan mobile (7XXXXXXXX)
                if (allDigits.length() == 9 && allDigits.startsWith("7")) {
                    return "+254" + allDigits;
                }
                // If we have 12 digits starting with 254, that's likely the full number
                if (allDigits.length() == 12 && allDigits.startsWith("254")) {
                    return "+" + allDigits;
                }
                // If we have 10 digits starting with 07, convert to international
                if (allDigits.length() == 10 && allDigits.startsWith("07")) {
                    return "+254" + allDigits.substring(1);
                }
            }
            
            log.warn("Could not extract usable phone number from masked input: '{}'", maskedPhone);
            return null;
            
        } catch (Exception e) {
            log.error("Error extracting phone number from masked input '{}': {}", maskedPhone, e.getMessage());
            return null;
        }
    }

    /**
     * Send bulk SMS
     */
    public void sendBulkSms(Map<String, String> recipients, String message) {
        if (!isSmsEnabled()) {
            log.info("SMS disabled. Would have sent bulk SMS to {} recipients", recipients.size());
            return;
        }

        SmsConfig config = resolveActiveConfig();
        if (config == null) {
            log.warn("No active SMS configuration found. Unable to send bulk SMS");
            return;
        }

        if (config.getProviderType() == SmsConfig.SmsProviderType.TEXT_SMS) {
            sendBulkViaTextSms(config, recipients, message);
            return;
        }

        recipients.forEach((phoneNumber, name) -> {
            String personalizedMessage = message.replace("{name}", name);
            sendSms(phoneNumber, personalizedMessage);
            // Add delay to avoid rate limiting
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void sendBulkViaTextSms(SmsConfig config, Map<String, String> recipients, String message) {
        try {
            String url = buildTextSmsUrl(config, "/api/services/sendbulk/");

            var payload = new HashMap<String, Object>();
            payload.put("count", recipients.size());

            var smsList = recipients.entrySet().stream().map(entry -> {
                Map<String, Object> sms = new HashMap<>();
                sms.put("partnerID", config.getPartnerId());
                sms.put("apikey", config.getApiKey());
                sms.put("pass_type", "plain");
                sms.put("clientsmsid", System.currentTimeMillis());
                sms.put("mobile", entry.getKey());
                sms.put("message", message.replace("{name}", entry.getValue()));
                sms.put("shortcode", config.getShortcode());
                return sms;
            }).toList();

            payload.put("smslist", smsList);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, payload, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Bulk SMS sent via TextSMS to {} recipients", recipients.size());
            } else {
                log.warn("Bulk SMS via TextSMS failed with status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending bulk SMS via TextSMS", e);
        }
    }

    public SmsTestResponse testConfiguration(SmsConfig config, SmsTestRequest request) {
        SmsTestResponse.SmsTestResponseBuilder builder = SmsTestResponse.builder()
            .configId(config.getId())
            .testType(request.getTestType())
            .testTime(LocalDateTime.now());

        try {
            String type = request.getTestType() != null ? request.getTestType().toUpperCase() : "SINGLE";
            switch (type) {
                case "SINGLE" -> {
                    String phone = request.getPhoneNumber() != null ? formatPhoneNumber(request.getPhoneNumber()) : "+254700000000";
                    String message = request.getMessage() != null ? request.getMessage() : "Test message from HELASUITE";
                    sendSmsWithConfig(config, phone, message);
                    builder.success(true).message("SMS sent successfully");
                }
                case "BULK" -> {
                    String phone = request.getPhoneNumber() != null ? formatPhoneNumber(request.getPhoneNumber()) : "+254700000001";
                    Map<String, String> recipients = new HashMap<>();
                    recipients.put(phone, "Test User");
                    sendBulkViaTextSms(config, recipients, request.getMessage() != null ? request.getMessage() : "Bulk test message");
                    builder.success(true).message("Bulk SMS request submitted");
                }
                case "BALANCE" -> {
                    Map<String, Object> details = queryTextSmsBalance(config);
                    builder.success(true).message("Balance retrieved").details(details);
                }
                case "DLR" -> {
                    Map<String, Object> details = queryTextSmsDeliveryStatus(config, request.getMessageId());
                    builder.success(true).message("Delivery status retrieved").details(details);
                }
                default -> builder.success(false).message("Unsupported test type: " + type);
            }
        } catch (Exception e) {
            builder.success(false).message("SMS test failed: " + e.getMessage()).errorCode("SMS_TEST_ERROR");
            log.error("SMS configuration test failed", e);
        }

        return builder.build();
    }

    private void sendSmsWithConfig(SmsConfig config, String phoneNumber, String message) {
        try {
            switch (config.getProviderType()) {
                case AFRICAS_TALKING:
                    sendViaAfricasTalking(config, phoneNumber, message);
                    break;
                case TEXT_SMS:
                    sendViaTextSmsSingle(config, phoneNumber, message);
                    break;
                case CUSTOM_GET:
                    sendViaCustomGet(config, phoneNumber, message);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported SMS provider type: " + config.getProviderType());
            }
            
            // Save communication record after SMS is sent
            Email communication = new Email();
            communication.setMessageType("SMS");
            communication.setRecipient(phoneNumber);
            // Truncate message to fit database constraint (900 chars safe limit)
            communication.setMessage(truncateMessage(message, 900));
            communication.setStatus("PROCESSED");
            communication.setDate(java.time.LocalDate.now());
            emailRepo.save(communication);
            log.info("Saved SMS communication record for {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS or save communication record: {}", e.getMessage());
            // Don't fail the SMS send if saving communication fails
        }
    }

    private Map<String, Object> queryTextSmsDeliveryStatus(SmsConfig config, String messageId) {
        String url = buildTextSmsUrl(config, "/api/services/getdlr/");
        Map<String, Object> payload = new HashMap<>();
        payload.put("apikey", config.getApiKey());
        payload.put("partnerID", config.getPartnerId());
        payload.put("messageID", messageId != null ? messageId : "TEST" + System.currentTimeMillis());

        return postForMap(url, payload);
    }

    private Map<String, Object> queryTextSmsBalance(SmsConfig config) {
        String url = buildTextSmsUrl(config, "/api/services/getbalance/");
        Map<String, Object> payload = new HashMap<>();
        payload.put("apikey", config.getApiKey());
        payload.put("partnerID", config.getPartnerId());

        return postForMap(url, payload);
    }

    private SmsConfig resolveActiveConfig() {
        return smsConfigRepository.findByActiveTrueAndDefaultConfigTrue()
            .orElseGet(() -> smsConfigRepository.findAll().stream()
                .filter(SmsConfig::getActive)
                .findFirst()
                .orElse(null));
    }

    private boolean isSmsEnabled() {
        return resolveActiveConfig() != null;
    }

    private String resolveAfricasTalkingUrl(SmsConfig config) {
        String url = config.getApiUrl();
        if (url == null || url.isBlank()) {
            return "https://api.africastalking.com/version1/messaging";
        }
        return url;
    }

    private String buildTextSmsUrl(SmsConfig config, String path) {
        String baseUrl = config.getApiUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_TEXTSMS_BASE_URL;
        }
        if (!baseUrl.startsWith("http")) {
            baseUrl = "https://" + baseUrl;
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return baseUrl + path;
    }

    private Map<String, Object> postForMap(String url, Object payload) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(payload),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody() != null ? response.getBody() : Map.of();
        } catch (Exception e) {
            log.error("Error calling SMS provider endpoint {}", url, e);
            return Map.of("error", e.getMessage());
        }
    }
    
    /**
     * Safely truncate message to fit database constraints
     * @param message Original message
     * @param maxLength Maximum allowed length
     * @return Truncated message if needed
     */
    private String truncateMessage(String message, int maxLength) {
        if (message == null) {
            return null;
        }
        if (message.length() <= maxLength) {
            return message;
        }
        // Truncate and add indicator
        return message.substring(0, maxLength - 15) + "... [truncated]";
    }
    
    /**
     * Get SMS history from database
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of SMS history records
     */
    public List<Map<String, Object>> getSmsHistory(int page, int size) {
        try {
            // Get all SMS communications from email repository
            List<Email> smsRecords = emailRepo.findAllOrderByIdDesc();
            
            // Filter only SMS type messages
            List<Email> smsOnly = smsRecords.stream()
                .filter(email -> "SMS".equalsIgnoreCase(email.getMessageType()))
                .limit(size)
                .skip((long) page * size)
                .toList();
            
            // Convert to Map format for frontend compatibility
            return smsOnly.stream()
                .map(sms -> {
                    Map<String, Object> record = new java.util.HashMap<>();
                    record.put("id", sms.getId());
                    record.put("recipient", sms.getRecipient());
                    record.put("message", sms.getMessage());
                    record.put("status", sms.getStatus() != null ? sms.getStatus() : "SENT");
                    record.put("sentDate", sms.getDate() != null ? sms.getDate().atStartOfDay().toString() : 
                        java.time.LocalDateTime.now().toString());
                    record.put("cost", 2.50); // Default SMS cost - could be stored in database
                    return record;
                })
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error fetching SMS history from database", e);
            // Return empty list on error
            return new java.util.ArrayList<>();
        }
    }
}
