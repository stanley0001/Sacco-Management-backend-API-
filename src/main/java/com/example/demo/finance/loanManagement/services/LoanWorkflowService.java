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
import java.util.Map;
import java.util.Set;

/**
 * Manages loan application workflow and state transitions
 * Ensures valid state transitions and triggers appropriate actions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanWorkflowService {
    
    private final ApplicationRepo applicationRepo;
    private final CustomerService customerService;
    private final CommunicationService communicationService;
    
    // Valid state transitions
    private static final Map<String, Set<String>> VALID_TRANSITIONS = new HashMap<>();
    
    static {
        VALID_TRANSITIONS.put("NEW", Set.of("PENDING_REVIEW", "APPROVED", "REJECTED"));
        VALID_TRANSITIONS.put("PENDING_REVIEW", Set.of("APPROVED", "REJECTED", "NEW"));
        VALID_TRANSITIONS.put("APPROVED", Set.of("READY_FOR_DISBURSEMENT", "REJECTED"));
        VALID_TRANSITIONS.put("READY_FOR_DISBURSEMENT", Set.of("DISBURSED", "REJECTED"));
        VALID_TRANSITIONS.put("DISBURSED", Set.of("CLOSED", "DEFAULTED"));
        VALID_TRANSITIONS.put("REJECTED", Set.of("NEW")); // Allow reapplication
        VALID_TRANSITIONS.put("CLOSED", Set.of()); // Terminal state
        VALID_TRANSITIONS.put("DEFAULTED", Set.of()); // Terminal state
    }
    
    /**
     * Move application to pending review
     */
    @Transactional
    public LoanApplication moveToPendingReview(Long applicationId, String reviewedBy, String comments) {
        log.info("Moving application {} to PENDING_REVIEW by {}", applicationId, reviewedBy);
        
        LoanApplication application = getApplication(applicationId);
        validateTransition(application, "PENDING_REVIEW");
        
        application.setApplicationStatus("PENDING_REVIEW");
        application = applicationRepo.save(application);
        
        sendNotification(application, "Loan Under Review", 
            "Your loan application is now under review. We will notify you of the outcome shortly.");
        
        return application;
    }
    
    /**
     * Approve application
     */
    @Transactional
    public LoanApplication approveApplication(Long applicationId, String approvedBy, String comments) {
        log.info("Approving application {} by {}", applicationId, approvedBy);
        
        LoanApplication application = getApplication(applicationId);
        validateTransition(application, "APPROVED");
        
        application.setApplicationStatus("APPROVED");
        application = applicationRepo.save(application);
        
        sendNotification(application, "Loan Approved", 
            "Congratulations! Your loan application for KES " + application.getLoanAmount() + 
            " has been approved. " + (comments != null && !comments.isEmpty() ? "Comments: " + comments : ""));
        
        log.info("Application {} approved successfully", applicationId);
        return application;
    }
    
    /**
     * Reject application
     */
    @Transactional
    public LoanApplication rejectApplication(Long applicationId, String rejectedBy, String reason) {
        log.info("Rejecting application {} by {}", applicationId, rejectedBy);
        
        LoanApplication application = getApplication(applicationId);
        validateTransition(application, "REJECTED");
        
        application.setApplicationStatus("REJECTED");
        application = applicationRepo.save(application);
        
        sendNotification(application, "Loan Application Not Approved", 
            "We regret to inform you that your loan application for KES " + application.getLoanAmount() + 
            " was not approved. Reason: " + reason);
        
        log.info("Application {} rejected", applicationId);
        return application;
    }
    
    /**
     * Mark as ready for disbursement
     */
    @Transactional
    public LoanApplication markReadyForDisbursement(Long applicationId) {
        log.info("Marking application {} as ready for disbursement", applicationId);
        
        LoanApplication application = getApplication(applicationId);
        validateTransition(application, "READY_FOR_DISBURSEMENT");
        
        application.setApplicationStatus("READY_FOR_DISBURSEMENT");
        application = applicationRepo.save(application);
        
        log.info("Application {} marked ready for disbursement", applicationId);
        return application;
    }
    
    /**
     * Mark as disbursed (called by booking service)
     */
    @Transactional
    public LoanApplication markDisbursed(Long applicationId, String disbursementMethod, String disbursementReference) {
        log.info("Marking application {} as DISBURSED", applicationId);
        
        LoanApplication application = getApplication(applicationId);
        
        // Allow DISBURSED from multiple states for flexibility
        if (!"READY_FOR_DISBURSEMENT".equals(application.getApplicationStatus()) &&
            !"APPROVED".equals(application.getApplicationStatus()) &&
            !"NEW".equals(application.getApplicationStatus())) {
            
            log.warn("Unusual state transition to DISBURSED from {}", application.getApplicationStatus());
        }
        
        application.setApplicationStatus("DISBURSED");
        application.setDisbursementMethod(disbursementMethod);
        application = applicationRepo.save(application);
        
        sendNotification(application, "Loan Disbursed", 
            "Your loan of KES " + application.getLoanAmount() + " has been disbursed successfully.");
        
        log.info("Application {} marked as disbursed", applicationId);
        return application;
    }
    
    /**
     * Check if transition is valid
     */
    public boolean isValidTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        
        Set<String> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        return allowedTransitions != null && allowedTransitions.contains(newStatus);
    }
    
    /**
     * Get application or throw exception
     */
    private LoanApplication getApplication(Long applicationId) {
        return applicationRepo.findById(applicationId)
            .orElseThrow(() -> new IllegalStateException("Application not found: " + applicationId));
    }
    
    /**
     * Validate state transition
     */
    private void validateTransition(LoanApplication application, String newStatus) {
        String currentStatus = application.getApplicationStatus();
        
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid state transition from %s to %s for application %d", 
                    currentStatus, newStatus, application.getApplicationId()));
        }
    }
    
    /**
     * Send notification to customer
     */
    private void sendNotification(LoanApplication application, String subject, String message) {
        try {
            Customer customer = customerService.findById(Long.valueOf(application.getCustomerId())).getClient();
            
            if (customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                Email email = new Email();
                email.setRecipient(customer.getEmail());
                email.setMessageType(subject);
                email.setMessage("Hello " + customer.getFirstName() + ", " + message);
                communicationService.sendCustomEmail(email);
                
                log.debug("Notification sent to customer: {}", customer.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to send notification for application {}: {}", 
                application.getApplicationId(), e.getMessage());
            // Don't fail the workflow if notification fails
        }
    }
}
