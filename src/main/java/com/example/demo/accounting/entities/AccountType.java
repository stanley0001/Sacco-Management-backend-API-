package com.example.demo.accounting.entities;

public enum AccountType {
    // Asset Types
    CURRENT_ASSET("Current Asset", AccountCategory.ASSET),
    FIXED_ASSET("Fixed Asset", AccountCategory.ASSET),
    INTANGIBLE_ASSET("Intangible Asset", AccountCategory.ASSET),
    
    // Liability Types
    CURRENT_LIABILITY("Current Liability", AccountCategory.LIABILITY),
    LONG_TERM_LIABILITY("Long-term Liability", AccountCategory.LIABILITY),
    
    // Equity Types
    SHARE_CAPITAL("Share Capital", AccountCategory.EQUITY),
    RETAINED_EARNINGS("Retained Earnings", AccountCategory.EQUITY),
    
    // Revenue Types
    OPERATING_REVENUE("Operating Revenue", AccountCategory.REVENUE),
    NON_OPERATING_REVENUE("Non-operating Revenue", AccountCategory.REVENUE),
    
    // Expense Types
    OPERATING_EXPENSE("Operating Expense", AccountCategory.EXPENSE),
    ADMINISTRATIVE_EXPENSE("Administrative Expense", AccountCategory.EXPENSE),
    FINANCIAL_EXPENSE("Financial Expense", AccountCategory.EXPENSE);

    private final String displayName;
    private final AccountCategory category;

    AccountType(String displayName, AccountCategory category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AccountCategory getCategory() {
        return category;
    }
}
