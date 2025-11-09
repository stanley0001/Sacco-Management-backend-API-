package com.example.demo.erp.customerManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for returning bulk import results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDto {
    private int successful;
    private int failed;
    private int totalRecords;
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
    private String fileName;
    private String importedBy;
    private String timestamp;
}
