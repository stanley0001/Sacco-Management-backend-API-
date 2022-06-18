package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.models.Charges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargesRepo extends JpaRepository<Charges,Long> {
    Optional<Charges> findByProductIdAndName(String productId, String name);
}
