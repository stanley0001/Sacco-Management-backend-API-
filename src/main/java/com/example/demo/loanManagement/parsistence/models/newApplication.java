package com.example.demo.loanManagement.parsistence.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class newApplication {
    private String amount;
    private String phone;           // For backward compatibility
    private String phoneNumber;     // From frontend
    private String productCode;
    private String installments;
    private String term;
    private Long customerId;
    private String purpose;
    private String notes;
    private Boolean termsAccepted;
    
    // Helper method to get phone number from either field
    public String getPhoneNumberValue() {
        return phoneNumber != null ? phoneNumber : phone;
    }
}
