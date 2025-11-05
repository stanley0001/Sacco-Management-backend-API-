# 📋 Complete Loan Approval to Disbursement Flow

## Overview
This guide provides step-by-step instructions to implement a complete loan lifecycle from application submission to disbursement with accounting integration.

---

## 🔄 Loan Lifecycle Stages

```
1. NEW → (Customer submits application)
2. APPROVED → (Loan officer approves)
3. DISBURSED → (Finance disburses funds)
4. ACTIVE → (Loan is being repaid)
5. CLOSED → (Loan fully repaid)
```

---

## 📊 Current Status & Required Changes

### ✅ Already Implemented (Backend):
- `LoanApplicationApprovalService.approveApplication()` - Approves loans
- `LoanDisbursementService.disburseLoan()` - Disburses approved loans
- `LoanAccountingService` - Posts to accounting (just created)
- Repayment schedules generation
- M-PESA disbursement support

### ⚠️ Missing Integrations:
1. Accounting entry on approval
2. Accounting entry on disbursement
3. Schedule validation before booking
4. Frontend approval workflow
5. Frontend disbursement UI

---

## 🛠️ BACKEND IMPLEMENTATION

### Step 1: Enhance Loan Approval Service (Add Accounting)

**File:** `LoanApplicationApprovalService.java`

**Current Code (lines 28-56):**
```java
@Transactional
public LoanApplication approveApplication(Long applicationId, String approvedBy, String comments) {
    log.info("Approving loan application ID: {}", applicationId);
    
    LoanApplication application = applicationRepo.findById(applicationId)
            .orElseThrow(() -> new IllegalStateException("Application not found"));

    if (!"NEW".equals(application.getApplicationStatus())) {
        throw new IllegalStateException("Only NEW applications can be approved");
    }

    // Update application status
    application.setApplicationStatus("APPROVED");
    application = applicationRepo.save(application);

    // Send approval notification
    try {
        Customer customer = customerService.findById(Long.valueOf(application.getCustomerId())).getClient();
        Email email = new Email();
        email.setRecipient(customer.getEmail());
        email.setMessageType("Loan Approval");
        email.setMessage("Hello " + customer.getFirstName() + ", your loan application for KES " + 
                application.getLoanAmount() + " has been APPROVED.");
        communicationService.sendCustomEmail(email);
    } catch (Exception e) {
        log.error("Failed to send approval notification", e);
    }

    return application;
}
```

**ADD AFTER EMAIL NOTIFICATION:**
```java
    // Post to accounting (memo entry for approved loans)
    try {
        log.info("📊 Recording loan approval in accounting system");
        // This is just tracking - no GL posting at approval stage
        // You can add to a separate "Approved Loans Register" table if needed
    } catch (Exception e) {
        log.error("Failed to post loan approval to accounting", e);
        // Don't fail approval if accounting fails
    }

    log.info("✅ Loan approved successfully. Application ID: {}, Amount: {}", 
        applicationId, application.getLoanAmount());
    
    return application;
```

---

### Step 2: Enhance Loan Disbursement Service (Add Accounting)

**File:** `LoanDisbursementService.java`

**Find the `disburseLoan()` method around line 58-110 and ADD AFTER creating loan account:**

```java
// Generate payment schedules
List<LoanRepaymentSchedule> schedules = generatePaymentSchedules(loanAccount, loanTerm);

// IMPORTANT: Validate schedules were created
if (schedules == null || schedules.isEmpty()) {
    throw new IllegalStateException("Failed to generate repayment schedules for loan #" + loanAccount.getAccountId());
}

log.info("✅ Generated {} repayment schedules for loan #{}", schedules.size(), loanAccount.getAccountId());

// Save schedules
scheduleRepository.saveAll(schedules);

// ⭐ POST DISBURSEMENT TO ACCOUNTING ⭐
try {
    log.info("📊 Posting loan disbursement to accounting system");
    loanAccountingService.postLoanDisbursement(loanAccount, disbursementMethod, disbursedBy);
    log.info("✅ Loan disbursement posted to accounting successfully");
} catch (Exception e) {
    log.error("❌ Failed to post loan disbursement to accounting", e);
    // Log error but don't fail disbursement - accounting can be reconciled later
}
```

