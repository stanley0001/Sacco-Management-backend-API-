package com.example.demo.accounting.repositories;

import com.example.demo.accounting.entities.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    
    Optional<ExpenseCategory> findByCode(String code);
    
    List<ExpenseCategory> findByIsActiveTrueOrderByNameAsc();
    
    List<ExpenseCategory> findByParentIdOrderByNameAsc(Long parentId);
    
    boolean existsByCode(String code);
}
