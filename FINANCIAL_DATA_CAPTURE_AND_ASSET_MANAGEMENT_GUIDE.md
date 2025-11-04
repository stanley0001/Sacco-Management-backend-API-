# 💰 Professional Financial Data Capture & Asset Management System

## 🎯 Overview

This comprehensive system provides **professional financial data capture** and **asset-based financing** capabilities for your SACCO Management System.

### **Two Major Systems Added:**

1. **📊 Accounting System** - Double-entry bookkeeping with Chart of Accounts and Journal Entries
2. **🏢 Asset Management** - Complete asset tracking for asset-based financing

---

## 📊 ACCOUNTING SYSTEM

### **Features:**

✅ **Chart of Accounts (COA)**
- Hierarchical account structure
- 5 account types: Assets, Liabilities, Equity, Revenue, Expenses
- Multiple account categories
- Normal balance tracking (Debit/Credit)
- Parent-child relationships
- System account protection

✅ **Journal Entries**
- Double-entry bookkeeping
- Automatic balance verification
- Multiple journal types
- Draft, Posted, Approved, Reversed status workflow
- Automatic account balance updates
- Reversal functionality with audit trail

---

### **1. CHART OF ACCOUNTS**

#### **Entity: ChartOfAccounts**

```java
public class ChartOfAccounts {
    Long id;
    String accountCode;        // e.g., "1010", "2020"
    String accountName;        // e.g., "Cash", "Bank Accounts"
    AccountType accountType;   // ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    AccountCategory category;  // CURRENT_ASSET, FIXED_ASSET, etc.
    String parentAccountCode;  // For hierarchical structure
    Boolean isActive;
    Boolean isSystemAccount;   // Cannot be deleted
    Double currentBalance;
    NormalBalance normalBalance; // DEBIT or CREDIT
}
```

#### **Account Types & Categories:**

**ASSETS (Normal Balance: DEBIT)**
- Current Assets
- Fixed Assets
- Intangible Assets
- Other Assets

**LIABILITIES (Normal Balance: CREDIT)**
- Current Liabilities
- Long-term Liabilities
- Other Liabilities

**EQUITY (Normal Balance: CREDIT)**
- Capital
- Retained Earnings
- Drawings

**REVENUE (Normal Balance: CREDIT)**
- Operating Revenue
- Non-operating Revenue

**EXPENSES (Normal Balance: DEBIT)**
- Operating Expenses
- Administrative Expenses
- Financial Expenses
- Other Expenses

---

### **2. JOURNAL ENTRIES**

#### **Entity: JournalEntry**

```java
public class JournalEntry {
    Long id;
    String journalNumber;      // Auto-generated or manual
    LocalDate transactionDate;
    String description;
    String reference;          // Invoice #, Receipt #, etc.
    JournalType journalType;
    JournalStatus status;      // DRAFT, POSTED, APPROVED, REVERSED
    List<JournalEntryLine> lines; // Debit and Credit lines
    Double totalDebit;
    Double totalCredit;
    Boolean isBalanced;        // Must be balanced to post
}
```

#### **Journal Entry Line**

```java
public class JournalEntryLine {
    String accountCode;
    EntryType type;           // DEBIT or CREDIT
    Double amount;
    String description;
    Integer lineNumber;
}
```

#### **Journal Types:**
- **GENERAL** - General journal entries
- **SALES** - Sales transactions
- **PURCHASES** - Purchase transactions
- **CASH_RECEIPTS** - Cash received
- **CASH_PAYMENTS** - Cash paid
- **LOAN_DISBURSEMENT** - Loan disbursement
- **LOAN_REPAYMENT** - Loan repayment
- **DEPOSIT** - Member deposits
- **WITHDRAWAL** - Member withdrawals
- **ADJUSTMENT** - Adjusting entries
- **CLOSING** - Closing entries

---

### **API ENDPOINTS - Accounting**

#### **Chart of Accounts:**

```
POST   /api/accounting/accounts              Create new account
PUT    /api/accounting/accounts/{id}         Update account
GET    /api/accounting/accounts               Get all accounts
GET    /api/accounting/accounts/active        Get active accounts
GET    /api/accounting/accounts/{code}        Get account by code
GET    /api/accounting/accounts/type/{type}   Get accounts by type
POST   /api/accounting/accounts/initialize    Initialize standard COA
```

#### **Journal Entries:**

```
POST   /api/accounting/journal-entries                Create journal entry
POST   /api/accounting/journal-entries/{id}/post      Post entry to ledger
POST   /api/accounting/journal-entries/{id}/approve   Approve entry
POST   /api/accounting/journal-entries/{id}/reverse   Reverse entry
GET    /api/accounting/journal-entries                Get entries by date range
GET    /api/accounting/journal-entries/status/{status} Get entries by status
GET    /api/accounting/journal-entries/{id}           Get entry by ID
```

