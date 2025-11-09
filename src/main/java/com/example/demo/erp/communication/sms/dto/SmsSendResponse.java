package com.example.demo.erp.communication.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendResponse {
    private boolean success;
    private String message;
    private String phoneNumber;
    private Long configId;
    private LocalDateTime sentAt;
    private String errorCode;
}
