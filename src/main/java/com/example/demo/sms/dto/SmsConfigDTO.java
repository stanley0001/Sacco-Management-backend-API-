package com.example.demo.sms.dto;

import com.example.demo.sms.entities.SmsConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsConfigDTO {

    private Long id;
    private String configName;
    private String providerType;
    private Boolean active;
    private Boolean defaultConfig;
    private String description;
    private String apiUrl;
    private String apiKey;
    private String username;
    private String partnerId;
    private String shortcode;
    private String senderId;
    private String authToken;
    private String template;
    private Boolean lastTestSuccess;
    private LocalDateTime lastTestDate;
    private String lastTestMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String maskedApiKey;

    public static SmsConfigDTO fromEntity(SmsConfig entity, boolean maskSensitive) {
        if (entity == null) {
            return null;
        }

        SmsConfigDTO dto = SmsConfigDTO.builder()
            .id(entity.getId())
            .configName(entity.getConfigName())
            .providerType(entity.getProviderType() != null ? entity.getProviderType().name() : null)
            .active(entity.getActive())
            .defaultConfig(entity.getDefaultConfig())
            .description(entity.getDescription())
            .apiUrl(entity.getApiUrl())
            .apiKey(maskSensitive ? null : entity.getApiKey())
            .username(entity.getUsername())
            .partnerId(entity.getPartnerId())
            .shortcode(entity.getShortcode())
            .senderId(entity.getSenderId())
            .authToken(maskSensitive ? null : entity.getAuthToken())
            .template(entity.getTemplate())
            .lastTestSuccess(entity.getLastTestSuccess())
            .lastTestDate(entity.getLastTestDate())
            .lastTestMessage(entity.getLastTestMessage())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedBy(entity.getUpdatedBy())
            .maskedApiKey(maskSensitive ? maskApiKey(entity.getApiKey()) : entity.getApiKey())
            .build();

        return dto;
    }

    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "***";
        }
        if (apiKey.length() <= 4) {
            return "****";
        }
        return apiKey.substring(0, 2) + "***" + apiKey.substring(apiKey.length() - 2);
    }
}
