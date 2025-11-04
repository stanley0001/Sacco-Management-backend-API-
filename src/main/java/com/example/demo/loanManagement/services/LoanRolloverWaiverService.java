package com.example.demo.loanManagement.services;

import com.example.demo.loanManagement.parsistence.entities.*;
import com.example.demo.loanManagement.parsistence.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for handling loan rollovers and interest waivers
 */
@Service
@RequiredArgsConstructor
public class LoanRolloverWaiverService {
    
    private final LoanRolloverRepository loanRolloverRepository;
    private final LoanWaiverRepository loanWaiverRepository;
    private final ProductRepo productRepo;
    
    /**
     * Process loan rollover: Member pays interest and creates new loan with principal + 500 fee
     */
    @Transactional
    public LoanRollover processRollover(Long originalLoanId, Long customerId, 
                                       Double outstandingPrincipal, Double interestPaid,
                                       Integer newTerm, LocalDateTime originalDueDate) {
        
        // Calculate rollover details
        Double applicationFee = 500.0; // As per requirements
        Double newPrincipal = outstandingPrincipal + applicationFee;
        
        // Calculate new due date (same duration as original)
        LocalDateTime newDueDate = LocalDateTime.now().plus(newTerm, getChronoUnit("MONTHS"));
        
        LoanRollover rollover = LoanRollover.builder()
                .originalLoanId(originalLoanId)
                .customerId(customerId)
                .outstandingPrincipal(outstandingPrincipal)
                .interestPaid(interestPaid)
                .applicationFee(applicationFee)
                .newPrincipal(newPrincipal)
                .newTerm(newTerm)
                .originalDueDate(originalDueDate)
                .newDueDate(newDueDate)
                .status("PENDING")
                .build();
        
        return loanRolloverRepository.save(rollover);
    }
    
    /**
     * Approve loan rollover
     */
    @Transactional
    public LoanRollover approveRollover(Long rolloverId, Long newLoanId, String approvedBy) {
        LoanRollover rollover = loanRolloverRepository.findById(rolloverId)
                .orElseThrow(() -> new RuntimeException("Rollover not found"));
        
        rollover.setNewLoanId(newLoanId);
        rollover.setApprovedBy(approvedBy);
        rollover.setStatus("COMPLETED");
        
        return loanRolloverRepository.save(rollover);
    }
    
    /**
     * Process interest waiver for early loan payment
     * Example: 6 month loan paid in 2 months - waive months 4, 5, 6 interest
     */
    @Transactional
    public LoanWaiver processEarlyPaymentWaiver(Long loanId, Long customerId, 
                                                Long productId, Integer originalTerm,
                                                Integer monthsPaid, Double originalInterest) {
        
        // Get product to check if waiving is allowed
        Products product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (!product.getAllowInterestWaiving() || !product.getWaiveOnEarlyPayment()) {
            throw new RuntimeException("Interest waiving not allowed for this product");
        }
        
        // Calculate waived interest
        Integer monthsWaived = originalTerm - monthsPaid;
        Double interestPerMonth = originalInterest / originalTerm;
        Double waivedInterest = interestPerMonth * monthsWaived;
        Double remainingInterest = originalInterest - waivedInterest;
        
        LoanWaiver waiver = LoanWaiver.builder()
                .loanId(loanId)
                .customerId(customerId)
                .waiverType("EARLY_PAYMENT")
                .originalInterest(originalInterest)
                .waivedInterest(waivedInterest)
                .remainingInterest(remainingInterest)
                .monthsPaidEarly(monthsPaid)
                .monthsWaived(monthsWaived)
                .paymentDate(LocalDateTime.now())
                .status("APPROVED")
                .autoWaived(true)
                .reason("Early loan repayment - Auto waiver of future interest")
                .build();
        
        return loanWaiverRepository.save(waiver);
    }
    
    /**
     * Calculate interest to waive based on payment schedule
     */
    public Double calculateWaiverAmount(Double totalInterest, Integer totalMonths, Integer monthsPaid) {
        if (monthsPaid >= totalMonths) {
            return 0.0; // No waiver if full term completed
        }
        
        Double interestPerMonth = totalInterest / totalMonths;
        Integer remainingMonths = totalMonths - monthsPaid;
        
        return interestPerMonth * remainingMonths;
    }
    
    /**
     * Check if loan qualifies for rollover
     */
    public boolean canRollover(Long productId, Long customerId) {
        Products product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (!product.getRollOver()) {
            return false;
        }
        
        // Check customer's rollover history
        Long rolloverCount = loanRolloverRepository.countRolloversByCustomer(customerId);
        
        // Can add business rules here (e.g., max 3 rollovers per customer)
        return rolloverCount < 5;
    }
    
    /**
     * Get customer's rollover history
     */
    public List<LoanRollover> getCustomerRollovers(Long customerId) {
        return loanRolloverRepository.findByCustomerId(customerId);
    }
    
    /**
     * Get customer's waiver history
     */
    public List<LoanWaiver> getCustomerWaivers(Long customerId) {
        return loanWaiverRepository.findByCustomerId(customerId);
    }
    
    /**
     * Get total waived interest for a loan
     */
    public Double getTotalWaivedInterest(Long loanId) {
        Double total = loanWaiverRepository.getTotalWaivedInterestByLoan(loanId);
        return total != null ? total : 0.0;
    }
    
    /**
     * Helper method to convert time span string to ChronoUnit
     */
    private ChronoUnit getChronoUnit(String timeSpan) {
        switch (timeSpan.toUpperCase()) {
            case "DAYS":
                return ChronoUnit.DAYS;
            case "WEEKS":
                return ChronoUnit.WEEKS;
            case "MONTHS":
                return ChronoUnit.MONTHS;
            case "YEARS":
                return ChronoUnit.YEARS;
            default:
                return ChronoUnit.DAYS;
        }
    }
}
