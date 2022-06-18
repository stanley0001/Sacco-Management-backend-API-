package com.example.demo.customerManagement.parsistence.models;

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
    private String lastName;
    private String sirName;
    private Float accountBalance;
    private String documentNumber;
    private String externalId;
    private String occupation;
    private String address;
    private LocalDate dob;
    @Column(unique = true,nullable = false)
    private String phoneNumber;
    private String email;
    private String altPhoneNumber;
    private String createdBy;
    private Boolean status;
    private LocalDate externalStartDate;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    @OneToMany
    List<BankAccounts> bankAccounts;

    public Customer() {
    }

    public Customer(Long id) {
        this.id = id;
    }

    public Customer(String firstName, String lastName, Float accountBalance, String email, Boolean status, String documentNumber, String externalId, String occupation, String address, LocalDate dob, String phoneNumber, String altPhoneNumber, String createdBy, LocalDate externalStartDate, LocalDate createdAt, LocalDate updatedAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountBalance = accountBalance;
        this.documentNumber = documentNumber;
        this.externalId = externalId;
        this.occupation = occupation;
        this.address = address;
        this.dob = dob;
        this.phoneNumber = phoneNumber;
        this.altPhoneNumber = altPhoneNumber;
        this.createdBy = createdBy;
        this.externalStartDate = externalStartDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.email =email;
        this.status =status;
    }

    public Customer(Long id, String firstName, String lastName, Float accountBalance, String documentNumber, String externalId, String occupation, String address, LocalDate dob, String phoneNumber, String email, String altPhoneNumber, String createdBy, Boolean status, LocalDate externalStartDate, LocalDate createdAt, LocalDate updatedAt, List<BankAccounts> bankAccounts) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountBalance = accountBalance;
        this.documentNumber = documentNumber;
        this.externalId = externalId;
        this.occupation = occupation;
        this.address = address;
        this.dob = dob;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.altPhoneNumber = altPhoneNumber;
        this.createdBy = createdBy;
        this.status = status;
        this.externalStartDate = externalStartDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.bankAccounts = bankAccounts;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", accountBalance=" + accountBalance +
                ", documentNumber='" + documentNumber + '\'' +
                ", externalId='" + externalId + '\'' +
                ", occupation='" + occupation + '\'' +
                ", address='" + address + '\'' +
                ", dob=" + dob +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", altPhoneNumber='" + altPhoneNumber + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", status=" + status +
                ", externalStartDate=" + externalStartDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", bankAccounts=" + bankAccounts +
                '}';
    }


}
