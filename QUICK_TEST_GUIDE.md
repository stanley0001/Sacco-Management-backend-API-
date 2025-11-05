# 🧪 QUICK TESTING GUIDE - Payment Approval System

## 🚀 START TESTING IN 5 MINUTES

### Step 1: Start Backend
```bash
cd s:\code\PERSONAL\java\Sacco-Management-backend-API-
mvn spring-boot:run
```
**Wait for**: "Started DemoApplication"

### Step 2: Start Frontend
```bash
cd s:\code\PERSONAL\angular\Sacco-Management-Frontend-Angular-Portal-
ng serve
```
**Wait for**: "Compiled successfully"
**Open**: http://localhost:4200

---

## ✅ TEST SCENARIO 1: Manual Payment Approval (2 minutes)

### A. Create Manual Payment:
1. Log in to system
2. Click **"Payment Approvals"** in sidebar (new menu item)
3. Click **"New Payment"** button
4. Select any customer from dropdown
5. **Payment Method**: Select **"CASH"** or **"CHEQUE"**
6. **Amount**: Enter 5000
7. **Reference**: Enter "CASH001"
8. Click **"Submit Payment"**

**Expected Result**: 
- ✅ Alert: "Manual payment recorded successfully! Status: Awaiting Approval"
- ✅ Payment appears in pending list with yellow badge

### B. Approve Payment:
1. Find the payment in the table
2. Click green checkmark (✓) button
3. Confirm approval

**Expected Result**:
- ✅ Alert: "Payment approved and posted successfully!"
- ✅ **SMS should be sent to customer phone number**
- ✅ Payment disappears from pending list
- ✅ Account balance updated
- ✅ Check backend logs for: "SMS notification sent for approved payment"

### C. Verify SMS Sent:
**Check backend logs for**:
```
SMS notification sent for approved payment: {id} to {phone}
```

**SMS Message Format**:
```
Payment approved! Amount: KES 5,000.00. Type: SAVINGS_DEPOSIT. 
Method: CASH. Reference: CASH001. Your payment has been posted to your account.
```

---

## ✅ TEST SCENARIO 2: M-PESA Auto-Post (2 minutes)

### Create M-PESA Payment:
1. Go to **"Members"** menu
2. Click on any customer
3. Click **"Make Payment"** or **"Deposit"**
4. **Payment Method**: Select **"M-PESA"**
5. Enter phone number and amount
6. Click **"Send Payment Request"**
7. Enter M-PESA PIN on phone

**Expected Result**:
- ✅ STK Push sent to phone
- ✅ After PIN entry → **Automatically posts** (no approval needed)
- ✅ **SMS sent immediately**
- ✅ Does NOT appear in "Payment Approvals" page
- ✅ Balance updates instantly

**Key Difference**: M-PESA bypasses approval workflow completely!

---

## ✅ TEST SCENARIO 3: Bulk Approval (1 minute)

### Create Multiple Payments:
1. Create 3-4 manual payments (Cash/Cheque)
2. Go to **"Payment Approvals"**
3. Select checkboxes for multiple payments
4. Click **"Bulk Approval"** button
5. Click **"Approve All"**

**Expected Result**:
- ✅ All payments approved sequentially
- ✅ **SMS sent to each customer**
- ✅ Alert shows: "Bulk approval complete! ✅ Approved: 3 ❌ Failed: 0"
- ✅ All payments disappear from pending list

---

## ✅ TEST SCENARIO 4: Reject Payment (1 minute)

### Reject Manual Payment:
1. Create manual payment
2. Click red X button
3. Enter rejection reason: "Insufficient documentation"
4. Confirm

**Expected Result**:
- ✅ Payment marked as FAILED
- ✅ **NO SMS sent** (important!)
- ✅ Payment stays in list with red badge
- ✅ Rejection reason recorded

---

## ✅ TEST SCENARIO 5: Transaction Balance Verification (1 minute)

### Create Loan Payment:
1. Go to any client with active loan
2. Make a payment (any method)
3. Go to database and check `loan_transactions` table

