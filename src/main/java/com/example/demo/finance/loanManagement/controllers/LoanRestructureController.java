package com.example.demo.finance.loanManagement.controllers;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.services.LoanRestructureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for loan restructuring operations
 */
@RestController
@RequestMapping("/api/loan-restructure")
@RequiredArgsConstructor
@Slf4j
public class LoanRestructureController {
    
    private final LoanRestructureService restructureService;
    
    /**
     * Extend loan term (reduce monthly payment)
     */
    @PostMapping("/extend-term")
    public ResponseEntity<Map<String, Object>> extendTerm(
            @RequestParam Long loanId,
            @RequestParam Integer newTermMonths,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String reason) {
        
        try {
            LoanAccount loan = restructureService.extendLoanTerm(loanId, newTermMonths, approvedBy,
                reason != null ? reason : "Term extension approved");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan term extended successfully");
            response.put("loan", loan);
            response.put("newTerm", newTermMonths);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error extending loan term", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Change interest rate
     */
    @PostMapping("/change-rate")
    public ResponseEntity<Map<String, Object>> changeRate(
            @RequestParam Long loanId,
            @RequestParam BigDecimal newRate,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String reason) {
        
        try {
            LoanAccount loan = restructureService.changeInterestRate(loanId, newRate, approvedBy,
                reason != null ? reason : "Interest rate change approved");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Interest rate changed successfully");
            response.put("loan", loan);
            response.put("newRate", newRate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error changing interest rate", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Reduce monthly payment
     */
    @PostMapping("/reduce-payment")
    public ResponseEntity<Map<String, Object>> reducePayment(
            @RequestParam Long loanId,
            @RequestParam BigDecimal targetPayment,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String reason) {
        
        try {
            LoanAccount loan = restructureService.reduceMonthlyPayment(loanId, targetPayment, approvedBy,
                reason != null ? reason : "Payment reduction approved");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Monthly payment reduced successfully");
            response.put("loan", loan);
            response.put("targetPayment", targetPayment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error reducing monthly payment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Complete restructure (term + rate)
     */
    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeRestructure(
            @RequestParam Long loanId,
            @RequestParam Integer newTerm,
            @RequestParam BigDecimal newRate,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String reason) {
        
        try {
            LoanAccount loan = restructureService.completeRestructure(loanId, newTerm, newRate, approvedBy,
                reason != null ? reason : "Complete restructure approved");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan completely restructured");
            response.put("loan", loan);
            response.put("newTerm", newTerm);
            response.put("newRate", newRate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error restructuring loan", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
