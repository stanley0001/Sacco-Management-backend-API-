package com.example.demo.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendRequest {
    private String phoneNumber;
    private String message;
    private Long configId; // Optional - if null, use default config

    // Frontend compatibility fields
    private String recipient; // Alternative field name from frontend
    private String messageType;
    private String subject;
    private String urgent;
    private Long customerId;

    // Get the actual phone number to use (handles both field names)
    public String getEffectivePhoneNumber() {
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }
        if (recipient != null && !recipient.trim().isEmpty()) {
            return recipient;
        }
        return null;
    }

    // Get the actual message to use
    public String getEffectiveMessage() {
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        return null;
    }
}
