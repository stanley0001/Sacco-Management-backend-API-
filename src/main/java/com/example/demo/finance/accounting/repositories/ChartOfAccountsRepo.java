package com.example.demo.finance.accounting.repositories;

import com.example.demo.finance.accounting.entities.ChartOfAccounts;
import com.example.demo.finance.accounting.entities.ChartOfAccounts.AccountType;
import com.example.demo.finance.accounting.entities.ChartOfAccounts.AccountCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChartOfAccountsRepo extends JpaRepository<ChartOfAccounts, Long> {
    
    Optional<ChartOfAccounts> findByAccountCode(String accountCode);
    
    List<ChartOfAccounts> findByAccountType(AccountType accountType);
    
    List<ChartOfAccounts> findByAccountCategory(AccountCategory accountCategory);
    
    List<ChartOfAccounts> findByIsActiveTrue();
    
    List<ChartOfAccounts> findByParentAccountCode(String parentAccountCode);
    
    boolean existsByAccountCode(String accountCode);
    
    List<ChartOfAccounts> findByAccountNameContainingIgnoreCase(String searchTerm);
}