**Add dependency at the top of the class:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanDisbursementService {
    
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanAccountRepo loanAccountRepo;
    private final LoanRepaymentScheduleRepository scheduleRepository;
    private final ProductsRepository productsRepository;
    private final SmsService smsService;
    private final LoanAccountingService loanAccountingService; // ⭐ ADD THIS
```

---

### Step 3: Add Schedule Validation Endpoint

**File:** `LoanDisbursementController.java` (or create new controller)

```java
/**
 * Get loan with schedules preview before disbursement
 */
@GetMapping("/application/{applicationId}/schedules-preview")
@PreAuthorize("hasAnyAuthority('LOAN_DISBURSE', 'ADMIN_ACCESS')")
public ResponseEntity<?> getSchedulesPreview(@PathVariable Long applicationId) {
    try {
        LoanApplication application = applicationService.getApplicationById(applicationId);
        
        if (!"APPROVED".equals(application.getApplicationStatus())) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Only APPROVED applications can be previewed for disbursement"
            ));
        }

        // Get product to calculate schedules
        Products product = productsRepository.findById(application.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Generate preview schedules (don't save yet)
        int term = application.getTerm() != null ? application.getTerm() : product.getTerm();
        List<Map<String, Object>> schedulePreview = new ArrayList<>();
        
        double principalAmount = Double.parseDouble(application.getLoanAmount());
        double interestRate = product.getInterestRate() != null ? product.getInterestRate() : 0.0;
        double totalAmount = principalAmount * (1 + (interestRate / 100));
        double installmentAmount = totalAmount / term;
        
        LocalDate startDate = LocalDate.now().plusMonths(1);
        
        for (int i = 1; i <= term; i++) {
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("installmentNumber", i);
            schedule.put("dueDate", startDate.plusMonths(i - 1));
            schedule.put("installmentAmount", Math.round(installmentAmount * 100.0) / 100.0);
            schedule.put("principalPortion", Math.round((principalAmount / term) * 100.0) / 100.0);
            schedule.put("interestPortion", Math.round(((totalAmount - principalAmount) / term) * 100.0) / 100.0);
            schedule.put("status", "PENDING");
            schedulePreview.add(schedule);
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "application", application,
            "product", product,
            "schedulePreview", schedulePreview,
            "summary", Map.of(
                "principalAmount", principalAmount,
                "interestRate", interestRate,
                "term", term,
                "installmentAmount", installmentAmount,
                "totalAmount", totalAmount
            )
        ));

    } catch (Exception e) {
        log.error("Error generating schedules preview", e);
        return ResponseEntity.status(500).body(Map.of(
            "success", false,
            "message", "Failed to generate schedules preview: " + e.getMessage()
        ));
    }
}
```

---

## 🎨 FRONTEND IMPLEMENTATION

### Step 1: Create Loan Approval Component

**File:** `src/app/loans/loan-approval/loan-approval.component.ts` (NEW)

```typescript
import { Component, OnInit } from '@angular/core';
import { LoanService } from '../services/loan.service';

@Component({
  selector: 'app-loan-approval',
  templateUrl: './loan-approval.component.html',
  styleUrls: ['./loan-approval.component.css']
})
export class LoanApprovalComponent implements OnInit {
  pendingApplications: any[] = [];
  selectedApplication: any = null;
  showApprovalModal = false;
  showRejectModal = false;
  approvalComments = '';
  rejectionReason = '';
  loading = false;

  constructor(private loanService: LoanService) {}

  ngOnInit() {
    this.loadPendingApplications();
  }

  loadPendingApplications() {
    this.loading = true;
    this.loanService.getApplicationsByStatus('NEW').subscribe({
      next: (response: any) => {
        this.pendingApplications = response.data || response;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading applications:', error);
        this.loading = false;
      }
    });
  }

  viewApplication(application: any) {
    this.selectedApplication = application;
  }

  openApprovalModal(application: any) {
    this.selectedApplication = application;
    this.showApprovalModal = true;
    this.approvalComments = '';
  }

