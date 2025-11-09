package com.example.demo.erp.customerManagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilterDTO {
    
    private String transactionType;
    private String dateFrom;
    private String dateTo;
    private Double minAmount;
    private Double maxAmount;
    private String searchTerm;
    private String status;
    private String paymentMethod;
    private String accountType;
}
