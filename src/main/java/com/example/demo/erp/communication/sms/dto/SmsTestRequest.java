package com.example.demo.erp.communication.sms.dto;

import lombok.Data;

@Data
public class SmsTestRequest {
    private Long configId;
    private String testType; // SINGLE, BULK, BALANCE, DLR
    private String phoneNumber;
    private String message;
    private String messageId;
    private Integer count;
}
