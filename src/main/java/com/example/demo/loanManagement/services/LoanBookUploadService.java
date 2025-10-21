package com.example.demo.loanManagement.services;

import com.example.demo.loanManagement.dto.LoanBookUploadDTO;
import com.example.demo.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.loanManagement.parsistence.entities.Products;
import com.example.demo.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.loanManagement.parsistence.repositories.ProductRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanBookUploadService {
    
    private final LoanBookValidationService validationService;
    private final LoanAccountRepo loanAccountRepo;
    private final ProductRepo productRepo;
    
    /**
     * Process uploaded loan book file
     */
    public UploadResult processUpload(MultipartFile file) throws IOException {
        log.info("Processing loan book upload: {}", file.getOriginalFilename());
        
        List<LoanBookUploadDTO> loans = parseExcelFile(file);
        log.info("Parsed {} loans from file", loans.size());
        
        // Validate all loans
        List<LoanBookUploadDTO> validatedLoans = validationService.validateLoans(loans);
        
        // Separate valid and invalid loans
        List<LoanBookUploadDTO> validLoans = new ArrayList<>();
        List<LoanBookUploadDTO> invalidLoans = new ArrayList<>();
        
        for (LoanBookUploadDTO loan : validatedLoans) {
            if (loan.getIsValid()) {
                validLoans.add(loan);
            } else {
                invalidLoans.add(loan);
            }
        }
        
        log.info("Validation complete: {} valid, {} invalid", validLoans.size(), invalidLoans.size());
        
        UploadResult result = new UploadResult();
        result.setTotalRows(loans.size());
        result.setValidRows(validLoans.size());
        result.setInvalidRows(invalidLoans.size());
        result.setValidLoans(validLoans);
        result.setInvalidLoans(invalidLoans);
        result.setFileName(file.getOriginalFilename());
        result.setUploadDate(LocalDateTime.now());
        
        return result;
    }
    
    /**
     * Import validated loans into the system
     */
    @Transactional
    public ImportResult importLoans(List<LoanBookUploadDTO> loans) {
        log.info("Importing {} loans", loans.size());
        
        ImportResult result = new ImportResult();
        List<LoanBookUploadDTO> successfulImports = new ArrayList<>();
        List<LoanBookUploadDTO> failedImports = new ArrayList<>();
        
        for (LoanBookUploadDTO loanDTO : loans) {
            try {
                LoanAccount loanAccount = createLoanAccount(loanDTO);
                LoanAccount saved = loanAccountRepo.save(loanAccount);
                
                loanDTO.setIsProcessed(true);
                loanDTO.setLoanAccountId(saved.getAccountId());
                loanDTO.setLoanReference(saved.getLoanref());
                successfulImports.add(loanDTO);
                
                log.debug("Imported loan: {}", saved.getLoanref());
            } catch (Exception e) {
                log.error("Error importing loan for customer {}: {}", loanDTO.getCustomerId(), e.getMessage());
                loanDTO.setIsProcessed(false);
                loanDTO.setErrorMessage("Import failed: " + e.getMessage());
                failedImports.add(loanDTO);
            }
        }
        
        result.setSuccessCount(successfulImports.size());
        result.setFailureCount(failedImports.size());
        result.setSuccessfulImports(successfulImports);
        result.setFailedImports(failedImports);
        result.setImportDate(LocalDateTime.now());
        
        log.info("Import complete: {} successful, {} failed", 
            successfulImports.size(), failedImports.size());
        
        return result;
    }
    
    /**
     * Parse Excel file and convert to DTOs
     */
    private List<LoanBookUploadDTO> parseExcelFile(MultipartFile file) throws IOException {
        List<LoanBookUploadDTO> loans = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("Loan Data");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0); // Use first sheet if "Loan Data" not found
            }
            
            // Skip header row
            int rowNum = 1;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                
                try {
                    LoanBookUploadDTO loan = parseRow(row, rowNum);
                    if (loan != null && isRowNotEmpty(row)) {
                        loans.add(loan);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing row {}: {}", rowNum, e.getMessage());
                    LoanBookUploadDTO errorLoan = new LoanBookUploadDTO();
                    errorLoan.setRowNumber(rowNum);
                    errorLoan.setIsValid(false);
                    errorLoan.setErrorMessage("Parse error: " + e.getMessage());
                    loans.add(errorLoan);
                }
                rowNum++;
            }
        }
        
        return loans;
    }
    
    /**
     * Parse a single row into DTO
     */
    private LoanBookUploadDTO parseRow(Row row, int rowNum) {
        LoanBookUploadDTO loan = new LoanBookUploadDTO();
        loan.setRowNumber(rowNum);
        
        int col = 0;
        loan.setCustomerId(getCellValueAsString(row.getCell(col++)));
        loan.setCustomerName(getCellValueAsString(row.getCell(col++)));
        loan.setPhoneNumber(getCellValueAsString(row.getCell(col++)));
        loan.setEmail(getCellValueAsString(row.getCell(col++)));
        loan.setProductCode(getCellValueAsString(row.getCell(col++)));
        loan.setProductName(getCellValueAsString(row.getCell(col++)));
        loan.setPrincipal(getCellValueAsDouble(row.getCell(col++)));
        loan.setInterestRate(getCellValueAsDouble(row.getCell(col++)));
        loan.setTerm(getCellValueAsInteger(row.getCell(col++)));
        loan.setDisbursementDate(getCellValueAsDate(row.getCell(col++)));
        loan.setStatus(getCellValueAsString(row.getCell(col++)));
        loan.setOutstandingBalance(getCellValueAsDouble(row.getCell(col++)));
        loan.setTotalPaid(getCellValueAsDouble(row.getCell(col++)));
        loan.setPaymentsMade(getCellValueAsInteger(row.getCell(col++)));
        loan.setLastPaymentDate(getCellValueAsDate(row.getCell(col++)));
        loan.setCollateralType(getCellValueAsString(row.getCell(col++)));
        loan.setCollateralValue(getCellValueAsString(row.getCell(col++)));
        loan.setGuarantorName(getCellValueAsString(row.getCell(col++)));
        loan.setGuarantorPhone(getCellValueAsString(row.getCell(col++)));
        loan.setLoanPurpose(getCellValueAsString(row.getCell(col++)));
        loan.setBranchCode(getCellValueAsString(row.getCell(col++)));
        loan.setLoanOfficer(getCellValueAsString(row.getCell(col++)));
        
        return loan;
    }
    
    /**
     * Create LoanAccount entity from DTO
     */
    private LoanAccount createLoanAccount(LoanBookUploadDTO dto) {
        LoanAccount loan = new LoanAccount();
        
        // Generate loan reference
        loan.setLoanref("LN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        // Set customer info (customerId is String in LoanAccount)
        loan.setCustomerId(dto.getCustomerId());
        
        // Verify product exists
        Products product = productRepo.getByCode(dto.getProductCode())
            .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductCode()));
        
        // Set loan details (matching actual LoanAccount fields)
        loan.setAmount(dto.getPrincipal().floatValue());
        loan.setPayableAmount(dto.getPrincipal().floatValue()); // Will be calculated with interest
        loan.setInstallments(dto.getTerm());
        loan.setStartDate(dto.getDisbursementDate().atStartOfDay());
        loan.setStatus(dto.getStatus());
        
        // Set current balance
        if (dto.getOutstandingBalance() != null) {
            loan.setAccountBalance(dto.getOutstandingBalance().floatValue());
        } else {
            loan.setAccountBalance(dto.getPrincipal().floatValue());
        }
        
        // Set other reference if available
        if (dto.getLoanPurpose() != null) {
            loan.setOtherRef("Purpose: " + dto.getLoanPurpose());
        }
        
        return loan;
    }
    
    /**
     * Helper methods to extract cell values
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
    
    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                return value.isEmpty() ? null : Double.parseDouble(value);
            }
        } catch (Exception e) {
            log.warn("Error parsing double from cell: {}", e.getMessage());
        }
        return null;
    }
    
    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                return value.isEmpty() ? null : Integer.parseInt(value);
            }
        } catch (Exception e) {
            log.warn("Error parsing integer from cell: {}", e.getMessage());
        }
        return null;
    }
    
    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                return value.isEmpty() ? null : LocalDate.parse(value);
            }
        } catch (Exception e) {
            log.warn("Error parsing date from cell: {}", e.getMessage());
        }
        return null;
    }
    
    private boolean isRowNotEmpty(Row row) {
        if (row == null) return false;
        
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * DTO for upload result
     */
    @lombok.Data
    public static class UploadResult {
        private String fileName;
        private LocalDateTime uploadDate;
        private Integer totalRows;
        private Integer validRows;
        private Integer invalidRows;
        private List<LoanBookUploadDTO> validLoans;
        private List<LoanBookUploadDTO> invalidLoans;
    }
    
    /**
     * DTO for import result
     */
    @lombok.Data
    public static class ImportResult {
        private LocalDateTime importDate;
        private Integer successCount;
        private Integer failureCount;
        private List<LoanBookUploadDTO> successfulImports;
        private List<LoanBookUploadDTO> failedImports;
    }
}
