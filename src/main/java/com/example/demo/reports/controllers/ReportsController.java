package com.example.demo.reports.controllers;

import com.example.demo.reports.models.LoanPortfolioReport;
import com.example.demo.reports.models.ReportType;
import com.example.demo.reports.models.SASRAReport;
import com.example.demo.reports.services.ExcelExportService;
import com.example.demo.reports.services.ReportGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Comprehensive reporting including SASRA compliance reports")
public class ReportsController {

    private final ReportGenerationService reportGenerationService;
    private final ExcelExportService excelExportService;

    @GetMapping("/loan-portfolio")
    public ResponseEntity<LoanPortfolioReport> getLoanPortfolioReport(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LoanPortfolioReport report = reportGenerationService.generateLoanPortfolioReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }


    @GetMapping("/loan-portfolio/export")
    @Operation(summary = "Export loan portfolio report to Excel")
    public ResponseEntity<byte[]> exportLoanPortfolioReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        LoanPortfolioReport report = reportGenerationService.generateLoanPortfolioReport(startDate, endDate);
        byte[] excelData = excelExportService.exportLoanPortfolioToExcel(report, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "loan-portfolio-report.xlsx");

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/sasra")
    public ResponseEntity<SASRAReport> getSASRAReport(
            @RequestParam(name = "reportDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        SASRAReport report = reportGenerationService.generateSASRAReport(reportDate);
        return ResponseEntity.ok(report);
    }


    @GetMapping("/sasra/export")
    @Operation(summary = "Export SASRA report to Excel")
    public ResponseEntity<byte[]> exportSASRAReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) throws IOException {
        SASRAReport report = reportGenerationService.generateSASRAReport(reportDate);
        byte[] excelData = excelExportService.exportSASRAReportToExcel(report);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", 
                "sasra-report-" + reportDate.toString() + ".xlsx");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/sasra/sg3")
    @Operation(summary = "Get SG3 Loan Classification Report")
    public ResponseEntity<SASRAReport.SG3LoanClassification> getSG3Report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        SASRAReport report = reportGenerationService.generateSASRAReport(reportDate);
        return ResponseEntity.ok(report.getLoanClassification());
    }

    @GetMapping("/sasra/sg4")
    @Operation(summary = "Get SG4 Liquidity Report")
    public ResponseEntity<SASRAReport.SG4Liquidity> getSG4Report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        SASRAReport report = reportGenerationService.generateSASRAReport(reportDate);
        return ResponseEntity.ok(report.getLiquidity());
    }

    @GetMapping("/sasra/sg5")
    @Operation(summary = "Get SG5 Capital Adequacy Report")
    public ResponseEntity<SASRAReport.SG5CapitalAdequacy> getSG5Report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        SASRAReport report = reportGenerationService.generateSASRAReport(reportDate);
        return ResponseEntity.ok(report.getCapitalAdequacy());
    }

    @GetMapping("/types")
    @Operation(summary = "Get all available report types")
    public ResponseEntity<ReportType[]> getReportTypes() {
        return ResponseEntity.ok(ReportType.values());
    }
}
