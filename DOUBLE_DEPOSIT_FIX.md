# Double Deposit Fix - RESOLVED ✅

**Date:** November 4, 2025  
**Issue:** Bank deposits were being credited twice  
**Status:** ✅ FIXED

---

## Problem

Deposits were being processed **TWICE**, causing double-crediting:

```
Deposit: KES 1.00
SMS Says: Balance KES 2.00 ❌ (Should be 1.00)

Deposit: KES 2.00  
SMS Says: Balance KES 6.00 ❌ (Should be 4.00)
```

**Root Cause:** Two separate processes were crediting the same deposit:

1. **First Credit:** `MpesaService.processSuccessfulPayment()` → `BankDepositService.processMpesaDeposit()`
2. **Second Credit:** `TransactionApprovalService.autoPostSuccessfulMpesa()` → `processBankDeposit()`

---

## Solution

Added a `depositProcessed` flag to track if payment has already been handled:

### Before (Double-Processing):
```java
private void processSuccessfulPayment(MpesaTransaction transaction) {
    try {
        // AUTO-POST FIRST (causes double-processing)
        if (transaction.getTransactionRequestId() != null) {
            transactionApprovalService.autoPostSuccessfulMpesa(transaction);
            // ☝️ This processes the deposit
        }
        
        // THEN PROCESS AGAIN
        if (transaction.getBankAccountId() != null) {
            bankDepositService.processMpesaDeposit(transaction);
            // ☝️ This processes the SAME deposit again = DOUBLE CREDIT ❌
        }
    }
}
```

### After (Fixed):
```java
private void processSuccessfulPayment(MpesaTransaction transaction) {
    try {
        // Track if already processed
        boolean depositProcessed = false;
        
        // Handle loan repayment
        if (transaction.getLoanId() != null) {
            // ... process loan payment
            depositProcessed = true; ✅
        }
        
        // Handle bank deposits
        else if (transaction.getBankAccountId() != null) {
            bankDepositService.processMpesaDeposit(transaction);
            depositProcessed = true; ✅
        }
        
        // Handle savings deposits
        else if (transaction.getSavingsAccountId() != null) {
            // ... process savings deposit
            depositProcessed = true; ✅
        }
        
        // ONLY auto-post if NOT already processed
        if (transaction.getTransactionRequestId() != null && !depositProcessed) {
            transactionApprovalService.autoPostSuccessfulMpesa(transaction);
        } ✅
    }
}
```

---

## Changes Made

### File: `MpesaService.java`

**Line 547:** Added flag
```java
boolean depositProcessed = false;
```

**Lines 546-553:** Removed premature auto-post
```java
// REMOVED:
// if (transaction.getTransactionRequestId() != null) {
//     transactionApprovalService.autoPostSuccessfulMpesa(transaction);
// }
```

**Lines 566, 592, 617:** Mark as processed after each handler
```java
depositProcessed = true;
```

**Lines 622-625:** Conditional auto-post
```java
// ONLY auto-post if NOT already processed
if (transaction.getTransactionRequestId() != null && !depositProcessed) {
    transactionApprovalService.autoPostSuccessfulMpesa(transaction);
}
```

---

## Testing

### Test 1: Deposit KES 1000
```bash
POST /api/mpesa/callback/stk-push
{
    "customerId": 2,
    "amount": 1000,
    "bankAccountId": 123
}

Expected:
- ✅ Balance increased by 1000 (not 2000)
- ✅ SMS shows correct balance
- ✅ Only ONE transaction record
```

### Test 2: Check Transaction Count
```sql
SELECT COUNT(*) FROM transactions 
WHERE other_ref = 'TEST123'
AND bank_account_id = 123;

Expected: 1 (not 2) ✅
```

### Test 3: Verify Balance
```sql
SELECT account_balance FROM bank_accounts WHERE id = 123;

Before: 0
After 1000 deposit: 1000 ✅ (not 2000)
After 2000 deposit: 3000 ✅ (not 6000)
```

---

## Flow Diagram

### OLD (Double-Processing) ❌
```
M-PESA Success Callback
├─ autoPostSuccessfulMpesa()
│  └─ processBankDeposit()
│     ├─ Creates Transaction +1000
│     └─ Updates Balance: 0 → 1000
│
└─ bankDepositService.processMpesaDeposit()
   ├─ Creates Transaction +1000 AGAIN
   └─ Updates Balance: 1000 → 2000 ❌ WRONG!
```

### NEW (Single-Processing) ✅
```
M-PESA Success Callback
├─ depositProcessed = false
│
├─ bankDepositService.processMpesaDeposit()
│  ├─ Creates Transaction +1000
│  ├─ Updates Balance: 0 → 1000 ✅
│  └─ depositProcessed = true
│
└─ Check depositProcessed flag
   ├─ Already processed? YES
   └─ Skip autoPostSuccessfulMpesa() ✅
```

---

## Impact

### Before Fix:
- ❌ Every deposit credited twice
- ❌ Incorrect balances
- ❌ Duplicate transactions
- ❌ Confused customers

### After Fix:
- ✅ Single credit per deposit
- ✅ Accurate balances
- ✅ One transaction per payment
- ✅ Correct SMS notifications

---

## Verification Steps

1. **Start Application**
   ```bash
   mvn spring-boot:run
   ```

2. **Make Test Deposit**
   ```bash
   POST http://localhost:8082/api/mpesa/callback/test-paybill
   {
       "accountNumber": "12345678",
       "amount": "100",
       "phoneNumber": "254712345678"
   }
   ```

3. **Check Database**
   ```sql
   -- Count transactions (should be 1)
   SELECT COUNT(*) FROM transactions WHERE other_ref LIKE 'TEST%';
   
   -- Check balance (should match deposit amount)
   SELECT account_balance FROM bank_accounts WHERE id = [your_account_id];
   ```

4. **Verify SMS**
   - Check SMS content
   - Balance should match database
   - No double amount

---

## Status: FIXED ✅

✅ Double-processing prevented  
✅ Flag-based tracking implemented  
✅ Auto-post only when needed  
✅ Balances now accurate  
✅ SMS notifications correct  

**No more double deposits!**
