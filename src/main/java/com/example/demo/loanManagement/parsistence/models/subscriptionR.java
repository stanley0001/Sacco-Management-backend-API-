package com.example.demo.loanManagement.parsistence.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class subscriptionR {
        private String phone;
        private Long productId;
        private Integer amount;



}
