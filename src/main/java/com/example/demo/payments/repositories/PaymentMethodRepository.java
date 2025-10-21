package com.example.demo.payments.repositories;

import com.example.demo.payments.entities.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    
    List<PaymentMethod> findByCustomerId(Long customerId);
    
    List<PaymentMethod> findByCustomerIdAndIsActive(Long customerId, Boolean isActive);
    
    Optional<PaymentMethod> findByCustomerIdAndIsPrimary(Long customerId, Boolean isPrimary);
    
    List<PaymentMethod> findByCustomerIdAndType(Long customerId, PaymentMethod.PaymentType type);
    
    Optional<PaymentMethod> findByPhoneNumber(String phoneNumber);
    
    Optional<PaymentMethod> findByBankAccountNumber(String accountNumber);
    
    List<PaymentMethod> findByCustomerIdAndIsVerified(Long customerId, Boolean isVerified);
    
    boolean existsByCustomerIdAndPhoneNumber(Long customerId, String phoneNumber);
}
