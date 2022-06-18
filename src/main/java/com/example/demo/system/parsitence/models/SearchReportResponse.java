package com.example.demo.model.models;

import java.util.ArrayList;
import java.util.List;

public class SearchReportResponse {

    private List<SReportData> loanAccounts = new ArrayList<>();
    private Integer count;
    private String totalAmount;
    private String totalBalance;
    private String totalPayable;
    private String TotalPaid;
    private String revenue;
    private String expectedRevenue;
    private String loss;


    public List<SReportData> getLoanAccounts() {
        return loanAccounts;
    }

    public void setLoanAccounts(List<SReportData> loanAccounts) {
        if (loanAccounts == null) {
            this.loanAccounts = new ArrayList<>();
        } else {
            this.loanAccounts = loanAccounts;
        }
    }

    public String getLoss() {
        return loss;
    }

    public void setLoss(String loss) {
        this.loss = loss;
    }

    public String getExpectedRevenue() {
        return expectedRevenue;
    }

    public void setExpectedRevenue(String expectedRevenue) {
        this.expectedRevenue = expectedRevenue;
    }

    public String getTotalPayable() {
        return totalPayable;
    }

    public void setTotalPayable(String totalPayable) {
        this.totalPayable = totalPayable;
    }

    public String getTotalPaid() {
        return TotalPaid;
    }

    public void setTotalPaid(String TotalPaid) {
        this.TotalPaid = TotalPaid;
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(String totalBalance) {
        this.totalBalance = totalBalance;
    }


}
