package com.example.demo.services;

import com.example.demo.system.parsitence.models.Schedule.Schedule;
import com.example.demo.loanManagement.parsistence.models.loanTransactions;
import com.example.demo.system.parsitence.repositories.ScheduleRepo;
import com.example.demo.loanManagement.parsistence.repositories.TransactionsRepo;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@Log4j2
public class Backbone {
    public final TransactionsRepo transactionsRepo;
    public final ScheduleRepo scheduleRepo;
@Autowired
public Test test;
    public Backbone(TransactionsRepo transactionsRepo, ScheduleRepo scheduleRepo) {
        this.transactionsRepo = transactionsRepo;
        this.scheduleRepo = scheduleRepo;
    }

    public void createSchedule(Schedule schedule) {

        scheduleRepo.save(schedule);
    }

    public loanTransactions saveTransaction(String[] data){
//data[0]==accountNumber,data[1]==loanRef,data[2]==transactionType,data[3]==initialBal,data[4]==finalBal,data[5]==otherRef,data[6]==otherResponses
        log.info(data);
        loanTransactions transaction=new loanTransactions();
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setTransactionType(data[2]);
        transaction.setAccountNumber(data[0]);
        log.info("generating transaction code....");
        transaction.setLoanRef(base64encode(data[1]).toUpperCase());
        transaction.setInitialBalance(data[3]);
        transaction.setFinalBalance(data[4]);
        transaction.setOtherRef(data[5]);
        transaction.setOtherResponses(data[6]);
        transactionsRepo.save(transaction);
        log.info("saving transaction....");
        return transaction;
    }
    public String base64encode(String plainText){
        byte[] bytes = plainText.getBytes(StandardCharsets.UTF_8);
        String base36 = new BigInteger(1, bytes).toString(36);
        return  base36;
    }

    public String base64decode(String plainText){
        byte[] decodedString = Base64.decodeBase64(plainText);
       // return  decodedString.toString();
        return  plainText;
    }


}
