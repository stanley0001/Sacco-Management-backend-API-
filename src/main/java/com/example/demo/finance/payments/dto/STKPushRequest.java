package com.example.demo.finance.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class STKPushRequest {
    
    private String phoneNumber;
    private BigDecimal amount;
    private String accountReference;
    private String transactionDesc;
    
    // Optional: for linking to specific entities
    private Long customerId;
    /**
     * Legacy generic account identifier (remains for backward compatibility).
     * Prefer the specific loan/savings/bank identifiers when available.
     */
    private Long accountId;
    private Long loanId;
    private Long savingsAccountId;
    private Long bankAccountId;
    
    // Optional: provider metadata for multi-tenant configurations
    private Long providerConfigId;
    private String providerCode;
    
    // Optional: link back to transaction request record
    private Long transactionRequestId;
    
    // Validate phone number format
    public String getFormattedPhoneNumber() {
        return com.example.demo.finance.payments.utils.PhoneNumberUtils.normalizeForMpesa(phoneNumber);
    }
}
