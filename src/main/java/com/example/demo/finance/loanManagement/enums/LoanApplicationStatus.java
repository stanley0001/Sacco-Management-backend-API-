package com.example.demo.finance.loanManagement.enums;

/**
 * Centralized enum for Loan Application statuses
 * Ensures consistent status values across the entire system
 */
public enum LoanApplicationStatus {
    /**
     * Application has been created but not yet submitted
     */
    DRAFT("DRAFT", "Draft - Not Submitted"),
    
    /**
     * Application submitted and awaiting initial review
     */
    NEW("NEW", "New Application"),
    
    /**
     * Application is being reviewed by loan officer
     */
    PENDING_REVIEW("PENDING_REVIEW", "Under Review"),
    
    /**
     * Application is in approval workflow
     */
    PENDING_APPROVAL("PENDING_APPROVAL", "Pending Approval"),
    
    /**
     * Application approved and ready for disbursement
     */
    APPROVED("APPROVED", "Approved"),
    
    /**
     * Alternative approval status (for backward compatibility)
     */
    AUTHORISED("AUTHORISED", "Authorised"),
    
    /**
     * Application has been rejected
     */
    REJECTED("REJECTED", "Rejected"),
    
    /**
     * Application returned to applicant for corrections
     */
    RETURNED_FOR_CORRECTION("RETURNED_FOR_CORRECTION", "Returned for Correction"),
    
    /**
     * Application cancelled by applicant or system
     */
    CANCELLED("CANCELLED", "Cancelled"),
    
    /**
     * Application expired (e.g., no action taken within timeframe)
     */
    EXPIRED("EXPIRED", "Expired"),
    
    /**
     * Loan has been disbursed (terminal state for application)
     */
    DISBURSED("DISBURSED", "Disbursed");

    private final String code;
    private final String displayName;

    LoanApplicationStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get enum from string code (case-insensitive)
     */
    public static LoanApplicationStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        for (LoanApplicationStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status;
            }
        }
        
        // Fallback: try matching by enum name
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Check if this status allows approval
     */
    public boolean isApprovable() {
        return this == NEW || this == PENDING_REVIEW || this == PENDING_APPROVAL;
    }

    /**
     * Check if this status allows rejection
     */
    public boolean isRejectable() {
        return this == NEW || this == PENDING_REVIEW || this == PENDING_APPROVAL;
    }

    /**
     * Check if this is a terminal status (no further changes allowed)
     */
    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == CANCELLED || 
               this == EXPIRED || this == DISBURSED;
    }

    /**
     * Check if disbursement is allowed from this status
     */
    public boolean canDisburse() {
        return this == APPROVED || this == AUTHORISED;
    }

    @Override
    public String toString() {
        return code;
    }
}
