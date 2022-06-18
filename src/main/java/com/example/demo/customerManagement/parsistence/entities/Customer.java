package com.example.demo.customerManagement.parsistence.entities;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
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
    private Boolean status;
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
    private LocalDate createdAt;
    private LocalDate updatedAt;
    @OneToMany
    List<BankAccounts> bankAccounts;

    public Customer() {
    }

    public Customer(Long id) {
        this.id = id;
    }

    public Customer(String firstName, String middleName, String lastName, Float accountBalance, String documentType, String documentNumber, LocalDate dob, String employmentType, String maritalStatus, String occupation, Float salary, String externalId, LocalDate externalStartDate, String address, String accountStatus, Boolean status, String nextOfKin, String nextOfKinRelationship, String nextOfKinDocumentNumber, String nextOfKinPhone, String phoneNumber, String email, String altPhoneNumber, String createdBy, String referredBy, String branchCode, LocalDate createdAt, LocalDate updatedAt, List<BankAccounts> bankAccounts) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.accountBalance = accountBalance;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.dob = dob;
        this.employmentType = employmentType;
        this.maritalStatus = maritalStatus;
        this.occupation = occupation;
        this.salary = salary;
        this.externalId = externalId;
        this.externalStartDate = externalStartDate;
        this.address = address;
        AccountStatus = accountStatus;
        this.status = status;
        this.nextOfKin = nextOfKin;
        this.nextOfKinRelationship = nextOfKinRelationship;
        this.nextOfKinDocumentNumber = nextOfKinDocumentNumber;
        this.nextOfKinPhone = nextOfKinPhone;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.altPhoneNumber = altPhoneNumber;
        this.createdBy = createdBy;
        this.referredBy = referredBy;
        this.branchCode = branchCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.bankAccounts = bankAccounts;
    }

    public Customer(Long id, String firstName, String middleName, String lastName, Float accountBalance, String documentType, String documentNumber, LocalDate dob, String employmentType, String maritalStatus, String occupation, Float salary, String externalId, LocalDate externalStartDate, String address, String accountStatus, Boolean status, String nextOfKin, String nextOfKinRelationship, String nextOfKinDocumentNumber, String nextOfKinPhone, String phoneNumber, String email, String altPhoneNumber, String createdBy, String referredBy, String branchCode, LocalDate createdAt, LocalDate updatedAt, List<BankAccounts> bankAccounts) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.accountBalance = accountBalance;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.dob = dob;
        this.employmentType = employmentType;
        this.maritalStatus = maritalStatus;
        this.occupation = occupation;
        this.salary = salary;
        this.externalId = externalId;
        this.externalStartDate = externalStartDate;
        this.address = address;
        AccountStatus = accountStatus;
        this.status = status;
        this.nextOfKin = nextOfKin;
        this.nextOfKinRelationship = nextOfKinRelationship;
        this.nextOfKinDocumentNumber = nextOfKinDocumentNumber;
        this.nextOfKinPhone = nextOfKinPhone;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.altPhoneNumber = altPhoneNumber;
        this.createdBy = createdBy;
        this.referredBy = referredBy;
        this.branchCode = branchCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.bankAccounts = bankAccounts;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", accountBalance=" + accountBalance +
                ", documentType='" + documentType + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", dob=" + dob +
                ", employmentType='" + employmentType + '\'' +
                ", maritalStatus='" + maritalStatus + '\'' +
                ", occupation='" + occupation + '\'' +
                ", salary=" + salary +
                ", externalId='" + externalId + '\'' +
                ", externalStartDate=" + externalStartDate +
                ", address='" + address + '\'' +
                ", AccountStatus='" + AccountStatus + '\'' +
                ", status=" + status +
                ", nextOfKin='" + nextOfKin + '\'' +
                ", nextOfKinRelationship='" + nextOfKinRelationship + '\'' +
                ", nextOfKinDocumentNumber='" + nextOfKinDocumentNumber + '\'' +
                ", nextOfKinPhone='" + nextOfKinPhone + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", altPhoneNumber='" + altPhoneNumber + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", referredBy='" + referredBy + '\'' +
                ", branchCode='" + branchCode + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", bankAccounts=" + bankAccounts +
                '}';
    }
}
