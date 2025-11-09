package com.example.demo.erp.customerManagement.parsistence.entities;

import com.example.demo.finance.loanManagement.parsistence.models.LoanBookUpload;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Serializable {

    private static final Long serialVersionUID = 1L;

    @Id
    @Column(unique = true,updatable = false,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private Float accountBalance;
    private String documentType;
    private String documentNumber;
    private LocalDate dob;
    private String  employmentType;
    private String maritalStatus;
    private String occupation;
    private Float salary;
    @Column(unique = true)
    private String externalId;
    private LocalDate externalStartDate;
    private String address;
    private String AccountStatus;
    private Boolean accountStatusFlag;
    private String status; // Mobile banking status: ACTIVE, PENDING_VERIFICATION, LOCKED, etc.
    private String nextOfKin;
    private String nextOfKinRelationship;
    private String nextOfKinDocumentNumber;
    private String nextOfKinPhone;
    @Column(unique = true,nullable = false)
    private String phoneNumber;
    private String email;
    private String altPhoneNumber;
    private String createdBy;
    private String referredBy;
    private String branchCode;
    private String county; // For credit manager filtering
    private Long assignedLoanOfficerId; // Current loan officer
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Column(name = "initial_deposit_amount")
    @JsonProperty("initialDeposit")
    private Double initialDepositAmount;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "product_id", nullable = true)
    private LoanBookUpload loanBookUpload;

    // Mobile banking fields
    private String memberNumber;
    private String pinHash;
    private Integer failedPinAttempts = 0;
    private LocalDateTime lastLogin;
    
    // Additional fields for bulk processing
    private Long branchId;
    private Boolean isActive = true;
    
    // Convenience methods for bulk processing compatibility
    public void setIdNumber(String idNumber) {
        this.documentNumber = idNumber; // National ID is document number
    }
    
    public String getIdNumber() {
        return this.documentNumber;
    }
    
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public boolean getIsActive() {
        return this.isActive != null ? this.isActive : true;
    }
    
    public Long getBranchId() {
        return this.branchId;
    }
    
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public Customer(LoanBookUpload upload) {
        String[] customerName = upload.getCustomerName().trim().split(" ");
        this.firstName=customerName.length<1?upload.getCustomerName():customerName[0];
        this.middleName=customerName.length>1?customerName[1]:null;
        this.lastName=customerName.length>2?customerName[2]:middleName;
        this.documentNumber=upload.getDocumentNumber();
        this.phoneNumber=upload.getPhoneNumber();
    }
    /*@OneToMany
    List<BankAccounts> bankAccounts;

   */
}
