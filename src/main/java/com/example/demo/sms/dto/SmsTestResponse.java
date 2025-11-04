package com.example.demo.sms.dto;

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
public class SmsTestResponse {
    private boolean success;
    private String message;
    private String testType;
    private LocalDateTime testTime;
    private Long configId;
    private Map<String, Object> details;
    private String errorCode;
}
