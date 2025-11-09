package com.example.demo.finance.loanManagement.services;


import com.example.demo.finance.loanManagement.parsistence.entities.Charges;

import java.util.Optional;

public interface ChargeService {
    Charges createCharge(Charges charge);
    Optional<Charges> getChargeByProductIdAndName(String productId, String name);
}
