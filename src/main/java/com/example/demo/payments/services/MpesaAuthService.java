package com.example.demo.payments.services;

import com.example.demo.payments.entities.MpesaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpesaAuthService {

    private final RestTemplate restTemplate;
    private final MpesaConfigService configService;

    private String accessToken;
    private long tokenExpiryTime;
    private Long cachedConfigId;

    /**
     * Get valid access token (cached or fresh) for the resolved configuration
     */
    public String getAccessToken(Long providerConfigId, String providerCode) {
        MpesaConfig config = configService.getActiveConfiguration(providerConfigId, providerCode);

        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime &&
            cachedConfigId != null && cachedConfigId.equals(config.getId())) {
            log.debug("Using cached access token for config {}", config.getConfigName());
            return accessToken;
        }

        log.info("Generating new access token for config {}", config.getConfigName());
        return generateAccessToken(config);
    }

    /**
     * Generate new OAuth access token from Daraja API for specific configuration
     */
    private String generateAccessToken(MpesaConfig config) {
        try {
            if (config.getConsumerKey() == null || config.getConsumerKey().trim().isEmpty() ||
                config.getConsumerSecret() == null || config.getConsumerSecret().trim().isEmpty()) {
                log.warn("M-PESA credentials missing for configuration {}. Returning empty token.", config.getConfigName());
                return "";
            }

            String auth = config.getConsumerKey() + ":" + config.getConsumerSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = config.getApiUrl() + "/oauth/v1/generate?grant_type=client_credentials";
            log.debug("Requesting access token from: {}", url);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                accessToken = (String) response.getBody().get("access_token");
                String expiresIn = (String) response.getBody().get("expires_in");

                long expirySeconds = Long.parseLong(expiresIn) - 60;
                tokenExpiryTime = System.currentTimeMillis() + (expirySeconds * 1000);
                cachedConfigId = config.getId();

                log.info("Successfully generated access token for config {}, expires in {} seconds", config.getConfigName(), expirySeconds);
                return accessToken;
            }

            throw new RuntimeException("Failed to get M-PESA access token: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Error generating M-PESA access token", e);
            log.warn("M-PESA integration disabled due to missing credentials or API errors for config {}", config.getConfigName());
            return "";
        }
    }

    /**
     * Clear cached token (useful for testing or troubleshooting)
     */
    public void clearToken() {
        accessToken = null;
        tokenExpiryTime = 0;
        cachedConfigId = null;
        log.info("Access token cleared");
    }
}
