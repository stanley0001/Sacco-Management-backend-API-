package com.example.demo.branch.repositories;

import com.example.demo.branch.entities.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    
    /**
     * Find branch by branch code
     */
    Optional<Branch> findByBranchCode(String branchCode);
    
    /**
     * Find all active branches
     */
    List<Branch> findByIsActiveTrue();
    
    /**
     * Find branches by name containing text (case insensitive)
     */
    @Query("SELECT b FROM Branch b WHERE LOWER(b.branchName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Branch> findByBranchNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find branches by manager name
     */
    @Query("SELECT b FROM Branch b WHERE LOWER(b.managerName) LIKE LOWER(CONCAT('%', :managerName, '%'))")
    List<Branch> findByManagerNameContainingIgnoreCase(@Param("managerName") String managerName);
}
