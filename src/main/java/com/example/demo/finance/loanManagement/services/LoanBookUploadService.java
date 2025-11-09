package com.example.demo.finance.loanManagement.services;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.repositories.ProductRepo;
import com.example.demo.finance.loanManagement.dto.LoanBookUploadDTO;
import com.example.demo.finance.loanManagement.parsistence.entities.*;
import com.example.demo.finance.loanManagement.parsistence.repositories.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanBookUploadService {
    
    private final LoanBookValidationService validationService;
    private final LoanAccountRepo loanAccountRepo;
    private final ProductRepo productRepo;
    private final LoanCalculatorService loanCalculatorService;
    private final LoanRepaymentScheduleRepository repaymentScheduleRepo;
    private final ApplicationRepo applicationRepo;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepo subscriptionRepo;
    
    // New centralized services
    private final LoanApplicationOrchestrator applicationOrchestrator;
    private final LoanBookingService bookingService;
    private final RepaymentScheduleEngine scheduleEngine;
    
    /**
     * Process uploaded loan book file (CSV or Excel)
     */
    public UploadResult processUpload(MultipartFile file) throws IOException {
        log.info("Processing loan book upload: {}", file.getOriginalFilename());

        List<LoanBookUploadDTO> loans;

        // Detect file type and parse accordingly
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".csv")) {
            loans = parseCsvFile(file);
            log.info("Parsed {} loans from CSV file", loans.size());
        } else {
            loans = parseExcelFile(file);
            log.info("Parsed {} loans from Excel file", loans.size());
        }

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
     * Import validated loans into the system with repayment schedules
     * Each loan is imported in its own transaction to prevent one failure from affecting others
     */
    public ImportResult importLoans(List<LoanBookUploadDTO> loans) {
        log.info("Importing {} loans", loans.size());
        
        ImportResult result = new ImportResult();
        List<LoanBookUploadDTO> successfulImports = new ArrayList<>();
        List<LoanBookUploadDTO> failedImports = new ArrayList<>();
        
        for (LoanBookUploadDTO loanDTO : loans) {
            try {
                // Import each loan in its own transaction
                importSingleLoan(loanDTO);
                
                loanDTO.setIsProcessed(true);
                successfulImports.add(loanDTO);
                
                log.debug("Imported loan: {}", loanDTO.getLoanReference());
            } catch (Exception e) {
                log.error("Error importing loan for customer {}: {}", loanDTO.getCustomerId(), e.getMessage(), e);
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
     * Import a single loan in its own transaction
     * This prevents one loan failure from rolling back the entire batch
     * Now uses centralized orchestrator and booking services
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importSingleLoan(LoanBookUploadDTO loanDTO) {
        log.info("Importing loan from upload: customer={}, product={}, amount={}", 
            loanDTO.getCustomerId(), loanDTO.getProductCode(), loanDTO.getPrincipal());
        
        // Check for duplicate loan by loan ID (which will be used as loanref or other_ref)
        if (loanDTO.getLoanId() != null && !loanDTO.getLoanId().trim().isEmpty()) {
            String loanId = loanDTO.getLoanId().trim();
            
            // Check if this loan ID already exists as loanref
            Optional<LoanAccount> existingByLoanRef = loanAccountRepo.findByLoanref(loanId);
            if (existingByLoanRef.isPresent()) {
                throw new RuntimeException("Loan with ID '" + loanId + "' already exists in the system (loan reference)");
            }
            
            // Also check if it exists as other_ref
            Optional<LoanAccount> existingByOtherRef = loanAccountRepo.findByOtherRef(loanId);
            if (existingByOtherRef.isPresent()) {
                throw new RuntimeException("Loan with ID '" + loanId + "' already exists in the system (other reference)");
            }
        }
        
        // 1. Create loan application using centralized orchestrator
        com.example.demo.finance.loanManagement.dto.LoanApplicationCommand command = buildApplicationCommand(loanDTO);
        com.example.demo.finance.loanManagement.dto.LoanApplicationResponse appResponse = applicationOrchestrator.createApplication(command);
        
        log.info("Loan application created from upload: ID={}, Status={}", 
            appResponse.getApplicationId(), appResponse.getApplicationStatus());
        
        // 2. Create subscription for the customer with product
        createSubscriptionForLoan(loanDTO, null); // Pass null for now, will enhance later
        
        // 3. Book the loan using centralized booking service
        com.example.demo.finance.loanManagement.dto.LoanBookingCommand bookingCommand = buildBookingCommand(appResponse, loanDTO);
        LoanAccount loanAccount = bookingService.bookLoan(bookingCommand);
        
        log.info("Loan booked successfully from upload: AccountID={}, Reference={}", 
            loanAccount.getAccountId(), loanAccount.getLoanref());
        
        // Update DTO with saved loan info
        loanDTO.setLoanAccountId(loanAccount.getAccountId());
        loanDTO.setLoanReference(loanAccount.getLoanref());
    }
    
    /**
     * Build application command from upload DTO
     */
    private com.example.demo.finance.loanManagement.dto.LoanApplicationCommand buildApplicationCommand(LoanBookUploadDTO dto) {
        return com.example.demo.finance.loanManagement.dto.LoanApplicationCommand.builder()
            .source(com.example.demo.finance.loanManagement.dto.LoanApplicationCommand.ApplicationSource.UPLOAD)
            .sourceReference(dto.getLoanId())
            .customerExternalId(dto.getCustomerId())
            .customerIdNumber(dto.getCustomerId())
            .customerMobileNumber(dto.getPhoneNumber())
            .productCode(dto.getProductCode())
            .loanAmount(java.math.BigDecimal.valueOf(dto.getPrincipal()))
            .term(dto.getTerm())
            .interestRate(dto.getInterestRate())
            .disbursementType("UPLOADED")
            .destinationAccount(dto.getCustomerId())
            .disbursementDate(dto.getDisbursementDate())
            .isUpload(true)
            .uploadStatus(dto.getStatus())
            .outstandingBalance(dto.getOutstandingBalance())
            .totalPaid(dto.getTotalPaid())
            .paymentsMade(dto.getPaymentsMade())
            .lastPaymentDate(dto.getLastPaymentDate())
            .loanPurpose(dto.getLoanPurpose())
            .guarantorName(dto.getGuarantorName())
            .guarantorPhone(dto.getGuarantorPhone())
            .collateralType(dto.getCollateralType())
            .collateralValue(dto.getCollateralValue())
            .branchCode(dto.getBranchCode())
            .loanOfficer(dto.getLoanOfficer())
            .requestedBy("SYSTEM_UPLOAD")
            .build();
    }
    
    /**
     * Build booking command from application response and upload DTO
     */
    private com.example.demo.finance.loanManagement.dto.LoanBookingCommand buildBookingCommand(
            com.example.demo.finance.loanManagement.dto.LoanApplicationResponse appResponse,
            LoanBookUploadDTO dto) {
        return com.example.demo.finance.loanManagement.dto.LoanBookingCommand.builder()
            .applicationId(appResponse.getApplicationId())
            .disbursementMethod("UPLOAD")
            .disbursementReference("UPLOAD-" + dto.getLoanId())
            .disbursedBy("SYSTEM_UPLOAD")
            .disbursementDate(dto.getDisbursementDate())
            .skipDisbursement(true) // Upload loans are already disbursed
            .postToAccounting(true) // Still post to accounting for record keeping
            .build();
    }
    
    /**
     * Create subscription for customer with product when uploading loan
     */
    private void createSubscriptionForLoan(LoanBookUploadDTO dto, LoanAccount loanAccount) {
        try {
            // Get customer entity
            Customer customer = getOrCreateCustomer(dto);
            
            // Get product to fetch limits
            Products product = productRepo.getByCode(dto.getProductCode())
                .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductCode()));
            
            // Check if subscription already exists for this customer and product
            Optional<Subscriptions> existingSubscription = subscriptionRepo
                .findByCustomerIdAndProductCode(String.valueOf(customer.getId()), dto.getProductCode());
            
            if (existingSubscription.isEmpty()) {
                // Create new subscription
                Subscriptions subscription = new Subscriptions();
                subscription.setCustomerId(String.valueOf(customer.getId()));
                subscription.setCustomerPhoneNumber(customer.getPhoneNumber());
                subscription.setCustomerDocumentNumber(customer.getDocumentNumber());
                subscription.setProductCode(dto.getProductCode());
                
                // Set credit limit to the loan amount (principal)
                subscription.setCreditLimit(dto.getPrincipal().intValue());
                subscription.setCreditLimitOverridden(true); // Mark as manually set from upload
                subscription.setCreditLimitCalculationRule("LOAN_UPLOAD");
                
                // Set product terms
                subscription.setTerm(dto.getTerm());
                subscription.setInterestRate(dto.getInterestRate().intValue());
                subscription.setTimeSpan(product.getTimeSpan() != null ? product.getTimeSpan() : "MONTHS");
                
                // Set status and dates
                subscription.setStatus(true); // Active subscription
                subscription.setCreditStatusDate(dto.getDisbursementDate());
                subscription.setCreatedAt(LocalDate.now());
                subscription.setUpdatedAt(LocalDate.now());
                
                subscriptionRepo.save(subscription);
                log.info("Created subscription for customer {} with product {} and credit limit {}", 
                    customer.getId(), dto.getProductCode(), dto.getPrincipal());
            } else {
                log.info("Subscription already exists for customer {} with product {}", 
                    customer.getId(), dto.getProductCode());
            }
        } catch (Exception e) {
            log.error("Error creating subscription for loan upload: {}", e.getMessage());
            // Don't fail the entire loan upload if subscription creation fails
        }
    }
    
    /**
     * Generate repayment schedules for imported loan with backdating
     */
    private void generateRepaymentSchedules(LoanAccount loanAccount, LoanBookUploadDTO dto) {
        log.debug("Generating repayment schedules for loan: {}", loanAccount.getLoanref());
        
        // Get product for interest strategy (use uploaded data for actual values)
        Products product = productRepo.getByCode(dto.getProductCode())
            .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductCode()));
        
        // Get product's interest strategy
        InterestStrategy strategy = product.getInterestStrategy() != null ? 
            product.getInterestStrategy() : InterestStrategy.REDUCING_BALANCE;
        
        // Create a temporary product with uploaded data for calculation
        Products calcProduct = new Products();
        calcProduct.setInterest(dto.getInterestRate().intValue());
        calcProduct.setTerm(dto.getTerm());
        calcProduct.setTimeSpan("MONTHS");
        calcProduct.setInterestType(product.getInterestType() != null ? product.getInterestType() : com.example.demo.finance.loanManagement.parsistence.entities.InterestType.PER_MONTH);
        calcProduct.setInterestStrategy(strategy);
        
        // Calculate loan schedule using uploaded values
        LoanCalculatorService.LoanCalculation calculation = 
            loanCalculatorService.calculateLoan(dto.getPrincipal(), calcProduct, strategy);
        
        List<LoanCalculatorService.RepaymentScheduleItem> scheduleItems = calculation.getSchedule();
        
        // Calculate how much has been paid and how to distribute it
        double totalPaidAmount = dto.getTotalPaid() != null ? dto.getTotalPaid() : 0.0;
        double remainingPaidAmount = totalPaidAmount;
        
        // Generate schedule entries starting from disbursement date
        for (int i = 0; i < scheduleItems.size(); i++) {
            LoanCalculatorService.RepaymentScheduleItem item = scheduleItems.get(i);
            
            // Calculate due date for this installment (backdated)
            LocalDate dueDate = dto.getDisbursementDate().plusMonths(i + 1);
            
            // Determine payment status and amount paid for this installment
            // Status values: CURRENT=0, PAID=1, DEFAULT=2, REVERSED=3, OVERDUE=4, PENDING=5
            int statusValue;
            double amountPaid;
            double installmentAmount = item.getTotalPayment();
            
            if (remainingPaidAmount >= installmentAmount) {
                // Fully paid installment
                statusValue = 1;  // PAID
                amountPaid = installmentAmount;
                remainingPaidAmount -= installmentAmount;
            } else if (remainingPaidAmount > 0) {
                // Partially paid installment
                statusValue = 0;  // CURRENT (partially paid)
                amountPaid = remainingPaidAmount;
                remainingPaidAmount = 0;
            } else {
                // Unpaid installment
                if (dueDate.isBefore(LocalDate.now())) {
                    statusValue = 4;  // OVERDUE
                } else {
                    statusValue = 5;  // PENDING
                }
                amountPaid = 0.0;
            }
            
            // Calculate remaining balance for this installment
            double installmentBalance = installmentAmount - amountPaid;
            
            // Create and save repayment schedule entity
            LoanRepaymentSchedule schedule = new LoanRepaymentSchedule();
            schedule.setLoanAccountId(loanAccount.getAccountId());
            schedule.setInstallmentNumber(item.getInstallmentNumber());
            schedule.setDueDate(dueDate);
            
            // Set amounts as BigDecimal
            schedule.setPrincipalAmount(BigDecimal.valueOf(item.getPrincipalAmount()));
            schedule.setInterestAmount(BigDecimal.valueOf(item.getInterestAmount()));
            schedule.setTotalAmount(BigDecimal.valueOf(installmentAmount));
            
            // Set paid amounts
            schedule.setTotalPaid(BigDecimal.valueOf(amountPaid));
            schedule.setPaidPrincipal(BigDecimal.valueOf(Math.min(amountPaid, item.getPrincipalAmount())));
            schedule.setPaidInterest(BigDecimal.valueOf(Math.max(0, amountPaid - schedule.getPaidPrincipal().doubleValue())));
            
            // Set outstanding amounts
            schedule.setOutstandingPrincipal(BigDecimal.valueOf(item.getPrincipalAmount()).subtract(schedule.getPaidPrincipal()));
            schedule.setOutstandingInterest(BigDecimal.valueOf(item.getInterestAmount()).subtract(schedule.getPaidInterest()));
            schedule.setTotalOutstanding(BigDecimal.valueOf(installmentBalance));
            
            // Set balance tracking
            schedule.setOpeningBalance(BigDecimal.valueOf(item.getBalanceAfterPayment()));
            schedule.setClosingBalance(BigDecimal.valueOf(item.getBalanceAfterPayment() - item.getPrincipalAmount()));
            schedule.setBalanceAfterPayment(BigDecimal.valueOf(Math.max(0, item.getBalanceAfterPayment() - amountPaid)));
            
            // Set status based on payment
            if (statusValue == 1) {
                schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.PAID);
                schedule.setPaidDate(dto.getLastPaymentDate() != null ? dto.getLastPaymentDate() : dueDate);
            } else if (statusValue == 0) {
                schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.PARTIAL);
            } else if (statusValue == 4) {
                schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.OVERDUE);
            } else {
                schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.PENDING);
            }
            
            // Set timestamps
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());
            schedule.setCreatedBy("SYSTEM_UPLOAD");
            
            repaymentScheduleRepo.save(schedule);
        }
        
        log.debug("Created {} repayment schedules for loan {}", 
            scheduleItems.size(), loanAccount.getLoanref());
    }
    
    
    /**
     * Parse Excel file and convert to DTOs using header-based mapping
     */
    private List<LoanBookUploadDTO> parseExcelFile(MultipartFile file) throws IOException {
        List<LoanBookUploadDTO> loans = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("Loan Data");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0); // Use first sheet if "Loan Data" not found
            }
            
            // Read header row to create column mapping
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IOException("No header row found in Excel file");
            }
            
            Map<String, Integer> columnMap = createColumnMapping(headerRow);
            
            // Parse data rows
            int rowNum = 1;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                
                try {
                    LoanBookUploadDTO loan = parseRowWithMapping(row, rowNum, columnMap);
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
     * Parse CSV file and convert to DTOs using header-based mapping
     */
    private List<LoanBookUploadDTO> parseCsvFile(MultipartFile file) throws IOException {
        List<LoanBookUploadDTO> loans = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream);
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();
            int actualRowNum = 1; // Track actual data row number
            Map<String, Integer> columnMap = null;

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                
                // Skip empty rows
                if (!isRowNotEmptyFromArray(row)) {
                    continue;
                }
                
                // Skip instruction rows (start with "===", "IMPORTANT", or "DELETE")
                String firstCell = row.length > 0 ? row[0].trim() : "";
                if (firstCell.startsWith("===") || firstCell.startsWith("IMPORTANT") || 
                    firstCell.startsWith("DELETE") || firstCell.startsWith("---")) {
                    continue;
                }
                
                // Check if this is the header row (contains "Customer" or "Loan")
                if (columnMap == null && (firstCell.toLowerCase().contains("customer") || 
                                         firstCell.toLowerCase().contains("loan") ||
                                         firstCell.toLowerCase().contains("id"))) {
                    columnMap = createColumnMappingFromArray(row);
                    log.info("CSV Header row found at index {}", i);
                    continue; // Skip header row
                }
                
                // Only parse data rows after header is found
                if (columnMap == null) {
                    continue; // Still looking for header
                }

                try {
                    LoanBookUploadDTO loan = parseCsvRowWithMapping(row, actualRowNum, columnMap);
                    if (loan != null && isRowNotEmptyFromArray(row)) {
                        loans.add(loan);
                        actualRowNum++;
                    }
                } catch (Exception e) {
                    log.warn("Error parsing CSV row {}: {}", actualRowNum, e.getMessage());
                    LoanBookUploadDTO errorLoan = new LoanBookUploadDTO();
                    errorLoan.setRowNumber(actualRowNum);
                    errorLoan.setIsValid(false);
                    errorLoan.setErrorMessage("Parse error: " + e.getMessage());
                    loans.add(errorLoan);
                    actualRowNum++;
                }
            }
        } catch (CsvException e) {
            throw new IOException("Error reading CSV file: " + e.getMessage(), e);
        }

        return loans;
    }
    
    /**
     * Create column mapping from CSV header array
     */
    private Map<String, Integer> createColumnMappingFromArray(String[] headers) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        log.info("=== Creating CSV Column Mapping ===");
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            if (header != null && !header.trim().isEmpty()) {
                String normalizedHeader = header.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
                columnMap.put(normalizedHeader, i);
                log.info("Column {}: '{}' -> normalized: '{}'", i, header, normalizedHeader);
            }
        }
        log.info("=== CSV Column Mapping Complete: {} columns mapped ===", columnMap.size());
        
        return columnMap;
    }
    
    /**
     * Parse CSV row with column mapping
     */
    private LoanBookUploadDTO parseCsvRowWithMapping(String[] row, int rowNum, Map<String, Integer> columnMap) {
        LoanBookUploadDTO loan = new LoanBookUploadDTO();
        loan.setRowNumber(rowNum);
        
        if (rowNum == 1) {
            log.info("=== Parsing First CSV Data Row ===");
            log.info("Available columns in map: {}", columnMap.keySet());
        }
        
        loan.setLoanId(getCellValueFromArray(row, columnMap, "loanid", "loannumber", "accountnumber"));
        loan.setCustomerId(getCellValueFromArray(row, columnMap, "customerid", "clientid", "memberid", "idnumber"));
        loan.setCustomerName(getCellValueFromArray(row, columnMap, "customername", "clientname", "name", "fullname"));
        loan.setPhoneNumber(parsePhoneNumberSafely(getCellValueFromArray(row, columnMap, "phonenumber", "phone", "mobile", "contact")));
        loan.setEmail(getCellValueFromArray(row, columnMap, "email", "emailaddress"));
        loan.setProductCode(getCellValueFromArray(row, columnMap, "productcode", "product", "loanproduct"));
        loan.setProductName(getCellValueFromArray(row, columnMap, "productname", "loantype"));
        loan.setPrincipal(parseDoubleSafely(getCellValueFromArray(row, columnMap, "principal", "principalamount", "loanamount", "amount")));
        loan.setInterestRate(parseDoubleSafely(getCellValueFromArray(row, columnMap, "interestrate", "interest", "rate")));
        loan.setTerm(parseIntegerSafely(getCellValueFromArray(row, columnMap, "term", "termmonths", "duration", "period", "loanterm", "months")));
        loan.setDisbursementDate(parseDateSafely(getCellValueFromArray(row, columnMap, "disbursementdate", "startdate", "loandate")));
        loan.setStatus(getCellValueFromArray(row, columnMap, "status", "loanstatus"));
        loan.setOutstandingBalance(parseDoubleSafely(getCellValueFromArray(row, columnMap, "outstandingbalance", "balance", "outstanding")));
        loan.setTotalPaid(parseDoubleSafely(getCellValueFromArray(row, columnMap, "totalpaid", "paid", "amountpaid")));
        loan.setPaymentsMade(parseIntegerSafely(getCellValueFromArray(row, columnMap, "paymentsmade", "installmentspaid")));
        loan.setLastPaymentDate(parseDateSafely(getCellValueFromArray(row, columnMap, "lastpaymentdate", "lastpayment")));
        loan.setCollateralType(getCellValueFromArray(row, columnMap, "collateraltype", "collateral"));
        loan.setCollateralValue(getCellValueFromArray(row, columnMap, "collateralvalue"));
        loan.setGuarantorName(getCellValueFromArray(row, columnMap, "guarantorname", "guarantor"));
        loan.setGuarantorPhone(parsePhoneNumberSafely(getCellValueFromArray(row, columnMap, "guarantorphone", "guarantorcontact")));
        loan.setLoanPurpose(getCellValueFromArray(row, columnMap, "loanpurpose", "purpose"));
        loan.setBranchCode(getCellValueFromArray(row, columnMap, "branchcode", "branch"));
        loan.setLoanOfficer(getCellValueFromArray(row, columnMap, "loanofficer", "officer"));
        
        if (rowNum == 1) {
            log.info("CSV Parsed values - LoanId: {}, CustomerId: {}, Name: {}, Phone: {}, Product: {}, Principal: {}",
                loan.getLoanId(), loan.getCustomerId(), loan.getCustomerName(), 
                loan.getPhoneNumber(), loan.getProductCode(), loan.getPrincipal());
        }
        
        return loan;
    }
    
    /**
     * Get value from CSV array by trying multiple possible column names
     */
    private String getCellValueFromArray(String[] row, Map<String, Integer> columnMap, String... possibleNames) {
        for (String name : possibleNames) {
            Integer colIndex = columnMap.get(name);
            if (colIndex != null && colIndex < row.length) {
                String value = row[colIndex];
                return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
            }
        }
        return null;
    }

    /**
     * Parse a single CSV row into DTO
     */
    private LoanBookUploadDTO parseCsvRow(String[] row, int rowNum) {
        if (row.length < 22) { // Need at least 22 columns based on the DTO
            throw new IllegalArgumentException("Row has insufficient columns: " + row.length);
        }

        LoanBookUploadDTO loan = new LoanBookUploadDTO();
        loan.setRowNumber(rowNum);

        int col = 0;
        loan.setCustomerId(row[col++].trim());
        loan.setCustomerName(row[col++].trim());
        loan.setPhoneNumber(parsePhoneNumberSafely(row[col++]));
        loan.setEmail(row[col++].trim());
        loan.setProductCode(row[col++].trim());
        loan.setProductName(row[col++].trim());
        loan.setPrincipal(parseDoubleSafely(row[col++]));
        loan.setInterestRate(parseDoubleSafely(row[col++]));
        loan.setTerm(parseIntegerSafely(row[col++]));
        loan.setDisbursementDate(parseDateSafely(row[col++]));
        loan.setStatus(row[col++].trim());
        loan.setOutstandingBalance(parseDoubleSafely(row[col++]));
        loan.setTotalPaid(parseDoubleSafely(row[col++]));
        loan.setPaymentsMade(parseIntegerSafely(row[col++]));
        loan.setLastPaymentDate(parseDateSafely(row[col++]));
        loan.setCollateralType(row[col++].trim());
        loan.setCollateralValue(row[col++].trim());
        loan.setGuarantorName(row[col++].trim());
        loan.setGuarantorPhone(parsePhoneNumberSafely(row[col++]));
        loan.setLoanPurpose(row[col++].trim());
        loan.setBranchCode(row[col++].trim());
        loan.setLoanOfficer(row[col++].trim());

        return loan;
    }

    /**
     * Helper method to parse phone numbers and handle scientific notation from Excel
     * Converts "2.555E+11" back to "254555000000" or similar
     */
    private String parsePhoneNumberSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = value.trim();
        
        // Check if it's in scientific notation (contains 'E' or 'e')
        if (trimmed.toUpperCase().contains("E")) {
            try {
                // Parse as double, then convert to long to avoid decimal points
                double number = Double.parseDouble(trimmed);
                long phoneNumber = (long) number;
                return String.valueOf(phoneNumber);
            } catch (NumberFormatException e) {
                log.warn("Could not parse phone number from scientific notation: {}", trimmed);
                return trimmed; // Return as-is, validation will catch it
            }
        }
        
        // If it starts with a single quote (Excel text format indicator), remove it
        if (trimmed.startsWith("'")) {
            trimmed = trimmed.substring(1);
        }
        
        // Remove any spaces or dashes
        trimmed = trimmed.replaceAll("[\\s-]", "");
        
        return trimmed;
    }

    /**
     * Helper method to parse string to double safely
     */
    private Double parseDoubleSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Helper method to parse string to integer safely
     */
    private Integer parseIntegerSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Helper method to parse string to date safely
     */
    private LocalDate parseDateSafely(String value) {
        log.info("date string {}", value);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate date = LocalDate.parse(value.trim(), formatter);
                log.info("converted date {}", date);
                return date;
            } catch (Exception ignored) {}
        }

        log.error("error converting date field: unsupported format '{}'", value);
        return null;
    }


    /**
     * Check if CSV row array is not empty
     */
    private boolean isRowNotEmptyFromArray(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Create column mapping from header row
     */
    private Map<String, Integer> createColumnMapping(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        log.info("=== Creating Column Mapping ===");
        for (Cell cell : headerRow) {
            String header = getCellValueAsString(cell);
            if (header != null && !header.trim().isEmpty()) {
                String normalizedHeader = header.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
                columnMap.put(normalizedHeader, cell.getColumnIndex());
                log.info("Column {}: '{}' -> normalized: '{}'", cell.getColumnIndex(), header, normalizedHeader);
            }
        }
        log.info("=== Column Mapping Complete: {} columns mapped ===", columnMap.size());
        
        return columnMap;
    }
    
    /**
     * Parse a single row into DTO using column mapping
     */
    private LoanBookUploadDTO parseRowWithMapping(Row row, int rowNum, Map<String, Integer> columnMap) {
        LoanBookUploadDTO loan = new LoanBookUploadDTO();
        loan.setRowNumber(rowNum);
        
        // Log first row for debugging
        if (rowNum == 1) {
            log.info("=== Parsing First Data Row ===");
            log.info("Available columns in map: {}", columnMap.keySet());
        }
        
        loan.setLoanId(getCellValue(row, columnMap, "loanid", "loannumber", "accountnumber", "loannumber"));
        loan.setCustomerId(getCellValue(row, columnMap, "customerid", "clientid", "memberid", "idnumber"));
        loan.setCustomerName(getCellValue(row, columnMap, "customername", "clientname", "name", "fullname"));
        loan.setPhoneNumber(parsePhoneNumberSafely(getCellValue(row, columnMap, "phonenumber", "phone", "mobile", "contact", "mobilenumber")));
        loan.setEmail(getCellValue(row, columnMap, "email", "emailaddress", "emailid"));
        loan.setProductCode(getCellValue(row, columnMap, "productcode", "product", "loanproduct", "producttype"));
        loan.setProductName(getCellValue(row, columnMap, "productname", "loantype", "productdescription"));
        loan.setPrincipal(getCellValueAsDouble(row, columnMap, "principal", "principalamount", "loanamount", "amount"));
        loan.setInterestRate(getCellValueAsDouble(row, columnMap, "interestrate", "interest", "rate", "interestpa"));
        loan.setTerm(getCellValueAsInteger(row, columnMap, "term", "termmonths", "duration", "period", "loanterm", "months"));
        loan.setDisbursementDate(getCellValueAsDate(row, columnMap, "disbursementdate", "startdate", "loandate", "dateissued", "issuedate"));
        loan.setStatus(getCellValue(row, columnMap, "status", "loanstatus", "accountstatus"));
        loan.setOutstandingBalance(getCellValueAsDouble(row, columnMap, "outstandingbalance", "balance", "outstanding", "remainingbalance"));
        loan.setTotalPaid(getCellValueAsDouble(row, columnMap, "totalpaid", "paid", "amountpaid", "paidamount"));
        loan.setPaymentsMade(getCellValueAsInteger(row, columnMap, "paymentsmade", "installmentspaid", "numberofpayments"));
        loan.setLastPaymentDate(getCellValueAsDate(row, columnMap, "lastpaymentdate", "lastpayment", "recentpaymentdate"));
        loan.setCollateralType(getCellValue(row, columnMap, "collateraltype", "collateral", "security"));
        loan.setCollateralValue(getCellValue(row, columnMap, "collateralvalue", "securityvalue"));
        loan.setGuarantorName(getCellValue(row, columnMap, "guarantorname", "guarantor", "guarantorsfullname"));
        loan.setGuarantorPhone(parsePhoneNumberSafely(getCellValue(row, columnMap, "guarantorphone", "guarantorcontact", "guarantorphonenumber")));
        loan.setLoanPurpose(getCellValue(row, columnMap, "loanpurpose", "purpose", "reasonforloan"));
        loan.setBranchCode(getCellValue(row, columnMap, "branchcode", "branch", "branchname"));
        loan.setLoanOfficer(getCellValue(row, columnMap, "loanofficer", "officer", "accountofficer"));
        
        if (rowNum == 1) {
            log.info("Parsed values - LoanId: {}, CustomerId: {}, Name: {}, Phone: {}, Product: {}, Principal: {}",
                loan.getLoanId(), loan.getCustomerId(), loan.getCustomerName(), 
                loan.getPhoneNumber(), loan.getProductCode(), loan.getPrincipal());
        }
        
        return loan;
    }
    
    /**
     * Get cell value by trying multiple possible column names
     */
    private String getCellValue(Row row, Map<String, Integer> columnMap, String... possibleNames) {
        for (String name : possibleNames) {
            Integer colIndex = columnMap.get(name);
            if (colIndex != null) {
                return getCellValueAsString(row.getCell(colIndex));
            }
        }
        return null;
    }
    
    /**
     * Get cell value as Double by trying multiple possible column names
     */
    private Double getCellValueAsDouble(Row row, Map<String, Integer> columnMap, String... possibleNames) {
        for (String name : possibleNames) {
            Integer colIndex = columnMap.get(name);
            if (colIndex != null) {
                return getCellValueAsDouble(row.getCell(colIndex));
            }
        }
        return null;
    }
    
    /**
     * Get cell value as Integer by trying multiple possible column names
     */
    private Integer getCellValueAsInteger(Row row, Map<String, Integer> columnMap, String... possibleNames) {
        for (String name : possibleNames) {
            Integer colIndex = columnMap.get(name);
            if (colIndex != null) {
                return getCellValueAsInteger(row.getCell(colIndex));
            }
        }
        return null;
    }
    
    /**
     * Get cell value as Date by trying multiple possible column names
     */
    private LocalDate getCellValueAsDate(Row row, Map<String, Integer> columnMap, String... possibleNames) {
        for (String name : possibleNames) {
            Integer colIndex = columnMap.get(name);
            if (colIndex != null) {
                return getCellValueAsDate(row.getCell(colIndex));
            }
        }
        return null;
    }
    
    /**
     * Create LoanAccount entity from DTO with backdating support
     */
    private LoanAccount createLoanAccount(LoanBookUploadDTO dto) {
        LoanAccount loan = new LoanAccount();
        
        // Use uploaded loan ID as account number if provided, otherwise generate new one
        if (dto.getLoanId() != null && !dto.getLoanId().trim().isEmpty()) {
            loan.setLoanref(dto.getLoanId().trim());
        } else {
            loan.setLoanref("LN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        // Fetch actual customer entity to get the database ID
        Customer customer = getOrCreateCustomer(dto);
        
        // Set customer info using the database ID from the actual customer entity
        loan.setCustomerId(String.valueOf(customer.getId()));
        
        // Verify product exists
        Products product = productRepo.getByCode(dto.getProductCode())
            .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductCode()));
        
        // For uploaded loans, use actual tracked figures instead of recalculating
        // These are historical loans that have been managed elsewhere
        loan.setAmount(dto.getPrincipal().floatValue());
        loan.setInstallments(dto.getTerm());
        
        // Calculate payable amount: if we have outstanding balance and total paid, use those
        // Otherwise calculate: Principal + (Principal * InterestRate/100 * Term)
        float payableAmount;
        if (dto.getOutstandingBalance() != null && dto.getTotalPaid() != null) {
            // Use actual figures from the upload
            payableAmount = dto.getOutstandingBalance().floatValue() + dto.getTotalPaid().floatValue();
        } else {
            // Fallback: simple calculation if data is incomplete
            double totalInterest = dto.getPrincipal() * (dto.getInterestRate() / 100.0) * dto.getTerm();
            payableAmount = (float) (dto.getPrincipal() + totalInterest);
        }
        loan.setPayableAmount(payableAmount);
        
        // Use disbursement date from DTO for backdating support
        loan.setStartDate(dto.getDisbursementDate().atStartOfDay());
        
        // Calculate due date based on disbursement date + term
        LocalDateTime dueDate = dto.getDisbursementDate()
            .plusMonths(dto.getTerm())
            .atTime(23, 59);
        loan.setDueDate(dueDate);
        
        loan.setStatus(dto.getStatus().toUpperCase());
        
        // Set current balance
        if (dto.getOutstandingBalance() != null) {
            loan.setAccountBalance(dto.getOutstandingBalance().floatValue());
        } else {
            loan.setAccountBalance(dto.getPrincipal().floatValue());
        }
        
        // Set amount paid if available
        if (dto.getTotalPaid() != null) {
            loan.setAmountPaid(dto.getTotalPaid().floatValue());
        } else {
            loan.setAmountPaid(0f);
        }
        
        // Set other reference - always generate unique one since loanId is used for loanref
        // other_ref has a unique constraint, so we need to ensure uniqueness
        // Use loan purpose if available, otherwise generate unique reference
        if (dto.getLoanPurpose() != null && !dto.getLoanPurpose().trim().isEmpty()) {
            loan.setOtherRef("PURPOSE-" + dto.getLoanPurpose().trim() + "-" + System.currentTimeMillis());
        } else {
            // Generate unique reference using loan ref + timestamp
            loan.setOtherRef("IMPORT-" + loan.getLoanref() + "-" + System.currentTimeMillis());
        }
        
        // Create actual loan application for proper reference (pass customer entity)
        LoanApplication application = createLoanApplication(dto, customer);
        loan.setApplicationId(application.getApplicationId());
        
        log.debug("Created loan account with backdated start: {} and due: {}", 
            loan.getStartDate(), loan.getDueDate());
        
        return loan;
    }

    /**
     * Create actual loan application for imported loan
     * Application is marked as APPROVED since the loan already exists
     */
    private LoanApplication createLoanApplication(LoanBookUploadDTO dto, Customer customer) {
        LoanApplication application = new LoanApplication();
        
        // Generate unique loan number
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 90000) + 10000;
        application.setLoanNumber(timestamp + random);
        
        // Set customer information using actual customer entity
        application.setCustomerId(String.valueOf(customer.getId()));
        application.setCustomerIdNumber(customer.getDocumentNumber() != null ? customer.getDocumentNumber() : dto.getCustomerId());
        application.setCustomerMobileNumber(customer.getPhoneNumber());
        
        // Set loan details
        application.setLoanAmount(String.valueOf(dto.getPrincipal()));
        application.setProductCode(dto.getProductCode());
        application.setLoanTerm(String.valueOf(dto.getTerm()));
        application.setLoanInterest(String.valueOf(dto.getInterestRate()));
        application.setInstallments(String.valueOf(dto.getTerm()));
        
        // Set credit limit based on principal
        application.setCreditLimit(String.valueOf(dto.getPrincipal()));
        
        // Set destination account (use customer ID as fallback)
        application.setDestinationAccount(dto.getCustomerId());
        
        // Set disbursement type
        application.setDisbursementType("IMPORTED");
        
        // Mark as APPROVED since this is an imported loan that already exists
        application.setApplicationStatus("APPROVED");
        
        // Use disbursement date as application time for historical accuracy
        if (dto.getDisbursementDate() != null) {
            application.setApplicationTime(dto.getDisbursementDate().atStartOfDay());
        } else {
            application.setApplicationTime(LocalDateTime.now());
        }
        
        // Save and return the application
        return applicationRepo.save(application);
    }
    
    /**
     * Get or create customer entity from loan upload DTO
     * Returns the actual customer entity with database ID
     */
    private Customer getOrCreateCustomer(LoanBookUploadDTO dto) {
        Optional<Customer> optionalCustomer = Optional.empty();
        
        // Try to find existing customer by various identifiers
        if (dto.getCustomerId() != null && !dto.getCustomerId().trim().isEmpty()) {
            // First try by external ID
            optionalCustomer = customerRepository.findByExternalId(dto.getCustomerId());
            
            // If not found, try by database ID
            if (optionalCustomer.isEmpty()) {
                try {
                    optionalCustomer = customerRepository.findById(Long.parseLong(dto.getCustomerId()));
                } catch (NumberFormatException e) {
                    // Not a valid Long ID, skip
                }
            }
            
            // If not found, try by document number
            if (optionalCustomer.isEmpty()) {
                optionalCustomer = customerRepository.findByDocumentNumber(dto.getCustomerId());
            }
        }
        
        // If still not found, create new customer
        if (optionalCustomer.isEmpty()) {
            Customer newCustomer = createCustomerFromDTO(dto);
            return customerRepository.save(newCustomer);
        }
        
        return optionalCustomer.get();
    }
    
    /**
     * Create customer entity from loan upload DTO
     */
    private Customer createCustomerFromDTO(LoanBookUploadDTO dto) {
        Customer customer = new Customer();
        
        // Parse customer name
        if (dto.getCustomerName() != null && !dto.getCustomerName().trim().isEmpty()) {
            String[] parts = dto.getCustomerName().trim().split(" ");
            customer.setFirstName(parts.length > 0 ? parts[0] : dto.getCustomerName());
            customer.setMiddleName(parts.length > 2 ? parts[1] : null);
            customer.setLastName(parts.length > 2 ? parts[2] : (parts.length > 1 ? parts[1] : null));
        }
        
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setEmail(dto.getEmail());
        
        // Set external ID and document number from uploaded customer ID
        customer.setExternalId(dto.getCustomerId());
        customer.setDocumentNumber(dto.getCustomerId());
        
        customer.setBranchCode(dto.getBranchCode());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setAccountStatusFlag(true);
        customer.setAccountStatus("ACTIVE");
        customer.setStatus("ACTIVE");
        
        return customer;
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
