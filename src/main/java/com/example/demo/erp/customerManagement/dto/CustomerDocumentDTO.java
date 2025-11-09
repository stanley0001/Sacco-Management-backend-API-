package com.example.demo.erp.customerManagement.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDocumentDTO {
    
    private Long id;
    private String documentType; // ID_COPY, PASSPORT_PHOTO, BANK_STATEMENT, PAYSLIP, CONTRACT, OTHER
    private String documentName;
    private String description;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private String status; // PENDING, VERIFIED, REJECTED
    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
    
    private Boolean isActive;
    private String icon;
}
