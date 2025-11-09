package com.example.demo.finance.savingsManagement.persistence.repositories;

import com.example.demo.finance.savingsManagement.persistence.entities.SavingsAccount;
import com.example.demo.finance.savingsManagement.persistence.entities.SavingsAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {
    
    Optional<SavingsAccount> findByAccountNumber(String accountNumber);
    
    List<SavingsAccount> findByCustomerId(Long customerId);
    
    List<SavingsAccount> findByStatus(String status);
    
    Page<SavingsAccount> findByCustomerId(Long customerId, Pageable pageable);
    
    Page<SavingsAccount> findByStatus(String status, Pageable pageable);
    
    Page<SavingsAccount> findByProductCode(String productCode, Pageable pageable);
    
    @Query("SELECT s FROM SavingsAccount s WHERE s.customerId = :customerId AND s.status = :status")
    List<SavingsAccount> findByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                                     @Param("status") String status);
    
    @Query("SELECT SUM(s.balance) FROM SavingsAccount s WHERE s.status = 'ACTIVE'")
    BigDecimal getTotalSavingsBalance();
    
    @Query("SELECT COUNT(s) FROM SavingsAccount s WHERE s.status = 'ACTIVE'")
    Long countActiveSavingsAccounts();
    
    @Query("SELECT s FROM SavingsAccount s WHERE s.lastTransactionDate < :date AND s.status = 'ACTIVE'")
    List<SavingsAccount> findDormantAccounts(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM SavingsAccount s WHERE s.balance < s.minimumBalance AND s.status = 'ACTIVE'")
    List<SavingsAccount> findAccountsBelowMinimumBalance();
}
