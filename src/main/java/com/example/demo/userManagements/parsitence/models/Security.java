package com.example.demo.userManagements.parsitence.models;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Security  {

    @Id
    @Column(updatable = false,unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String userId;
    private String password;
    private Boolean isActive;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;

    public Security() {
    }

    public Security(Long id) {
        this.id = id;
    }

    public Security(String userId, String password, Boolean isActive, String status, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.password = password;
        this.isActive = isActive;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Security(Long id, String userId, String password, Boolean isActive, String status, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.isActive = isActive;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Security{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", password='" + password + '\'' +
                ", isActive=" + isActive +
                ", status='" + status + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
