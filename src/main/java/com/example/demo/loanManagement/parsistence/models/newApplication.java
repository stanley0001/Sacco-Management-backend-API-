package com.example.demo.loanManagement.parsistence.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class newApplication {
    String amount;
    String phone;
    String productCode;
    String installments;
}
