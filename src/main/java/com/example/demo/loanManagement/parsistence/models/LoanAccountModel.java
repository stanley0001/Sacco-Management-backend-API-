package com.example.demo.loanManagement.parsistence.models;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Data
public class LoanAccountModel {
    private Long applicationId;
    private String OtherRef;
    private Float amount;
    private Float interest;
    private Float totalRepayment;
    private Integer installments;
    private Float payableAmount;
    private Float accountBalance;
    private  LocalDateTime startDate;
    private  LocalDateTime dueDate;
    private  String status;
    private  String customerId;
    private  String loanRef;

}
