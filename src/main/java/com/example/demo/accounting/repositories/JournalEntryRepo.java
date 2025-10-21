package com.example.demo.accounting.repositories;

import com.example.demo.accounting.entities.JournalEntry;
import com.example.demo.accounting.entities.JournalEntry.JournalStatus;
import com.example.demo.accounting.entities.JournalEntry.JournalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepo extends JpaRepository<JournalEntry, Long> {
    
    Optional<JournalEntry> findByJournalNumber(String journalNumber);
    
    List<JournalEntry> findByStatus(JournalStatus status);
    
    List<JournalEntry> findByJournalType(JournalType journalType);
    
    List<JournalEntry> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT j FROM JournalEntry j WHERE j.transactionDate >= :startDate AND j.transactionDate <= :endDate AND j.status = :status")
    List<JournalEntry> findByDateRangeAndStatus(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("status") JournalStatus status
    );
    
    List<JournalEntry> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    
    @Query("SELECT j FROM JournalEntry j WHERE j.isBalanced = false")
    List<JournalEntry> findUnbalancedEntries();
    
    boolean existsByJournalNumber(String journalNumber);
}
