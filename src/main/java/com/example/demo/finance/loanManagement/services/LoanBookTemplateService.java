package com.example.demo.finance.loanManagement.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanBookTemplateService {
    
    /**
     * Generate Excel template for loan book upload
     */
    public byte[] generateTemplate() throws IOException {
        log.info("Generating loan book upload template");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create Instructions Sheet
            Sheet instructionsSheet = workbook.createSheet("Instructions");
            createInstructionsSheet(instructionsSheet, workbook);
            
            // Create Template Sheet
            Sheet templateSheet = workbook.createSheet("Loan Data");
            createTemplateSheet(templateSheet, workbook);
            
            // Create Sample Data Sheet
            Sheet sampleSheet = workbook.createSheet("Sample Data");
            createSampleDataSheet(sampleSheet, workbook);
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            log.info("Template generated successfully");
            return outputStream.toByteArray();
        }
    }
    
    /**
     * Create instructions sheet
     */
    private void createInstructionsSheet(Sheet sheet, Workbook workbook) {
        // Create title style
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SACCO Loan Book Upload Template");
        titleCell.setCellStyle(titleStyle);
        rowNum++;
        
        // Instructions
        createInstructionRow(sheet, rowNum++, "Instructions:", headerStyle);
        createInstructionRow(sheet, rowNum++, "1. Fill in all REQUIRED fields marked with (*) in the 'Loan Data' sheet");
        createInstructionRow(sheet, rowNum++, "2. Use the 'Sample Data' sheet as a reference");
        createInstructionRow(sheet, rowNum++, "3. Do NOT modify the column headers");
        createInstructionRow(sheet, rowNum++, "4. Customer ID must exist in the system");
        createInstructionRow(sheet, rowNum++, "5. Product Code must match existing loan products");
        createInstructionRow(sheet, rowNum++, "6. All amounts should be in KES (Kenyan Shillings)");
        createInstructionRow(sheet, rowNum++, "7. Dates should be in format: YYYY-MM-DD (e.g., 2024-01-15)");
        createInstructionRow(sheet, rowNum++, "8. Status must be one of: ACTIVE, CLOSED, DEFAULTED, WRITTEN_OFF");
        createInstructionRow(sheet, rowNum++, "9. After filling, save and upload the file");
        rowNum++;
        
        // Field Descriptions
        createInstructionRow(sheet, rowNum++, "Field Descriptions:", headerStyle);
        createInstructionRow(sheet, rowNum++, "• Customer ID (*): Existing customer reference number");
        createInstructionRow(sheet, rowNum++, "• Customer Name (*): Customer full name (for validation)");
        createInstructionRow(sheet, rowNum++, "• Phone Number (*): Customer M-PESA phone number");
        createInstructionRow(sheet, rowNum++, "• Product Code (*): Loan product code (e.g., PERS001)");
        createInstructionRow(sheet, rowNum++, "• Principal (*): Original loan amount");
        createInstructionRow(sheet, rowNum++, "• Interest Rate (*): Annual interest rate in percentage");
        createInstructionRow(sheet, rowNum++, "• Term (*): Loan duration in months");
        createInstructionRow(sheet, rowNum++, "• Disbursement Date (*): Date when loan was given");
        createInstructionRow(sheet, rowNum++, "• Status (*): Current loan status");
        createInstructionRow(sheet, rowNum++, "• Outstanding Balance: Current balance owed");
        createInstructionRow(sheet, rowNum++, "• Total Paid: Total amount paid so far");
        rowNum++;
        
        // Notes
        createInstructionRow(sheet, rowNum++, "Important Notes:", headerStyle);
        createInstructionRow(sheet, rowNum++, "• The system will validate all data before importing");
        createInstructionRow(sheet, rowNum++, "• Errors will be reported for each row");
        createInstructionRow(sheet, rowNum++, "• You can fix errors and re-upload");
        createInstructionRow(sheet, rowNum++, "• Successfully imported loans will generate loan accounts");
        createInstructionRow(sheet, rowNum++, "• Repayment schedules will be auto-generated");
        
        // Auto-size columns
        sheet.setColumnWidth(0, 15000);
    }
    
    /**
     * Create template sheet with headers
     */
    private void createTemplateSheet(Sheet sheet, Workbook workbook) {
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Customer ID *", "Customer Name *", "Phone Number *", "Email",
            "Product Code *", "Product Name", "Principal *", "Interest Rate *",
            "Term (Months) *", "Disbursement Date *", "Status *",
            "Outstanding Balance", "Total Paid", "Payments Made", "Last Payment Date",
            "Collateral Type", "Collateral Value", "Guarantor Name", "Guarantor Phone",
            "Loan Purpose", "Branch Code", "Loan Officer"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4000);
        }
        
        // Freeze header row
        sheet.createFreezePane(0, 1);
    }
    
    /**
     * Create sample data sheet
     */
    private void createSampleDataSheet(Sheet sheet, Workbook workbook) {
        // Copy headers from template
        createTemplateSheet(sheet, workbook);
        
        // Add sample data
        Object[][] sampleData = {
            {"C001", "John Doe", "254712345678", "john@example.com", 
             "PERS001", "Personal Loan", 100000.0, 12.0, 
             12, LocalDate.now().minusMonths(6).toString(), "ACTIVE",
             75000.0, 25000.0, 6, LocalDate.now().minusDays(30).toString(),
             "Title Deed", "200000", "Jane Doe", "254723456789",
             "Business Expansion", "BR001", "LO001"},
            
            {"C002", "Mary Smith", "254798765432", "mary@example.com",
             "BUS001", "Business Loan", 500000.0, 15.0,
             24, LocalDate.now().minusMonths(12).toString(), "ACTIVE",
             300000.0, 200000.0, 12, LocalDate.now().minusDays(15).toString(),
             "Vehicle Logbook", "800000", "Peter Smith", "254787654321",
             "Working Capital", "BR002", "LO002"},
            
            {"C003", "James Wilson", "254776543210", "james@example.com",
             "EMRG001", "Emergency Loan", 50000.0, 5.0,
             6, LocalDate.now().minusMonths(3).toString(), "CLOSED",
             0.0, 50000.0, 6, LocalDate.now().minusDays(5).toString(),
             "", "", "", "",
             "Medical Emergency", "BR001", "LO001"}
        };
        
        for (int i = 0; i < sampleData.length; i++) {
            Row row = sheet.createRow(i + 1);
            Object[] rowData = sampleData[i];
            for (int j = 0; j < rowData.length; j++) {
                Cell cell = row.createCell(j);
                Object value = rowData[j];
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else {
                    cell.setCellValue(value != null ? value.toString() : "");
                }
            }
        }
    }
    
    /**
     * Helper method to create instruction rows
     */
    private void createInstructionRow(Sheet sheet, int rowNum, String text) {
        createInstructionRow(sheet, rowNum, text, null);
    }
    
    private void createInstructionRow(Sheet sheet, int rowNum, String text, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
}
