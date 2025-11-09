# 🎯 AUTO LOAN DEDUCTION - COMPLETE IMPLEMENTATION GUIDE

## ✅ COMPLETED FEATURES

### **Backend Implementation** ✅

#### **1. Database Entities**
- ✅ `GlobalConfig.java` - System-wide configuration storage
- ✅ `LoanStandingOrder.java` - Standing order configuration per customer/loan

#### **2. Repositories**
- ✅ `GlobalConfigRepository.java` - Config CRUD operations
- ✅ `LoanStandingOrderRepository.java` - Standing order queries

#### **3. Services**
- ✅ `AutoLoanDeductionService.java` - Main deduction logic

#### **4. Controllers**
- ✅ `GlobalConfigController.java` - Global settings API
- ✅ `LoanStandingOrderController.java` - Standing order management API

---

## 🎯 **HOW IT WORKS**

### **Automatic Loan Deduction Flow**

```
1. Customer Deposits Money (M-PESA, Bank, Cash)
   ↓
2. Transaction Processed → Account Credited
   ↓
3. Auto Deduction Service Triggered
   ↓
4. Check: Is Auto Deduction Enabled Globally? 
   └─ NO → Skip deduction
   └─ YES → Continue
   ↓
5. Find Active Standing Orders for Customer
   ↓
6. For Each Standing Order:
   ├─ Check if loan is still active
   ├─ Calculate deduction amount based on type
   ├─ Verify minimum balance maintained
   ├─ Process loan payment
   └─ Send SMS notification
   ↓
7. Deduction Complete → Customer Notified
```

---

## 📊 **DEDUCTION TYPES**

### **1. Fixed Amount** 
```
Example: Deduct KES 5,000 per deposit
Use Case: Regular fixed installments
```

### **2. Percentage**
```
Example: Deduct 50% of every deposit
Calculation: If deposit = KES 10,000 → Deduct KES 5,000
Use Case: Variable income customers
```

### **3. Full Installment**
```
Example: Deduct exact next installment amount
Calculation: Fetches from repayment schedule
Use Case: Salary-based deductions
```

### **4. Outstanding Balance**
```
Example: Clear entire remaining loan balance
Calculation: Deducts full outstanding amount
Use Case: Loan closure
```

---

## 🔧 **CONFIGURATION OPTIONS**

### **Global Settings** (Apply to All)

| Config Key | Type | Default | Description |
|-----------|------|---------|-------------|
| `AUTO_LOAN_DEDUCTION_ENABLED` | Boolean | false | Master switch for auto deduction |
| `DEFAULT_DEDUCTION_PERCENTAGE` | Decimal | 50 | Default deduction % (0-100) |
| `MINIMUM_BALANCE_AFTER_DEDUCTION` | Decimal | 100 | Minimum KES to keep in account |

### **Per-Customer Standing Order Settings**

| Field | Type | Description |
|-------|------|-------------|
| `deductionType` | Enum | FIXED_AMOUNT, PERCENTAGE, FULL_INSTALLMENT, OUTSTANDING_BALANCE |
| `deductionAmount` | Decimal | Fixed amount to deduct (for FIXED_AMOUNT type) |
| `deductionPercentage` | Decimal | Percentage to deduct (for PERCENTAGE type) |
| `minimumBalance` | Decimal | Customer-specific minimum balance |
| `maximumDeduction` | Decimal | Cap on deduction amount |
| `triggerType` | Enum | ON_DEPOSIT, ON_SALARY, ON_MPESA, ON_ANY_CREDIT |
| `sendSmsNotification` | Boolean | Send SMS on each deduction |

---

## 🔌 **API ENDPOINTS**

### **Global Configuration APIs**

```http
# Get all configurations
GET /api/global-config

# Get active configurations
GET /api/global-config/active

# Get by category
GET /api/global-config/category/{category}

# Get by key
GET /api/global-config/key/{key}

# Create configuration
POST /api/global-config
Body: {
  "configKey": "AUTO_LOAN_DEDUCTION_ENABLED",
  "configName": "Auto Loan Deduction",
  "description": "Enable automatic loan deduction",
  "configValue": "true",
  "configType": "BOOLEAN",
  "category": "STANDING_ORDER",
  "isActive": true
}

# Update configuration
PUT /api/global-config/{id}

# Update configuration value by key
PUT /api/global-config/key/{key}/value
Body: {
  "value": "true",
  "updatedBy": "admin"
}

# Initialize default configurations
POST /api/global-config/init-defaults
```

