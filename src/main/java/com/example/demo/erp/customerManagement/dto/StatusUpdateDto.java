package com.example.demo.erp.customerManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating customer status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateDto {
    private Boolean isActive;
    private String reason;
    private String updatedBy;
}
