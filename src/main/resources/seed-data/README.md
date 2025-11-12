# Seed Data CSV Files - User Guide

## Overview
This folder contains CSV files used to seed the database with initial data. You can edit these files directly to customize the seed data without modifying any Java code.

## File Structure

```
seed-data/
├── permissions.csv          # System permissions
├── roles.csv               # User roles
├── users.csv               # System users
├── products.csv            # Loan and savings products
├── expense-categories.csv  # Expense categories for accounting
├── asset-categories.csv    # Fixed asset categories
├── employees.csv          # Employee records
└── README.md              # This file
```

## CSV File Format

### General Rules
1. **First line is the header** - Column names that match entity fields
2. **Comments start with #** - Lines beginning with # are ignored
3. **Empty lines are skipped** - Blank lines are automatically ignored
4. **Quoted values supported** - Use quotes for values containing commas: `"value, with, commas"`
5. **Case-sensitive headers** - Column names must match exactly

### Data Types
- **Text**: Any string value (e.g., `John Doe`)
- **Numbers**: Integer or decimal (e.g., `100` or `14.5`)
- **Booleans**: `true`, `false`, `yes`, `no`, `1`, `0`
- **Empty values**: Leave blank or use empty quotes `""`

## File Descriptions

### 1. permissions.csv
**Purpose**: Defines all system permissions

**Columns**:
- `name` - Permission name/code (e.g., `ADMIN_ACCESS`)
- `value` - Permission value for backend (e.g., `ADMIN_ACCESS`)
- `description` - Human-readable description
- `status` - Permission status (usually `ACTIVE`)

**Example**:
```csv
name,value,description,status
ADMIN_ACCESS,ADMIN_ACCESS,Full administrative access,ACTIVE
LOAN_APPROVE,APPROVE_LOAN,Approve loan applications,ACTIVE
```

**Notes**:
- Do not delete critical permissions like `ADMIN_ACCESS`
- Comment out lines with `#` to temporarily disable permissions

---

### 2. roles.csv
**Purpose**: Defines user roles in the system

**Columns**:
- `roleName` - Role identifier (e.g., `ADMIN`, `MANAGER`)
- `description` - Role description

**Example**:
```csv
roleName,description
ADMIN,Full system access with all permissions
TELLER,Customer service and transaction processing
```

**Notes**:
- Role names should be UPPERCASE with underscores
- ADMIN role will get ALL permissions automatically

---

### 3. users.csv
**Purpose**: Creates initial system users

**Columns**:
- `firstName` - User's first name
- `lastName` - User's last name
- `otherName` - Middle name or other name
- `userName` - Login username (unique)
- `email` - Email address (unique)
- `phone` - Phone number (format: 254XXXXXXXXX)
- `documentNumber` - ID or passport number
- `roleName` - Role name (must exist in roles.csv)
- `password` - Plain text password (will be encrypted)
- `active` - Account status (`true` or `false`)

**Example**:
```csv
firstName,lastName,otherName,userName,email,phone,documentNumber,roleName,password,active
Admin,System,Super,admin,admin@sacco.com,254700000000,00000000,ADMIN,Admin@123,true
```

**Important Security Notes**:
- ⚠️ **Change default passwords** before production deployment
- Use strong passwords with uppercase, lowercase, numbers, and symbols
- Admin password should be changed immediately after first login

---

### 4. products.csv
**Purpose**: Loan and savings products configuration

**Columns**:
- `name` - Product name
- `code` - Product code (unique identifier)
- `transactionType` - `LOAN` or `SAVINGS`
- `term` - Loan/savings term duration
- `timeSpan` - `DAYS`, `MONTHS`, or `YEARS`
- `interest` - Interest rate (for monthly rates, multiply by 10, e.g., 14.4% = 144)
- `minLimit` - Minimum amount
- `maxLimit` - Maximum amount
- `eligibilityMultiplier` - Multiplier for eligibility (e.g., 3.0 = 3x savings)
- `interestStrategy` - `SIMPLE_INTEREST`, `REDUCING_BALANCE`, `FLAT_RATE`
- `interestType` - `PER_MONTH`, `PER_YEAR`, `ONCE_TOTAL`
- `mpesaEnabled` - M-PESA integration (`true`/`false`)
- `allowEarlyRepayment` - Allow early repayment (`true`/`false`)
- `active` - Product active status
- `description` - Product description
- `shareValue` - (Savings only) Value per share
- `minShares` - (Savings only) Minimum shares
- `maxShares` - (Savings only) Maximum shares
- `requireCheckOff` - (Loan only) Require check-off members
- `topUp` - (Loan only) Top-up enabled
- `autoPaymentEnabled` - Auto payment enabled
- `maxAge` - Maximum age for eligibility

**Example**:
```csv
name,code,transactionType,term,timeSpan,interest,minLimit,maxLimit,eligibilityMultiplier,interestStrategy,interestType,mpesaEnabled,allowEarlyRepayment,active,description
Normal Loan,NORM_LOAN,LOAN,108,MONTHS,14.4,1000,999999999,3.0,REDUCING_BALANCE,PER_MONTH,true,true,true,Standard loan with reducing balance
```

**Notes**:
- Interest rates for monthly calculations should be multiplied by 10 (e.g., 14.4% = 144)
- Leave optional columns empty if not applicable
- Comments can be added using `#` prefix