  openRejectionModal(application: any) {
    this.selectedApplication = application;
    this.showRejectModal = true;
    this.rejectionReason = '';
  }

  approveApplication() {
    if (!this.selectedApplication) return;

    this.loading = true;
    this.loanService.approveApplication(
      this.selectedApplication.id,
      this.approvalComments
    ).subscribe({
      next: (response) => {
        alert('✅ Loan application approved successfully!');
        this.showApprovalModal = false;
        this.loadPendingApplications();
        this.loading = false;
      },
      error: (error) => {
        alert('❌ Error approving application: ' + error.message);
        this.loading = false;
      }
    });
  }

  rejectApplication() {
    if (!this.selectedApplication) return;

    this.loading = true;
    this.loanService.rejectApplication(
      this.selectedApplication.id,
      this.rejectionReason
    ).subscribe({
      next: (response) => {
        alert('✅ Loan application rejected');
        this.showRejectModal = false;
        this.loadPendingApplications();
        this.loading = false;
      },
      error: (error) => {
        alert('❌ Error rejecting application: ' + error.message);
        this.loading = false;
      }
    });
  }

  closeModals() {
    this.showApprovalModal = false;
    this.showRejectModal = false;
    this.selectedApplication = null;
  }
}
```

---

### Step 2: Create Loan Approval HTML Template

**File:** `src/app/loans/loan-approval/loan-approval.component.html` (NEW)

```html
<div class="container-fluid p-4">
  <div class="row mb-4">
    <div class="col-12">
      <h2><i class="fas fa-check-circle"></i> Loan Approvals</h2>
      <p class="text-muted">Review and approve pending loan applications</p>
    </div>
  </div>

  <!-- Pending Applications Table -->
  <div class="card">
    <div class="card-header bg-primary text-white">
      <h5 class="mb-0">Pending Applications ({{pendingApplications.length}})</h5>
    </div>
    <div class="card-body">
      <div *ngIf="loading" class="text-center p-5">
        <div class="spinner-border text-primary" role="status"></div>
        <p>Loading applications...</p>
      </div>

      <div *ngIf="!loading && pendingApplications.length === 0" class="text-center p-5">
        <i class="fas fa-inbox fa-3x text-muted mb-3"></i>
        <p class="text-muted">No pending applications</p>
      </div>

      <table *ngIf="!loading && pendingApplications.length > 0" class="table table-hover">
        <thead>
          <tr>
            <th>App ID</th>
            <th>Customer</th>
            <th>Product</th>
            <th>Amount</th>
            <th>Term</th>
            <th>Applied Date</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let app of pendingApplications">
            <td><strong>#{{app.id}}</strong></td>
            <td>{{app.customerName || app.customerId}}</td>
            <td>{{app.productName || app.productId}}</td>
            <td><strong>KES {{app.loanAmount | number}}</strong></td>
            <td>{{app.term}} months</td>
            <td>{{app.applicationDate | date:'short'}}</td>
            <td><span class="badge bg-warning">{{app.applicationStatus}}</span></td>
            <td>
              <button class="btn btn-sm btn-info me-1" (click)="viewApplication(app)">
                <i class="fas fa-eye"></i> View
              </button>
              <button class="btn btn-sm btn-success me-1" (click)="openApprovalModal(app)">
                <i class="fas fa-check"></i> Approve
              </button>
              <button class="btn btn-sm btn-danger" (click)="openRejectionModal(app)">
                <i class="fas fa-times"></i> Reject
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</div>

