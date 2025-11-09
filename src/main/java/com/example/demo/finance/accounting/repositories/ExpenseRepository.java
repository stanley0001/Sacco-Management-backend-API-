package com.example.demo.finance.accounting.repositories;

import com.example.demo.finance.accounting.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    Optional<Expense> findByExpenseNumber(String expenseNumber);
    
    List<Expense> findByStatus(Expense.ExpenseStatus status);
    
    List<Expense> findByExpenseDateBetweenOrderByExpenseDateDesc(LocalDate startDate, LocalDate endDate);
    
    List<Expense> findByCategoryIdOrderByExpenseDateDesc(Long categoryId);
    
    List<Expense> findByCreatedByOrderByExpenseDateDesc(String createdBy);
    
    @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.expenseDate BETWEEN :startDate AND :endDate AND e.status = 'PAID'")
    Double getTotalExpenses(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.category.id = :categoryId AND e.expenseDate BETWEEN :startDate AND :endDate AND e.status = 'PAID'")
    Double getTotalExpensesByCategory(@Param("categoryId") Long categoryId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
