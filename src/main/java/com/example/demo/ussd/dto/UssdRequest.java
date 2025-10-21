package com.example.demo.ussd.dto;

import lombok.Data;

@Data
public class UssdRequest {
    private String sessionId;
    private String serviceCode;
    private String phoneNumber;
    private String text;
}