---

### **USAGE EXAMPLES - Accounting**

#### **1. Initialize Standard Chart of Accounts**

```bash
POST /api/accounting/accounts/initialize
```

**Response:** Creates standard SACCO accounts:
- 1000 - Assets
- 1010 - Cash
- 1020 - Bank Accounts
- 1030 - Loans Receivable
- 1040 - Interest Receivable
- 2000 - Liabilities
- 2010 - Member Deposits
- 2020 - Member Savings
- 3000 - Equity
- 4000 - Revenue
- 5000 - Expenses

#### **2. Create Custom Account**

```json
POST /api/accounting/accounts
{
  "accountCode": "1050",
  "accountName": "Petty Cash",
  "accountType": "ASSET",
  "accountCategory": "CURRENT_ASSET",
  "parentAccountCode": "1010",
  "normalBalance": "DEBIT",
  "isActive": true,
  "description": "Petty cash for daily expenses"
}
```

#### **3. Record Loan Disbursement**

```json
POST /api/accounting/journal-entries
{
  "transactionDate": "2025-10-19",
  "description": "Loan disbursement to John Doe",
  "reference": "LOAN-12345",
  "journalType": "LOAN_DISBURSEMENT",
  "lines": [
    {
      "accountCode": "1030",
      "type": "DEBIT",
      "amount": 100000.00,
      "description": "Loan disbursed",
      "lineNumber": 1
    },
    {
      "accountCode": "1020",
      "type": "CREDIT",
      "amount": 100000.00,
      "description": "Cash paid out",
      "lineNumber": 2
    }
  ]
}
```

#### **4. Record Loan Repayment with Interest**

```json
POST /api/accounting/journal-entries
{
  "transactionDate": "2025-10-19",
  "description": "Loan repayment from John Doe",
  "reference": "PAYMENT-67890",
  "journalType": "LOAN_REPAYMENT",
  "lines": [
    {
      "accountCode": "1020",
      "type": "DEBIT",
      "amount": 12000.00,
      "description": "Payment received",
      "lineNumber": 1
    },
    {
      "accountCode": "1030",
      "type": "CREDIT",
      "amount": 10000.00,
      "description": "Principal repayment",
      "lineNumber": 2
    },
    {
      "accountCode": "4010",
      "type": "CREDIT",
      "amount": 2000.00,
      "description": "Interest income",
      "lineNumber": 3
    }
  ]
}
```

---

## 🏢 ASSET MANAGEMENT SYSTEM

### **Features:**

✅ **Complete Asset Registry**
- Detailed asset tracking
- Multiple asset types and categories
- Owner tracking
- Serial numbers and registration

✅ **Asset Valuation**
- Purchase price
- Current value
- Market value
- Depreciation tracking
- Professional valuations

✅ **Collateral Management**
- Pledge assets for loans
- Loan-to-Value (LTV) ratio calculation
- Collateral status tracking
- Release management

✅ **Depreciation Management**
- Multiple depreciation methods
- Automatic calculations
- Book value tracking
- Useful life management

✅ **Asset Maintenance**
- Inspection scheduling
- Insurance tracking
- Condition monitoring
- Document management

---

### **ENTITY: Asset**

```java
public class Asset {
    Long id;
    String assetNumber;        // Unique identifier
    String assetName;
    AssetType assetType;       // TANGIBLE, INTANGIBLE, FINANCIAL
    AssetCategory category;    // LAND, VEHICLE, EQUIPMENT, etc.
    
    // Ownership
    Long ownerId;              // Customer ID
    String ownerName;
    String ownerType;          // MEMBER, SACCO, THIRD_PARTY
    
    // Valuation
    Double purchasePrice;
    Double currentValue;
    Double marketValue;
    LocalDate valuationDate;
    String valuedBy;
    
    // Depreciation
    DepreciationMethod method; // STRAIGHT_LINE, DECLINING_BALANCE, etc.
    Integer usefulLifeYears;
    Double depreciationRate;
    Double salvageValue;
    Double accumulatedDepreciation;
    Double bookValue;
    
    // Physical details
    String serialNumber;
    String registrationNumber;
    String make;
    String model;
    String yearOfManufacture;
    String condition;          // EXCELLENT, GOOD, FAIR, POOR
    
    // Location
    String location;
    String address;
    String gpsCoordinates;
    
    // Insurance
    String insuranceCompany;
    String insurancePolicyNumber;
    Double insuranceValue;
    LocalDate insuranceExpiryDate;
    
    // Collateral tracking
    CollateralStatus collateralStatus; // AVAILABLE, PLEDGED, RELEASED
    Long linkedLoanAccountId;
    String loanAccountNumber;
    Double loanToValueRatio;   // Percentage
    
    // Status
    AssetStatus status;        // ACTIVE, DISPOSED, STOLEN, etc.
}
```

