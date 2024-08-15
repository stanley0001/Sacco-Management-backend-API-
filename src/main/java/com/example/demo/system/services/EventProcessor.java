package com.example.demo.system.services;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.serviceImplimentations.CustomerService;
import com.example.demo.enums.Statuses;
import com.example.demo.events.appEvents.CreateAccountEvent;
import com.example.demo.events.appEvents.CreateRepaymentScheduleEvent;
import com.example.demo.events.appEvents.SubscriptionEvent;
import com.example.demo.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.loanManagement.parsistence.entities.LoanRepaymentSchedules;
import com.example.demo.loanManagement.parsistence.entities.Products;
import com.example.demo.loanManagement.parsistence.models.LoanAccountModel;
import com.example.demo.loanManagement.parsistence.models.LoanBookUpload;
import com.example.demo.loanManagement.parsistence.models.RepaymentSchedules;
import com.example.demo.loanManagement.parsistence.repositories.RepaymentScheduleRepo;
import com.example.demo.loanManagement.services.LoanService;
import lombok.extern.apachecommons.CommonsLog;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
@Service
@Log4j2
public class EventProcessor {
    @Autowired
    CustomerService customerService;
    @Autowired
    LoanService loanService;
    @Autowired
    RepaymentScheduleRepo scheduleRepo;
    @Autowired
    ApplicationEventPublisher eventPublisher;
    ModelMapper mapper =new ModelMapper();
    public void uploadManualLoanBook(List<LoanBookUpload> data) {
        //        Loan Book upload
//        1.create customer
//        2.create subscription
//        3.create loan account
//        4.Post payment
//        6.sync loan status
        int icount=0;
        log.info("start for loop accounts {}",data.size());
        for (LoanBookUpload upload : data) {
            icount++;
            Customer customer = new Customer(upload);
            customer=this.createCustomerWithSubscripton(customer, upload.getProductName());
            //fire create account event
             eventPublisher.publishEvent(new CreateAccountEvent(this,upload,customer));
        }
        log.info("for end..at {}",icount);

    }
    public void createLoanAccount(LoanBookUpload upload,Customer customer){
        LoanAccount loanAccount = loanService.loadAccount(upload,customer);
        //fire create schedule event
        eventPublisher.publishEvent(new CreateRepaymentScheduleEvent(this,loanAccount));
    }
    private Customer createCustomerWithSubscripton(Customer customer,String product){
        //subscribe
        Products loanProduct=loanService.productService.findByProductCode(product);
        customer=customerService.saveCustomer(customer);
        if (loanProduct!=null && customer!=null){
            //fire subscription event
            eventPublisher.publishEvent(new SubscriptionEvent(this,loanProduct,customer));
        }
        return customer;
    }
    public void subscribeCustomer(Customer customer,Products product){
        customerService.subscriptionService.subscribe(customer.getPhoneNumber(),product.getId(),product.getMaxLimit());
    }

    public void createRepaymentSchedules(LoanAccount data) {
        LoanAccountModel accountModel=new LoanAccountModel();
        accountModel.setInterest(data.getPayableAmount()-data.getAmount());
        accountModel.setAmount(data.getAmount());
        accountModel.setStartDate(data.getStartDate());
        accountModel.setInstallments(data.getInstallments()!=null?data.getInstallments():1);
        accountModel.setAccountBalance(data.getAccountBalance());
        accountModel.setTotalRepayment(data.getPayableAmount());
        List<RepaymentSchedules> repaymentSchedules=loanService.getInstallments(accountModel);
        AtomicReference<Float> amountPaid= new AtomicReference<>(data.getAmountPaid());
        List<LoanRepaymentSchedules> schedules=repaymentSchedules.stream().map(e->{
            e.setLoanAccount(Math.toIntExact(data.getAccountId()));
            e.setStatus(Statuses.valueOf(data.getStatus()));
           if (data.getStatus().equalsIgnoreCase("PAID")) {
               e.setAmountPaid(e.getBalance());
               e.setBalance(0.0);
               e.setStatus(Statuses.valueOf("PAID"));
           }
           else{
               if (amountPaid.get() >0){
                  if (e.getBalance()<= amountPaid.get()) {
                      e.setStatus(Statuses.valueOf("PAID"));
                      e.setAmountPaid(e.getBalance());
                      e.setBalance(0.0);
                      amountPaid.updateAndGet(v -> (float) (v - e.getBalance()));
                  }else {
                      e.setBalance(e.getBalance()- amountPaid.get());
                      amountPaid.updateAndGet(v -> (float) (0));
                      e.setAmountPaid(e.getAmount()>e.getBalance()?e.getAmount()-e.getBalance():0);
                  }

               }
           }
           return mapper.map(e,LoanRepaymentSchedules.class);
        }).collect(Collectors.toList());
        scheduleRepo.saveAll(schedules);
    }


    //        Customer upload
//        1.create customer
//        2.create subscription
}
