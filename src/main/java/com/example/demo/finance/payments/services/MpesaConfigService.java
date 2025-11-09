package com.example.demo.finance.payments.services;

import com.example.demo.finance.payments.dto.*;
import com.example.demo.finance.payments.entities.MpesaConfig;
import com.example.demo.finance.payments.repositories.MpesaConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaConfigService {
    
    private final MpesaConfigRepository configRepository;
    private final RestTemplate restTemplate;
    
    /**
     * Get all M-PESA configurations
     */
    public List<MpesaConfigDTO> getAllConfigurations() {
        return configRepository.findAll().stream()
            .map(config -> MpesaConfigDTO.fromEntity(config, true))
            .toList();
    }
    
    /**
     * Get configuration by ID
     */
    public MpesaConfigDTO getConfigurationById(Long id) {
        MpesaConfig config = configRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Configuration not found with ID: " + id));
        return MpesaConfigDTO.fromEntity(config, false); // Don't mask for edit view
    }
    
    /**
     * Get default configuration
     */
    public MpesaConfig getDefaultConfiguration() {
        return configRepository.findByDefaultConfigTrue()
            .orElseThrow(() -> new RuntimeException("No default M-PESA configuration found"));
    }

    /**
     * Resolve an active configuration based on optional identifiers.
     * Falls back to the default configuration when none is provided or selected config is inactive.
     */
    public MpesaConfig getActiveConfiguration(Long configId, String providerCode) {
        MpesaConfig config = null;

        if (configId != null) {
            config = configRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("M-PESA configuration not found with ID: " + configId));
        } else if (StringUtils.hasText(providerCode)) {
            config = configRepository.findByConfigName(providerCode)
                .orElseThrow(() -> new RuntimeException("M-PESA configuration not found with code: " + providerCode));
        } else {
            config = getDefaultConfiguration();
        }

        if (!Boolean.TRUE.equals(config.getActive())) {
            MpesaConfig defaultConfig = getDefaultConfiguration();
            if (!defaultConfig.getId().equals(config.getId())) {
                log.warn("Selected M-PESA configuration {} is inactive. Falling back to default configuration {}.",
                    config.getConfigName(), defaultConfig.getConfigName());
                return defaultConfig;
            }
        }

        return config;
    }
    
    /**
     * Create new configuration
     */
    @Transactional
    public MpesaConfigDTO createConfiguration(MpesaConfigDTO dto, String currentUser) {
        log.info("Creating new M-PESA configuration: {}", dto.getConfigName());
        
        // Check if config name already exists
        if (configRepository.findByConfigName(dto.getConfigName()).isPresent()) {
            throw new RuntimeException("Configuration with name '" + dto.getConfigName() + "' already exists");
        }
        
        MpesaConfig config = new MpesaConfig();
        updateConfigFromDTO(config, dto);
        config.setCreatedBy(currentUser);
        config.setUpdatedBy(currentUser);
        
        // If this is set as default, unset other defaults
        if (Boolean.TRUE.equals(dto.getDefaultConfig())) {
            unsetAllDefaults();
        }
        
        config = configRepository.save(config);
        log.info("M-PESA configuration created with ID: {}", config.getId());
        
        return MpesaConfigDTO.fromEntity(config, true);
    }
    
    /**
     * Update configuration
     */
    @Transactional
    public MpesaConfigDTO updateConfiguration(Long id, MpesaConfigDTO dto, String currentUser) {
        log.info("Updating M-PESA configuration ID: {}", id);
        
        MpesaConfig config = configRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Configuration not found with ID: " + id));
        
        updateConfigFromDTO(config, dto);
        config.setUpdatedBy(currentUser);
        
        // If this is set as default, unset other defaults
        if (Boolean.TRUE.equals(dto.getDefaultConfig()) && !Boolean.TRUE.equals(config.getDefaultConfig())) {
            unsetAllDefaults();
        }
        
        config = configRepository.save(config);
        log.info("M-PESA configuration updated: {}", id);
        
        return MpesaConfigDTO.fromEntity(config, true);
    }
    
    /**
     * Delete configuration
     */
    @Transactional
    public void deleteConfiguration(Long id) {
        log.info("Deleting M-PESA configuration ID: {}", id);
        
        MpesaConfig config = configRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Configuration not found with ID: " + id));
        
        if (Boolean.TRUE.equals(config.getDefaultConfig())) {
            throw new RuntimeException("Cannot delete default configuration. Set another as default first.");
        }
        
        configRepository.delete(config);
        log.info("M-PESA configuration deleted: {}", id);
    }
    
    /**
     * Toggle configuration active status
     */
    @Transactional
    public MpesaConfigDTO toggleActiveStatus(Long id) {
        MpesaConfig config = configRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Configuration not found with ID: " + id));
        
        config.setActive(!config.getActive());
        config = configRepository.save(config);
        
        return MpesaConfigDTO.fromEntity(config, true);
    }
    
    /**
     * Set configuration as default
     */
    @Transactional
    public MpesaConfigDTO setAsDefault(Long id) {
        MpesaConfig config = configRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Configuration not found with ID: " + id));
        
        unsetAllDefaults();
        config.setDefaultConfig(true);
        config = configRepository.save(config);
        
        return MpesaConfigDTO.fromEntity(config, true);
    }
    
    /**
     * Test M-PESA connection
     */
    public MpesaTestConnectionResponse testConnection(MpesaTestConnectionRequest request) {
        log.info("Testing M-PESA connection: {}", request.getTestType());
        long startTime = System.currentTimeMillis();
        
        try {
            // Get configuration
            MpesaConfig config;
            if (request.getConfigId() != null) {
                config = configRepository.findById(request.getConfigId())
                    .orElseThrow(() -> new RuntimeException("Configuration not found"));
            } else if (request.getConsumerKey() != null) {
                // Use provided test credentials
                config = buildTestConfig(request);
            } else {
                config = getDefaultConfiguration();
            }
            
            String testType = request.getTestType() != null ? request.getTestType() : "AUTH";
            
            MpesaTestConnectionResponse response = switch (testType) {
                case "AUTH" -> testAuthentication(config);
                case "STK_PUSH" -> testSTKPush(config, request);
                case "QUERY" -> testQuery(config);
                default -> throw new IllegalArgumentException("Invalid test type: " + testType);
            };
            
            response.setDuration(System.currentTimeMillis() - startTime);
            
            // Update test results in config
            if (request.getConfigId() != null) {
                updateTestResults(request.getConfigId(), response);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("M-PESA connection test failed", e);
            return MpesaTestConnectionResponse.failure(
                request.getTestType(),
                "Connection test failed: " + e.getMessage(),
                "TEST_FAILED"
            );
        }
    }
    
    /**
     * Test authentication (get access token)
     */
    private MpesaTestConnectionResponse testAuthentication(MpesaConfig config) {
        try {
            log.info("Testing M-PESA authentication...");
            
            String credentials = Base64.getEncoder().encodeToString(
                (config.getConsumerKey() + ":" + config.getConsumerSecret()).getBytes()
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + credentials);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = config.getApiUrl() + "/oauth/v1/generate?grant_type=client_credentials";
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                Map<String, Object> details = new HashMap<>();
                details.put("access_token", body.get("access_token"));
                details.put("expires_in", body.get("expires_in"));
                
                return MpesaTestConnectionResponse.builder()
                    .success(true)
                    .testType("AUTH")
                    .message("Authentication successful! Access token obtained.")
                    .testTime(LocalDateTime.now())
                    .authenticationSuccess(true)
                    .apiReachable(true)
                    .configurationValid(true)
                    .details(details)
                    .build();
            }
            
            return MpesaTestConnectionResponse.failure(
                "AUTH",
                "Authentication failed: Invalid response",
                "AUTH_FAILED"
            );
            
        } catch (Exception e) {
            log.error("Authentication test failed", e);
            return MpesaTestConnectionResponse.failure(
                "AUTH",
                "Authentication failed: " + e.getMessage(),
                "AUTH_ERROR"
            );
        }
    }
    
    /**
     * Test STK Push
     */
    private MpesaTestConnectionResponse testSTKPush(MpesaConfig config, MpesaTestConnectionRequest request) {
        try {
            log.info("Testing M-PESA STK Push...");
            
            // First authenticate
            MpesaTestConnectionResponse authResponse = testAuthentication(config);
            if (!authResponse.getSuccess()) {
                return authResponse;
            }
            
            String accessToken = (String) authResponse.getDetails().get("access_token");
            
            // Prepare STK Push request
            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            String password = Base64.getEncoder().encodeToString(
                (config.getShortcode() + config.getPasskey() + timestamp).getBytes()
            );
            
            String phoneNumber = request.getTestPhoneNumber() != null 
                ? formatPhoneNumber(request.getTestPhoneNumber())
                : "254708374149"; // Test number
            
            BigDecimal amount = request.getTestAmount() != null 
                ? request.getTestAmount()
                : BigDecimal.ONE;
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("BusinessShortCode", config.getShortcode());
            requestBody.put("Password", password);
            requestBody.put("Timestamp", timestamp);
            requestBody.put("TransactionType", "CustomerPayBillOnline");
            requestBody.put("Amount", amount.intValue());
            requestBody.put("PartyA", phoneNumber);
            requestBody.put("PartyB", config.getShortcode());
            requestBody.put("PhoneNumber", phoneNumber);
            requestBody.put("CallBackURL", config.getCallbackUrl());
            requestBody.put("AccountReference", "TEST" + System.currentTimeMillis());
            requestBody.put("TransactionDesc", "Connection Test");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = config.getApiUrl() + "/mpesa/stkpush/v1/processrequest";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                return MpesaTestConnectionResponse.builder()
                    .success(true)
                    .testType("STK_PUSH")
                    .message("STK Push initiated successfully! Check phone " + phoneNumber + " for payment prompt.")
                    .testTime(LocalDateTime.now())
                    .authenticationSuccess(true)
                    .apiReachable(true)
                    .configurationValid(true)
                    .checkoutRequestId((String) body.get("CheckoutRequestID"))
                    .merchantRequestId((String) body.get("MerchantRequestID"))
                    .details(body)
                    .build();
            }
            
            return MpesaTestConnectionResponse.failure(
                "STK_PUSH",
                "STK Push failed: Invalid response",
                "STK_FAILED"
            );
            
        } catch (Exception e) {
            log.error("STK Push test failed", e);
            return MpesaTestConnectionResponse.failure(
                "STK_PUSH",
                "STK Push test failed: " + e.getMessage(),
                "STK_ERROR"
            );
        }
    }
    
    /**
     * Test query endpoint
     */
    private MpesaTestConnectionResponse testQuery(MpesaConfig config) {
        try {
            log.info("Testing M-PESA query endpoint...");
            
            // Just test authentication for now
            return testAuthentication(config);
            
        } catch (Exception e) {
            log.error("Query test failed", e);
            return MpesaTestConnectionResponse.failure(
                "QUERY",
                "Query test failed: " + e.getMessage(),
                "QUERY_ERROR"
            );
        }
    }
    
    // Helper methods
    
    private void updateConfigFromDTO(MpesaConfig config, MpesaConfigDTO dto) {
        config.setConfigName(dto.getConfigName());
        config.setConsumerKey(dto.getConsumerKey());
        
        // Only update secrets if provided (not masked)
        if (dto.getConsumerSecret() != null && !dto.getConsumerSecret().contains("***")) {
            config.setConsumerSecret(dto.getConsumerSecret());
        }
        if (dto.getPasskey() != null && !dto.getPasskey().contains("***")) {
            config.setPasskey(dto.getPasskey());
        }
        if (dto.getSecurityCredential() != null && !dto.getSecurityCredential().contains("***")) {
            config.setSecurityCredential(dto.getSecurityCredential());
        }
        
        config.setShortcode(dto.getShortcode());
        config.setInitiatorName(dto.getInitiatorName());
        config.setApiUrl(dto.getApiUrl());
        config.setCallbackUrl(dto.getCallbackUrl());
        config.setTimeoutUrl(dto.getTimeoutUrl());
        config.setResultUrl(dto.getResultUrl());
        
        // Dynamic callback URLs
        config.setStkCallbackUrl(dto.getStkCallbackUrl());
        config.setPaybillCallbackUrl(dto.getPaybillCallbackUrl());
        config.setB2cCallbackUrl(dto.getB2cCallbackUrl());
        config.setValidationUrl(dto.getValidationUrl());
        config.setConfirmationUrl(dto.getConfirmationUrl());
        config.setStatusCallbackUrl(dto.getStatusCallbackUrl());
        
        config.setEnvironment(MpesaConfig.EnvironmentType.valueOf(dto.getEnvironment()));
        config.setActive(dto.getActive());
        config.setDefaultConfig(dto.getDefaultConfig());
        config.setDescription(dto.getDescription());
    }
    
    private void unsetAllDefaults() {
        List<MpesaConfig> defaults = configRepository.findAll().stream()
            .filter(c -> Boolean.TRUE.equals(c.getDefaultConfig()))
            .toList();
        
        defaults.forEach(c -> c.setDefaultConfig(false));
        configRepository.saveAll(defaults);
    }
    
    private MpesaConfig buildTestConfig(MpesaTestConnectionRequest request) {
        MpesaConfig config = new MpesaConfig();
        config.setConsumerKey(request.getConsumerKey());
        config.setConsumerSecret(request.getConsumerSecret());
        config.setShortcode(request.getShortcode());
        config.setPasskey(request.getPasskey());
        config.setApiUrl(request.getApiUrl());
        return config;
    }
    
    @Transactional
    private void updateTestResults(Long configId, MpesaTestConnectionResponse response) {
        configRepository.findById(configId).ifPresent(config -> {
            config.setLastTestSuccess(response.getSuccess());
            config.setLastTestDate(LocalDateTime.now());
            config.setLastTestMessage(response.getMessage());
            configRepository.save(config);
        });
    }
    
    private String formatPhoneNumber(String phone) {
        // Remove any non-digit characters
        phone = phone.replaceAll("[^0-9]", "");
        
        // If starts with 0, replace with 254
        if (phone.startsWith("0")) {
            phone = "254" + phone.substring(1);
        }
        
        // If doesn't start with 254, add it
        if (!phone.startsWith("254")) {
            phone = "254" + phone;
        }
        
        return phone;
    }
}
