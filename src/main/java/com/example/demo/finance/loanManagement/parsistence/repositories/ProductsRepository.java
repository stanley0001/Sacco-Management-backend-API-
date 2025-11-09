package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Long> {
    
    /**
     * Find product by code
     */
    Optional<Products> findByCode(String code);
    
    /**
     * Find active products
     */
    @Query("SELECT p FROM Products p WHERE p.isActive = true")
    List<Products> findActiveProducts();
    
    /**
     * Find products by transaction type
     */
    List<Products> findByTransactionType(String transactionType);
    
    /**
     * Find products by name containing
     */
    List<Products> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find products by term range
     */
    @Query("SELECT p FROM Products p WHERE p.term >= :minTerm AND p.term <= :maxTerm AND p.isActive = true")
    List<Products> findByTermRange(@Param("minTerm") Integer minTerm, @Param("maxTerm") Integer maxTerm);
    
    /**
     * Find products by interest rate range
     */
    @Query("SELECT p FROM Products p WHERE p.interest >= :minRate AND p.interest <= :maxRate AND p.isActive = true")
    List<Products> findByInterestRange(@Param("minRate") Integer minRate, @Param("maxRate") Integer maxRate);
    
    /**
     * Find products by amount limits
     */
    @Query("SELECT p FROM Products p WHERE p.minLimit <= :amount AND p.maxLimit >= :amount AND p.isActive = true")
    List<Products> findByAmountRange(@Param("amount") Integer amount);
}
