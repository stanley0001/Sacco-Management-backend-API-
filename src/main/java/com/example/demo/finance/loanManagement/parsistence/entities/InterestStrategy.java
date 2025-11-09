package com.example.demo.finance.loanManagement.parsistence.entities;

/**
 * Interest calculation strategies for loan products
 */
public enum InterestStrategy {
    /**
     * Flat Rate: Interest calculated on original principal for entire term
     * Formula: Principal × Rate × Term
     * Example: 100,000 at 10% for 12 months = 10,000 interest total
     */
    FLAT_RATE("Flat Rate", "Interest on original principal only"),
    
    /**
     * Reducing Balance: Interest calculated on outstanding balance
     * Formula: Outstanding Balance × Rate × Time Period
     * Most common method for installment loans
     */
    REDUCING_BALANCE("Reducing Balance", "Interest on remaining balance (most common)"),
    
    /**
     * Declining Balance: Similar to reducing but monthly reduction
     * Formula: (Principal / Term) + (Outstanding × Rate)
     */
    DECLINING_BALANCE("Declining Balance", "Monthly declining principal method"),
    
    /**
     * Simple Interest: P × R × T / 100
     * Formula: Principal × Rate × Time / 100
     */
    SIMPLE_INTEREST("Simple Interest", "P × R × T / 100"),
    
    /**
     * Compound Interest: Interest on interest
     * Formula: P × (1 + R)^T - P
     * Rarely used for loans, more for savings
     */
    COMPOUND_INTEREST("Compound Interest", "Interest on interest"),
    
    /**
     * Add-On Interest: Interest added upfront
     * Total = Principal + (Principal × Rate × Term)
     * Then divided by number of payments
     */
    ADD_ON_INTEREST("Add-On Interest", "Interest added upfront to principal");
    
    private final String displayName;
    private final String description;
    
    InterestStrategy(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
