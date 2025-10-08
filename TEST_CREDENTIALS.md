# Test User Credentials

## Default Test Users Created by DataSeeder

### ✅ Passwords Are Auto-Created by DataSeeder
The DataSeeder now automatically creates passwords for all test users!

### Admin User
- **Username:** `admin`
- **Email:** `admin@sacco.com`
- **Phone:** `254700000000`
- **Role:** ADMIN (Full system access)
- **Password:** `Admin@123` ✅
- **User ID:** Will be generated (typically ID: 1)

### Sample Users (Created by Seeder)
The seeder creates 20 additional users with random data:
- Each user is assigned a random role (ADMIN, MANAGER, TELLER, LOAN_OFFICER, ACCOUNTANT, or USER)
- Usernames are auto-generated in format: `{faker_username}{number}`
- 75% of users are marked as active
- **All users have password:** `Password@123` ✅

## How to Set Passwords

Since passwords are stored in a separate `Security` table, you need to create them after seeding:

### Using API (Recommended)
```bash
POST /users/createPassword
Content-Type: application/json

{
  "userId": "1",
  "password": "admin123",
  "isActive": true,
  "status": "ACTIVE",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31"
}
```

### Suggested Test Credentials

#### Admin Account
```json
{
  "userId": "1",
  "password": "Admin@123"
}
```

#### Manager Account
```json
{
  "userId": "2",
  "password": "Manager@123"
}
```

#### Teller Account
```json
{
  "userId": "3",
  "password": "Teller@123"
}
```

## Available Roles

The seeder creates the following roles:
1. **ADMIN** - Full system access
2. **MANAGER** - Branch management access
3. **TELLER** - Customer service and transactions
4. **LOAN_OFFICER** - Loan processing and approval
5. **ACCOUNTANT** - Financial reporting and reconciliation
6. **USER** - Basic user access

## Database Query to Get User Info

```sql
-- Get all users with their roles
SELECT u.id, u.user_name, u.email, u.phone, r.role_name 
FROM users u 
LEFT JOIN roles r ON u.role_id = CAST(r.id AS VARCHAR)
ORDER BY u.id
LIMIT 10;

-- Check if password exists for user
SELECT * FROM security WHERE user_id = '1';
```

## Notes

- All passwords are BCrypt encoded when stored
- The system uses Spring Security with BCrypt for authentication
- Users must have an entry in both `users` and `security` tables to login
- The seeder creates 150 customers, ~300 savings accounts, and numerous transactions