---

### **Asset Categories:**

#### **Tangible Assets:**
- **LAND** - Real estate property
- **BUILDING** - Structures, houses
- **VEHICLE** - Cars, trucks, motorcycles
- **MACHINERY** - Industrial equipment
- **EQUIPMENT** - Tools, devices
- **FURNITURE** - Office furniture
- **ELECTRONICS** - Computers, phones
- **INVENTORY** - Stock, goods
- **LIVESTOCK** - Animals

#### **Intangible Assets:**
- **GOODWILL** - Business goodwill
- **PATENTS** - Patents, copyrights
- **TRADEMARKS** - Brands
- **SOFTWARE** - Software licenses

#### **Financial Assets:**
- **SHARES** - Company shares
- **BONDS** - Bonds, securities
- **TREASURY_BILLS** - Government securities
- **FIXED_DEPOSITS** - Term deposits

---

### **Depreciation Methods:**

1. **STRAIGHT_LINE** - Equal depreciation each year
2. **DECLINING_BALANCE** - Percentage of book value
3. **DOUBLE_DECLINING_BALANCE** - Double the straight-line rate
4. **UNITS_OF_PRODUCTION** - Based on usage
5. **SUM_OF_YEARS_DIGITS** - Accelerated depreciation
6. **NONE** - For land and intangible assets

---

### **API ENDPOINTS - Assets**

```
POST   /api/assets                          Register new asset
PUT    /api/assets/{id}                     Update asset
GET    /api/assets                          Get all assets
GET    /api/assets/{id}                     Get asset by ID
GET    /api/assets/number/{assetNumber}    Get by asset number
GET    /api/assets/owner/{ownerId}         Get assets by owner
GET    /api/assets/owner/{ownerId}/value   Get total asset value

POST   /api/assets/{id}/valuation          Update asset valuation
POST   /api/assets/{id}/pledge             Pledge as collateral
POST   /api/assets/{id}/release            Release from collateral
POST   /api/assets/{id}/depreciation       Calculate depreciation
POST   /api/assets/{id}/inspection         Record inspection
POST   /api/assets/{id}/dispose            Dispose asset

GET    /api/assets/available-collateral    Get available for collateral
GET    /api/assets/loan/{loanAccountId}    Get assets for loan
GET    /api/assets/search?searchTerm=      Search assets
GET    /api/assets/insurance-expiring?days= Get insurance expiring
GET    /api/assets/inspections-due         Get inspections due
```

---

### **USAGE EXAMPLES - Assets**

#### **1. Register Vehicle as Asset**

```json
POST /api/assets
{
  "assetName": "Toyota Land Cruiser V8",
  "assetType": "TANGIBLE",
  "assetCategory": "VEHICLE",
  "ownerId": 123,
  "ownerName": "John Doe",
  "ownerType": "MEMBER",
  "purchasePrice": 5000000.00,
  "purchaseDate": "2020-01-15",
  "currentValue": 5000000.00,
  "depreciationMethod": "DECLINING_BALANCE",
  "usefulLifeYears": 10,
  "depreciationRate": 20.0,
  "salvageValue": 500000.00,
  "make": "Toyota",
  "model": "Land Cruiser V8",
  "yearOfManufacture": "2019",
  "serialNumber": "JT164W1B6K0123456",
  "registrationNumber": "KAA 123X",
  "condition": "EXCELLENT",
  "location": "Nairobi, Kenya",
  "insuranceCompany": "AAR Insurance",
  "insurancePolicyNumber": "AAR/2024/12345",
  "insuranceValue": 6000000.00,
  "insuranceExpiryDate": "2025-12-31",
  "status": "ACTIVE"
}
```

#### **2. Pledge Asset as Collateral**

```json
POST /api/assets/{assetId}/pledge
{
  "loanAccountId": 456,
  "loanAccountNumber": "LOAN-2024-001",
  "loanAmount": 3000000.00
}
```

**Response includes:**
- LTV Ratio: 60% (3M loan / 5M asset value)
- Collateral Status changed to "PLEDGED"

#### **3. Update Asset Valuation**

```json
POST /api/assets/{assetId}/valuation
{
  "marketValue": 4500000.00,
  "valuationDate": "2025-10-19"
}
```