### **Standing Order APIs**

```http
# Get customer's standing orders
GET /api/loan-standing-orders/customer/{customerId}

# Create standing order
POST /api/loan-standing-orders
Body: {
  "customerId": 123,
  "loanAccountId": 456,
  "savingsAccountId": 789,
  "deductionType": "PERCENTAGE",
  "amount": null,
  "percentage": 50.00,
  "createdBy": "admin"
}

# Deactivate standing order
DELETE /api/loan-standing-orders/{id}?updatedBy=admin

# Check if auto deduction is enabled
GET /api/loan-standing-orders/status
```

---

## 💻 **INTEGRATION POINTS**

### **Where to Call Auto Deduction**

#### **1. M-PESA C2B Payments**
```java
// In C2BPaymentProcessingService.java
after processing payment:
autoLoanDeductionService.processAutoDeduction(
    customerId,
    savingsAccountId,
    depositAmount,
    newBalance,
    "MPESA_DEPOSIT",
    transactionReference
);
```

#### **2. Manual Deposits**
```java
// In DepositService.java or BankingService.java
after crediting account:
autoLoanDeductionService.processAutoDeduction(
    customerId,
    accountId,
    depositAmount,
    currentBalance,
    "MANUAL_DEPOSIT",
    receiptNumber
);
```

#### **3. Salary Deposits**
```java
// In PayrollService.java or SalaryProcessing.java
after salary credit:
autoLoanDeductionService.processAutoDeduction(
    employeeCustomerId,
    salaryAccountId,
    salaryAmount,
    accountBalance,
    "SALARY_DEPOSIT",
    payrollReference
);
```

---

## 📱 **SMS NOTIFICATIONS**

### **Deduction Notification Template**
```
Auto Loan Payment: KES 5,000.00 deducted from your account 
for loan LOAN-12345. Remaining loan balance: KES 45,000.00. 
Account balance: KES 15,000.00. Thank you.
```

---

## 🎨 **FRONTEND IMPLEMENTATION**

### **1. Global Config Tab Component**

Location: `Admin → Settings → Global Config`

**Features**:
- List all configurations grouped by category
- Toggle switches for boolean configs
- Input fields for numeric configs
- Save button with confirmation
- Enable/Disable auto deduction master switch

**Sample UI**:
```typescript
// global-config.component.ts
export class GlobalConfigComponent {
  configs: GlobalConfig[] = [];
  categories = ['STANDING_ORDER', 'APPROVAL_WORKFLOW', 'LOAN_SETTINGS'];

  loadConfigs() {
    this.http.get('/api/global-config/active').subscribe(...)
  }

  updateConfig(key: string, value: any) {
    this.http.put(`/api/global-config/key/${key}/value`, {
      value: value.toString(),
      updatedBy: this.currentUser.username
    }).subscribe(...)
  }
}
```

### **2. Standing Order Management**

Location: `Client Profile → Loans → Standing Orders`

**Features**:
- View active standing orders
- Create new standing order
- Edit deduction amount/percentage
- Deactivate standing order
- View deduction history

**Sample UI**:
```html
<!-- standing-order-modal.html -->
<div class="modal">
  <h3>Setup Auto Loan Deduction</h3>
  
  <label>Deduction Type</label>
  <select [(ngModel)]="order.deductionType">
    <option value="FIXED_AMOUNT">Fixed Amount</option>
    <option value="PERCENTAGE">Percentage</option>
    <option value="FULL_INSTALLMENT">Full Installment</option>
    <option value="OUTSTANDING_BALANCE">Outstanding Balance</option>
  </select>

  <div *ngIf="order.deductionType === 'FIXED_AMOUNT'">
    <label>Amount (KES)</label>
    <input type="number" [(ngModel)]="order.amount">
  </div>

  <div *ngIf="order.deductionType === 'PERCENTAGE'">
    <label>Percentage (%)</label>
    <input type="number" [(ngModel)]="order.percentage" min="0" max="100">
  </div>

  <label>Minimum Balance (KES)</label>
  <input type="number" [(ngModel)]="order.minimumBalance">

  <label>Send SMS Notifications</label>
  <input type="checkbox" [(ngModel)]="order.sendSmsNotification">

  <button (click)="createStandingOrder()">Create</button>
</div>
```

