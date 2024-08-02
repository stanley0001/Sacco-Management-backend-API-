package com.example.demo.loanManagement.parsistence.models;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LoanBookUpload {
    private String customerName;
    private String documentNumber;
    private String phoneNumber;
    private String loanRef;
    private String loanStatus;
    private String loanAmount;
    private String interest;
    private String penalties;
    private String balance;
    private String commencementDate;
    private String dueDate;
    private String productName;
}
