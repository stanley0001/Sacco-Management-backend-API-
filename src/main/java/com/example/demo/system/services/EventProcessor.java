package com.example.demo.system.services;

import com.example.demo.finance.banking.services.BankingService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.serviceImplimentations.CustomerService;
import com.example.demo.system.events.appEvents.CreateAccountEvent;
import com.example.demo.system.events.appEvents.CreateRepaymentScheduleEvent;
import com.example.demo.system.events.appEvents.SubscriptionEvent;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.Products;
import com.example.demo.finance.loanManagement.parsistence.models.LoanBookUpload;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanRepaymentScheduleRepository;
import com.example.demo.finance.loanManagement.services.LoanService;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class EventProcessor {
    @Autowired
    CustomerService customerService;
    @Autowired
    LoanService loanService;
    @Autowired
    LoanRepaymentScheduleRepository scheduleRepo;
    @Autowired
    ApplicationEventPublisher eventPublisher;
    @Autowired
    BankingService bankingService;
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
            bankingService.createBankAccounts(customer);
            bankingService.processInitialDepositIfPresent(customer);
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
        log.warn("createRepaymentSchedules is temporarily disabled during entity migration");
        /* FIXME: Update this method to work with LoanRepaymentSchedule entity
        LoanAccountModel accountModel=new LoanAccountModel();
        accountModel.setInterest(data.getPayableAmount()-data.getAmount());
        accountModel.setAmount(data.getAmount());
        accountModel.setStartDate(data.getStartDate());
        accountModel.setInstallments(data.getInstallments()!=null?data.getInstallments():1);
        accountModel.setAccountBalance(data.getAccountBalance());
        accountModel.setTotalRepayment(data.getPayableAmount());
        
        // Need to implement proper mapping to new LoanRepaymentSchedule entity structure
        */
    }


    //        Customer upload
//        1.create customer
//        2.create subscription
}
