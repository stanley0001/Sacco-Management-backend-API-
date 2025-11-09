package com.example.demo.finance.loanManagement.services;

import com.example.demo.erp.communication.sms.SmsService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanRepaymentSchedule;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanRepaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for sending SMS notifications throughout the loan lifecycle
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanNotificationService {

    private final SmsService smsService;
    private final CustomerRepository customerRepository;
    private final LoanRepaymentScheduleRepository scheduleRepo;

    /**
     * Send application submission SMS
     */
    public void sendApplicationSubmittedSMS(LoanApplication application) {
        try {
            Customer customer = customerRepository.findById(Long.valueOf(application.getCustomerId()))
                    .orElse(null);
            if (customer != null && customer.getPhoneNumber() != null) {
                String message = String.format(
                    "Dear %s, your loan application of KES %,.2f has been received. " +
                    "Application Ref: %s. We will notify you once processed.",
                    customer.getFirstName(),
                    application.getLoanAmount(),
                    application.getApplicationId()
                );
                smsService.sendSms(customer.getPhoneNumber(), message);
                log.info("Application submission SMS sent to {}", customer.getPhoneNumber());
            }
        } catch (Exception e) {
            log.error("Failed to send application submission SMS", e);
        }
    }

    /**
     * Send application approval SMS
     */
    public void sendApplicationApprovedSMS(LoanApplication application) {
        try {
            Customer customer = customerRepository.findById(Long.valueOf(application.getCustomerId()))
                    .orElse(null);
            if (customer != null && customer.getPhoneNumber() != null) {
                String message = String.format(
                    "Dear %s, congratulations! Your loan application of KES %,.2f has been APPROVED. " +
                    "Your loan will be disbursed shortly. Ref: %s",
                    customer.getFirstName(),
                    application.getLoanAmount(),
                    application.getApplicationId()
                );
                smsService.sendSms(customer.getPhoneNumber(), message);
                log.info("Application approval SMS sent to {}", customer.getPhoneNumber());
            }
        } catch (Exception e) {
            log.error("Failed to send approval SMS", e);
        }
    }

    /**
     * Send application rejection SMS
     */
    public void sendApplicationRejectedSMS(LoanApplication application, String reason) {
        try {
            Customer customer = customerRepository.findById(Long.valueOf(application.getCustomerId()))
                    .orElse(null);
            if (customer != null && customer.getPhoneNumber() != null) {
                String message = String.format(
                    "Dear %s, we regret to inform you that your loan application of KES %,.2f has been declined. " +
                    "Reason: %s. Contact us for more information.",
                    customer.getFirstName(),
                    application.getLoanAmount(),
                    reason != null ? reason : "See loan officer"
                );
                smsService.sendSms(customer.getPhoneNumber(), message);
                log.info("Application rejection SMS sent to {}", customer.getPhoneNumber());
            }
        } catch (Exception e) {
            log.error("Failed to send rejection SMS", e);
        }
    }

    /**
     * Send disbursement confirmation SMS
     */
    public void sendDisbursementSMS(LoanAccount loanAccount, String method) {
        try {
            Customer customer = customerRepository.findById(Long.valueOf(loanAccount.getCustomerId()))
                    .orElse(null);
            if (customer != null && customer.getPhoneNumber() != null) {
                String message = String.format(
                    "Dear %s, your loan of KES %,.2f has been disbursed via %s. " +
                    "Loan Ref: %s. Total payable: KES %,.2f over %d months. Thank you.",
                    customer.getFirstName(),
                    loanAccount.getPrincipalAmount(),
                    method,
                    loanAccount.getLoanReference(),
                    loanAccount.getTotalAmount(),
                    loanAccount.getTerm()
                );
                smsService.sendSms(customer.getPhoneNumber(), message);
                log.info("Disbursement SMS sent to {}", customer.getPhoneNumber());
            }
        } catch (Exception e) {
            log.error("Failed to send disbursement SMS", e);
        }
    }

    /**
     * Scheduled: Send payment reminders 3 days before due date
     * Runs daily at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendPaymentReminders() {
        log.info("Running payment reminder job...");
        
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(3);
        
        try {
            List<LoanRepaymentSchedule> upcomingPayments = scheduleRepo
                    .findByDueDateAndStatusNot(reminderDate, LoanRepaymentSchedule.ScheduleStatus.PAID);
            
            log.info("Found {} payments due in 3 days", upcomingPayments.size());
            
            for (LoanRepaymentSchedule schedule : upcomingPayments) {
                sendPaymentReminderSMS(schedule);
            }
            
            log.info("Payment reminder job completed. Sent {} reminders", upcomingPayments.size());
        } catch (Exception e) {
            log.error("Error in payment reminder job", e);
        }
    }

    /**
     * Send individual payment reminder SMS
     */
    private void sendPaymentReminderSMS(LoanRepaymentSchedule schedule) {
        try {
            LoanAccount loan = schedule.getLoanAccount();
            if (loan == null) {
                log.warn("Loan account not found for schedule {}", schedule.getId());
                return;
            }
            
            Customer customer = customerRepository.findById(Long.valueOf(loan.getCustomerId()))
                    .orElse(null);
            
            if (customer != null && customer.getPhoneNumber() != null) {
                String message = String.format(
                    "Payment Reminder: Dear %s, your loan payment of KES %,.2f is due on %s. " +
                    "Loan Ref: %s. Please pay on time to avoid penalties. Thank you.",
                    customer.getFirstName(),
                    schedule.getTotalAmount(),
                    schedule.getDueDate(),
                    loan.getLoanReference()
                );
                smsService.sendSms(customer.getPhoneNumber(), message);
                log.info("Payment reminder SMS sent to {} for schedule {}", 
                    customer.getPhoneNumber(), schedule.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send payment reminder SMS for schedule {}", schedule.getId(), e);
        }
    }

    /**
     * Scheduled: Send overdue notifications
     * Runs daily at 10:00 AM
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void sendOverdueNotifications() {
        log.info("Running overdue notification job...");
        
        LocalDate today = LocalDate.now();
        
        try {
            List<LoanRepaymentSchedule> overdueSchedules = scheduleRepo
                    .findByStatusAndDueDateBefore(LoanRepaymentSchedule.ScheduleStatus.OVERDUE, today);
            
            log.info("Found {} overdue payments", overdueSchedules.size());
            
            for (LoanRepaymentSchedule schedule : overdueSchedules) {
                sendOverdueNotificationSMS(schedule);
            }
            
            log.info("Overdue notification job completed. Sent {} notifications", overdueSchedules.size());
        } catch (Exception e) {
            log.error("Error in overdue notification job", e);
        }
    }

    /**
     * Send individual overdue notification SMS
     */
    private void sendOverdueNotificationSMS(LoanRepaymentSchedule schedule) {
        try {
            LoanAccount loan = schedule.getLoanAccount();
            if (loan == null) {
                log.warn("Loan account not found for schedule {}", schedule.getId());
                return;
            }
            
            Customer customer = customerRepository.findById(Long.valueOf(loan.getCustomerId()))
                    .orElse(null);
            
            if (customer != null && customer.getPhoneNumber() != null) {
                long daysOverdue = schedule.getDaysOverdue();
                String message = String.format(
                    "OVERDUE NOTICE: Dear %s, your loan payment of KES %,.2f is %d days overdue. " +
                    "Loan Ref: %s. Please pay immediately to avoid additional penalties. Contact us for assistance.",
                    customer.getFirstName(),
                    schedule.getTotalOutstanding(),
                    daysOverdue,
                    loan.getLoanReference()
                );
                smsService.sendSms(customer.getPhoneNumber(), message);
                log.info("Overdue notification SMS sent to {} for schedule {}", 
                    customer.getPhoneNumber(), schedule.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send overdue notification SMS for schedule {}", schedule.getId(), e);
        }
    }

    /**
     * Send custom notification
     */
    public void sendCustomNotification(String customerId, String message) {
        try {
            Customer customer = customerRepository.findById(Long.valueOf(customerId))
                    .orElse(null);
            if (customer != null && customer.getPhoneNumber() != null) {
                smsService.sendSms(customer.getPhoneNumber(), message);
                log.info("Custom notification sent to {}", customer.getPhoneNumber());
            }
        } catch (Exception e) {
            log.error("Failed to send custom notification", e);
        }
    }
}
