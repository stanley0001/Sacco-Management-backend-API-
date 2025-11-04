package com.example.demo.customerManagement.parsistence.repositories;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRepo extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByphoneNumber(String customerPhone);
    Optional<Customer> findByDocumentNumber(String documentNumber);

    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByDocumentNumber(String documentNumber);

    @Query("select c from  Customer c where c.createdAt >= :date")
    List<Customer> findAllByCreatedAtBefore(@Param("date") LocalDate localDate1);

    @Query("select c from  Customer c where c.createdAt < :date")
    List<Customer> findAllByCreatedAtAfter(@Param("date")LocalDate localDate);

    Customer findByEmail(String recipient);

    Optional<Customer> findByExternalId(String customerId);

    @Query("SELECT c FROM Customer c WHERE (:status IS NULL OR c.accountStatusFlag = :status) " +
            "AND (:query IS NULL OR " +
            "LOWER(CONCAT(COALESCE(c.firstName, ''), ' ', COALESCE(c.lastName, ''))) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(COALESCE(c.documentNumber, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(COALESCE(c.phoneNumber, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    org.springframework.data.domain.Page<Customer> search(
            @Param("status") Boolean status,
            @Param("query") String query,
            org.springframework.data.domain.Pageable pageable);

}
