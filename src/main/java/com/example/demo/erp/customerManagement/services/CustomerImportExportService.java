package com.example.demo.erp.customerManagement.services;

import com.example.demo.erp.customerManagement.dto.ImportResultDto;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepo;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing and exporting customers via Excel/CSV
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerImportExportService {

    private final CustomerRepo customerRepo;

    /**
     * Import customers from Excel or CSV file
     */
    @Transactional
    public ImportResultDto importCustomers(MultipartFile file, String importedBy) {
        String fileName = file.getOriginalFilename();
        ImportResultDto result = ImportResultDto.builder()
                .fileName(fileName)
                .importedBy(importedBy)
                .timestamp(LocalDateTime.now().toString())
                .build();

        try {
            List<Customer> customers;
            
            if (fileName.endsWith(".csv")) {
                customers = parseCSV(file, result);
            } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                customers = parseExcel(file, result);
            } else {
                result.getErrors().add("Unsupported file format. Please upload .xlsx, .xls, or .csv file");
                result.setFailed(1);
                return result;
            }

            // Save customers in batches
            int batchSize = 50;
            int successful = 0;
            int failed = 0;

            for (int i = 0; i < customers.size(); i++) {
                try {
                    Customer customer = customers.get(i);
                    validateCustomer(customer, result, i + 2); // +2 because row 1 is header, array is 0-indexed
                    
                    if (result.getErrors().size() > failed) {
                        failed++;
                        continue;
                    }

                    customerRepo.save(customer);
                    successful++;

                    // Flush every batch
                    if (i % batchSize == 0 && i > 0) {
                        log.info("Imported {} customers", i);
                    }
                } catch (Exception e) {
                    failed++;
                    result.getErrors().add("Row " + (i + 2) + ": " + e.getMessage());
                }
            }

            result.setSuccessful(successful);
            result.setFailed(failed);
            result.setTotalRecords(customers.size());
            
            log.info("Import completed: {} successful, {} failed", successful, failed);

        } catch (Exception e) {
            log.error("Error importing customers", e);
            result.getErrors().add("File processing error: " + e.getMessage());
            result.setFailed(result.getFailed() + 1);
        }

        return result;
    }

    /**
     * Parse Excel file
     */
    private List<Customer> parseExcel(MultipartFile file, ImportResultDto result) throws IOException {
        List<Customer> customers = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skip header row
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    Customer customer = parseExcelRow(row, i + 1);
                    if (customer != null) {
                        customers.add(customer);
                    }
                } catch (Exception e) {
                    result.getErrors().add("Row " + (i + 1) + ": " + e.getMessage());
                    result.setFailed(result.getFailed() + 1);
                }
            }
        }
        
        return customers;
    }

    /**
     * Parse CSV file
     */
    private List<Customer> parseCSV(MultipartFile file, ImportResultDto result) throws IOException {
        List<Customer> customers = new ArrayList<>();
        
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReader(reader)) {
            
            String[] nextLine;
            int rowNum = 0;
            
            while ((nextLine = csvReader.readNext()) != null) {
                rowNum++;
                if (rowNum == 1) continue; // Skip header
                
                if (isLineEmpty(nextLine)) continue;

                try {
                    Customer customer = parseCSVRow(nextLine, rowNum);
                    if (customer != null) {
                        customers.add(customer);
                    }
                } catch (Exception e) {
                    result.getErrors().add("Row " + rowNum + ": " + e.getMessage());
                    result.setFailed(result.getFailed() + 1);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        return customers;
    }

    /**
     * Parse individual Excel row
     */
    private Customer parseExcelRow(Row row, int rowNum) {
        Customer customer = new Customer();
        
        customer.setFirstName(getCellValue(row, 0));
        customer.setLastName(getCellValue(row, 1));
        customer.setDocumentNumber(getCellValue(row, 2));
        customer.setPhoneNumber(getCellValue(row, 3));
        customer.setEmail(getCellValue(row, 4));
        
        // Parse DOB
        String dobStr = getCellValue(row, 5);
        if (dobStr != null && !dobStr.trim().isEmpty()) {
            customer.setDob(parseDate(dobStr));
        }
        
        customer.setAddress(getCellValue(row, 7));
        customer.setOccupation(getCellValue(row, 9));
        
        // Next of Kin
        customer.setNextOfKin(getCellValue(row, 11));
        customer.setNextOfKinPhone(getCellValue(row, 12));
        customer.setNextOfKinRelationship(getCellValue(row, 13));
        
        // Set defaults
        customer.setAccountStatusFlag(true);
        customer.setStatus("PENDING_VERIFICATION");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setAccountBalance(0.0f);
        
        return customer;
    }

    /**
     * Parse individual CSV row
     */
    private Customer parseCSVRow(String[] data, int rowNum) {
        Customer customer = new Customer();
        
        customer.setFirstName(data.length > 0 ? data[0].trim() : null);
        customer.setLastName(data.length > 1 ? data[1].trim() : null);
        customer.setDocumentNumber(data.length > 2 ? data[2].trim() : null);
        customer.setPhoneNumber(data.length > 3 ? data[3].trim() : null);
        customer.setEmail(data.length > 4 ? data[4].trim() : null);
        
        // Parse DOB
        if (data.length > 5 && !data[5].trim().isEmpty()) {
            customer.setDob(parseDate(data[5].trim()));
        }
        
        customer.setAddress(data.length > 7 ? data[7].trim() : null);
        customer.setOccupation(data.length > 9 ? data[9].trim() : null);
        
        // Next of Kin
        customer.setNextOfKin(data.length > 11 ? data[11].trim() : null);
        customer.setNextOfKinPhone(data.length > 12 ? data[12].trim() : null);
        customer.setNextOfKinRelationship(data.length > 13 ? data[13].trim() : null);
        
        // Set defaults
        customer.setAccountStatusFlag(true);
        customer.setStatus("PENDING_VERIFICATION");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setAccountBalance(0.0f);
        
        return customer;
    }

    /**
     * Validate customer data
     */
    private void validateCustomer(Customer customer, ImportResultDto result, int rowNum) {
        List<String> errors = new ArrayList<>();

        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            errors.add("First name is required");
        }
        
        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            errors.add("Last name is required");
        }
        
        if (customer.getDocumentNumber() == null || customer.getDocumentNumber().trim().isEmpty()) {
            errors.add("ID number is required");
        } else if (customerRepo.existsByDocumentNumber(customer.getDocumentNumber())) {
            errors.add("ID number already exists: " + customer.getDocumentNumber());
        }
        
        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            errors.add("Phone number is required");
        } else if (!customer.getPhoneNumber().matches("^254\\d{9}$")) {
            errors.add("Invalid phone number format. Expected format: 254XXXXXXXXX");
        } else if (customerRepo.existsByPhoneNumber(customer.getPhoneNumber())) {
            errors.add("Phone number already exists: " + customer.getPhoneNumber());
        }
        
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (!customer.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                errors.add("Invalid email format");
            } else if (customerRepo.existsByEmail(customer.getEmail())) {
                errors.add("Email already exists: " + customer.getEmail());
            }
        }

        if (!errors.isEmpty()) {
            result.getErrors().add("Row " + rowNum + ": " + String.join(", ", errors));
        }
    }

    /**
     * Export customers to Excel
     */
    public ByteArrayOutputStream exportCustomersToExcel() throws IOException {
        List<Customer> customers = customerRepo.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Members");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "ID", "First Name", "Last Name", "ID Number", "Phone Number", "Email",
                "Date of Birth", "Gender", "Address", "County", "Occupation",
                "Next of Kin Name", "Next of Kin Phone", "Next of Kin Relationship",
                "Account Balance", "Status", "Created Date"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                // Style header
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
            }
            
            // Add data rows
            int rowNum = 1;
            for (Customer customer : customers) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(customer.getId() != null ? customer.getId().toString() : "");
                row.createCell(1).setCellValue(customer.getFirstName() != null ? customer.getFirstName() : "");
                row.createCell(2).setCellValue(customer.getLastName() != null ? customer.getLastName() : "");
                row.createCell(3).setCellValue(customer.getDocumentNumber() != null ? customer.getDocumentNumber() : "");
                row.createCell(4).setCellValue(customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "");
                row.createCell(5).setCellValue(customer.getEmail() != null ? customer.getEmail() : "");
                row.createCell(6).setCellValue(customer.getDob() != null ? customer.getDob().toString() : "");
                row.createCell(7).setCellValue(""); // Gender - not in entity
                row.createCell(8).setCellValue(customer.getAddress() != null ? customer.getAddress() : "");
                row.createCell(9).setCellValue(""); // County - not in entity
                row.createCell(10).setCellValue(customer.getOccupation() != null ? customer.getOccupation() : "");
                row.createCell(11).setCellValue(customer.getNextOfKin() != null ? customer.getNextOfKin() : "");
                row.createCell(12).setCellValue(customer.getNextOfKinPhone() != null ? customer.getNextOfKinPhone() : "");
                row.createCell(13).setCellValue(customer.getNextOfKinRelationship() != null ? customer.getNextOfKinRelationship() : "");
                row.createCell(14).setCellValue(customer.getAccountBalance() != null ? customer.getAccountBalance().toString() : "0");
                row.createCell(15).setCellValue(customer.getStatus() != null ? customer.getStatus() : "");
                row.createCell(16).setCellValue(customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;
        }
    }

    // Utility methods
    
    private String getCellValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private boolean isLineEmpty(String[] line) {
        for (String field : line) {
            if (field != null && !field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private LocalDate parseDate(String dateStr) {
        try {
            // Try parsing as LocalDate (YYYY-MM-DD)
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            try {
                // Try parsing as different formats
                // Add more formats as needed
                return LocalDate.parse(dateStr);
            } catch (Exception ex) {
                log.warn("Could not parse date: {}", dateStr);
                return null;
            }
        }
    }
}
