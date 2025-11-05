package com.example.demo.loanManagement.controllers;

import com.example.demo.loanManagement.parsistence.entities.SuspensePayments;
import com.example.demo.loanManagement.parsistence.repositories.SuspensePaymentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing suspense payments
 * Provides endpoints to view and reconcile unallocated payments
 */
@RestController
@RequestMapping("/api/suspense-payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class SuspensePaymentController {

    private final SuspensePaymentRepo suspensePaymentRepo;

    /**
     * Get all suspense payments with pagination
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSuspensePayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String status
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("paymentTime").descending());
            Page<SuspensePayments> suspensePaymentsPage;

            if (status != null && !status.isEmpty()) {
                suspensePaymentsPage = suspensePaymentRepo.findByStatus(status, pageable);
            } else {
                suspensePaymentsPage = suspensePaymentRepo.findAll(pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("suspensePayments", suspensePaymentsPage.getContent());
            response.put("currentPage", suspensePaymentsPage.getNumber());
            response.put("totalItems", suspensePaymentsPage.getTotalElements());
            response.put("totalPages", suspensePaymentsPage.getTotalPages());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching suspense payments", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get suspense payments by phone number
     */
    @GetMapping("/by-phone/{phoneNumber}")
    public ResponseEntity<List<SuspensePayments>> getSuspenseByPhone(
            @PathVariable String phoneNumber,
            @RequestParam(defaultValue = "NEW") String status
    ) {
        try {
            return suspensePaymentRepo.findByAccountNumberAndStatus(phoneNumber, status)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        } catch (Exception e) {
            log.error("Error fetching suspense payments by phone", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get suspense payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SuspensePayments> getSuspensePaymentById(@PathVariable Long id) {
        try {
            return suspensePaymentRepo.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching suspense payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get suspense payments for a customer (by customer ID or phone)
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SuspensePayments>> getCustomerSuspensePayments(@PathVariable String customerId) {
        try {
            return suspensePaymentRepo.findByAccountNumberAndStatus(customerId, "NEW")
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.ok(List.of()));
        } catch (Exception e) {
            log.error("Error fetching customer suspense payments", e);
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Update suspense payment status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<SuspensePayments> updateSuspenseStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate
    ) {
        try {
            SuspensePayments suspense = suspensePaymentRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Suspense payment not found"));

            String newStatus = statusUpdate.get("status");
            String utilisedBy = statusUpdate.get("utilisedBy");

            suspense.setStatus(newStatus);
            if (utilisedBy != null) {
                suspense.setUtilisedBy(utilisedBy);
            }

            SuspensePayments updated = suspensePaymentRepo.save(suspense);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            log.error("Error updating suspense payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get suspense payment statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSuspenseStats() {
        try {
            List<SuspensePayments> allSuspense = suspensePaymentRepo.findAll();
            
            long newCount = allSuspense.stream().filter(s -> "NEW".equals(s.getStatus())).count();
            long processedCount = allSuspense.stream().filter(s -> "PROCESSED".equals(s.getStatus())).count();
            
            double totalNewAmount = allSuspense.stream()
                    .filter(s -> "NEW".equals(s.getStatus()))
                    .mapToDouble(s -> {
                        try {
                            return Double.parseDouble(s.getAmount());
                        } catch (Exception e) {
                            return 0.0;
                        }
                    })
                    .sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalNew", newCount);
            stats.put("totalProcessed", processedCount);
            stats.put("totalNewAmount", totalNewAmount);
            stats.put("total", allSuspense.size());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching suspense statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
