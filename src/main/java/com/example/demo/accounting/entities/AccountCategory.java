package com.example.demo.accounting.entities;

public enum AccountCategory {
    ASSET("Asset", "DEBIT"),
    LIABILITY("Liability", "CREDIT"),
    EQUITY("Equity", "CREDIT"),
    REVENUE("Revenue", "CREDIT"),
    EXPENSE("Expense", "DEBIT");

    private final String displayName;
    private final String normalBalance;

    AccountCategory(String displayName, String normalBalance) {
        this.displayName = displayName;
        this.normalBalance = normalBalance;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getNormalBalance() {
        return normalBalance;
    }
}
