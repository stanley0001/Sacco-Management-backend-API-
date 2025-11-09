package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.Products;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepo extends JpaRepository<Products, Long> {

    Optional<Products> getByCode(String code);

    Products findByCode(String productCode);
    
    Optional<Products> findByName(String name);
}
