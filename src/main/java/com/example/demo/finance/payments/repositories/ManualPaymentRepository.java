package com.example.demo.finance.payments.repositories;

import com.example.demo.finance.payments.entities.ManualPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ManualPaymentRepository extends JpaRepository<ManualPayment, Long> {
    
    List<ManualPayment> findByStatus(ManualPayment.PaymentStatus status);
    
    List<ManualPayment> findByTargetTypeAndTargetId(String targetType, Long targetId);
    
    List<ManualPayment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<ManualPayment> findByPaymentMethod(String paymentMethod);
    
    List<ManualPayment> findByReceivedBy(String receivedBy);
    
    List<ManualPayment> findByApprovedBy(String approvedBy);
    
    List<ManualPayment> findByStatusAndPaymentMethod(ManualPayment.PaymentStatus status, String paymentMethod);
    
    List<ManualPayment> findByPostedToAccountingFalse();
    
    List<ManualPayment> findByRequiresApprovalTrue();
}
