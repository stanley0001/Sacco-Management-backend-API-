package com.example.demo.finance.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Charges {
    @Id
    @Column(nullable = false,unique = true,updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String productId;
    private Boolean fixedRate;
    private Boolean isActive;
    private Integer rate;
    private Integer daysAfterCommerce;
    private Integer invocationCount;
    private Integer interval;
    private String intervalUnit;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public Charges() {
    }

    public Charges(Long id) {
        this.id = id;
    }

    public Charges(String name, String productId, Boolean fixedRate, Boolean isActive, Integer rate, Integer daysAfterCommerce, Integer invocationCount, Integer interval, String intervalUnit, LocalDate createdAt, LocalDate updatedAt) {
        this.name = name;
        this.productId = productId;
        this.fixedRate = fixedRate;
        this.isActive = isActive;
        this.rate = rate;
        this.daysAfterCommerce = daysAfterCommerce;
        this.invocationCount = invocationCount;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Charges(Long id, String name, String productId, Boolean fixedRate, Boolean isActive, Integer rate, Integer daysAfterCommerce, Integer invocationCount, Integer interval, String intervalUnit, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.name = name;
        this.productId = productId;
        this.fixedRate = fixedRate;
        this.isActive = isActive;
        this.rate = rate;
        this.daysAfterCommerce = daysAfterCommerce;
        this.invocationCount = invocationCount;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Boolean getFixedRate() {
        return fixedRate;
    }

    public void setFixedRate(Boolean fixedRate) {
        this.fixedRate = fixedRate;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Integer getDaysAfterCommerce() {
        return daysAfterCommerce;
    }

    public void setDaysAfterCommerce(Integer daysAfterCommerce) {
        this.daysAfterCommerce = daysAfterCommerce;
    }

    public Integer getInvocationCount() {
        return invocationCount;
    }

    public void setInvocationCount(Integer invocationCount) {
        this.invocationCount = invocationCount;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public String getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(String intervalUnit) {
        this.intervalUnit = intervalUnit;
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
        return "Charges{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", productId='" + productId + '\'' +
                ", fixedRate=" + fixedRate +
                ", isActive=" + isActive +
                ", rate=" + rate +
                ", daysAfterCommerce=" + daysAfterCommerce +
                ", invocationCount=" + invocationCount +
                ", interval=" + interval +
                ", intervalUnit='" + intervalUnit + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
