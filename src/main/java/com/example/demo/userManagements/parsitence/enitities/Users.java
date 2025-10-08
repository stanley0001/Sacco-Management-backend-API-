package com.example.demo.userManagements.parsitence.enitities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Users  {
    @Id
    @Column(nullable = false,unique = true,updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String otherName;
    @Column(unique = true)
    private String userName;
    @Column(unique = true)
    private String email;
    private String roleId;
    private String phone;
    private String documentNumber;
    private Boolean isActive;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public Users() {
    }

    public Users(Long id) {
        this.id = id;
    }

    public Users(String firstName, String lastName, String otherName, String userName, String email, String phone, String roleId, String documentNumber, Boolean isActive, LocalDate createdAt, LocalDate updatedAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherName = otherName;
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.documentNumber = documentNumber;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roleId =roleId;
    }

    public Users(Long id, String firstName, String lastName, String otherName, String userName, String email, String phone, String roleId, String documentNumber, Boolean isActive, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherName = otherName;
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.documentNumber = documentNumber;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roleId = roleId;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOtherName() {
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
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

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", otherName='" + otherName + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", roleId='" + roleId + '\'' +
                ", phone='" + phone + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

