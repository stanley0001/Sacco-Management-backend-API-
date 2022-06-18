package com.example.demo.system.services;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.loanManagement.parsistence.models.LoanAccount;
import com.example.demo.loanManagement.parsistence.models.LoanStates;
import com.example.demo.customerManagement.parsistence.repositories.CustomerRepo;
import com.example.demo.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.loanManagement.parsistence.repositories.LoanStatesRepo;
import com.example.demo.loanManagement.parsistence.repositories.ProductRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Log4j2
public class ScoreService {
    Integer totalScore=0;
    public  final CustomerRepo customerRepo;
   // public final ProductRepo productRepo;
    public final LoanAccountRepo accountRepo;
    public final LoanStatesRepo statesRepo;

    public ScoreService(CustomerRepo customerRepo, ProductRepo productRepo, LoanAccountRepo accountRepo, LoanStatesRepo statesRepo) {
        this.customerRepo = customerRepo;
     //   this.productRepo = productRepo;
        this.accountRepo = accountRepo;
        this.statesRepo = statesRepo;
    }

    public Integer loadData(Long id){
        Customer client=customerRepo.findById(id).get();
       // Products product=productRepo.findByCode(productCode);
        List<LoanAccount> allAccounts=accountRepo.findByCustomerIdOrderByStartDateDesc(id.toString());
        List<LoanAccount> defaultAccounts=accountRepo.findByStatusAndCustomerId(id.toString(),"DEFAULT");
        Boolean isDefault=Boolean.FALSE;
        Integer cusAge= LocalDate.now().getYear()-client.getDob().getYear();
      //  Integer maxValue=product.getMaxLimit();
        for (LoanAccount account:allAccounts

             ) {
            log.info("account, {}",account);
            List<LoanStates> states=statesRepo.findByAccountNumber(account.getAccountId().toString());
            for (LoanStates state:states
                 ) {
                if (state.getStatus()=="DEFAULT"){
                    log.warn("default one found");
                    isDefault=Boolean.TRUE;
                }
                log.info("state, {}",state.getStatus());


            }
            if (isDefault==Boolean.TRUE){
                defaultAccounts.add(account);
            }
        }
        Integer numberOfAccounts=allAccounts.size();
        Integer numberOdDefaults=defaultAccounts.size();
        Float defaultRate = null;
        if (numberOdDefaults>0){
            log.info("number of defaults {}",numberOdDefaults);
            defaultRate=Float.valueOf(numberOdDefaults/numberOfAccounts*100);
            Integer sum=1/2;
            log.info("simple division {}",sum);
        }
        log.info("Default rate is, {} and total accounts are {}",defaultRate,numberOfAccounts);
        //age scoring
        Integer ageScore=0;
        Integer defaultScore=0;
        Integer transactionScore=0;
          if (cusAge<20){
              ageScore=1;
              log.info("age below 20");
          }
          if (cusAge>80){
              ageScore=2;
              log.info("age above 80");
          }
          if (cusAge >20 && cusAge<38){
              ageScore=5;
              log.info("age between 25 and 38");
          }
          if (cusAge>38 && cusAge<55){
              ageScore=10;
              log.info("jackpot age score");
          }
          if (cusAge>55 && cusAge<80){
              log.info("between 55 and 80");
              ageScore=7;
          }
          if (defaultRate==0 && numberOfAccounts>1){
              defaultScore=10;
              log.info("jackpot default score");
          }
        if (defaultRate==0 && numberOfAccounts==0){
            defaultScore=5;
            log.info("new client");
        }
        if (defaultRate>70 && defaultRate<80){
            defaultScore=2;
            log.info(" default rate between 70 and 80");
        }
        if (defaultRate>80 && defaultRate<100){
            defaultScore=1;
            log.info(" default rate between 80 and 100");
        }
        if (defaultRate>50 && defaultRate<70){
            defaultScore=3;
            log.info(" default rate between 50 and 70");
        }
        if (defaultRate>40 && defaultRate<50){
            defaultScore=4;
            log.info(" default rate between 40 and 50");
        }
        if (defaultRate>30 && defaultRate<40){
            defaultScore=5;
            log.info(" default rate between 30 and 40");
        }
        if (defaultRate>20 && defaultRate<30){
            defaultScore=6;
            log.info(" default rate between 20 and 30");
        }
        if (defaultRate>10 && defaultRate<20){
            defaultScore=7;
            log.info(" default rate between 10 and 20");
        }
        if (defaultRate>0 && defaultRate<10){
            defaultScore=8;
            log.info(" default rate between 0 and 10");
        }
        if (numberOfAccounts<10){
            transactionScore=2;
            log.info(" transaction  less than 10");
        }
        if (numberOfAccounts>60){
            transactionScore=10;
            log.info("jackpot transaction score");
        }
        if (numberOfAccounts>10 && numberOfAccounts<30){
            transactionScore=5;
            log.info(" transaction  between 10 and 30");
        }
        if (numberOfAccounts>30 && numberOfAccounts<50){
            transactionScore=7;
            log.info(" transaction  between 30 and 50");
        }
        if (numberOfAccounts>50 && numberOfAccounts<60){
            transactionScore=8;
            log.info(" transaction  between 50 and 60");
        }
       totalScore=transactionScore+defaultScore+ageScore;
        return totalScore;
    }

    public Integer score(){
         Integer score=0;


        return score;
    }



}
