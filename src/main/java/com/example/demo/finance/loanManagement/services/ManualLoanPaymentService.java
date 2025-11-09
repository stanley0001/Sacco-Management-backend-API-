package com.example.demo.finance.loanManagement.services;

import com.example.demo.erp.communication.sms.SmsService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.ManualLoanPayment;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.finance.loanManagement.parsistence.repositories.ManualLoanPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManualLoanPaymentService {

    private final ManualLoanPaymentRepository manualPaymentRepo;
    private final LoanAccountRepo loanAccountRepo;
    private final CustomerRepository customerRepository;
    private final SmsService smsService;

    @Transactional
    public ManualLoanPayment submitPayment(Long loanAccountId, ManualLoanPayment payment) {
        log.info("Submitting manual payment for loan {}", loanAccountId);
        
        // Validate loan exists
        LoanAccount loanAccount = loanAccountRepo.findById(loanAccountId)
                .orElseThrow(() -> new IllegalStateException("Loan account not found"));
        
        // Set loan details
        payment.setLoanAccountId(loanAccountId);
        payment.setLoanReference(loanAccount.getLoanReference());
        payment.setCustomerId(loanAccount.getCustomerId());
        
        // Get customer name
        try {
            Customer customer = customerRepository.findById(Long.valueOf(loanAccount.getCustomerId()))
                    .orElse(null);
            if (customer != null) {
                payment.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
            }
        } catch (Exception e) {
            log.warn("Could not fetch customer name", e);
        }
        
        if (payment.getSubmittedAt() == null) {
            payment.setSubmittedAt(LocalDateTime.now());
        }
        if (payment.getStatus() == null) {
            payment.setStatus("PENDING");
        }
        
        ManualLoanPayment saved = manualPaymentRepo.save(payment);
        log.info("Manual payment submitted successfully. ID: {}", saved.getId());
        return saved;
    }

    public List<ManualLoanPayment> getPendingPayments() {
        return manualPaymentRepo.findByStatusOrderBySubmittedAtDesc("PENDING");
    }

    @Transactional
    public ManualLoanPayment approvePayment(Long paymentId, String approvedBy, String comments) {
        log.info("Approving manual payment {}", paymentId);
        
        ManualLoanPayment payment = manualPaymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found"));
        
        if (!"PENDING".equals(payment.getStatus())) {
            throw new IllegalStateException("Only pending payments can be approved");
        }
        
        payment.setStatus("APPROVED");
        payment.setApprovedBy(approvedBy);
        payment.setApprovedAt(LocalDateTime.now());
        payment.setApprovalComments(comments);
        
        ManualLoanPayment saved = manualPaymentRepo.save(payment);
        
        // Send SMS notification
        sendApprovalSMS(payment);
        
        log.info("Manual payment approved successfully. ID: {}", paymentId);
        return saved;
    }

    @Transactional
    public ManualLoanPayment rejectPayment(Long paymentId, String rejectedBy, String reason) {
        log.info("Rejecting manual payment {}", paymentId);
        
        ManualLoanPayment payment = manualPaymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found"));
        
        if (!"PENDING".equals(payment.getStatus())) {
            throw new IllegalStateException("Only pending payments can be rejected");
        }
        
        payment.setStatus("REJECTED");
        payment.setRejectedBy(rejectedBy);
        payment.setRejectedAt(LocalDateTime.now());
        payment.setRejectionReason(reason);
        
        ManualLoanPayment saved = manualPaymentRepo.save(payment);
        
        // Send SMS notification
        sendRejectionSMS(payment);
        
        log.info("Manual payment rejected. ID: {}", paymentId);
        return saved;
    }

    public List<ManualLoanPayment> getPaymentHistory(String status, Long loanAccountId) {
        if (status != null && loanAccountId != null) {
            return manualPaymentRepo.findByStatusAndLoanAccountIdOrderBySubmittedAtDesc(status, loanAccountId);
        } else if (status != null) {
            return manualPaymentRepo.findByStatusOrderBySubmittedAtDesc(status);
        } else if (loanAccountId != null) {
            return manualPaymentRepo.findByLoanAccountIdOrderBySubmittedAtDesc(loanAccountId);
        } else {
            return manualPaymentRepo.findAllByOrderBySubmittedAtDesc();
        }
    }

    private void sendApprovalSMS(ManualLoanPayment payment) {
        try {
            Customer customer = customerRepository.findById(Long.valueOf(payment.getCustomerId()))
                    .orElse(null);
            if (customer != null && customer.getPhoneNumber() != null) {
                String message = String.format(
                    "Dear %s, your loan payment of KES %,.2f has been approved. Receipt: %s. Thank you.",
                    customer.getFirstName(),
                    payment.getAmount(),
                    payment.getReferenceNumber()
                );
                smsService.sendSms(customer.getPhoneNumber(), message);
            }
        } catch (Exception e) {
            log.error("Failed to send approval SMS", e);
        }
    }

    private void sendRejectionSMS(ManualLoanPayment payment) {
        try {
            Customer customer = customerRepository.findById(Long.valueOf(payment.getCustomerId()))
                    .orElse(null);
            if (customer != null && customer.getPhoneNumber() != null) {
                String message = String.format(
                    "Dear %s, your loan payment of KES %,.2f has been rejected. " +
                    "Reason: %s. Please contact us for assistance.",
                    customer.getFirstName(),
                    payment.getAmount(),
                    payment.getRejectionReason()
                );
                smsService.sendSms(customer.getPhoneNumber(), message);
            }
        } catch (Exception e) {
            log.error("Failed to send rejection SMS", e);
        }
    }
}