#### **4. Calculate Depreciation**

```bash
POST /api/assets/{assetId}/depreciation
```

**Automatic calculation based on:**
- Purchase price
- Purchase date
- Depreciation method
- Useful life
- Salvage value

#### **5. Record Asset Inspection**

```json
POST /api/assets/{assetId}/inspection
{
  "condition": "GOOD",
  "notes": "Regular maintenance completed. Minor scratches on body. Engine in excellent condition."
}
```

#### **6. Get Available Collateral**

```bash
GET /api/assets/available-collateral
```

**Returns:** All active assets with status "AVAILABLE"

#### **7. Get Total Asset Value for Member**

```bash
GET /api/assets/owner/123/value
```

**Response:**
```json
{
  "ownerId": 123,
  "totalAssetValue": 15000000.00,
  "currency": "KES"
}
```

---

## 🔄 INTEGRATED WORKFLOWS

### **Workflow 1: Asset-Based Loan Processing**

1. **Member registers asset**
   ```
   POST /api/assets
   ```

2. **Asset gets appraised**
   ```
   POST /api/assets/{id}/valuation
   ```

3. **Member applies for loan**
   - System checks available collateral
   - Calculates maximum loan amount (typically 70-80% of asset value)

4. **Loan is approved and asset is pledged**
   ```
   POST /api/assets/{id}/pledge
   ```

5. **Loan disbursement is recorded**
   ```
   POST /api/accounting/journal-entries
   ```

6. **Loan is repaid and asset is released**
   ```
   POST /api/assets/{id}/release
   ```

---

### **Workflow 2: Monthly Financial Close**

1. **Record all transactions** (Loan disbursements, repayments, expenses)
   ```
   POST /api/accounting/journal-entries
   ```

2. **Post all draft entries**
   ```
   POST /api/accounting/journal-entries/{id}/post
   ```

3. **Calculate depreciation for all assets**
   ```
   POST /api/assets/{id}/depreciation
   ```

4. **Record depreciation**
   ```
   POST /api/accounting/journal-entries
   {
     "journalType": "ADJUSTMENT",
     "lines": [
       {"accountCode": "5030", "type": "DEBIT", "amount": 50000, "description": "Depreciation expense"},
       {"accountCode": "1051", "type": "CREDIT", "amount": 50000, "description": "Accumulated depreciation"}
     ]
   }
   ```

5. **Generate financial reports**
   ```
   GET /api/financial-reports/balance-sheet
   GET /api/financial-reports/profit-loss
   ```

---

## 📋 DATABASE TABLES CREATED

### **Accounting:**
- `chart_of_accounts` - Account structure
- `journal_entries` - Transaction headers
- `journal_entry_lines` - Transaction details (debits/credits)

### **Assets:**
- `assets` - Asset registry

---

## 🎯 BENEFITS

### **For Accounting:**
✅ Professional double-entry bookkeeping  
✅ Automatic balance updates  
✅ Audit trail with reversal functionality  
✅ Multiple journal types for different transactions  
✅ Supports complex financial operations  
✅ Ready for financial reporting  

### **For Asset Management:**
✅ Complete asset registry  
✅ Professional collateral management  
✅ Automatic depreciation calculation  
✅ Insurance and inspection tracking  
✅ Supports asset-based financing  
✅ Risk management through LTV tracking  

---

## 🚀 GETTING STARTED

### **Step 1: Initialize Chart of Accounts**
```bash
POST /api/accounting/accounts/initialize
```

### **Step 2: Add Custom Accounts** (if needed)
```bash
POST /api/accounting/accounts
```

### **Step 3: Start Recording Transactions**
```bash
POST /api/accounting/journal-entries
```

### **Step 4: Register Assets**
```bash
POST /api/assets
```

### **Step 5: Use Assets for Financing**
```bash
POST /api/assets/{id}/pledge
```

---

## 📊 REPORTING

Once data is captured, use existing endpoints:

```
GET /api/financial-reports/balance-sheet
GET /api/financial-reports/profit-loss
GET /api/financial-reports/trial-balance
```

All account balances are automatically updated from journal entries!

---

## ✅ PRODUCTION READY

Both systems are:
- ✅ **Transaction-safe** with @Transactional annotations
- ✅ **Validated** with automatic balance checking
- ✅ **Audited** with created/updated timestamps
- ✅ **Secured** with authentication integration
- ✅ **Documented** with Swagger/OpenAPI
- ✅ **Professional** following accounting standards

---

**Created:** October 19, 2025  
**Status:** ✅ **PRODUCTION READY - Accounting & Asset Management Systems Complete!**  
**Next:** Create Frontend UI for data capture
