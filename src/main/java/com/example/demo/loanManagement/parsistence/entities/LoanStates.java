package com.example.demo.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LoanStates  {
    @Id
    @Column(unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String Status;
    private String accountNumber;
    private Boolean isActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public LoanStates() {
    }

    public LoanStates(Long id) {
        this.id = id;
    }

    public LoanStates(String status, String accountNumber, Boolean isActive, LocalDateTime startDate, LocalDateTime endDate) {
        Status = status;
        this.accountNumber = accountNumber;
        this.isActive = isActive;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LoanStates(Long id, String status, String accountNumber, Boolean isActive, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        Status = status;
        this.accountNumber = accountNumber;
        this.isActive = isActive;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "LoanStates{" +
                "id=" + id +
                ", Status='" + Status + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", isActive=" + isActive +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
