package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.Charges;
import com.example.demo.finance.loanManagement.parsistence.entities.Charges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargesRepo extends JpaRepository<Charges,Long> {
    Optional<Charges> findByProductIdAndName(String productId, String name);
}
