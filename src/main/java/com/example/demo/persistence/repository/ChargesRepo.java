package com.example.demo.persistence.repository;

import com.example.demo.model.Charges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargesRepo extends JpaRepository<Charges,Long> {
    Optional<Charges> findByProductIdAndName(String productId, String name);
}
