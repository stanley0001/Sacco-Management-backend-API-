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
public class STKPushRequest {
    
    private String phoneNumber;
    private BigDecimal amount;
    private String accountReference;
    private String transactionDesc;
    
    // Optional: for linking to specific entities
    private Long customerId;
    private Long loanId;
    private Long savingsAccountId;
    
    // Validate phone number format
    public String getFormattedPhoneNumber() {
        String phone = phoneNumber.replaceAll("[^0-9]", "");
        if (phone.startsWith("0")) {
            phone = "254" + phone.substring(1);
        } else if (!phone.startsWith("254")) {
            phone = "254" + phone;
        }
        return phone;
    }
}
