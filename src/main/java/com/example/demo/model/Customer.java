package com.example.demo.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
public class Customer implements Serializable {

    private static final Long serialVersionUID = 1L;

    @Id
    @Column(unique = true,updatable = false,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
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

    public Customer(Long id, String firstName, String lastName, Float accountBalance, String email, Boolean status, String documentNumber, String externalId, String occupation, String address, LocalDate dob, String phoneNumber, String altPhoneNumber, String createdBy, LocalDate externalStartDate, LocalDate createdAt, LocalDate updatedAt) {
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
        this.altPhoneNumber = altPhoneNumber;
        this.createdBy = createdBy;
        this.externalStartDate = externalStartDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.email =email;
        this.status =status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Float getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(Float accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAltPhoneNumber() {
        return altPhoneNumber;
    }

    public void setAltPhoneNumber(String altPhoneNumber) {
        this.altPhoneNumber = altPhoneNumber;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getExternalStartDate() {
        return externalStartDate;
    }

    public void setExternalStartDate(LocalDate externalStartDate) {
        this.externalStartDate = externalStartDate;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
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
                '}';
    }


}
