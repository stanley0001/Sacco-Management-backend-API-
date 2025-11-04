package com.example.demo.loanManagement.controllers;

import com.example.demo.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.loanManagement.parsistence.repositories.ApplicationRepo;
import com.example.demo.loanManagement.services.LoanApplicationApprovalService;
import com.example.demo.loanManagement.services.LoanService;
import com.example.demo.loanManagement.parsistence.models.newApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loan Applications", description = "Manage loan applications and approvals")
public class LoanApplicationController {

    private final ApplicationRepo applicationRepo;
    private final LoanApplicationApprovalService approvalService;
    private final LoanService loanService;

    @GetMapping("/all")
    @Operation(summary = "Get all loan applications")
    public ResponseEntity<Page<LoanApplication>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanApplication> applications = applicationRepo.findAllByOrderByApplicationTimeDesc(pageable);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending loan applications")
    public ResponseEntity<List<LoanApplication>> getPendingApplications() {
        List<LoanApplication> applications = applicationRepo.findByApplicationStatus("NEW");
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get loan applications by status")
    public ResponseEntity<List<LoanApplication>> getApplicationsByStatus(@PathVariable String status) {
        List<LoanApplication> applications = applicationRepo.findByApplicationStatus(status);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get loan application by ID")
    public ResponseEntity<LoanApplication> getApplicationById(@PathVariable Long id) {
        Optional<LoanApplication> application = applicationRepo.findById(id);
        return application.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a loan application")
    public ResponseEntity<?> approveApplication(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            String approvedBy = requestBody != null ? requestBody.get("approvedBy") : "SYSTEM";
            String comments = requestBody != null ? requestBody.get("comments") : "";
            boolean createAccount = requestBody != null && "true".equalsIgnoreCase(requestBody.get("createAccount"));
            
            LoanApplication approved = approvalService.approveApplication(id, approvedBy, comments);
            
            // If createAccount flag is true, create loan account immediately
            if (createAccount) {
                try {
                    Map<String, Object> accountResult = loanService.createLoanAccountFromApplication(id);
                    return ResponseEntity.ok(Map.of(
                        "application", approved,
                        "loanAccount", accountResult,
                        "message", "Application approved and loan account created successfully"
                    ));
                } catch (Exception e) {
                    log.error("Error creating loan account", e);
                    return ResponseEntity.ok(Map.of(
                        "application", approved,
                        "warning", "Application approved but loan account creation failed: " + e.getMessage()
                    ));
                }
            }
            
            return ResponseEntity.ok(approved);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error approving application", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to approve application"));
        }
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a loan application")
    public ResponseEntity<?> rejectApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        try {
            String rejectedBy = requestBody.get("rejectedBy");
            String reason = requestBody.get("reason");
            
            LoanApplication rejected = approvalService.rejectApplication(id, rejectedBy, reason);
            return ResponseEntity.ok(rejected);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error rejecting application", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reject application"));
        }
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get loan applications by customer ID")
    public ResponseEntity<List<LoanApplication>> getApplicationsByCustomerId(@PathVariable String customerId) {
        List<LoanApplication> applications = applicationRepo.findByCustomerId(customerId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get loan application statistics")
    public ResponseEntity<Map<String, Object>> getApplicationStatistics() {
        Map<String, Object> stats = approvalService.getApplicationStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get paginated loan applications")
    public ResponseEntity<Page<LoanApplication>> getPaginatedApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("applicationTime").descending());
        Page<LoanApplication> applications;
        
        if (status != null && !status.isEmpty()) {
            applications = applicationRepo.findByApplicationStatus(status, pageRequest);
        } else {
            applications = applicationRepo.findAll(pageRequest);
        }
        
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/apply")
    @Operation(summary = "Submit a new loan application")
    public ResponseEntity<?> applyForLoan(@RequestBody newApplication application) {
        try {
            log.info("Loan application received for customerId: {}, phone: {}, product: {}, amount: {}",
                application.getCustomerId(), application.getPhoneNumberValue(), 
                application.getProductCode(), application.getAmount());

            // Call the existing loan service method
            LoanApplication loanApplication = loanService.loanApplication(
                application.getCustomerId(),
                application.getPhoneNumberValue(),
                application.getProductCode(),
                application.getAmount()
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Loan application submitted successfully",
                "applicationId", loanApplication.getApplicationId(),
                "status", loanApplication.getApplicationStatus()
            ));

        } catch (IllegalStateException e) {
            log.error("Error applying for loan: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage(),
                "errorCode", "ERR_APPLICATION_FAILED"
            ));
        } catch (Exception e) {
            log.error("Unexpected error applying for loan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to submit loan application",
                "errorCode", "ERR_INTERNAL_ERROR"
            ));
        }
    }
    
    @PostMapping("/{id}/create-account")
    @Operation(summary = "Create loan account from approved application")
    public ResponseEntity<?> createLoanAccount(@PathVariable Long id) {
        try {
            log.info("Creating loan account for application ID: {}", id);
            
            Map<String, Object> result = loanService.createLoanAccountFromApplication(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Loan account created successfully",
                "data", result
            ));
            
        } catch (IllegalStateException e) {
            log.error("Error creating loan account: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error creating loan account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Failed to create loan account: " + e.getMessage()
            ));
        }
    }
}
