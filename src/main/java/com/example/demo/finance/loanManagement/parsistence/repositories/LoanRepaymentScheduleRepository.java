package com.example.demo.finance.loanManagement.parsistence.repositories;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanRepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepaymentScheduleRepository extends JpaRepository<LoanRepaymentSchedule, Long> {
    
    /**
     * Find schedules by loan account ID
     */
    List<LoanRepaymentSchedule> findByLoanAccountId(Long loanAccountId);
    
    /**
     * Find schedules by loan account ID ordered by installment number
     */
    List<LoanRepaymentSchedule> findByLoanAccountIdOrderByInstallmentNumber(Long loanAccountId);
    
    /**
     * Find schedules by loan account ID and status
     */
    List<LoanRepaymentSchedule> findByLoanAccountIdAndStatus(Long loanAccountId, LoanRepaymentSchedule.ScheduleStatus status);
    
    /**
     * Find overdue schedules
     */
    @Query("SELECT ls FROM LoanRepaymentSchedule ls WHERE ls.dueDate < CURRENT_DATE AND (ls.status = :current OR ls.status = :overdue OR ls.status = :partial)")
    List<LoanRepaymentSchedule> findOverdueSchedules(
        @Param("current") LoanRepaymentSchedule.ScheduleStatus current,
        @Param("overdue") LoanRepaymentSchedule.ScheduleStatus overdue,
        @Param("partial") LoanRepaymentSchedule.ScheduleStatus partial
    );
    
    /**
     * Find overdue schedules (convenience method)
     */
    default List<LoanRepaymentSchedule> findOverdueSchedules() {
        return findOverdueSchedules(LoanRepaymentSchedule.ScheduleStatus.CURRENT, 
                                  LoanRepaymentSchedule.ScheduleStatus.OVERDUE, 
                                  LoanRepaymentSchedule.ScheduleStatus.PARTIAL);
    }
    
    /**
     * Find schedules due within date range
     */
    @Query("SELECT ls FROM LoanRepaymentSchedule ls WHERE ls.dueDate BETWEEN :startDate AND :endDate")
    List<LoanRepaymentSchedule> findByDueDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find current and upcoming schedules for a loan
     */
    @Query("SELECT ls FROM LoanRepaymentSchedule ls WHERE ls.loanAccountId = :loanAccountId AND (ls.status = :pending OR ls.status = :current) ORDER BY ls.installmentNumber")
    List<LoanRepaymentSchedule> findUpcomingSchedules(
        @Param("loanAccountId") Long loanAccountId,
        @Param("pending") LoanRepaymentSchedule.ScheduleStatus pending,
        @Param("current") LoanRepaymentSchedule.ScheduleStatus current
    );
    
    /**
     * Find current and upcoming schedules for a loan (convenience method)
     */
    default List<LoanRepaymentSchedule> findUpcomingSchedules(Long loanAccountId) {
        return findUpcomingSchedules(loanAccountId, 
                                   LoanRepaymentSchedule.ScheduleStatus.PENDING, 
                                   LoanRepaymentSchedule.ScheduleStatus.CURRENT);
    }
    
    /**
     * Get next due installment for a loan
     */
    @Query("SELECT ls FROM LoanRepaymentSchedule ls WHERE ls.loanAccountId = :loanAccountId AND (ls.status = :pending OR ls.status = :current OR ls.status = :overdue) ORDER BY ls.installmentNumber")
    LoanRepaymentSchedule findNextDueInstallment(
        @Param("loanAccountId") Long loanAccountId,
        @Param("pending") LoanRepaymentSchedule.ScheduleStatus pending,
        @Param("current") LoanRepaymentSchedule.ScheduleStatus current,
        @Param("overdue") LoanRepaymentSchedule.ScheduleStatus overdue
    );
    
    /**
     * Get next due installment for a loan (convenience method)
     */
    default LoanRepaymentSchedule findNextDueInstallment(Long loanAccountId) {
        return findNextDueInstallment(loanAccountId,
                                    LoanRepaymentSchedule.ScheduleStatus.PENDING,
                                    LoanRepaymentSchedule.ScheduleStatus.CURRENT,
                                    LoanRepaymentSchedule.ScheduleStatus.OVERDUE);
    }
    
    /**
     * Get total outstanding amount for a loan
     */
    @Query("SELECT COALESCE(SUM(ls.totalOutstanding), 0) FROM LoanRepaymentSchedule ls WHERE ls.loanAccountId = :loanAccountId")
    BigDecimal getTotalOutstandingByLoanAccountId(@Param("loanAccountId") Long loanAccountId);
    
    /**
     * Count overdue schedules for a loan
     */
    @Query("SELECT COUNT(ls) FROM LoanRepaymentSchedule ls WHERE ls.loanAccountId = :loanAccountId AND ls.status = :status")
    Long countOverdueSchedules(@Param("loanAccountId") Long loanAccountId, @Param("status") LoanRepaymentSchedule.ScheduleStatus status);
    
    /**
     * Count overdue schedules for a loan (convenience method)
     */
    default Long countOverdueSchedules(Long loanAccountId) {
        return countOverdueSchedules(loanAccountId, LoanRepaymentSchedule.ScheduleStatus.OVERDUE);
    }
    
    /**
     * Find schedules by status
     */
    List<LoanRepaymentSchedule> findByStatus(LoanRepaymentSchedule.ScheduleStatus status);
    
    /**
     * Find schedules with outstanding amount greater than specified value
     */
    @Query("SELECT ls FROM LoanRepaymentSchedule ls WHERE ls.totalOutstanding >= :amount")
    List<LoanRepaymentSchedule> findByTotalOutstandingGreaterThanEqual(@Param("amount") BigDecimal amount);
    
    /**
     * Find paid schedules within date range
     */
    @Query("SELECT ls FROM LoanRepaymentSchedule ls WHERE ls.status = :status AND ls.paidDate BETWEEN :startDate AND :endDate")
    List<LoanRepaymentSchedule> findPaidSchedulesBetween(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate,
        @Param("status") LoanRepaymentSchedule.ScheduleStatus status
    );
    
    /**
     * Find paid schedules within date range (convenience method)
     */
    default List<LoanRepaymentSchedule> findPaidSchedulesBetween(LocalDate startDate, LocalDate endDate) {
        return findPaidSchedulesBetween(startDate, endDate, LoanRepaymentSchedule.ScheduleStatus.PAID);
    }
    
    /**
     * Get payment summary for a loan account
     */
    @Query("SELECT NEW map(" +
           "COALESCE(SUM(ls.totalAmount), 0) as totalScheduled, " +
           "COALESCE(SUM(ls.totalPaid), 0) as totalPaid, " +
           "COALESCE(SUM(ls.totalOutstanding), 0) as totalOutstanding, " +
           "COALESCE(SUM(ls.penaltyAmount), 0) as totalPenalty, " +
           "COUNT(ls) as totalInstallments, " +
           "COUNT(CASE WHEN ls.status = :paidStatus THEN 1 END) as paidInstallments" +
           ") FROM LoanRepaymentSchedule ls WHERE ls.loanAccountId = :loanAccountId")
    java.util.Map<String, Object> getPaymentSummary(
        @Param("loanAccountId") Long loanAccountId,
        @Param("paidStatus") LoanRepaymentSchedule.ScheduleStatus paidStatus
    );
    
    /**
     * Get payment summary for a loan account (convenience method)
     */
    default java.util.Map<String, Object> getPaymentSummary(Long loanAccountId) {
        return getPaymentSummary(loanAccountId, LoanRepaymentSchedule.ScheduleStatus.PAID);
    }
    
    /**
     * Delete schedules by loan account ID
     */
    void deleteByLoanAccountId(Long loanAccountId);
    
    /**
     * Find schedules by installment number
     */
    List<LoanRepaymentSchedule> findByLoanAccountIdAndInstallmentNumber(Long loanAccountId, Integer installmentNumber);
    
    /**
     * Find unpaid schedules ordered by due date (for payment allocation)
     */
    List<LoanRepaymentSchedule> findByLoanAccountIdAndStatusNotOrderByDueDateAsc(Long loanAccountId, LoanRepaymentSchedule.ScheduleStatus status);
    
    /**
     * Find schedules by due date and status
     */
    List<LoanRepaymentSchedule> findByDueDateAndStatusNot(LocalDate dueDate, LoanRepaymentSchedule.ScheduleStatus status);
    
    /**
     * Find schedules by status and due date before (for overdue notifications)
     */
    List<LoanRepaymentSchedule> findByStatusAndDueDateBefore(LoanRepaymentSchedule.ScheduleStatus status, LocalDate date);
}