<!-- Approval Modal -->
<div class="modal fade" [class.show]="showApprovalModal" [style.display]="showApprovalModal ? 'block' : 'none'">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header bg-success text-white">
        <h5 class="modal-title">Approve Loan Application</h5>
        <button type="button" class="btn-close" (click)="closeModals()"></button>
      </div>
      <div class="modal-body">
        <p><strong>Application ID:</strong> #{{selectedApplication?.id}}</p>
        <p><strong>Customer:</strong> {{selectedApplication?.customerName}}</p>
        <p><strong>Amount:</strong> KES {{selectedApplication?.loanAmount | number}}</p>
        <p><strong>Term:</strong> {{selectedApplication?.term}} months</p>

        <div class="form-group mt-3">
          <label>Approval Comments (Optional)</label>
          <textarea 
            class="form-control" 
            rows="3" 
            [(ngModel)]="approvalComments"
            placeholder="Enter any comments or conditions..."></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" (click)="closeModals()">Cancel</button>
        <button class="btn btn-success" (click)="approveApplication()" [disabled]="loading">
          <i class="fas fa-check"></i> Approve Loan
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Rejection Modal -->
<div class="modal fade" [class.show]="showRejectModal" [style.display]="showRejectModal ? 'block' : 'none'">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header bg-danger text-white">
        <h5 class="modal-title">Reject Loan Application</h5>
        <button type="button" class="btn-close" (click)="closeModals()"></button>
      </div>
      <div class="modal-body">
        <p><strong>Application ID:</strong> #{{selectedApplication?.id}}</p>
        <p><strong>Customer:</strong> {{selectedApplication?.customerName}}</p>

        <div class="form-group mt-3">
          <label>Rejection Reason <span class="text-danger">*</span></label>
          <textarea 
            class="form-control" 
            rows="3" 
            [(ngModel)]="rejectionReason"
            placeholder="Enter reason for rejection..."
            required></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" (click)="closeModals()">Cancel</button>
        <button 
          class="btn btn-danger" 
          (click)="rejectApplication()" 
          [disabled]="!rejectionReason || loading">
          <i class="fas fa-times"></i> Reject Application
        </button>
      </div>
    </div>
  </div>
</div>
```

---

### Step 3: Create Loan Disbursement Component

**File:** `src/app/loans/loan-disbursement/loan-disbursement.component.ts` (NEW)

```typescript
import { Component, OnInit } from '@angular/core';
import { LoanService } from '../services/loan.service';

@Component({
  selector: 'app-loan-disbursement',
  templateUrl: './loan-disbursement.component.html',
  styleUrls: ['./loan-disbursement.component.css']
})
export class LoanDisbursementComponent implements OnInit {
  approvedApplications: any[] = [];
  selectedApplication: any = null;
  showDisbursementModal = false;
  showSchedulePreview = false;
  schedulePreview: any[] = [];
  scheduleSummary: any = {};
  
  disbursementData = {
    method: 'MPESA',
    reference: '',
    destination: ''
  };
  
  loading = false;

  constructor(private loanService: LoanService) {}

  ngOnInit() {
    this.loadApprovedApplications();
  }

  loadApprovedApplications() {
    this.loading = true;
    this.loanService.getApplicationsByStatus('APPROVED').subscribe({
      next: (response: any) => {
        this.approvedApplications = response.data || response;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading applications:', error);
        this.loading = false;
      }
    });
  }

  previewSchedules(application: any) {
    this.loading = true;
    this.selectedApplication = application;
    
    this.loanService.getSchedulesPreview(application.id).subscribe({
      next: (response: any) => {
        this.schedulePreview = response.schedulePreview;
        this.scheduleSummary = response.summary;
        this.showSchedulePreview = true;
        this.loading = false;
      },
      error: (error) => {
        alert('Error loading schedule preview: ' + error.message);
        this.loading = false;
      }
    });
  }

  openDisbursementModal(application: any) {
    this.selectedApplication = application;
    this.showDisbursementModal = true;
    this.disbursementData = {
      method: 'MPESA',
      reference: 'DISB-' + Date.now(),
      destination: ''
    };
  }

  disburseLoan() {
    if (!this.selectedApplication) return;

    this.loading = true;
    this.loanService.disburseLoan(
      this.selectedApplication.id,
      this.disbursementData.method,
      this.disbursementData.reference,
      this.disbursementData.destination
    ).subscribe({
      next: (response) => {
        alert('✅ Loan disbursed successfully!');
        this.showDisbursementModal = false;
        this.loadApprovedApplications();
        this.loading = false;
      },
      error: (error) => {
        alert('❌ Error disbursing loan: ' + error.message);
        this.loading = false;
      }
    });
  }

  closeModals() {
    this.showDisbursementModal = false;
    this.showSchedulePreview = false;
    this.selectedApplication = null;
  }
}
```

Continue in next file...
