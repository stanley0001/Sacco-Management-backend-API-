package com.example.demo.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpesaTestConnectionRequest {
    
    private Long configId; // Optional - test specific config, otherwise use default
    
    private String testType; // "AUTH", "STK_PUSH", "QUERY"
    
    // For STK Push test
    private String testPhoneNumber; // Phone number to test STK push
    private BigDecimal testAmount; // Test amount (e.g., 1 KES)
    
    // Override config for testing without saving
    private String consumerKey;
    private String consumerSecret;
    private String shortcode;
    private String passkey;
    private String apiUrl;
}
