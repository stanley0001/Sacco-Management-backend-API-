package com.example.demo.loanManagement.services;

import com.example.demo.communication.parsitence.models.Email;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.loanManagement.parsistence.models.*;
import com.example.demo.loanManagement.parsistence.repositories.ApplicationRepo;
import com.example.demo.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.loanManagement.parsistence.repositories.TransactionsRepo;
import com.example.demo.communication.services.CommunicationService;
import com.example.demo.customerManagement.serviceImplimentations.CustomerService;
import com.example.demo.banking.services.Dispatcher;
import com.example.demo.system.services.InternalChecks;
import com.example.demo.system.services.Backbone;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@Log4j2
public class LoanService {
    public final ApplicationRepo applicationRepo;
    public final TransactionsRepo transactionsRepo;
    public final SubscriptionService subscriptionService;
    public final CustomerService customerService;
    public final ProductService productService;
    public final CommunicationService communicationService;
    public final InternalChecks internalChecks;
   public final LoanAccountRepo loanAccountRepo;
   public final Dispatcher dispatcher;
   public final Backbone backbone;

    public LoanService(ApplicationRepo applicationRepo, TransactionsRepo transactionsRepo, SubscriptionService subscriptionService, CustomerService customerService, ProductService productService, CommunicationService communicationService, InternalChecks internalChecks, LoanAccountRepo loanAccountRepo, Dispatcher dispatcher, Backbone backbone) {
        this.applicationRepo = applicationRepo;
        this.transactionsRepo = transactionsRepo;
        this.subscriptionService = subscriptionService;
        this.customerService = customerService;
        this.productService = productService;
        this.communicationService = communicationService;
        this.internalChecks = internalChecks;
        this.loanAccountRepo = loanAccountRepo;
        this.dispatcher = dispatcher;
        this.backbone = backbone;
    }
    public String base64encode(String plainText){
        byte[] bytes = plainText.getBytes(StandardCharsets.UTF_8);
        String base36 = new BigInteger(1, bytes).toString(36);
        return  base36;
    }
    Email email=new Email();
    public void loanApplication(String phoneNumber,String productCode,String amount){
        //get customer details
        log.info("fetching customer");
        Customer customer=customerService.findByPhone(phoneNumber).get();
        email.setRecipient(customer.getEmail());
        email.setMessageType("Loan Application");
        //get subscriptions
        Subscriptions subscription=subscriptionService.findCustomerIdandproductCode(customer.getId().toString(),productCode).get();

        loanApplication loanApplication=new loanApplication();
        //save loan application
        loanApplication.setApplicationTime(LocalDateTime.now());
        loanApplication.setCreditLimit(subscription.getCreditLimit().toString());
        loanApplication.setCustomerId(customer.getId().toString());
        loanApplication.setLoanAmount(amount);
        loanApplication.setProductCode(subscription.getProductCode());
        loanApplication.setLoanTerm(subscription.getTerm().toString());
        loanApplication.setCustomerIdNumber(customer.getDocumentNumber());

        Long loanNumber=Long.valueOf(new Date().getTime());
        log.warn(loanNumber);
                //Long.valueOf(new Random().nextInt()+customer.getDocumentNumber());
        loanApplication.setApplicationStatus("NEW");
        loanApplication.setLoanNumber(loanNumber);
        loanApplication.setCustomerMobileNumber(phoneNumber);
        loanApplication.setCustomerId(customer.getId().toString());
        loanApplication.setDestinationAccount(customer.getPhoneNumber());
        loanApplication.setDisbursementType("MPESA");
        loanApplication.setLoanInterest(subscription.getInterestRate().toString());
        //all checks
        String[] data=new String[]{
              subscription.getId().toString(),amount,customer.getId().toString()
        };
        email.setMessage("Hello "+customer.getFirstName()+" your application of Ksh "+loanApplication.getLoanAmount()+"have bee received please wait as we process your request");
        communicationService.sendCustomEmail(email);
        if (internalChecks.Productchecks(data).isBlank()){
            loanApplication.setApplicationStatus("AUTHORISED");
            log.info("saving loan application....");
            loanApplication application=applicationRepo.save(loanApplication);
            //interest application
            loanTransactions transaction=interestCalculator(loanApplication);
            //create loan account
            LoanAccount loanAccount=new LoanAccount();
            loanAccount.setApplicationId(application.getApplicationId());
            loanAccount.setAmount(Float.valueOf(application.getLoanAmount()));
            loanAccount.setPayableAmount(Float.valueOf(transaction.getFinalBalance()));
            loanAccount.setAccountBalance(Float.valueOf(transaction.getFinalBalance()));
            loanAccount.setCustomerId(customer.getId().toString());
            loanAccount.setStatus("INIT");
            loanAccount.setLoanref(base64encode(loanApplication.getLoanNumber().toString()).toUpperCase());
            LoanAccount loanAccount1= loanAccountRepo.save(loanAccount);
            //dispatch funds
            String[] disbursmentData=new String[]{
               loanApplication.getCustomerMobileNumber(),loanApplication.getDestinationAccount(),loanAccount1.getAccountId().toString(),loanApplication.getLoanAmount()
             };
            email.setMessage("Hello "+customer.getFirstName()+" your application of Ksh "+loanApplication.getLoanAmount()+"have been Approved please wait for find disbursement");
            communicationService.sendCustomEmail(email);
            Disbursements disbursementData=dispatcher.Disburse(disbursmentData);
            if(disbursementData.getStatus()=="PROCESSED"){
                email.setMessageType("Disbursement");
                email.setMessage("Hello "+customer.getFirstName()+" We have disbursed Ksh "+loanApplication.getLoanAmount()+" To your account");
                communicationService.sendCustomEmail(email);
            }
            //save transaction
            String[] transactionData=new String[]{
                    application.getCustomerMobileNumber(),application.getLoanNumber().toString(),"Disbursement",disbursementData.getAmount(),loanAccount.getAccountBalance().toString(),disbursementData.getOtherRef(),disbursementData.getResponse(),application.getCustomerId()
            };
            backbone.saveTransaction(transactionData);

        }else {
            String errorMessage =internalChecks.Productchecks(data);
            loanApplication.setApplicationStatus(errorMessage);
         log.warn(errorMessage);
            applicationRepo.save(loanApplication);
            email.setMessage("Hello "+customer.getFirstName()+" your application of Ksh "+loanApplication.getLoanAmount()+" Failed withe the below error :: "+errorMessage);
            communicationService.sendCustomEmail(email);
        }
        //update application status

    }
   public ResponseEntity newApplication(newApplication application){
       log.info("fetching customer");
       Optional<Customer> customer1=customerService.findByPhone(application.getPhone());
       if (customer1.isPresent()){
           Customer customer=customer1.get();
           Optional<Subscriptions> subscription1=subscriptionService.findCustomerIdandproductCode(customer.getId().toString(),application.getProductCode());
            if (subscription1.isPresent()){
                Subscriptions subscription=subscription1.get();
                email.setRecipient(customer.getEmail());
                email.setMessageType("Loan Application");
                loanApplication loanApplication=new loanApplication();
                loanApplication.setApplicationTime(LocalDateTime.now());
                loanApplication.setCreditLimit(subscription.getCreditLimit().toString());
                loanApplication.setCustomerId(customer.getId().toString());
                loanApplication.setLoanAmount(application.getAmount());
                loanApplication.setProductCode(subscription.getProductCode());
                loanApplication.setLoanTerm(subscription.getTerm().toString());
                loanApplication.setCustomerIdNumber(customer.getDocumentNumber());
                Long loanNumber=Long.valueOf(new Date().getTime());
                log.warn(loanNumber);
                loanApplication.setApplicationStatus("NEW");
                loanApplication.setInstallments(application.getInstallments());
                loanApplication.setLoanNumber(loanNumber);
                loanApplication.setCustomerMobileNumber(application.getPhone());
                loanApplication.setCustomerId(customer.getId().toString());
                loanApplication.setDestinationAccount(customer.getPhoneNumber());
                loanApplication.setDisbursementType("MPESA");
                loanApplication.setLoanInterest(subscription.getInterestRate().toString());
                String[] data=new String[]{
                        subscription.getId().toString(),application.getAmount(),customer.getId().toString()
                };
                email.setMessage("Hello "+customer.getFirstName()+" your application of Ksh "+loanApplication.getLoanAmount()+"have bee received please wait as we process your request");
                communicationService.sendCustomEmail(email);

                if (internalChecks.Productchecks(data).isBlank()){
                    loanApplication.setApplicationStatus("AUTHORISED");
                    log.info("saving loan application....");
                    loanApplication=applicationRepo.save(loanApplication);
                    //interest application
                    loanTransactions transaction=interestCalculator(loanApplication);
                    //create loan account
                    LoanAccount loanAccount=new LoanAccount();
                    loanAccount.setApplicationId(loanApplication.getApplicationId());
                    loanAccount.setAmount(Float.valueOf(loanApplication.getLoanAmount()));
                    loanAccount.setPayableAmount(Float.valueOf(transaction.getFinalBalance()));
                    loanAccount.setAccountBalance(Float.valueOf(transaction.getFinalBalance()));
                    loanAccount.setCustomerId(customer.getId().toString());
                    loanAccount.setStatus("INIT");
                    loanAccount.setLoanref(base64encode(loanApplication.getLoanNumber().toString()).toUpperCase());
                    LoanAccount loanAccount1= loanAccountRepo.save(loanAccount);
                    //dispatch funds
                    String[] disbursmentData=new String[]{
                            loanApplication.getCustomerMobileNumber(),loanApplication.getDestinationAccount(),loanAccount1.getAccountId().toString(),loanApplication.getLoanAmount()
                    };
                    email.setMessage("Hello "+customer.getFirstName()+" your application of Ksh "+loanApplication.getLoanAmount()+"have been Approved please wait for find disbursement");
                    communicationService.sendCustomEmail(email);
                   /* Disbursements disbursementData=dispatcher.Disburse(disbursmentData);
                    if(disbursementData.getStatus()=="PROCESSED"){
                        email.setMessageType("Disbursement");
                        email.setMessage("Hello "+customer.getFirstName()+" We have disbursed Ksh "+loanApplication.getLoanAmount()+" To your account");
                        communicationService.sendCustomEmail(email);
                    }
                    //save transaction
                    String[] transactionData=new String[]{
                            loanApplication.getCustomerMobileNumber(),loanApplication.getLoanNumber().toString(),"Disbursement",disbursementData.getAmount(),loanAccount.getAccountBalance().toString(),disbursementData.getOtherRef(),disbursementData.getResponse(),application.getCustomerId()
                    };
                    backbone.saveTransaction(transactionData);
*/
                }
            }
           return new ResponseEntity("No Subscription Found",HttpStatus.OK);
       }

    return new ResponseEntity("No Customer found",HttpStatus.OK);
   }
    public loanTransactions interestCalculator(loanApplication loan){
        log.info("calculating interest....");
        Float interestRate=Float.valueOf(loan.getLoanInterest())/100;
        Float interest=interestRate*Float.valueOf(loan.getLoanAmount());
        Float finalBal=Float.valueOf(loan.getLoanAmount())+interest;
        Customer customer=customerService.findById(Long.valueOf(loan.getCustomerId())).getClient();
        log.info(customer);
           log.info(loan);
        String[] transactionData=new String[]{
              loan.getCustomerMobileNumber(),loan.getLoanNumber().toString(),"Interest",loan.getLoanAmount(),finalBal.toString(),"","",loan.getCustomerId()
        };
        //charges

        //communications
       // communicationService.sendCustomEmail();
        loanTransactions transaction= backbone.saveTransaction(transactionData);
        return transaction;
    }

    public Optional<loanApplication> findApplicationByPhone(String phone){
        return applicationRepo.findByCustomerMobileNumber(phone);

    }

    public Optional<loanApplication> findApplicationById(Long applicationId) {
        return applicationRepo.findById(applicationId);
    }
}
