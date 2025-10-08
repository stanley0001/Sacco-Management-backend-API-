package com.example.demo.reports.services;

import com.example.demo.reports.models.LoanPortfolioReport;
import com.example.demo.reports.models.SASRAReport;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ExcelExportService {

    public byte[] exportLoanPortfolioToExcel(LoanPortfolioReport report, LocalDate startDate, LocalDate endDate) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        
        // Summary Sheet
        Sheet summarySheet = workbook.createSheet("Summary");
        createLoanPortfolioSummary(summarySheet, report, startDate, endDate, workbook);
        
        // Product Breakdown Sheet
        Sheet productSheet = workbook.createSheet("Product Breakdown");
        createProductBreakdown(productSheet, report, workbook);
        
        // Aging Analysis Sheet
        Sheet agingSheet = workbook.createSheet("Aging Analysis");
        createAgingAnalysis(agingSheet, report, workbook);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    public byte[] exportSASRAReportToExcel(SASRAReport report) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        
        // SG3 - Loan Classification
        Sheet sg3Sheet = workbook.createSheet("SG3-Loan Classification");
        createSG3Sheet(sg3Sheet, report.getLoanClassification(), workbook);
        
        // SG4 - Liquidity
        Sheet sg4Sheet = workbook.createSheet("SG4-Liquidity");
        createSG4Sheet(sg4Sheet, report.getLiquidity(), workbook);
        
        // SG5 - Capital Adequacy
        Sheet sg5Sheet = workbook.createSheet("SG5-Capital Adequacy");
        createSG5Sheet(sg5Sheet, report.getCapitalAdequacy(), workbook);
        
        // Prudential Returns
        Sheet prudentialSheet = workbook.createSheet("Prudential Returns");
        createPrudentialReturnsSheet(prudentialSheet, report.getPrudentialReturns(), workbook);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    private void createLoanPortfolioSummary(Sheet sheet, LoanPortfolioReport report, 
                                           LocalDate startDate, LocalDate endDate, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("LOAN PORTFOLIO REPORT");
        titleCell.setCellStyle(headerStyle);
        
        // Date Range
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Report Period:");
        dateRow.createCell(1).setCellValue(startDate.format(DateTimeFormatter.ISO_DATE) + 
                                          " to " + endDate.format(DateTimeFormatter.ISO_DATE));
        
        rowNum++; // Empty row
        
        // Summary Data
        createDataRow(sheet, rowNum++, "Total Loans Outstanding", report.getTotalLoansOutstanding(), currencyStyle);
        createDataRow(sheet, rowNum++, "Total Principal Disbursed", report.getTotalPrincipalDisbursed(), currencyStyle);
        createDataRow(sheet, rowNum++, "Total Interest Earned", report.getTotalInterestEarned(), currencyStyle);
        createDataRow(sheet, rowNum++, "Total Arrears Amount", report.getTotalArrearsAmount(), currencyStyle);
        
        rowNum++; // Empty row
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total Loan Accounts: " + report.getTotalLoanAccounts());
        sheet.createRow(rowNum++).createCell(0).setCellValue("Active Loans: " + report.getActiveLoans());
        sheet.createRow(rowNum++).createCell(0).setCellValue("Completed Loans: " + report.getCompletedLoans());
        sheet.createRow(rowNum++).createCell(0).setCellValue("Defaulted Loans: " + report.getDefaultedLoans());
        
        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createProductBreakdown(Sheet sheet, LoanPortfolioReport report, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Product Name", "Product Code", "Number of Loans", "Total Outstanding", "Total Disbursed"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        for (LoanPortfolioReport.ProductBreakdown product : report.getProductBreakdown()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getProductName());
            row.createCell(1).setCellValue(product.getProductCode());
            row.createCell(2).setCellValue(product.getNumberOfLoans());
            
            Cell outstandingCell = row.createCell(3);
            outstandingCell.setCellValue(product.getTotalOutstanding().doubleValue());
            outstandingCell.setCellStyle(currencyStyle);
            
            Cell disbursedCell = row.createCell(4);
            disbursedCell.setCellValue(product.getTotalDisbursed().doubleValue());
            disbursedCell.setCellStyle(currencyStyle);
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createAgingAnalysis(Sheet sheet, LoanPortfolioReport report, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);
        
        int rowNum = 0;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Aging Bucket", "Number of Loans", "Amount", "Percentage"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        for (LoanPortfolioReport.LoanAging aging : report.getAgingAnalysis()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(aging.getAgingBucket());
            row.createCell(1).setCellValue(aging.getNumberOfLoans());
            
            Cell amountCell = row.createCell(2);
            amountCell.setCellValue(aging.getAmount().doubleValue());
            amountCell.setCellStyle(currencyStyle);
            
            Cell percentCell = row.createCell(3);
            percentCell.setCellValue(aging.getPercentage().doubleValue() / 100);
            percentCell.setCellStyle(percentStyle);
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSG3Sheet(Sheet sheet, SASRAReport.SG3LoanClassification classification, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SASRA SG3 - LOAN CLASSIFICATION");
        titleCell.setCellStyle(headerStyle);
        
        rowNum++; // Empty row
        
        createDataRow(sheet, rowNum++, "Normal Loans", classification.getNormalLoans(), currencyStyle);
        createDataRow(sheet, rowNum++, "Watch Loans", classification.getWatchLoans(), currencyStyle);
        createDataRow(sheet, rowNum++, "Sub-Standard Loans", classification.getSubStandardLoans(), currencyStyle);
        createDataRow(sheet, rowNum++, "Doubtful Loans", classification.getDoubtfulLoans(), currencyStyle);
        createDataRow(sheet, rowNum++, "Loss Loans", classification.getLossLoans(), currencyStyle);
        
        rowNum++; // Empty row
        
        createDataRow(sheet, rowNum++, "Total Gross Loans", classification.getTotalGrossLoans(), currencyStyle);
        createDataRow(sheet, rowNum++, "Total Provisions", classification.getTotalProvisions(), currencyStyle);
        createDataRow(sheet, rowNum++, "Net Loans", classification.getNetLoans(), currencyStyle);
        
        rowNum++; // Empty row
        
        Row nplRow = sheet.createRow(rowNum++);
        nplRow.createCell(0).setCellValue("NPL Ratio:");
        nplRow.createCell(1).setCellValue(classification.getNplRatio().doubleValue() + "%");
        
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSG4Sheet(Sheet sheet, SASRAReport.SG4Liquidity liquidity, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SASRA SG4 - LIQUIDITY");
        titleCell.setCellStyle(headerStyle);
        
        rowNum++; // Empty row
        
        createDataRow(sheet, rowNum++, "Cash and Bank Balances", liquidity.getCashAndBankBalances(), currencyStyle);
        createDataRow(sheet, rowNum++, "Liquid Assets", liquidity.getLiquidAssets(), currencyStyle);
        createDataRow(sheet, rowNum++, "Current Liabilities", liquidity.getCurrentLiabilities(), currencyStyle);
        createDataRow(sheet, rowNum++, "Member Deposits", liquidity.getMemberDeposits(), currencyStyle);
        
        rowNum++; // Empty row
        
        Row liquidityRatioRow = sheet.createRow(rowNum++);
        liquidityRatioRow.createCell(0).setCellValue("Liquidity Ratio:");
        liquidityRatioRow.createCell(1).setCellValue(liquidity.getLiquidityRatio().doubleValue() + "%");
        
        Row ldrRow = sheet.createRow(rowNum++);
        ldrRow.createCell(0).setCellValue("Loan to Deposit Ratio:");
        ldrRow.createCell(1).setCellValue(liquidity.getLoanToDepositRatio().doubleValue() + "%");
        
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSG5Sheet(Sheet sheet, SASRAReport.SG5CapitalAdequacy capital, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SASRA SG5 - CAPITAL ADEQUACY");
        titleCell.setCellStyle(headerStyle);
        
        rowNum++; // Empty row
        
        createDataRow(sheet, rowNum++, "Core Capital", capital.getCoreCapital(), currencyStyle);
        createDataRow(sheet, rowNum++, "Total Capital", capital.getTotalCapital(), currencyStyle);
        createDataRow(sheet, rowNum++, "Risk Weighted Assets", capital.getRiskWeightedAssets(), currencyStyle);
        createDataRow(sheet, rowNum++, "Institutional Capital", capital.getInstitutionalCapital(), currencyStyle);
        
        rowNum++; // Empty row
        
        Row coreRatioRow = sheet.createRow(rowNum++);
        coreRatioRow.createCell(0).setCellValue("Core Capital Ratio:");
        coreRatioRow.createCell(1).setCellValue(capital.getCoreCapitalRatio().doubleValue() + "%");
        
        Row totalRatioRow = sheet.createRow(rowNum++);
        totalRatioRow.createCell(0).setCellValue("Total Capital Ratio:");
        totalRatioRow.createCell(1).setCellValue(capital.getTotalCapitalRatio().doubleValue() + "%");
        
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createPrudentialReturnsSheet(Sheet sheet, SASRAReport.PrudentialReturns returns, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("PRUDENTIAL RETURNS");
        titleCell.setCellStyle(headerStyle);
        
        rowNum++; // Empty row
        
        createDataRow(sheet, rowNum++, "Total Assets", returns.getTotalAssets(), currencyStyle);
        createDataRow(sheet, rowNum++, "Total Liabilities", returns.getTotalLiabilities(), currencyStyle);
        createDataRow(sheet, rowNum++, "Total Equity", returns.getTotalEquity(), currencyStyle);
        
        rowNum++; // Empty row
        
        createDataRow(sheet, rowNum++, "Total Income", returns.getTotalIncome(), currencyStyle);
        createDataRow(sheet, rowNum++, "Total Expenses", returns.getTotalExpenses(), currencyStyle);
        createDataRow(sheet, rowNum++, "Net Income", returns.getNetIncome(), currencyStyle);
        
        rowNum++; // Empty row
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total Members: " + returns.getTotalMembers());
        sheet.createRow(rowNum++).createCell(0).setCellValue("Active Members: " + returns.getActiveMembers());
        sheet.createRow(rowNum++).createCell(0).setCellValue("New Members This Period: " + returns.getNewMembersThisPeriod());
        
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDataRow(Sheet sheet, int rowNum, String label, java.math.BigDecimal value, CellStyle currencyStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value.doubleValue());
        valueCell.setCellStyle(currencyStyle);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }
}
