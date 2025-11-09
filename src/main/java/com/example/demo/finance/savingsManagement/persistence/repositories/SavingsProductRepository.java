package com.example.demo.finance.savingsManagement.persistence.repositories;

import com.example.demo.finance.savingsManagement.persistence.entities.SavingsProduct;
import com.example.demo.finance.savingsManagement.persistence.entities.SavingsProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsProductRepository extends JpaRepository<SavingsProduct, Long> {
    
    Optional<SavingsProduct> findByCode(String code);
    
    Optional<SavingsProduct> findByName(String name);
    
    List<SavingsProduct> findByIsActive(Boolean isActive);
    
    boolean existsByCode(String code);
    
    boolean existsByName(String name);
}
