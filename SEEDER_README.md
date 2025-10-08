# SACCO Data Seeder Documentation

## Overview
The `DataSeeder` component automatically populates your SACCO database with realistic test data using the JavaFaker library. This provides a comprehensive dataset for testing all functionalities of the system.

## What Gets Seeded

### 1. **Roles** (6 roles)
- ADMIN - Full system access
- MANAGER - Branch management access
- TELLER - Customer service and transactions
- LOAN_OFFICER - Loan processing and approval
- ACCOUNTANT - Financial reporting and reconciliation
- USER - Basic user access

### 2. **Users** (21 users)
- 1 Admin user (username: `admin`, email: `admin@sacco.com`)
- 20 random users with various roles
- Realistic names, emails, phone numbers, and ID numbers
- 75% active status

### 3. **Customers** (150 customers)
- Full personal details (first name, middle name, last name)
- Document information (ID, Passport, etc.)
- Date of birth (ages 18-70)
- Employment and salary information
- Next of kin details
- Contact information (phone, email, address)
- Account balances and status
- Branch assignments

### 4. **Loan Products** (8 products)
- Emergency Loan (30 days, 5% interest)
- Personal Loan (12 months, 10% interest)
- Business Loan (24 months, 12% interest)
- Asset Finance (36 months, 15% interest)
- Development Loan (60 months, 8% interest)
- Education Loan (48 months, 6% interest)
- Salary Advance (1 month, 3% interest)
- Agriculture Loan (18 months, 9% interest)

### 5. **Savings Products** (6 products)
- Regular Savings (3.5% interest)
- Premium Savings (6% interest)
- Junior Savings (4.5% interest)
- Fixed Deposit (8% interest)
- Business Savings (5% interest)
- Investment Account (7.5% interest)

### 6. **Savings Accounts** (~300 accounts)
- 1-3 accounts per customer
- Realistic balances based on product minimums
- Various statuses (Active, Dormant, Closed)
- Different account types (Regular, Fixed Deposit, Junior, Business)
- Transaction history

### 7. **Savings Transactions** (~6,000 transactions)
- 10-30 transactions per active account
- Transaction types: Deposits, Withdrawals, Interest, Fees
- Payment methods: Cash, M-Pesa, Bank Transfer, Cheque
- Realistic amounts and running balances
- Transaction dates spread over the past year

### 8. **Loan Applications** (~210 applications)
- 70% of customers have loan applications
- 1-2 applications per customer
- Various statuses: Pending, Approved, Processed, Rejected, Cancelled
- Realistic loan amounts within product limits
- Different disbursement types

### 9. **Loan Accounts** (~150 accounts)
- Created for approved/processed applications
- Various statuses: Active, Closed, Defaulted, Restructured
- Realistic payment histories
- Balance calculations based on status

### 10. **Loan Transactions** (~2,500 transactions)
- Disbursement transactions
- Multiple repayment transactions
- Payment methods: Cash, M-Pesa, Bank Transfer, Cheque, Standing Order
- Transaction dates aligned with loan terms

## How It Works

### Automatic Execution
The seeder implements `CommandLineRunner` and runs automatically when the Spring Boot application starts.

### Smart Detection
The seeder checks if data already exists:
```java
if (customerRepo.count() > 50) {
    logger.info("Data already seeded. Skipping seeder...");
    return;
}
```

This prevents duplicate data on application restarts.

## Usage

### 1. **First Time Setup**
Simply start your Spring Boot application:
```bash
mvn spring-boot:run
```

The seeder will automatically populate the database with test data.

### 2. **Re-seeding the Database**
To re-seed, you need to clear the existing data first:

**Option A: Drop and recreate the database**
```sql
DROP DATABASE sacco_db;
CREATE DATABASE sacco_db;
```

**Option B: Truncate all tables**
```sql
TRUNCATE TABLE savings_transactions CASCADE;
TRUNCATE TABLE loan_transactions CASCADE;
TRUNCATE TABLE savings_accounts CASCADE;
TRUNCATE TABLE loan_accounts CASCADE;
TRUNCATE TABLE loan_applications CASCADE;
TRUNCATE TABLE customers CASCADE;
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE roles CASCADE;
-- Add other tables as needed
```

Then restart the application.

### 3. **Disabling the Seeder**
If you want to disable the seeder temporarily, you can:

**Option A: Comment out the @Component annotation**
```java
// @Component
public class DataSeeder implements CommandLineRunner {
```

**Option B: Add a condition in application.properties**
Create a property:
```properties
seeder.enabled=false
```

Then modify the seeder:
```java
@Value("${seeder.enabled:true}")
private boolean seederEnabled;

@Override
public void run(String... args) {
    if (!seederEnabled) {
        return;
    }
    // ... rest of the code
}
```

## Data Characteristics

### Realistic Data
- Names generated using Faker library
- Valid Kenyan phone numbers (254 7XX XXX XXX format)
- Valid email addresses
- Realistic amounts and balances
- Proper date ranges and relationships

### Data Relationships
- All foreign keys properly maintained
- Accounts linked to valid customers
- Transactions linked to valid accounts
- Applications linked to valid products and customers

### Status Distribution
- **Active accounts**: ~75%
- **Approved loans**: ~60%
- **Payment compliance**: Varied (some defaulted, some completed)

## Expected Data Volumes

| Entity | Count |
|--------|-------|
| Roles | 6 |
| Users | 21 |
| Customers | 150 |
| Loan Products | 8 |
| Savings Products | 6 |
| Savings Accounts | ~300 |
| Savings Transactions | ~6,000 |
| Loan Applications | ~210 |
| Loan Accounts | ~150 |
| Loan Transactions | ~2,500 |

**Total Records**: ~9,361 records

## Performance

The seeding process typically takes:
- **Small dataset (current)**: 30-60 seconds
- **Database**: PostgreSQL recommended for optimal performance

## Troubleshooting

### Issue: "Duplicate key violation"
**Solution**: Clear the database before re-running the seeder.

### Issue: "Foreign key constraint violation"
**Solution**: Ensure the seeding order is maintained (roles → users → customers → products → accounts → transactions).

### Issue: Seeder not running
**Solution**: 
1. Check that `@Component` annotation is present
2. Verify Spring Boot is scanning the package
3. Check application logs for errors

## Customization

To customize the data volumes, edit the following in `DataSeeder.java`:

```java
// Number of customers
for (int i = 0; i < 150; i++) { // Change 150 to desired number

// Number of users
for (int i = 0; i < 20; i++) { // Change 20 to desired number

// Accounts per customer
int numAccounts = random.nextInt(3) + 1; // Change formula

// Transactions per account
int numTransactions = random.nextInt(21) + 10; // Change range
```

## Dependencies

The seeder requires:
- **JavaFaker**: `net.datafaker:datafaker:2.0.2`
- **Spring Boot**: 3.2.5 or higher
- **Spring Data JPA**: For repository access
- **PostgreSQL**: Recommended database

## Notes

- The seeder uses `@Transactional` to ensure data integrity
- All dates are randomized within realistic ranges
- Customer phone numbers and emails are unique
- Account numbers are sequential and unique
- The admin user password is not set by the seeder (handle separately in your authentication setup)

## Next Steps

After seeding:
1. Test the dashboard to see populated statistics
2. Test search and filter functionalities
3. Test transaction processing with existing accounts
4. Generate reports with the seeded data
5. Test loan approval workflows

## Support

For issues or customization requests, refer to the main project documentation or contact the development team.
