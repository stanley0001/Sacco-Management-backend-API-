package com.example.demo.finance.loanManagement.controllers;

import com.example.demo.finance.loanManagement.services.LoanWaiverService;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.services.LoanWaiverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for loan waiver operations
 */
@RestController
@RequestMapping("/api/loan-waivers")
@RequiredArgsConstructor
@Slf4j
public class LoanWaiverController {
    
    private final LoanWaiverService waiverService;
    
    /**
     * Waive interest on a loan
     */
    @PostMapping("/interest")
    public ResponseEntity<Map<String, Object>> waiveInterest(
            @RequestParam Long loanId,
            @RequestParam BigDecimal amount,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String reason) {
        
        try {
            LoanAccount loan = waiverService.waiveInterest(loanId, amount, approvedBy, 
                reason != null ? reason : "Interest waiver approved");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Interest waiver processed successfully");
            response.put("loan", loan);
            response.put("waivedAmount", amount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing interest waiver", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Waive penalty on a loan
     */
    @PostMapping("/penalty")
    public ResponseEntity<Map<String, Object>> waivePenalty(
            @RequestParam Long loanId,
            @RequestParam BigDecimal amount,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String reason) {
        
        try {
            LoanAccount loan = waiverService.waivePenalty(loanId, amount, approvedBy,
                reason != null ? reason : "Penalty waiver approved");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Penalty waiver processed successfully");
            response.put("loan", loan);
            response.put("waivedAmount", amount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing penalty waiver", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Waive part of principal (partial write-off)
     */
    @PostMapping("/principal")
    public ResponseEntity<Map<String, Object>> waivePrincipal(
            @RequestParam Long loanId,
            @RequestParam BigDecimal amount,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String reason) {
        
        try {
            LoanAccount loan = waiverService.waivePrincipal(loanId, amount, approvedBy,
                reason != null ? reason : "Principal waiver approved");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Principal waiver processed successfully");
            response.put("loan", loan);
            response.put("waivedAmount", amount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing principal waiver", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Full waiver - write off entire loan
     */
    @PostMapping("/full")
    public ResponseEntity<Map<String, Object>> waiveFull(
            @RequestParam Long loanId,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String reason) {
        
        try {
            LoanAccount loan = waiverService.waiveFull(loanId, approvedBy,
                reason != null ? reason : "Full waiver - loan written off");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan fully waived and written off");
            response.put("loan", loan);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing full waiver", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
