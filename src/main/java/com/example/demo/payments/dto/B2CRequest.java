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
public class B2CRequest {
    
    private String phoneNumber;
    private BigDecimal amount;
    private String occasion;
    private String remarks;
    private Long customerId;
    private String commandId; // SalaryPayment, BusinessPayment, PromotionPayment
    private Long providerConfigId;
    private String providerCode;
    
    // Validate and format phone number
    public String getFormattedPhoneNumber() {
        String phone = phoneNumber.replaceAll("\\D", "");
        if (phone.startsWith("0")) {
            phone = "254" + phone.substring(1);
        } else if (!phone.startsWith("254")) {
            phone = "254" + phone;
        }
        return phone;
    }
}
