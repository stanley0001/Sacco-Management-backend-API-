package com.example.demo.system.parsitence.models;

import com.example.demo.loanManagement.parsistence.entities.LoanApplication;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashBoardData {
    private List<LoanApplication> applicationsToday;
    private String disbursementToday;
    private String collectionToday;
    private Integer amountDisbursedToday;
    private Double amountCollectedToday;
    private String totalDefaults;
    private String totalLeads;
    private String expectedToday;
    private Long membersCount;

}
