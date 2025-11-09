package com.example.demo.finance.loanManagement.controllers;

import com.example.demo.finance.loanManagement.dto.LoanBookUploadDTO;
import com.example.demo.finance.loanManagement.services.LoanBookTemplateService;
import com.example.demo.finance.loanManagement.services.LoanBookUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loan-book")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loan Book Upload", description = "Upload and import existing loan books")
public class LoanBookUploadController {
    
    private final LoanBookTemplateService templateService;
    private final LoanBookUploadService uploadService;
    
    /**
     * Download Excel template for loan book upload
     */
    @GetMapping("/template")
    @Operation(summary = "Download loan book upload template")
    public ResponseEntity<byte[]> downloadTemplate() {
        log.info("API: Downloading loan book template");
        
        try {
            byte[] template = templateService.generateTemplate();
            
            String filename = "loan_book_template_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(template.length);
            
            log.info("Template generated: {} bytes", template.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(template);
                
        } catch (IOException e) {
            log.error("Error generating template", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Upload and validate loan book file
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload loan book file for validation")
    public ResponseEntity<?> uploadFile(
        @RequestParam("file") @Parameter(description = "Excel file with loan data") MultipartFile file
    ) {
        log.info("API: Uploading loan book file: {}", file.getOriginalFilename());
        
        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "File is empty"));
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls") && !filename.endsWith(".csv"))) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid file type. Please upload an Excel file (.xlsx, .xls) or CSV file (.csv)"));
        }
        
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            return ResponseEntity.badRequest()
                .body(Map.of("error", "File too large. Maximum size is 10MB"));
        }
        
        try {
            LoanBookUploadService.UploadResult result = uploadService.processUpload(file);
            
            log.info("Upload processed: {} total, {} valid, {} invalid", 
                result.getTotalRows(), result.getValidRows(), result.getInvalidRows());
            
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            log.error("Error processing upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error processing file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    /**
     * Import validated loans into the system
     */
    @PostMapping("/import")
    @Operation(summary = "Import validated loans into the system")
    public ResponseEntity<?> importLoans(
        @RequestBody @Parameter(description = "List of validated loans to import") List<LoanBookUploadDTO> loans
    ) {
        log.info("API: Importing {} loans", loans.size());

        if (loans == null || loans.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "No loans to import"));
        }

        // Verify all loans are valid
        long invalidCount = loans.stream()
            .filter(loan -> loan.getIsValid() == null || !loan.getIsValid())
            .count();

        if (invalidCount > 0) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Cannot import invalid loans. " + invalidCount + " loans are invalid"));
        }

        try {
            LoanBookUploadService.ImportResult result = uploadService.importLoans(loans);

            log.info("Import complete: {} successful, {} failed",
                result.getSuccessCount(), result.getFailureCount());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error importing loans", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error importing loans: " + e.getMessage()));
        }
    }
    
    /**
     * Get upload statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get loan book upload statistics")
    public ResponseEntity<?> getUploadStats() {
        log.info("API: Getting upload statistics");
        
        // TODO: Implement statistics tracking
        // For now, return placeholder data
        Map<String, Object> stats = Map.of(
            "totalUploads", 0,
            "totalLoansImported", 0,
            "lastUploadDate", "N/A",
            "averageSuccessRate", 0.0
        );
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Validate a single loan (for testing)
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate a single loan entry")
    public ResponseEntity<?> validateLoan(
        @RequestBody @Parameter(description = "Loan data to validate") LoanBookUploadDTO loan
    ) {
        log.info("API: Validating single loan");
        
        try {
            // This endpoint can be used for real-time validation in the UI
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            log.error("Error validating loan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Validation error: " + e.getMessage()));
        }
    }
}
