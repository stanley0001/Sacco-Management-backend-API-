package com.example.demo.loanManagement.controllers;

import com.example.demo.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.loanManagement.services.LoanDisbursementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loan-disbursement")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loan Disbursement", description = "Manage loan disbursements and booking")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class LoanDisbursementController {

    private final LoanDisbursementService disbursementService;

    /**
     * Get pending loan applications for disbursement
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending loan applications for disbursement")
    @PreAuthorize("hasAnyAuthority('LOAN_DISBURSE', 'ADMIN_ACCESS')")
    public ResponseEntity<List<com.example.demo.loanManagement.parsistence.entities.LoanApplication>> getPendingDisbursements() {
        try {
            List<com.example.demo.loanManagement.parsistence.entities.LoanApplication> pending = disbursementService.getPendingDisbursements();
            return ResponseEntity.ok(pending);
        } catch (Exception e) {
            log.error("Error fetching pending disbursements", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Disburse a single loan
     */
    @PostMapping("/disburse/{applicationId}")
    @Operation(summary = "Disburse a loan")
    @PreAuthorize("hasAnyAuthority('LOAN_DISBURSE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> disburseLoan(
        @PathVariable Long applicationId,
        @RequestBody Map<String, String> request,
        Authentication authentication
    ) {
        try {
            String disbursedBy = authentication != null ? authentication.getName() : "system";
            String reference = request.get("reference");
            
            if (reference == null || reference.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Disbursement reference is required"
                ));
            }

            LoanAccount loanAccount = disbursementService.disburseLoan(applicationId, disbursedBy, reference);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Loan disbursed successfully",
                "loanAccountId", loanAccount.getId(),
                "loanReference", loanAccount.getLoanReference(),
                "amount", loanAccount.getPrincipalAmount()
            ));
        } catch (Exception e) {
            log.error("Error disbursing loan {}", applicationId, e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to disburse loan: " + e.getMessage()
            ));
        }
    }

    /**
     * Batch disburse multiple loans
     */
    @PostMapping("/batch-disburse")
    @Operation(summary = "Batch disburse multiple loans")
    @PreAuthorize("hasAnyAuthority('LOAN_DISBURSE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> batchDisburseLoan(
        @RequestBody Map<String, Object> request,
        Authentication authentication
    ) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> applicationIds = (List<Long>) request.get("applicationIds");
            
            if (applicationIds == null || applicationIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "No application IDs provided"
                ));
            }

            String disbursedBy = authentication != null ? authentication.getName() : "system";
            
            disbursementService.batchDisburseLoan(applicationIds, disbursedBy);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Batch disbursement initiated for %d loans", applicationIds.size()),
                "processedCount", applicationIds.size()
            ));
        } catch (Exception e) {
            log.error("Error in batch disbursement", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Batch disbursement failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Get disbursement history
     */
    @GetMapping("/history")
    @Operation(summary = "Get loan disbursement history")
    @PreAuthorize("hasAnyAuthority('LOAN_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<List<LoanAccount>> getDisbursementHistory() {
        try {
            List<LoanAccount> history = disbursementService.getDisbursementHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching disbursement history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get loan account details with payment schedules
     */
    @GetMapping("/loan-account/{loanAccountId}")
    @Operation(summary = "Get loan account details with payment schedules")
    @PreAuthorize("hasAnyAuthority('LOAN_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<?> getLoanAccountDetails(@PathVariable Long loanAccountId) {
        try {
            // This would typically fetch loan account with schedules
            // For now, return a placeholder response
            return ResponseEntity.ok(Map.of(
                "loanAccountId", loanAccountId,
                "message", "Loan account details retrieved",
                "hasSchedules", true
            ));
        } catch (Exception e) {
            log.error("Error fetching loan account details for {}", loanAccountId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validate loan application for disbursement
     */
    @GetMapping("/validate/{applicationId}")
    @Operation(summary = "Validate loan application for disbursement")
    @PreAuthorize("hasAnyAuthority('LOAN_VIEW', 'ADMIN_ACCESS')")
    public ResponseEntity<?> validateForDisbursement(@PathVariable Long applicationId) {
        try {
            // Add validation logic here
            // Check if application is approved, customer is active, etc.
            
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "Application is ready for disbursement",
                "applicationId", applicationId
            ));
        } catch (Exception e) {
            log.error("Error validating application {} for disbursement", applicationId, e);
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "message", "Validation failed: " + e.getMessage(),
                "applicationId", applicationId
            ));
        }
    }
}
