package com.example.demo.finance.loanManagement.services;

import com.example.demo.erp.communication.parsitence.models.Email;
import com.example.demo.erp.communication.services.CommunicationService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.serviceImplimentations.CustomerService;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.repositories.ApplicationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationApprovalService {

    private final ApplicationRepo applicationRepo;
    private final CustomerService customerService;
    private final CommunicationService communicationService;
    private final LoanAccountingService loanAccountingService;
    private final LoanNotificationService loanNotificationService;

    @Transactional
    public LoanApplication approveApplication(Long applicationId, String approvedBy, String comments) {
        log.info("Approving loan application ID: {}", applicationId);
        
        LoanApplication application = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalStateException("Application not found"));

        if (!"NEW".equals(application.getApplicationStatus())) {
            throw new IllegalStateException("Only NEW applications can be approved");
        }

        // Update application status
        application.setApplicationStatus("APPROVED");
        application = applicationRepo.save(application);

        // Send approval notification via email and SMS
        try {
            Customer customer = customerService.findById(Long.valueOf(application.getCustomerId())).getClient();
            
            // Send Email
            Email email = new Email();
            email.setRecipient(customer.getEmail());
            email.setMessageType("Loan Approval");
            email.setMessage("Hello " + customer.getFirstName() + ", your loan application for KES " + 
                    application.getLoanAmount() + " has been APPROVED. " +
                    (comments.isEmpty() ? "" : "Comments: " + comments));
            communicationService.sendCustomEmail(email);
            
            // Send SMS
            loanNotificationService.sendApplicationApprovedSMS(application);
        } catch (Exception e) {
            log.error("Failed to send approval notification", e);
        }

        return application;
    }

    @Transactional
    public LoanApplication rejectApplication(Long applicationId, String rejectedBy, String reason) {
        log.info("Rejecting loan application ID: {}", applicationId);
        
        LoanApplication application = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalStateException("Application not found"));

        if (!"NEW".equals(application.getApplicationStatus())) {
            throw new IllegalStateException("Only NEW applications can be rejected");
        }

        // Update application status
        application.setApplicationStatus("REJECTED");
        application = applicationRepo.save(application);

        // Send rejection notification via email and SMS
        try {
            Customer customer = customerService.findById(Long.valueOf(application.getCustomerId())).getClient();
            
            // Send Email
            Email email = new Email();
            email.setRecipient(customer.getEmail());
            email.setMessageType("Loan Rejection");
            email.setMessage("Hello " + customer.getFirstName() + ", we regret to inform you that your loan application for KES " + 
                    application.getLoanAmount() + " has been REJECTED. Reason: " + reason);
            communicationService.sendCustomEmail(email);
            
            // Send SMS
            loanNotificationService.sendApplicationRejectedSMS(application, reason);
        } catch (Exception e) {
            log.error("Failed to send rejection notification", e);
        }

        return application;
    }

    public Map<String, Object> getApplicationStatistics() {
        List<LoanApplication> allApplications = applicationRepo.findAll();
        
        long totalApplications = allApplications.size();
        long pendingApplications = allApplications.stream()
                .filter(app -> "NEW".equals(app.getApplicationStatus()))
                .count();
        long approvedApplications = allApplications.stream()
                .filter(app -> "APPROVED".equals(app.getApplicationStatus()) || "AUTHORISED".equals(app.getApplicationStatus()))
                .count();
        long rejectedApplications = allApplications.stream()
                .filter(app -> "REJECTED".equals(app.getApplicationStatus()))
                .count();
        
        double totalAmountRequested = allApplications.stream()
                .mapToDouble(app -> {
                    try {
                        return Double.parseDouble(app.getLoanAmount());
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();
        
        double approvedAmount = allApplications.stream()
                .filter(app -> "APPROVED".equals(app.getApplicationStatus()) || "AUTHORISED".equals(app.getApplicationStatus()))
                .mapToDouble(app -> {
                    try {
                        return Double.parseDouble(app.getLoanAmount());
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", totalApplications);
        stats.put("pendingApplications", pendingApplications);
        stats.put("approvedApplications", approvedApplications);
        stats.put("rejectedApplications", rejectedApplications);
        stats.put("totalAmountRequested", totalAmountRequested);
        stats.put("approvedAmount", approvedAmount);
        stats.put("approvalRate", totalApplications > 0 ? (approvedApplications * 100.0 / totalApplications) : 0);
        
        return stats;
    }
}
