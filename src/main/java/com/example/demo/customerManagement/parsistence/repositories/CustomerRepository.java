package com.example.demo.customerManagement.parsistence.repositories;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    
    Optional<Customer> findByDocumentNumber(String documentNumber);
    
    Optional<Customer> findByEmail(String email);
    
    Optional<Customer> findByMemberNumber(String memberNumber);
    
    Optional<Customer> findByExternalId(String externalId);
}
