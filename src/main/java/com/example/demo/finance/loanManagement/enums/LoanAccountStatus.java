package com.example.demo.finance.loanManagement.enums;

/**
 * Centralized enum for Loan Account statuses
 * Ensures consistent status values across the entire system
 */
public enum LoanAccountStatus {
    /**
     * Loan account created but not yet disbursed
     */
    INIT("INIT", "Initialized"),
    
    /**
     * Loan ready for disbursement (approved but funds not yet released)
     */
    READY_FOR_DISBURSEMENT("READY_FOR_DISBURSEMENT", "Ready for Disbursement"),
    
    /**
     * Loan has been disbursed and is active
     */
    ACTIVE("ACTIVE", "Active"),
    
    /**
     * Loan is current (all payments up to date)
     */
    CURRENT("CURRENT", "Current"),
    
    /**
     * Loan payment is overdue but not yet in default
     */
    OVERDUE("OVERDUE", "Overdue"),
    
    /**
     * Loan is in arrears (significant overdue amount)
     */
    IN_ARREARS("IN_ARREARS", "In Arrears"),
    
    /**
     * Loan has defaulted
     */
    DEFAULTED("DEFAULTED", "Defaulted"),
    
    /**
     * Loan has been fully paid
     */
    CLOSED("CLOSED", "Closed/Paid Off"),
    
    /**
     * Loan has been written off as bad debt
     */
    WRITTEN_OFF("WRITTEN_OFF", "Written Off"),
    
    /**
     * Loan is under restructuring
     */
    RESTRUCTURED("RESTRUCTURED", "Restructured"),
    
    /**
     * Loan is suspended (e.g., due to dispute)
     */
    SUSPENDED("SUSPENDED", "Suspended"),
    
    /**
     * Loan account cancelled before disbursement
     */
    CANCELLED("CANCELLED", "Cancelled");

    private final String code;
    private final String displayName;

    LoanAccountStatus(String code, String displayName) {
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
    public static LoanAccountStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        for (LoanAccountStatus status : values()) {
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
     * Check if payments can be made against this loan
     */
    public boolean acceptsPayments() {
        return this == ACTIVE || this == CURRENT || this == OVERDUE || 
               this == IN_ARREARS || this == DEFAULTED;
    }

    /**
     * Check if this loan is considered performing
     */
    public boolean isPerforming() {
        return this == ACTIVE || this == CURRENT;
    }

    /**
     * Check if this loan is non-performing
     */
    public boolean isNonPerforming() {
        return this == OVERDUE || this == IN_ARREARS || this == DEFAULTED;
    }

    /**
     * Check if this is a terminal status (no further changes expected)
     */
    public boolean isTerminal() {
        return this == CLOSED || this == WRITTEN_OFF || this == CANCELLED;
    }

    /**
     * Check if disbursement is allowed from this status
     */
    public boolean canDisburse() {
        return this == INIT || this == READY_FOR_DISBURSEMENT;
    }

    /**
     * Get the appropriate status based on days overdue
     */
    public static LoanAccountStatus getStatusByDaysOverdue(int daysOverdue) {
        if (daysOverdue <= 0) {
            return CURRENT;
        } else if (daysOverdue <= 30) {
            return OVERDUE;
        } else if (daysOverdue <= 90) {
            return IN_ARREARS;
        } else {
            return DEFAULTED;
        }
    }

    @Override
    public String toString() {
        return code;
    }
}
