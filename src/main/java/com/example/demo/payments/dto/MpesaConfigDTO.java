package com.example.demo.payments.dto;

import com.example.demo.payments.entities.MpesaConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpesaConfigDTO {
    
    private Long id;
    private String configName;
    private String consumerKey;
    private String consumerSecret; // Will be masked when sent to frontend
    private String shortcode;
    private String passkey; // Will be masked when sent to frontend
    private String initiatorName;
    private String securityCredential; // Will be masked when sent to frontend
    private String apiUrl;
    private String callbackUrl;
    private String timeoutUrl;
    private String resultUrl;
    
    // Dynamic Callback URLs for different transaction types
    private String stkCallbackUrl;
    private String paybillCallbackUrl;
    private String b2cCallbackUrl;
    private String validationUrl;
    private String confirmationUrl;
    private String statusCallbackUrl;
    
    private String environment; // SANDBOX or PRODUCTION
    private Boolean active;
    private Boolean defaultConfig;
    private String description;
    
    // Test Results
    private Boolean lastTestSuccess;
    private LocalDateTime lastTestDate;
    private String lastTestMessage;
    
    // Statistics
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private Double successRate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    /**
     * Mask sensitive fields for security
     */
    public void maskSensitiveFields() {
        if (consumerSecret != null && !consumerSecret.isEmpty()) {
            consumerSecret = maskString(consumerSecret);
        }
        if (passkey != null && !passkey.isEmpty()) {
            passkey = maskString(passkey);
        }
        if (securityCredential != null && !securityCredential.isEmpty()) {
            securityCredential = maskString(securityCredential);
        }
    }
    
    private String maskString(String value) {
        if (value == null || value.length() < 4) {
            return "***";
        }
        int visibleLength = Math.min(4, value.length() / 4);
        String visible = value.substring(0, visibleLength);
        return visible + "***" + value.substring(value.length() - visibleLength);
    }
    
    /**
     * Convert entity to DTO with masked sensitive fields
     */
    public static MpesaConfigDTO fromEntity(MpesaConfig entity, boolean maskSensitive) {
        MpesaConfigDTO dto = MpesaConfigDTO.builder()
            .id(entity.getId())
            .configName(entity.getConfigName())
            .consumerKey(entity.getConsumerKey())
            .consumerSecret(entity.getConsumerSecret())
            .shortcode(entity.getShortcode())
            .passkey(entity.getPasskey())
            .initiatorName(entity.getInitiatorName())
            .securityCredential(entity.getSecurityCredential())
            .apiUrl(entity.getApiUrl())
            .callbackUrl(entity.getCallbackUrl())
            .timeoutUrl(entity.getTimeoutUrl())
            .resultUrl(entity.getResultUrl())
            .stkCallbackUrl(entity.getStkCallbackUrl())
            .paybillCallbackUrl(entity.getPaybillCallbackUrl())
            .b2cCallbackUrl(entity.getB2cCallbackUrl())
            .validationUrl(entity.getValidationUrl())
            .confirmationUrl(entity.getConfirmationUrl())
            .statusCallbackUrl(entity.getStatusCallbackUrl())
            .environment(entity.getEnvironment().name())
            .active(entity.getActive())
            .defaultConfig(entity.getDefaultConfig())
            .description(entity.getDescription())
            .lastTestSuccess(entity.getLastTestSuccess())
            .lastTestDate(entity.getLastTestDate())
            .lastTestMessage(entity.getLastTestMessage())
            .totalTransactions(entity.getTotalTransactions())
            .successfulTransactions(entity.getSuccessfulTransactions())
            .failedTransactions(entity.getFailedTransactions())
            .successRate(entity.getSuccessRate())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .build();
        
        if (maskSensitive) {
            dto.maskSensitiveFields();
        }
        
        return dto;
    }
}
