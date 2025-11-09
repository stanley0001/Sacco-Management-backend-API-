package com.example.demo.finance.loanManagement.parsistence.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "loan_book_upload")
@Data
@ToString
public class LoanBookUpload {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
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
    private Integer installments;
}
