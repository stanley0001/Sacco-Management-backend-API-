package com.example.demo.reports.controllers;

import com.example.demo.reports.services.FinancialReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/financial-reports")
@RequiredArgsConstructor
@Tag(name = "Financial Reports", description = "Balance Sheet, P&L, Income Statement, Trial Balance")
public class FinancialReportsController {

    private final FinancialReportsService financialReportsService;

    @GetMapping("/balance-sheet")
    @Operation(summary = "Generate Balance Sheet")
    public ResponseEntity<Map<String, Object>> getBalanceSheet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        Map<String, Object> balanceSheet = financialReportsService.generateBalanceSheet(asOfDate);
        return ResponseEntity.ok(balanceSheet);
    }

    @GetMapping("/profit-loss")
    @Operation(summary = "Generate Profit & Loss Statement")
    public ResponseEntity<Map<String, Object>> getProfitLoss(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> profitLoss = financialReportsService.generateProfitLossStatement(startDate, endDate);
        return ResponseEntity.ok(profitLoss);
    }

    @GetMapping("/income-statement")
    @Operation(summary = "Generate Income Statement")
    public ResponseEntity<Map<String, Object>> getIncomeStatement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> incomeStatement = financialReportsService.generateIncomeStatement(startDate, endDate);
        return ResponseEntity.ok(incomeStatement);
    }

    @GetMapping("/trial-balance")
    @Operation(summary = "Generate Trial Balance")
    public ResponseEntity<Map<String, Object>> getTrialBalance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        Map<String, Object> trialBalance = financialReportsService.generateTrialBalance(asOfDate);
        return ResponseEntity.ok(trialBalance);
    }

    @GetMapping("/cash-flow")
    @Operation(summary = "Generate Cash Flow Statement")
    public ResponseEntity<Map<String, Object>> getCashFlowStatement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> cashFlow = financialReportsService.generateCashFlowStatement(startDate, endDate);
        return ResponseEntity.ok(cashFlow);
    }
}
