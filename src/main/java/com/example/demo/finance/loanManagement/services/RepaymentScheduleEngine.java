package com.example.demo.finance.loanManagement.services;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.entities.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized engine for generating loan repayment schedules
 * Handles both new loans and backdated uploads
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RepaymentScheduleEngine {
    
    /**
     * Generate repayment schedules for a loan account
     */
    public List<LoanRepaymentSchedule> generateSchedules(LoanAccount loanAccount,
                                                         LoanApplication application,
                                                         Products product,
                                                         LoanCalculatorService.LoanCalculation calculation) {
        log.info("Generating repayment schedules for loan: {}", loanAccount.getLoanref());
        
        List<LoanRepaymentSchedule> schedules = new ArrayList<>();
        
        if (calculation == null || calculation.getSchedule() == null) {
            log.warn("No calculation or schedule available for loan {}", loanAccount.getLoanref());
            return schedules;
        }
        
        List<LoanCalculatorService.RepaymentScheduleItem> scheduleItems = calculation.getSchedule();
        LocalDate disbursementDate = loanAccount.getDisbursementDate() != null ? 
            loanAccount.getDisbursementDate() : LocalDate.now();
        
        for (int i = 0; i < scheduleItems.size(); i++) {
            LoanCalculatorService.RepaymentScheduleItem item = scheduleItems.get(i);
            
            LoanRepaymentSchedule schedule = new LoanRepaymentSchedule();
            
            // Link to loan account
            schedule.setLoanAccountId(loanAccount.getAccountId());
            
            // Installment details
            schedule.setInstallmentNumber(item.getInstallmentNumber());
            schedule.setPrincipalAmount(BigDecimal.valueOf(item.getPrincipalAmount()));
            schedule.setInterestAmount(BigDecimal.valueOf(item.getInterestAmount()));
            schedule.setTotalAmount(BigDecimal.valueOf(item.getTotalPayment()));
            
            // Due date (disbursement date + installment number months)
            LocalDate dueDate = disbursementDate.plusMonths(item.getInstallmentNumber());
            schedule.setDueDate(dueDate);
            
            // Payment status - default to PENDING for future installments
            if (dueDate.isBefore(LocalDate.now())) {
                schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.OVERDUE);
            } else if (dueDate.isEqual(LocalDate.now())) {
                schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.CURRENT);
            } else {
                schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.PENDING);
            }
            
            // Payment tracking
            schedule.setTotalPaid(BigDecimal.ZERO);
            schedule.setTotalOutstanding(BigDecimal.valueOf(item.getTotalPayment()));
            
            // Note: openingBalance and closingBalance fields don't exist in entity
            // Balance tracking is handled through totalOutstanding calculations
            
            // Timestamps
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());
            
            schedules.add(schedule);
        }
        
        log.info("Generated {} repayment schedules for loan {}", schedules.size(), loanAccount.getLoanref());
        return schedules;
    }
    
    /**
     * Generate schedules for backdated loans with existing payments
     */
    public List<LoanRepaymentSchedule> generateSchedulesWithPayments(LoanAccount loanAccount,
                                                                     LoanApplication application,
                                                                     Products product,
                                                                     LoanCalculatorService.LoanCalculation calculation,
                                                                     Double totalPaid) {
        log.info("Generating schedules with payments for loan: {}, totalPaid: {}", 
            loanAccount.getLoanref(), totalPaid);
        
        List<LoanRepaymentSchedule> schedules = generateSchedules(loanAccount, application, product, calculation);
        
        if (totalPaid == null || totalPaid <= 0) {
            return schedules;
        }
        
        // Distribute total paid across installments
        double remainingPayment = totalPaid;
        
        for (LoanRepaymentSchedule schedule : schedules) {
            double installmentAmount = schedule.getTotalAmount().doubleValue();
            
            if (remainingPayment >= installmentAmount) {
                // Fully paid installment
                schedule.setTotalPaid(schedule.getTotalAmount());
                schedule.setTotalOutstanding(BigDecimal.ZERO);
                schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.PAID);
                schedule.setPaidDate(schedule.getDueDate()); // Assume paid on due date
                remainingPayment -= installmentAmount;
            } else if (remainingPayment > 0) {
                // Partially paid installment
                schedule.setTotalPaid(BigDecimal.valueOf(remainingPayment));
                schedule.setTotalOutstanding(schedule.getTotalAmount().subtract(BigDecimal.valueOf(remainingPayment)));
                
                // Status based on due date
                if (schedule.getDueDate().isBefore(LocalDate.now())) {
                    schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.OVERDUE);
                } else {
                    schedule.setStatus(LoanRepaymentSchedule.ScheduleStatus.CURRENT);
                }
                
                remainingPayment = 0;
            } else {
                // Unpaid installment - status already set in generateSchedules
                break;
            }
            
            schedule.setUpdatedAt(LocalDateTime.now());
        }
        
        log.info("Applied {} payment distribution across {} schedules", totalPaid, schedules.size());
        return schedules;
    }
}
