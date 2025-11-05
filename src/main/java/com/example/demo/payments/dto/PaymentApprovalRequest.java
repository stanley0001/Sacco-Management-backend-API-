package com.example.demo.payments.dto;

import lombok.Data;

@Data
public class PaymentApprovalRequest {
    private String referenceNumber; // For approval - optional additional reference
    private String rejectionReason; // For rejection - required
    private String comments;
}
