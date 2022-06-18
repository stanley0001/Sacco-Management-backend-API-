package com.example.demo.loanManagement.parsistence.models;

import javax.persistence.*;

@Entity
public class Products  {
    @Id
    @Column(nullable = false,updatable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    @Column(unique = true)
    private String code;
    private Boolean isActive;
    private Integer term;
    private Integer interest;
    private Integer maxLimit;
    private Integer minLimit;
    private Boolean topUp;
    private Boolean rollOver;
    private Boolean dailyInterest;
    private Boolean interestUpfront;
    private String transactionType;
    private String timeSpan;

    public Products() {
    }

    public Products(Long id) {
        this.id = id;
    }

    public Products(String name, String code, Boolean isActive, Integer term, Integer interest, Integer maxLimit, Integer minLimit, Boolean topUp, Boolean rollOver, Boolean dailyInterest, Boolean interestUpfront, String transactionType, String timeSpan) {
        this.name = name;
        this.code = code;
        this.isActive = isActive;
        this.term = term;
        this.interest = interest;
        this.maxLimit = maxLimit;
        this.minLimit = minLimit;
        this.topUp = topUp;
        this.rollOver = rollOver;
        this.dailyInterest = dailyInterest;
        this.interestUpfront = interestUpfront;
        this.transactionType = transactionType;
        this.timeSpan = timeSpan;
    }

    public Products(Long id, String name, String code, Boolean isActive, Integer term, Integer interest, Integer maxLimit, Integer minLimit, Boolean topUp, Boolean rollOver, Boolean dailyInterest, Boolean interestUpfront, String transactionType, String timeSpan) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.isActive = isActive;
        this.term = term;
        this.interest = interest;
        this.maxLimit = maxLimit;
        this.minLimit = minLimit;
        this.topUp = topUp;
        this.rollOver = rollOver;
        this.dailyInterest = dailyInterest;
        this.interestUpfront = interestUpfront;
        this.transactionType = transactionType;
        this.timeSpan = timeSpan;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getInterest() {
        return interest;
    }

    public void setInterest(Integer interest) {
        this.interest = interest;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(Integer maxLimit) {
        this.maxLimit = maxLimit;
    }

    public Integer getMinLimit() {
        return minLimit;
    }

    public void setMinLimit(Integer minLimit) {
        this.minLimit = minLimit;
    }

    public Boolean getTopUp() {
        return topUp;
    }

    public void setTopUp(Boolean topUp) {
        this.topUp = topUp;
    }

    public Boolean getRollOver() {
        return rollOver;
    }

    public void setRollOver(Boolean rollOver) {
        this.rollOver = rollOver;
    }

    public Boolean getDailyInterest() {
        return dailyInterest;
    }

    public void setDailyInterest(Boolean dailyInterest) {
        this.dailyInterest = dailyInterest;
    }

    public Boolean getInterestUpfront() {
        return interestUpfront;
    }

    public void setInterestUpfront(Boolean interestUpfront) {
        this.interestUpfront = interestUpfront;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTimeSpan() {
        return timeSpan;
    }

    public void setTimeSpan(String timeSpan) {
        this.timeSpan = timeSpan;
    }

    @Override
    public String toString() {
        return "Products{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", isActive=" + isActive +
                ", term=" + term +
                ", interest=" + interest +
                ", maxLimit=" + maxLimit +
                ", minLimit=" + minLimit +
                ", topUp=" + topUp +
                ", rollOver=" + rollOver +
                ", dailyInterest=" + dailyInterest +
                ", interestUpfront=" + interestUpfront +
                ", transactionType='" + transactionType + '\'' +
                ", timeSpan='" + timeSpan + '\'' +
                '}';
    }
}
