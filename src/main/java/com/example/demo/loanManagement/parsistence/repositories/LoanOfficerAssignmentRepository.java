package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.entities.LoanOfficerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanOfficerAssignmentRepository extends JpaRepository<LoanOfficerAssignment, Long> {
    
    /**
     * Find all active assignments for a loan officer
     */
    @Query("SELECT loa FROM LoanOfficerAssignment loa WHERE loa.loanOfficer.id = :officerId AND loa.isActive = true")
    List<LoanOfficerAssignment> findActiveByLoanOfficerId(@Param("officerId") Long officerId);
    
    /**
     * Find all assignments for a customer
     */
    @Query("SELECT loa FROM LoanOfficerAssignment loa WHERE loa.customer.id = :customerId")
    List<LoanOfficerAssignment> findByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * Find active assignment for a specific customer
     */
    @Query("SELECT loa FROM LoanOfficerAssignment loa WHERE loa.customer.id = :customerId AND loa.isActive = true")
    Optional<LoanOfficerAssignment> findActiveByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * Find all assignments by county (for credit managers)
     */
    @Query("SELECT loa FROM LoanOfficerAssignment loa WHERE loa.county = :county AND loa.isActive = true")
    List<LoanOfficerAssignment> findActiveByCounty(@Param("county") String county);
    
    /**
     * Find all assignments by loan officer and county
     */
    @Query("SELECT loa FROM LoanOfficerAssignment loa WHERE loa.loanOfficer.id = :officerId AND loa.county = :county AND loa.isActive = true")
    List<LoanOfficerAssignment> findActiveByOfficerAndCounty(@Param("officerId") Long officerId, @Param("county") String county);
    
    /**
     * Find all assignments by branch
     */
    @Query("SELECT loa FROM LoanOfficerAssignment loa WHERE loa.branchCode = :branchCode AND loa.isActive = true")
    List<LoanOfficerAssignment> findActiveByBranchCode(@Param("branchCode") String branchCode);
    
    /**
     * Count active clients for a loan officer
     */
    @Query("SELECT COUNT(loa) FROM LoanOfficerAssignment loa WHERE loa.loanOfficer.id = :officerId AND loa.isActive = true")
    Long countActiveByLoanOfficerId(@Param("officerId") Long officerId);
    
    /**
     * Find assignments for portfolio report within date range
     */
    @Query("SELECT loa FROM LoanOfficerAssignment loa WHERE loa.loanOfficer.id = :officerId " +
           "AND loa.isActive = true AND loa.assignmentDate BETWEEN :startDate AND :endDate")
    List<LoanOfficerAssignment> findByOfficerAndDateRange(
        @Param("officerId") Long officerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
