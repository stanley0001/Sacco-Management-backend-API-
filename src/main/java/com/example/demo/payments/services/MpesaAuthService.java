package com.example.demo.payments.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class MpesaAuthService {
    
    @Value("${mpesa.api.url:https://sandbox.safaricom.co.ke}")
    private String apiUrl;
    
    @Value("${mpesa.consumer.key}")
    private String consumerKey;
    
    @Value("${mpesa.consumer.secret}")
    private String consumerSecret;
    
    private final RestTemplate restTemplate;
    private String accessToken;
    private long tokenExpiryTime;
    
    public MpesaAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Get valid access token (cached or fresh)
     */
    public String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            log.debug("Using cached access token");
            return accessToken;
        }
        
        log.info("Generating new access token");
        return generateAccessToken();
    }
    
    /**
     * Generate new OAuth access token from Daraja API
     */
    private String generateAccessToken() {
        try {
            String auth = consumerKey + ":" + consumerSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = apiUrl + "/oauth/v1/generate?grant_type=client_credentials";
            log.debug("Requesting access token from: {}", url);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                accessToken = (String) response.getBody().get("access_token");
                String expiresIn = (String) response.getBody().get("expires_in");
                
                // Set expiry time with 60 second buffer
                long expirySeconds = Long.parseLong(expiresIn) - 60;
                tokenExpiryTime = System.currentTimeMillis() + (expirySeconds * 1000);
                
                log.info("Successfully generated access token, expires in {} seconds", expirySeconds);
                return accessToken;
            }
            
            throw new RuntimeException("Failed to get M-PESA access token: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Error generating M-PESA access token", e);
            throw new RuntimeException("Failed to generate M-PESA access token", e);
        }
    }
    
    /**
     * Clear cached token (useful for testing or troubleshooting)
     */
    public void clearToken() {
        accessToken = null;
        tokenExpiryTime = 0;
        log.info("Access token cleared");
    }
}
