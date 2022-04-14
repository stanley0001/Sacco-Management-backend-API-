package com.example.demo.services;


import com.example.demo.model.Charges;

import java.util.Optional;

public interface ChargeService {
    Charges createCharge(Charges charge);
    Optional<Charges> getChargeByProductIdAndName(String productId, String name);
}
