package com.example.demo.loanManagement.services;


import com.example.demo.loanManagement.parsistence.models.Charges;

import java.util.Optional;

public interface ChargeService {
    Charges createCharge(Charges charge);
    Optional<Charges> getChargeByProductIdAndName(String productId, String name);
}
