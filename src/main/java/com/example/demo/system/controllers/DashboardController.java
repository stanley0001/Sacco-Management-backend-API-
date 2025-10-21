package com.example.demo.system.controllers;

import com.example.demo.system.services.DashboardStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard statistics and metrics")
public class DashboardController {

    private final DashboardStatisticsService dashboardService;

    @GetMapping("/statistics")
    @Operation(summary = "Get comprehensive dashboard statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> statistics = dashboardService.getDashboardStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/loan-statistics")
    @Operation(summary = "Get loan portfolio statistics")
    public ResponseEntity<Map<String, Object>> getLoanStatistics() {
        Map<String, Object> loanStats = dashboardService.getLoanStatistics();
        return ResponseEntity.ok(loanStats);
    }

    @GetMapping("/customer-statistics")
    @Operation(summary = "Get customer statistics")
    public ResponseEntity<Map<String, Object>> getCustomerStatistics() {
        Map<String, Object> customerStats = dashboardService.getCustomerStatistics();
        return ResponseEntity.ok(customerStats);
    }

    @GetMapping("/savings-statistics")
    @Operation(summary = "Get savings statistics")
    public ResponseEntity<Map<String, Object>> getSavingsStatistics() {
        Map<String, Object> savingsStats = dashboardService.getSavingsStatistics();
        return ResponseEntity.ok(savingsStats);
    }

    @GetMapping("/financial-summary")
    @Operation(summary = "Get financial summary")
    public ResponseEntity<Map<String, Object>> getFinancialSummary() {
        Map<String, Object> financialSummary = dashboardService.getFinancialSummary();
        return ResponseEntity.ok(financialSummary);
    }
}
