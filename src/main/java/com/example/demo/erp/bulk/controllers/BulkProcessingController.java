package com.example.demo.erp.bulk.controllers;

import com.example.demo.erp.bulk.services.BulkProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bulk")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bulk Processing", description = "Bulk import/export operations for scalable data management")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class BulkProcessingController {

    private final BulkProcessingService bulkProcessingService;

    /**
     * Bulk import customers from CSV
     */
    @PostMapping("/import/customers")
    @Operation(summary = "Bulk import customers from CSV file")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_IMPORT', 'ADMIN_ACCESS')")
    public ResponseEntity<Map<String, Object>> bulkImportCustomers(
        @RequestParam("file") MultipartFile file,
        Authentication authentication
    ) {
        try {
            String importedBy = authentication != null ? authentication.getName() : "system";
            Map<String, Object> result = bulkProcessingService.bulkImportCustomers(file, importedBy);
            
            log.info("Bulk customer import completed by {}: {}", importedBy, result.get("message"));
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in bulk customer import", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Import failed: " + e.getMessage()));
        }
    }

    /**
     * Bulk import loan applications from CSV
     */
    @PostMapping("/import/loan-applications")
    @Operation(summary = "Bulk import loan applications from CSV file")
    @PreAuthorize("hasAnyAuthority('LOAN_IMPORT', 'ADMIN_ACCESS')")
    public ResponseEntity<Map<String, Object>> bulkImportLoanApplications(
        @RequestParam("file") MultipartFile file,
        Authentication authentication
    ) {
        try {
            String importedBy = authentication != null ? authentication.getName() : "system";
            Map<String, Object> result = bulkProcessingService.bulkImportLoanApplications(file, importedBy);
            
            log.info("Bulk loan applications import completed by {}: {}", importedBy, result.get("message"));
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in bulk loan applications import", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Import failed: " + e.getMessage()));
        }
    }

    /**
     * Bulk import users from CSV
     */
    @PostMapping("/import/users")
    @Operation(summary = "Bulk import users from CSV file")
    @PreAuthorize("hasAnyAuthority('USER_IMPORT', 'ADMIN_ACCESS')")
    public ResponseEntity<Map<String, Object>> bulkImportUsers(
        @RequestParam("file") MultipartFile file,
        Authentication authentication
    ) {
        try {
            String importedBy = authentication != null ? authentication.getName() : "system";
            Map<String, Object> result = bulkProcessingService.bulkImportUsers(file, importedBy);
            
            log.info("Bulk users import completed by {}: {}", importedBy, result.get("message"));
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in bulk users import", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Import failed: " + e.getMessage()));
        }
    }

    /**
     * Bulk process payments from CSV
     */
    @PostMapping("/process/payments")
    @Operation(summary = "Bulk process payments from CSV file")
    @PreAuthorize("hasAnyAuthority('PAYMENT_BULK_PROCESS', 'ADMIN_ACCESS')")
    public ResponseEntity<Map<String, Object>> bulkProcessPayments(
        @RequestParam("file") MultipartFile file,
        Authentication authentication
    ) {
        try {
            String processedBy = authentication != null ? authentication.getName() : "system";
            Map<String, Object> result = bulkProcessingService.bulkProcessPayments(file, processedBy);
            
            log.info("Bulk payments processing completed by {}: {}", processedBy, result.get("message"));
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in bulk payments processing", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Processing failed: " + e.getMessage()));
        }
    }

    /**
     * Bulk loan disbursement
     */
    @PostMapping("/disburse/loans")
    @Operation(summary = "Bulk disburse approved loans")
    @PreAuthorize("hasAnyAuthority('LOAN_DISBURSE', 'ADMIN_ACCESS')")
    public ResponseEntity<Map<String, Object>> bulkDisburseLoan(
        @RequestBody Map<String, Object> request,
        Authentication authentication
    ) {
        try {
            // Convert application IDs from Integer to Long (JSON deserializes numbers as Integer by default)
            @SuppressWarnings("unchecked")
            List<?> rawIds = (List<?>) request.get("applicationIds");
            List<Long> applicationIds = rawIds.stream()
                .map(id -> id instanceof Number ? ((Number) id).longValue() : Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toList());
            
            String disbursementMethod = (String) request.getOrDefault("disbursementMethod", "SACCO_ACCOUNT");
            String disbursedBy = authentication != null ? authentication.getName() : "system";
            
            Map<String, Object> result = bulkProcessingService.bulkDisburseLoan(
                applicationIds, disbursementMethod, disbursedBy);
            
            log.info("Bulk loan disbursement completed by {}: {}", disbursedBy, result.get("message"));
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in bulk loan disbursement", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Disbursement failed: " + e.getMessage()));
        }
    }

    /**
     * Export customers to CSV
     */
    @GetMapping("/export/customers")
    @Operation(summary = "Export customers to CSV file")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_EXPORT', 'ADMIN_ACCESS')")
    public ResponseEntity<ByteArrayResource> exportCustomers(
        @RequestParam(required = false) Long branchId,
        @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        try {
            String csvContent = bulkProcessingService.exportCustomersToCSV(branchId, includeInactive);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("customers_export_%s.csv", timestamp);
            
            ByteArrayResource resource = new ByteArrayResource(csvContent.getBytes());
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
                
        } catch (Exception e) {
            log.error("Error exporting customers", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export loan accounts to CSV
     */
    @GetMapping("/export/loan-accounts")
    @Operation(summary = "Export loan accounts to CSV file")
    @PreAuthorize("hasAnyAuthority('LOAN_EXPORT', 'ADMIN_ACCESS')")
    public ResponseEntity<ByteArrayResource> exportLoanAccounts(
        @RequestParam(required = false) Long branchId,
        @RequestParam(required = false) String status
    ) {
        try {
            String csvContent = bulkProcessingService.exportLoanAccountsToCSV(branchId, status);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("loan_accounts_export_%s.csv", timestamp);
            
            ByteArrayResource resource = new ByteArrayResource(csvContent.getBytes());
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
                
        } catch (Exception e) {
            log.error("Error exporting loan accounts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download CSV template for entity type
     */
    @GetMapping("/template/{entityType}")
    @Operation(summary = "Download CSV template for bulk import")
    @PreAuthorize("hasAnyAuthority('BULK_ACCESS', 'ADMIN_ACCESS')")
    public ResponseEntity<ByteArrayResource> downloadTemplate(@PathVariable String entityType) {
        try {
            String csvTemplate = bulkProcessingService.generateCsvTemplate(entityType);
            String filename = String.format("%s_template.csv", entityType);
            
            ByteArrayResource resource = new ByteArrayResource(csvTemplate.getBytes());
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
                
        } catch (Exception e) {
            log.error("Error generating template for {}", entityType, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get bulk processing statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get bulk processing statistics and summaries")
    @PreAuthorize("hasAnyAuthority('BULK_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<Map<String, Object>> getBulkProcessingStats() {
        try {
            Map<String, Object> stats = bulkProcessingService.getBulkProcessingStats();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error fetching bulk processing statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validate CSV file format before processing
     */
    @PostMapping("/validate/{entityType}")
    @Operation(summary = "Validate CSV file format before bulk processing")
    @PreAuthorize("hasAnyAuthority('BULK_ACCESS', 'ADMIN_ACCESS')")
    public ResponseEntity<Map<String, Object>> validateCsvFile(
        @PathVariable String entityType,
        @RequestParam("file") MultipartFile file
    ) {
        try {
            Map<String, Object> validation = validateFileFormat(file, entityType);
            return ResponseEntity.ok(validation);
            
        } catch (Exception e) {
            log.error("Error validating CSV file", e);
            return ResponseEntity.badRequest()
                .body(Map.of("valid", false, "message", "Validation failed: " + e.getMessage()));
        }
    }

    // Helper methods

    private Map<String, Object> validateFileFormat(MultipartFile file, String entityType) {
        Map<String, Object> result = Map.of(
            "valid", true,
            "message", "File format is valid",
            "recordCount", 0,
            "requiredColumns", getRequiredColumns(entityType)
        );

        // Basic validation
        if (file.isEmpty()) {
            return Map.of("valid", false, "message", "File is empty");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            return Map.of("valid", false, "message", "File must be a CSV file");
        }

        // Additional validation logic can be added here
        // For now, return success for basic checks

        return result;
    }

    private String[] getRequiredColumns(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "customers" -> new String[]{"firstName", "lastName", "email", "phoneNumber"};
            case "loans" -> new String[]{"customerId", "productId", "amount", "term"};
            case "payments" -> new String[]{"customerId", "amount", "paymentType", "paymentMethod"};
            case "users" -> new String[]{"username", "email", "firstName", "lastName"};
            default -> new String[]{};
        };
    }
}
