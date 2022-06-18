package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.models.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoanAccountRepo extends JpaRepository<LoanAccount, Long> {
    Optional<LoanAccount> findByApplicationId(Long id);

    Optional<LoanAccount> findByCustomerIdAndStatusNot(String customerId, String status);

    Optional<LoanAccount> findByCustomerIdAndStatus(String customerId, String status);
    //List<LoanAccount> findByCustomerIdAndStatus(String customerId, String status);

    List<LoanAccount> findByCustomerIdOrderByStartDateDesc(String customerId);

    @Query("select l from  LoanAccount l where l .startDate>= :date")
    List<LoanAccount> findAllByStartDateGreaterThan(@Param("date") LocalDateTime localDate1);
    /*@Query("SELECT l from LoanAccount l where l.status= :status and l.startDate between :fromDate and :toDate")
    List<LoanAccount> findAmountByStartDateAndStatus(@Param("fromDate") LocalDateTime from,@Param("toDate") LocalDateTime to,String status);

  */

    @Query("select sum(l.amount) from  LoanAccount l where l .startDate>= :date ")
    Integer findAmountByStartDateGreaterThan(@Param("date") LocalDateTime localDate1);

    List<LoanAccount> findByStatus(String status);


    @Query("select l from  LoanAccount l where l.customerId= :id and l.status=:status")
    List<LoanAccount> findByStatusAndCustomerId(@Param("id") String id, @Param("status") String aDefault);

    @Query("select l from  LoanAccount l where l.startDate between :from and :to and l.status=:status")
    List<LoanAccount> findAmountByStartDateAndStatus(@Param("from") LocalDateTime from,@Param("to") LocalDateTime to, @Param("status") String status);

    Optional<LoanAccount> findByLoanref(String loanNumber);
}
