package com.example.demo.system.controllers;

import com.example.demo.system.parsitence.models.SearchBody;
import com.example.demo.system.parsitence.models.SearchReportResponse;
import com.example.demo.system.services.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class LoanReports {

   public final ReportService reportService;

    public LoanReports(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/getLoanAccountByStatus")
    public ResponseEntity<SearchReportResponse> getAccountByStatus(String status){
        SearchReportResponse response=reportService.getLoanReportByStatus(status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/getLoanAccountByBody")
    public ResponseEntity<SearchReportResponse> getAccountByBody(@RequestBody SearchBody body){
        SearchReportResponse response=reportService.getLoanReportByDateAndStatus(body);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/getLoanAccountByDateRange")
    public ResponseEntity<SearchReportResponse> getAccountByDateRange(String status){
        SearchReportResponse response=reportService.getLoanReportByStatus(status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
