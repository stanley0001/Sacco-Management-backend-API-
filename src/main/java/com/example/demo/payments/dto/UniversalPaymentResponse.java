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
public class UniversalPaymentResponse {
    
    // Response Status
    private boolean success;
    private String responseCode;
    private String responseDescription;
    private String customerMessage;
    
    // Payment Information
    private String paymentMethod;
    private BigDecimal amount;
    private String referenceNumber;
    
    // M-PESA Specific Fields
    private String checkoutRequestId;
    private String merchantRequestId;
    private String mpesaReceiptNumber;
    private String transactionStatus;
    private String transactionId;
    private String phoneNumber;
    
    // Transaction Tracking
    private Long transactionRequestId;
    private Long mpesaTransactionId;
    
    // Status Checking
    private boolean requiresStatusCheck;
    private int recommendedCheckIntervalSeconds;
    private int maxStatusChecks;
    private String paymentStatus;
    private boolean paymentCompleted;
    
    // Payment Status Flags
    private boolean completed;
    private boolean cancelled;
    private boolean failed;
    
    // Error Information
    private String errorCode;
    private String errorMessage;
    
    // Helper methods for payment status
    public String getPaymentStatus() {
        if (paymentStatus != null) {
            return paymentStatus;
        }
        return transactionStatus != null ? transactionStatus : "UNKNOWN";
    }
    
    public boolean isPaymentCompleted() {
        if (paymentCompleted || completed) {
            return true;
        }
        String status = getPaymentStatus();
        return "COMPLETED".equalsIgnoreCase(status) || 
               "SUCCESS".equalsIgnoreCase(status) || 
               "0".equals(responseCode);
    }
}
