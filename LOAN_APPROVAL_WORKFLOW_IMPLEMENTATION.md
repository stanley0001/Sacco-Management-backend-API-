# 🎯 DYNAMIC LOAN APPROVAL WORKFLOW - COMPLETE IMPLEMENTATION GUIDE

## ✅ IMPLEMENTED FEATURES

### **Backend Completed** (In Progress)

#### **1. Database Entities** ✅
- ✅ `ApprovalWorkflowConfig.java` - Main workflow configuration
- ✅ `ApprovalWorkflowLevel.java` - Individual approval levels
- ✅ `LoanApprovalHistory.java` - Approval action tracking
- ✅ `LoanApprovalStatus.java` - Current approval status

#### **2. Repositories** ✅
- ✅ `ApprovalWorkflowConfigRepository.java`
- ✅ `LoanApprovalHistoryRepository.java`
- ✅ `LoanApprovalStatusRepository.java`

####  **3. Services** (Next)
- Pending: `LoanApprovalWorkflowService.java` - Main workflow logic
- Pending: `ApprovalNotificationService.java` - SMS/Email notifications

#### **4. Controllers** (Next)
- Pending: `ApprovalWorkflowController.java` - Workflow configuration API
- Pending: `LoanApprovalController.java` - Approval actions API

### **Frontend** (Next)
- Pending: Global Configuration Component
- Pending: Approval Workflow Management UI
- Pending: Loan Approval Actions UI

---

## 📊 **FEATURES OVERVIEW**

### **Dynamic Maker-Checker System**

**Key Features**:
1. ✅ **Configurable Approval Levels** - Set 1 to 5 levels per workflow
2. ✅ **Role-Based Approval** - Assign specific roles to each level
3. ✅ **Multi-Approver Support** - Require multiple approvals at same level
4. ✅ **Amount-Based Routing** - Different workflows for different loan amounts
5. ✅ **Product-Specific Workflows** - Workflows per product type
6. ✅ **SMS Notifications** - Auto-notify all users with required role
7. ✅ **Email Notifications** - Optional email notifications
8. ✅ **Approval History** - Complete audit trail
9. ✅ **Auto-Approval** - Timeout-based auto-approval option
10. ✅ **Skip Levels** - Optional level skipping

---

## 🏗️ **DATABASE SCHEMA**

### **approval_workflow_configs**
```sql
CREATE TABLE approval_workflow_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    min_amount DECIMAL(15,2),
    max_amount DECIMAL(15,2),
    applicable_products VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_default BOOLEAN NOT NULL DEFAULT false,
    priority INTEGER NOT NULL DEFAULT 1,
    total_levels INTEGER NOT NULL DEFAULT 1,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL
);
```

### **approval_workflow_levels**
```sql
CREATE TABLE approval_workflow_levels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_config_id BIGINT NOT NULL,
    level_number INTEGER NOT NULL,
    level_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    required_approvers INTEGER NOT NULL DEFAULT 1,
    allowed_roles VARCHAR(500) NOT NULL,
    can_skip BOOLEAN NOT NULL DEFAULT false,
    auto_approve_after_hours INTEGER,
    send_sms_notification BOOLEAN NOT NULL DEFAULT true,
    send_email_notification BOOLEAN NOT NULL DEFAULT false,
    sms_template VARCHAR(500),
    FOREIGN KEY (workflow_config_id) REFERENCES approval_workflow_configs(id)
);
```

### **loan_approval_status**
```sql
CREATE TABLE loan_approval_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_application_id BIGINT UNIQUE NOT NULL,
    workflow_config_id BIGINT NOT NULL,
    current_level INTEGER NOT NULL DEFAULT 1,
    total_levels INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_LEVEL_1',
    approvals_at_current_level INTEGER NOT NULL DEFAULT 0,
    approvals_required_at_current_level INTEGER NOT NULL DEFAULT 1,
    approved_by_at_current_level VARCHAR(500),
    level_start_time TIMESTAMP,
    last_action_time TIMESTAMP,
    completed_time TIMESTAMP,
    is_complete BOOLEAN NOT NULL DEFAULT false,
    is_finally_approved BOOLEAN NOT NULL DEFAULT false,
    current_level_comments VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### **loan_approval_history**
```sql
CREATE TABLE loan_approval_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_application_id BIGINT NOT NULL,
    workflow_config_id BIGINT NOT NULL,
    level_number INTEGER NOT NULL,
    level_name VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    approver_user_id VARCHAR(100) NOT NULL,
    approver_name VARCHAR(200),
    approver_role VARCHAR(100),
    comments VARCHAR(1000),
    rejection_reason VARCHAR(500),
    action_date TIMESTAMP NOT NULL,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500)
);
```

---

## 🔄 **WORKFLOW FLOW**

### **1. Loan Application Submission**
```
New Loan Application
    ↓