**Expected Result**:
```sql
SELECT 
    transaction_id,
    amount,
    initial_balance,
    final_balance,
    account_number
FROM loan_transactions
ORDER BY transaction_id DESC
LIMIT 1;
```

**All fields should have values** (not NULL) ✅

---

## 🔍 VERIFICATION CHECKLIST

### UI Checks:
- [ ] "Payment Approvals" menu item visible in sidebar
- [ ] Menu icon shows verified/checkmark shield
- [ ] Page title: "Payment Approvals"
- [ ] Statistics cards display correctly
- [ ] Pending payments list loads
- [ ] Approve button works
- [ ] Reject button works
- [ ] Bulk select checkboxes work
- [ ] Search/filter works
- [ ] Professional glassmorphism design

### Backend Checks:
- [ ] POST /api/payments/approvals/create works
- [ ] GET /api/payments/approvals/pending returns data
- [ ] POST /api/payments/approvals/approve/{id} works
- [ ] POST /api/payments/approvals/reject/{id} works
- [ ] SMS sent ONLY after approval
- [ ] SMS NOT sent on rejection
- [ ] Transaction balance fields populated
- [ ] M-PESA still auto-posts

### Database Checks:
- [ ] `transaction_requests` table has entries with status AWAITING_APPROVAL
- [ ] After approval, status changes to POSTED_TO_ACCOUNT
- [ ] `posted_to_account` field set to true
- [ ] `processed_by` field has approver name
- [ ] `processed_at` timestamp populated
- [ ] `loan_transactions` has initial_balance, final_balance, amount

---

## 🚨 COMMON ISSUES & SOLUTIONS

### Issue: SMS Not Sending
**Check**:
1. SMS service configured in application.properties
2. Backend logs for SMS errors
3. Phone number format correct
4. SMS provider credentials valid

### Issue: Payment Not Appearing in List
**Check**:
1. Browser console for errors
2. Network tab for API calls
3. Backend endpoint /api/payments/approvals/pending
4. Payment status is AWAITING_APPROVAL

### Issue: M-PESA Still Goes to Approval
**Problem**: M-PESA should auto-post
**Check**: 
- Using correct endpoint: /api/payments/universal/process
- Not using /api/payments/approvals/create for M-PESA

### Issue: Transaction Balance Still NULL
**Check**:
1. Using LoanPaymentService.processLoanPayment()
2. Not using old deprecated methods
3. Backend restart after code changes

---

## 📊 SUCCESS CRITERIA

### All Tests Pass If:
1. ✅ Manual payments (Cash/Cheque/Bank) require approval
2. ✅ M-PESA payments auto-post without approval
3. ✅ SMS sent ONLY after manual payment approval
4. ✅ SMS sent immediately for M-PESA
5. ✅ Transaction balance fields populated
6. ✅ Navigation menu shows "Payment Approvals"
7. ✅ Bulk approve/reject works
8. ✅ UI is professional and responsive
9. ✅ No console errors in browser
10. ✅ No exceptions in backend logs

---

## 📞 NEED HELP?

### Check Backend Logs:
```
tail -f logs/application.log
```

### Check Frontend Console:
- F12 → Console tab
- Look for red errors

### Check Network:
- F12 → Network tab
- Filter: XHR
- Check API responses

### Database Queries:
```sql
-- Check pending approvals
SELECT * FROM transaction_requests 
WHERE status = 'AWAITING_APPROVAL' 
ORDER BY initiated_at DESC;

-- Check approved payments
SELECT * FROM transaction_requests 
WHERE status = 'POSTED_TO_ACCOUNT' 
ORDER BY posted_at DESC;

-- Check loan transactions
SELECT * FROM loan_transactions 
ORDER BY transaction_id DESC 
LIMIT 10;

-- Check SMS logs (if implemented)
SELECT * FROM sms_logs 
ORDER BY created_at DESC 
LIMIT 10;
```

---

## 🎉 TESTING COMPLETE!

If all scenarios pass:
- ✅ System is production ready
- ✅ Deploy to production
- ✅ Train users
- ✅ Monitor for 24 hours

**Congratulations! Payment approval system is fully operational!** 🚀
