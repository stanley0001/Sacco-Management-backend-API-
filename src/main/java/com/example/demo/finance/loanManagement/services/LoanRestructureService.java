package com.example.demo.finance.loanManagement.services;

import com.example.demo.finance.accounting.entities.JournalEntry;
import com.example.demo.finance.accounting.entities.JournalEntryLine;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanRepaymentSchedule;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepository;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanRepaymentScheduleRepository;
import com.example.demo.finance.accounting.services.AccountingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for restructuring loans
 * Handles term extension, payment reduction, interest rate changes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanRestructureService {
    
    private final LoanAccountRepository loanAccountRepository;
    private final LoanRepaymentScheduleRepository scheduleRepository;
    private final RepaymentScheduleEngine scheduleEngine;
    private final AccountingService accountingService;
    
    /**
     * Extend loan term (reduce monthly payment)
     */
    @Transactional
    public LoanAccount extendLoanTerm(Long loanId, Integer newTermMonths, String approvedBy, String reason) {
        log.info("Extending loan term for loan: {}, new term: {} months", loanId, newTermMonths);
        
        LoanAccount loan = loanAccountRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        
        // Validate
        if (newTermMonths <= loan.getTerm()) {
            throw new IllegalArgumentException("New term must be longer than current term");
        }
        
        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new IllegalStateException("Can only restructure ACTIVE loans");
        }
        
        // Store old term for audit
        Integer oldTerm = loan.getTerm();
        
        // Update loan term
        loan.setTerm(newTermMonths);
        loan.setMaturityDate(loan.getDisbursementDate().plusMonths(newTermMonths));
        loan.setUpdatedAt(LocalDateTime.now());
        
        // Delete old schedules
        List<LoanRepaymentSchedule> oldSchedules = scheduleRepository.findByLoanAccountId(loanId);
        scheduleRepository.deleteAll(oldSchedules);
        
        // Generate new schedules
        List<LoanRepaymentSchedule> newSchedules = generateRestructuredSchedules(loan, newTermMonths);
        scheduleRepository.saveAll(newSchedules);
        
        LoanAccount updated = loanAccountRepository.save(loan);
        
        // Post to accounting (restructure adjustment)
        try {
            postRestructureToAccounting(loan, "TERM_EXTENSION", approvedBy, reason, 
                String.format("Term extended from %d to %d months", oldTerm, newTermMonths));
        } catch (Exception e) {
            log.error("Failed to post restructure to accounting", e);
        }
        
        log.info("Loan term extended successfully for loan: {}", loanId);
        return updated;
    }
    
    /**
     * Change interest rate
     */
    @Transactional
    public LoanAccount changeInterestRate(Long loanId, BigDecimal newRate, String approvedBy, String reason) {
        log.info("Changing interest rate for loan: {}, new rate: {}%", loanId, newRate);
        
        LoanAccount loan = loanAccountRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        
        // Validate
        if (newRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Interest rate must be greater than zero");
        }
        
        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new IllegalStateException("Can only restructure ACTIVE loans");
        }
        
        BigDecimal oldRate = loan.getInterestRate();
        
        // Recalculate total amount with new rate
        BigDecimal principal = loan.getOutstandingPrincipal();
        BigDecimal remainingTerm = BigDecimal.valueOf(loan.getTerm());
        BigDecimal newInterest = principal.multiply(newRate.divide(BigDecimal.valueOf(100)))
            .multiply(remainingTerm.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
        
        BigDecimal newTotalAmount = principal.add(newInterest);
        
        // Update loan
        loan.setInterestRate(newRate);
        loan.setOutstandingInterest(newInterest);
        loan.setTotalAmount(newTotalAmount);
        loan.setTotalOutstanding(newTotalAmount);
        loan.setUpdatedAt(LocalDateTime.now());
        
        // Delete old schedules and generate new ones
        List<LoanRepaymentSchedule> oldSchedules = scheduleRepository.findByLoanAccountId(loanId);
        scheduleRepository.deleteAll(oldSchedules);
        
        List<LoanRepaymentSchedule> newSchedules = generateRestructuredSchedules(loan, loan.getTerm());
        scheduleRepository.saveAll(newSchedules);
        
        LoanAccount updated = loanAccountRepository.save(loan);
        
        // Post to accounting
        try {
            postRestructureToAccounting(loan, "RATE_CHANGE", approvedBy, reason,
                String.format("Rate changed from %.2f%% to %.2f%%", oldRate, newRate));
        } catch (Exception e) {
            log.error("Failed to post restructure to accounting", e);
        }
        
        log.info("Interest rate changed successfully for loan: {}", loanId);
        return updated;
    }
    
    /**
     * Reduce monthly payment (by extending term)
     */
    @Transactional
    public LoanAccount reduceMonthlyPayment(Long loanId, BigDecimal targetPayment, String approvedBy, String reason) {
        log.info("Reducing monthly payment for loan: {}, target payment: {}", loanId, targetPayment);
        
        LoanAccount loan = loanAccountRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        
        // Calculate current monthly payment
        BigDecimal currentMonthly = loan.getTotalOutstanding().divide(
            BigDecimal.valueOf(loan.getTerm()), 2, RoundingMode.HALF_UP);
        
        if (targetPayment.compareTo(currentMonthly) >= 0) {
            throw new IllegalArgumentException("Target payment must be less than current monthly payment");
        }
        
        // Calculate new term needed
        int newTerm = loan.getTotalOutstanding().divide(targetPayment, 0, RoundingMode.UP).intValue();
        
        // Extend the term
        return extendLoanTerm(loanId, newTerm, approvedBy, 
            String.format("%s - Reduced payment from %.2f to %.2f", reason, currentMonthly, targetPayment));
    }
    
    /**
     * Complete restructure (term + rate)
     */
    @Transactional
    public LoanAccount completeRestructure(Long loanId, Integer newTerm, BigDecimal newRate, 
                                          String approvedBy, String reason) {
        log.info("Complete restructure for loan: {}, term: {}, rate: {}%", loanId, newTerm, newRate);
        
        LoanAccount loan = loanAccountRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        
        Integer oldTerm = loan.getTerm();
        BigDecimal oldRate = loan.getInterestRate();
        
        // Update both term and rate
        loan.setTerm(newTerm);
        loan.setInterestRate(newRate);
        loan.setMaturityDate(loan.getDisbursementDate().plusMonths(newTerm));
        
        // Recalculate amounts
        BigDecimal principal = loan.getOutstandingPrincipal();
        BigDecimal newInterest = principal.multiply(newRate.divide(BigDecimal.valueOf(100)))
            .multiply(BigDecimal.valueOf(newTerm).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
        BigDecimal newTotal = principal.add(newInterest);
        
        loan.setOutstandingInterest(newInterest);
        loan.setTotalAmount(newTotal);
        loan.setTotalOutstanding(newTotal);
        loan.setUpdatedAt(LocalDateTime.now());
        
        // Regenerate schedules
        List<LoanRepaymentSchedule> oldSchedules = scheduleRepository.findByLoanAccountId(loanId);
        scheduleRepository.deleteAll(oldSchedules);
        
        List<LoanRepaymentSchedule> newSchedules = generateRestructuredSchedules(loan, newTerm);
        scheduleRepository.saveAll(newSchedules);
        
        LoanAccount updated = loanAccountRepository.save(loan);
        
        // Post to accounting
        try {
            postRestructureToAccounting(loan, "COMPLETE_RESTRUCTURE", approvedBy, reason,
                String.format("Term: %d→%d months, Rate: %.2f%%→%.2f%%", oldTerm, newTerm, oldRate, newRate));
        } catch (Exception e) {
            log.error("Failed to post restructure to accounting", e);
        }
        
        log.info("Complete restructure processed for loan: {}", loanId);
        return updated;
    }
    
    /**
     * Generate new repayment schedules after restructure
     */
    private List<LoanRepaymentSchedule> generateRestructuredSchedules(LoanAccount loan, int termMonths) {
        List<LoanRepaymentSchedule> schedules = new ArrayList<>();
        
        BigDecimal totalAmount = loan.getTotalOutstanding();
        BigDecimal monthlyAmount = totalAmount.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        BigDecimal principalPerMonth = loan.getOutstandingPrincipal().divide(
            BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        BigDecimal interestPerMonth = loan.getOutstandingInterest().divide(
            BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        
        LocalDate startDate = LocalDate.now(); // Start from today for restructured loans
        BigDecimal runningBalance = totalAmount;
        
        for (int i = 1; i <= termMonths; i++) {
            LoanRepaymentSchedule schedule = new LoanRepaymentSchedule();
            schedule.setLoanAccountId(loan.getId());
            schedule.setInstallmentNumber(i);
            schedule.setDueDate(startDate.plusMonths(i));
            
            // For last installment, use remaining balance
            if (i == termMonths) {
                schedule.setPrincipalAmount(runningBalance.subtract(interestPerMonth));
                schedule.setInterestAmount(interestPerMonth);
                schedule.setTotalAmount(runningBalance);
            } else {
                schedule.setPrincipalAmount(principalPerMonth);
                schedule.setInterestAmount(interestPerMonth);
                schedule.setTotalAmount(monthlyAmount);
            }
            
            schedule.setPaidPrincipal(BigDecimal.ZERO);
            schedule.setPaidInterest(BigDecimal.ZERO);
            schedule.setTotalPaid(BigDecimal.ZERO);
            schedule.setOutstandingPrincipal(schedule.getPrincipalAmount());
            schedule.setOutstandingInterest(schedule.getInterestAmount());
            schedule.setTotalOutstanding(schedule.getTotalAmount());
            schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.PENDING);
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());
            
            runningBalance = runningBalance.subtract(schedule.getTotalAmount());
            schedules.add(schedule);
        }
        
        return schedules;
    }
    
    /**
     * Post restructure to accounting (memo entry)
     */
    private void postRestructureToAccounting(LoanAccount loan, String restructureType, 
                                            String approvedBy, String reason, String details) {
        // Create memo journal entry
        JournalEntry journalEntry =
            JournalEntry.builder()
                .description(String.format("Loan Restructure - %s - Loan: %s - %s - Reason: %s", 
                    restructureType, loan.getLoanReference(), details, reason))
                .journalType(JournalEntry.JournalType.ADJUSTMENT)
                .reference(loan.getLoanReference())
                .transactionDate(LocalDate.now())
                .createdBy(approvedBy)
                .build();
        
        // For restructure, we create a memo entry (no financial impact, just audit trail)
        java.util.List<JournalEntryLine> lines = new java.util.ArrayList<>();
        
        // Memo entry - Debit and Credit same account (zero net effect)
        JournalEntryLine line1 =
            new JournalEntryLine();
        line1.setJournalEntry(journalEntry);
        line1.setAccountCode("1200"); // Loans Receivable account code
        line1.setAccountName("Loans Receivable");
        line1.setType(JournalEntryLine.EntryType.DEBIT);
        line1.setAmount(0.01); // Nominal amount for memo
        line1.setDescription("Restructure memo - " + details);
        line1.setLineNumber(1);
        lines.add(line1);
        
        JournalEntryLine line2 =
            new JournalEntryLine();
        line2.setJournalEntry(journalEntry);
        line2.setAccountCode("1200"); // Same account (zero net effect)
        line2.setAccountName("Loans Receivable");
        line2.setType(JournalEntryLine.EntryType.CREDIT);
        line2.setAmount(0.01);
        line2.setDescription("Restructure memo - " + details);
        line2.setLineNumber(2);
        lines.add(line2);
        
        journalEntry.setLines(lines);
        
        // Post as memo
        accountingService.createJournalEntry(journalEntry, approvedBy);
        
        log.info("Restructure memo posted to accounting: loan={}, type={}", loan.getId(), restructureType);
    }
}