---

### 5. expense-categories.csv
**Purpose**: Expense categories for accounting module

**Columns**:
- `code` - Category code (e.g., `EXP-SAL`)
- `name` - Category name
- `description` - Category description
- `accountCode` - Chart of accounts code
- `budgetAmount` - Monthly budget amount
- `active` - Category active status

**Example**:
```csv
code,name,description,accountCode,budgetAmount,active
EXP-SAL,Salaries & Wages,Staff salaries and wages,5030,800000.0,true
EXP-RENT,Rent,Office rent and leases,5040,150000.0,true
```

---

### 6. asset-categories.csv
**Purpose**: Fixed asset categories

**Columns**:
- `code` - Asset category code
- `name` - Category name
- `description` - Description
- `accountCode` - GL account code
- `depreciationMethod` - `STRAIGHT_LINE` or `DECLINING_BALANCE`
- `depreciationRate` - Annual depreciation rate (%)
- `usefulLife` - Useful life in years
- `active` - Category status

**Example**:
```csv
code,name,description,accountCode,depreciationMethod,depreciationRate,usefulLife,active
ASSET-COMP,Computer Equipment,Computers and IT equipment,1052,STRAIGHT_LINE,25.0,4,true
```

---

### 7. employees.csv
**Purpose**: Employee records for payroll

**Columns**:
- `employeeCode` - Unique employee code
- `firstName`, `lastName`, `middleName` - Employee names
- `nationalId` - National ID number
- `phone` - Phone number
- `email` - Email address
- `position` - Job position
- `department` - Department name
- `basicSalary` - Basic monthly salary
- `housingAllowance` - Housing allowance
- `transportAllowance` - Transport allowance
- `otherAllowances` - Other allowances
- `bankName` - Bank name
- `bankBranch` - Bank branch
- `status` - Employment status (`ACTIVE`, `INACTIVE`, `SUSPENDED`)

**Example**:
```csv
employeeCode,firstName,lastName,middleName,nationalId,phone,email,position,department,basicSalary,housingAllowance,transportAllowance,otherAllowances,bankName,bankBranch,status
EMP001,John,Doe,Mwangi,12345678,0712345678,john.doe@company.com,Manager,Administration,80000.0,20000.0,10000.0,5000.0,KCB Bank,Nairobi Branch,ACTIVE
```

---

## Editing Guidelines

### Adding New Records
1. Open the CSV file in a text editor or Excel
2. Add a new line following the same format
3. Ensure all required columns have values
4. Save the file with UTF-8 encoding
5. Restart the application to load new data

### Modifying Existing Records
1. Locate the record you want to modify
2. Edit the values in the appropriate columns
3. Keep the header row unchanged
4. Save and restart application

### Removing Records
1. **Option 1**: Delete the entire line
2. **Option 2**: Comment out the line by adding `#` at the start
3. **Option 3**: Set `active` column to `false` (if available)

### Best Practices
- ✅ Always backup files before editing
- ✅ Use a text editor that preserves UTF-8 encoding
- ✅ Test changes in a development environment first
- ✅ Keep comments to document important records
- ✅ Validate data types (numbers, booleans) are correct
- ❌ Don't modify header row
- ❌ Don't leave required fields empty
- ❌ Don't use special characters in codes/identifiers

---

## Troubleshooting

### Data Not Loading
- **Check file location**: Ensure files are in `src/main/resources/seed-data/`
- **Check file encoding**: Files must be UTF-8 encoded
- **Check CSV format**: Ensure proper comma separation
- **Check logs**: Look for error messages in application logs

### Parsing Errors
- **Quoted values**: Use quotes for values containing commas
- **Line breaks**: Avoid line breaks within cell values
- **Special characters**: Ensure proper escaping of quotes (`""`)

### Duplicate Data Errors
- **Unique constraints**: Check for duplicate usernames, emails, codes
- **Database state**: Clear existing data if re-seeding
- **Skip check**: Seeder skips if data already exists (count > threshold)

---

## Application Behavior

### When Does Seeding Occur?
- Automatically on application startup
- Only if database tables are empty or have minimal records
- Check: `if (customerRepo.count() > 50)` - skips if already seeded

### Seeding Order
Data is seeded in dependency order:
1. Permissions (no dependencies)
2. Roles (no dependencies)
3. Role-Permission assignments
4. Users (depends on roles)
5. User passwords
6. Products
7. Expense categories
8. Asset categories
9. Employees

### Disabling Auto-Seed
To disable automatic seeding:
1. Comment out the `@Component` annotation in `DataSeeder.java`
2. Or set a high threshold in the count check

---

## Security Considerations

⚠️ **IMPORTANT**: 
- Change ALL default passwords before production
- Secure this seed data folder in production environments
- Remove or restrict access to default admin credentials
- Use strong password policies
- Rotate passwords regularly
- Limit permission assignments to principle of least privilege

---

## Support

For issues or questions:
1. Check application logs for detailed error messages
2. Validate CSV format using online CSV validators
3. Ensure all referenced entities exist (e.g., roles for users)
4. Contact system administrator or development team

---

**Last Updated**: November 2025  
**Version**: 1.0  
**System**: HelaSuite SACCO Management System
