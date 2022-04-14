package com.example.demo.model.models;

import com.example.demo.model.loanApplication;

import java.util.List;

public class DashBoardData {
    private List<loanApplication> applicationsToday;
    private String disbursementToday;
    private String collectionToday;
    private Integer amountDisbursedToday;
    private Integer amountCollectedToday;
    private String totalDefaults;
    private String totalLeads;
    private String expectedToday;

    public List<loanApplication> getApplicationsToday() {
        return applicationsToday;
    }

    public void setApplicationsToday(List<loanApplication> applicationsToday) {
        this.applicationsToday = applicationsToday;
    }

    public Integer getAmountDisbursedToday() {
        return amountDisbursedToday;
    }

    public Integer getAmountCollectedToday() {
        return amountCollectedToday;
    }

    public String getDisbursementToday() {
        return disbursementToday;
    }

    public String getCollectionToday() {
        return collectionToday;
    }

    public String getTotalDefaults() {
        return totalDefaults;
    }

    public String getTotalLeads() {
        return totalLeads;
    }

    public String getExpectedToday() {
        return expectedToday;
    }

    public void setAmountDisbursedToday(Integer amountDisbursedToday) {
        this.amountDisbursedToday = amountDisbursedToday;
    }

    public void setAmountCollectedToday(Integer amountCollectedToday) {
        this.amountCollectedToday = amountCollectedToday;
    }


    public void setDisbursementToday(String disbursementToday) {
        this.disbursementToday = disbursementToday;
    }

    public void setCollectionToday(String collectionToday) {
        this.collectionToday = collectionToday;
    }

    public void setTotalDefaults(String totalDefaults) {
        this.totalDefaults = totalDefaults;
    }

    public void setTotalLeads(String totalLeads) {
        this.totalLeads = totalLeads;
    }

    public void setExpectedToday(String expectedToday) {
        this.expectedToday = expectedToday;
    }
}
