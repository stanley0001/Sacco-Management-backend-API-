package com.example.demo.finance.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpesaTestConnectionResponse {
    
    private Boolean success;
    private String testType;
    private String message;
    private LocalDateTime testTime;
    private Long duration; // milliseconds
    
    // Detailed test results
    private Map<String, Object> details;
    
    // Error information if failed
    private String errorCode;
    private String errorMessage;
    
    // Test steps results
    private Boolean authenticationSuccess;
    private Boolean apiReachable;
    private Boolean configurationValid;
    
    // For STK Push tests
    private String checkoutRequestId;
    private String merchantRequestId;
    
    public static MpesaTestConnectionResponse success(String testType, String message) {
        return MpesaTestConnectionResponse.builder()
            .success(true)
            .testType(testType)
            .message(message)
            .testTime(LocalDateTime.now())
            .build();
    }
    
    public static MpesaTestConnectionResponse failure(String testType, String message, String errorCode) {
        return MpesaTestConnectionResponse.builder()
            .success(false)
            .testType(testType)
            .message(message)
            .errorCode(errorCode)
            .testTime(LocalDateTime.now())
            .build();
    }
}