Find Applicable Workflow (by amount & product)
    ↓
Create LoanApprovalStatus (current_level = 1)
    ↓
Send SMS to All Users with Level 1 Roles
    ↓
Wait for Approvals
```

### **2. Approval Process**
```
User Approves at Level X
    ↓
Record in LoanApprovalHistory
    ↓
Update LoanApprovalStatus (increment approvals_at_current_level)
    ↓
Check if Level Complete (approvals >= required)
    ├─ NO → Wait for more approvals
    └─ YES → Move to Next Level
            ↓
        Check if Final Level
            ├─ NO → Increment current_level
            │       Send SMS to Level X+1 Roles
            │       Wait for approvals
            └─ YES → Mark as FULLY_APPROVED
                    Trigger Loan Disbursement
```

### **3. Rejection Flow**
```
User Rejects at Level X
    ↓
Record in LoanApprovalHistory
    ↓
Mark LoanApprovalStatus as REJECTED
    ↓
Send SMS to Applicant
    ↓
End Workflow
```

---

## 📱 **SMS NOTIFICATION TEMPLATES**

### **Level Notification**
```
New loan application #{applicationId} requires your approval.
Customer: {customerName}
Amount: KES {amount}
Product: {productName}
Login to approve/reject: {portalUrl}
```

### **Approval Confirmation**
```
Your loan application #{applicationId} has been approved at Level {level}.
Current Status: {status}
Next Level: {nextLevel}
```

### **Final Approval**
```
Congratulations! Your loan application #{applicationId} for KES {amount} 
has been fully approved. Funds will be disbursed within 24 hours.
```

### **Rejection Notification**
```
Your loan application #{applicationId} has been rejected.
Reason: {rejectionReason}
Contact us for more information.
```

---

## 🎯 **CONFIGURATION EXAMPLES**

### **Example 1: Simple 2-Level Approval**
```json
{
  "workflowName": "Standard Loan Approval",
  "minAmount": 0,
  "maxAmount": 100000,
  "totalLevels": 2,
  "levels": [
    {
      "levelNumber": 1,
      "levelName": "Branch Manager",
      "requiredApprovers": 1,
      "allowedRoles": "BRANCH_MANAGER",
      "sendSmsNotification": true
    },
    {
      "levelNumber": 2,
      "levelName": "Credit Officer",
      "requiredApprovers": 1,
      "allowedRoles": "CREDIT_OFFICER",
      "sendSmsNotification": true
    }
  ]
}
```

### **Example 2: High-Value Loan (3 Levels)**
```json
{
  "workflowName": "High Value Loan Approval",
  "minAmount": 100000,
  "maxAmount": 1000000,
  "totalLevels": 3,
  "levels": [
    {
      "levelNumber": 1,
      "levelName": "Branch Manager",
      "requiredApprovers": 1,
      "allowedRoles": "BRANCH_MANAGER"
    },
    {
      "levelNumber": 2,
      "levelName": "Credit Committee",
      "requiredApprovers": 2,
      "allowedRoles": "CREDIT_COMMITTEE_MEMBER,SENIOR_CREDIT_OFFICER"
    },
    {
      "levelNumber": 3,
      "levelName": "CEO Approval",
      "requiredApprovers": 1,
      "allowedRoles": "CEO,COO"
    }
  ]
}
```

---

## 🔌 **API ENDPOINTS** (To Be Created)

### **Workflow Configuration**
```
GET    /api/loan-approval/workflows                - List all workflows
POST   /api/loan-approval/workflows                - Create workflow
GET    /api/loan-approval/workflows/{id}           - Get workflow details
PUT    /api/loan-approval/workflows/{id}           - Update workflow
DELETE /api/loan-approval/workflows/{id}           - Delete workflow
POST   /api/loan-approval/workflows/{id}/activate  - Activate workflow
POST   /api/loan-approval/workflows/{id}/deactivate - Deactivate workflow
```

### **Approval Actions**
```
GET    /api/loan-approval/pending                  - Get pending approvals
GET    /api/loan-approval/history/{loanId}         - Get approval history
POST   /api/loan-approval/approve                  - Approve application
POST   /api/loan-approval/reject                   - Reject application
POST   /api/loan-approval/return                   - Return for correction
```

### **Status & Reports**
```
GET    /api/loan-approval/status/{loanId}          - Get current status
GET    /api/loan-approval/my-pending               - Get my pending approvals
GET    /api/loan-approval/statistics               - Get approval statistics
```

---

## 📋 **FRONTEND COMPONENTS** (To Be Created)

### **1. Global Configuration Tab**
Location: Admin → Settings → Global Config
Features:
- List all workflows
- Create new workflow
- Edit workflow
- Activate/Deactivate workflows
- Test workflow

### **2. Workflow Configuration Form**
Features:
- Workflow name & description
- Amount range
- Product selection
- Add/Remove levels
- Configure roles per level
- Set required approvers
- SMS/Email settings

### **3. Loan Approval Dashboard**
Features:
- Pending approvals list
- Filter by level/status
- Approve/Reject actions
- View application details
- Add comments
- View approval history

### **4. Approval History View**
Features:
- Timeline view
- Action details
- Approver information
- Comments & reasons
- Status changes

---

## ⚙️ **CONFIGURATION SETTINGS**

### **Global Settings** (To Be Added)
```javascript
{
  "approvalWorkflow": {
    "enabled": true,
    "defaultWorkflow": "Standard Loan Approval",
    "allowSkipLevels": false,
    "enableAutoApproval": false,
    "autoApprovalHours": 24,
    "notificationSettings": {
      "smsEnabled": true,
      "emailEnabled": false,
      "notifyOnSubmission": true,
      "notifyOnApproval": true,
      "notifyOnRejection": true
    }
  }
}
```

---

## 🚀 **NEXT STEPS**

### **Immediate (In Progress)**
1. ✅ Create entities and repositories
2. ⏳ Create approval workflow service
3. ⏳ Create approval notification service
4. ⏳ Create controllers
5. ⏳ Integrate with loan application flow

### **Frontend Development**
1. Create global config component
2. Create workflow management UI
3. Create approval dashboard
4. Create approval action modals
5. Integrate with backend APIs

### **Testing & Deployment**
1. Unit tests for services
2. Integration tests for workflow
3. End-to-end testing
4. Documentation
5. Deployment

---

## 📝 **USAGE EXAMPLES**

### **Creating a Workflow**
```java
ApprovalWorkflowConfig workflow = ApprovalWorkflowConfig.builder()
    .workflowName("Medium Loan Approval")
    .minAmount(BigDecimal.valueOf(50000))
    .maxAmount(BigDecimal.valueOf(500000))
    .totalLevels(2)
    .isActive(true)
    .build();

// Add levels
ApprovalWorkflowLevel level1 = new ApprovalWorkflowLevel();
level1.setLevelNumber(1);
level1.setLevelName("Branch Manager");
level1.setRequiredApprovers(1);
level1.setAllowedRoles("BRANCH_MANAGER");
level1.setSendSmsNotification(true);
level1.setWorkflowConfig(workflow);

workflow.getLevels().add(level1);

workflowRepository.save(workflow);
```

### **Processing Approval**
```java
approvalWorkflowService.processApproval(
    loanApplicationId,
    currentUserId,
    ApprovalAction.APPROVED,
    "Application meets all criteria"
);
```

---

**Status**: Backend entities and repositories complete. Services and controllers in progress.
**Next**: Complete service layer and integrate with existing loan application workflow.
