package com.example.demo.finance.loanManagement.enums;

/**
 * Centralized enum for Repayment Schedule statuses
 * Ensures consistent status values across the entire system
 */
public enum RepaymentScheduleStatus {
    /**
     * Future installment not yet due (numeric code: 5)
     */
    PENDING(5, "PENDING", "Pending"),
    
    /**
     * Current installment due now (numeric code: 0)
     */
    CURRENT(0, "CURRENT", "Current"),
    
    /**
     * Installment fully paid (numeric code: 1)
     */
    PAID(1, "PAID", "Paid"),
    
    /**
     * Installment partially paid
     */
    PARTIALLY_PAID(2, "PARTIALLY_PAID", "Partially Paid"),
    
    /**
     * Installment overdue (numeric code: 4)
     */
    OVERDUE(4, "OVERDUE", "Overdue"),
    
    /**
     * Installment in default (severely overdue)
     */
    DEFAULTED(6, "DEFAULTED", "Defaulted"),
    
    /**
     * Installment waived or written off
     */
    WAIVED(7, "WAIVED", "Waived");

    private final int numericCode;
    private final String code;
    private final String displayName;

    RepaymentScheduleStatus(int numericCode, String code, String displayName) {
        this.numericCode = numericCode;
        this.code = code;
        this.displayName = displayName;
    }

    public int getNumericCode() {
        return numericCode;
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
    public static RepaymentScheduleStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        for (RepaymentScheduleStatus status : values()) {
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
     * Get enum from numeric code (for backward compatibility)
     */
    public static RepaymentScheduleStatus fromNumericCode(int code) {
        for (RepaymentScheduleStatus status : values()) {
            if (status.numericCode == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if payment is accepted for this status
     */
    public boolean acceptsPayment() {
        return this == CURRENT || this == OVERDUE || this == PARTIALLY_PAID || this == DEFAULTED;
    }

    /**
     * Check if this installment is considered delinquent
     */
    public boolean isDelinquent() {
        return this == OVERDUE || this == DEFAULTED;
    }

    /**
     * Get the appropriate status based on payment and due date
     */
    public static RepaymentScheduleStatus getStatus(double amountPaid, double amountDue, 
                                                    java.time.LocalDate dueDate, 
                                                    java.time.LocalDate currentDate) {
        if (amountPaid >= amountDue) {
            return PAID;
        } else if (amountPaid > 0) {
            return PARTIALLY_PAID;
        } else if (currentDate.isBefore(dueDate)) {
            return PENDING;
        } else if (currentDate.isEqual(dueDate)) {
            return CURRENT;
        } else {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, currentDate);
            if (daysOverdue > 90) {
                return DEFAULTED;
            } else {
                return OVERDUE;
            }
        }
    }

    @Override
    public String toString() {
        return code;
    }
}