---

## 🔐 **SECURITY & VALIDATION**

### **Validations Applied**
1. ✅ Check global enable flag before processing
2. ✅ Verify loan is active before deduction
3. ✅ Ensure sufficient balance after minimum
4. ✅ Validate deduction amount > 0
5. ✅ Don't deduct more than loan outstanding
6. ✅ Apply maximum deduction cap if set
7. ✅ Check trigger type matches transaction type

### **Error Handling**
```java
try {
    processAutoDeduction(...);
} catch (Exception e) {
    log.error("Auto deduction failed", e);
    // Don't throw - allow deposit to succeed
    // Record error for admin review
}
```

---

## 📊 **USAGE EXAMPLES**

### **Example 1: Salary Deduction (50%)**
```
Customer receives salary: KES 50,000
Standing Order: Deduct 50%
Calculation: 50,000 × 50% = KES 25,000
Minimum Balance: KES 1,000
Check: 50,000 - 25,000 = 25,000 ≥ 1,000 ✅
Result: Deduct KES 25,000 to loan
SMS: "Auto Loan Payment: KES 25,000 deducted..."
```

### **Example 2: Fixed Amount with Cap**
```
Customer deposits: KES 3,000
Standing Order: Fixed KES 5,000
Loan Outstanding: KES 2,500
Calculation: Min(5,000, 2,500, 3,000) = KES 2,500
Result: Deduct KES 2,500 (full outstanding)
Loan Status: PAID ✅
SMS: "Congratulations! Your loan is fully paid..."
```

### **Example 3: Minimum Balance Protection**
```
Customer deposits: KES 1,500
Standing Order: Deduct 80%
Calculation: 1,500 × 80% = KES 1,200
Minimum Balance: KES 500
Check: 1,500 - 1,200 = 300 < 500 ❌
Adjusted: 1,500 - 500 = KES 1,000 (deduct this)
Result: Deduct KES 1,000, keep KES 500
```

---

## 🚀 **DEPLOYMENT STEPS**

### **1. Database Migration**
```sql
-- Run these migrations
CREATE TABLE global_configs (...);
CREATE TABLE loan_standing_orders (...);
```

### **2. Initialize Default Configs**
```bash
POST http://localhost:8080/api/global-config/init-defaults
```

### **3. Enable Auto Deduction**
```bash
PUT http://localhost:8080/api/global-config/key/AUTO_LOAN_DEDUCTION_ENABLED/value
Body: {"value": "true", "updatedBy": "admin"}
```

### **4. Integrate with Deposit Flow**
Add `autoLoanDeductionService.processAutoDeduction()` calls in:
- C2BPaymentProcessingService
- ManualDepositService
- BankDepositService
- SalaryProcessingService

---

## ✅ **TESTING CHECKLIST**

- [ ] Create global config settings
- [ ] Enable auto deduction globally
- [ ] Create standing order for customer
- [ ] Process deposit → verify deduction happens
- [ ] Check SMS notification sent
- [ ] Verify minimum balance maintained
- [ ] Test percentage calculation
- [ ] Test fixed amount deduction
- [ ] Test loan closure (full outstanding)
- [ ] Verify loan marked as paid when complete
- [ ] Test deactivate standing order
- [ ] Test with disabled global setting

---

## 📝 **FRONTEND ROUTES TO CREATE**

```typescript
// In app-routing.module.ts
{
  path: 'admin/settings/global-config',
  component: GlobalConfigComponent,
  canActivate: [AuthGuard],
  data: { permission: 'ADMIN_ACCESS' }
}
```

---

**Status**: Backend complete. Frontend components pending.
**Integration Points**: Ready for connection with deposit services.
**Next Steps**: Create frontend UI and integrate with existing deposit flows.
