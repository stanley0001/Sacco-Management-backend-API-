package com.example.demo.services;

import com.example.demo.customerManagement.parsistence.models.Customer;
import com.example.demo.loanManagement.parsistence.models.LoanAccount;
import com.example.demo.loanManagement.parsistence.models.PaymentRequest;
import com.example.demo.loanManagement.parsistence.models.Payments;
import com.example.demo.loanManagement.parsistence.models.SuspensePayments;
import com.example.demo.loanManagement.parsistence.repositories.*;
import com.example.demo.system.parsitence.repositories.ScheduleRepo;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Log4j2
public class PaymentService extends LoanAccountService{
    @Autowired
private Dispatcher dispatcher;

    public PaymentService(LoanAccountRepo loanAccountRepo, ApplicationRepo applicationRepo, ProductService productService, Backbone backbone, LoanStatesRepo loanStatesRepo, ChargeServiceImpl chargeServiceImpl, SuspensePaymentRepo suspensePaymentRepo, SubscriptionService subscriptionService, PaymentRepo paymentRepo, PaymentRequestRepo paymentRequestRepo, CustomerService customerService, ScheduleRepo scheduleRepo, TransactionsRepo transactionsRepo) {
        super(loanAccountRepo, applicationRepo, productService, backbone, loanStatesRepo, chargeServiceImpl, suspensePaymentRepo, subscriptionService, paymentRepo, paymentRequestRepo, customerService, scheduleRepo, transactionsRepo);
    }

public String paymentRef="";
    public void paymentRequest(String phoneNumber, String productCode, String amount) {
        //save request
        PaymentRequest request = new PaymentRequest();
        request.setAmount(amount);
        request.setDestinationAccount(phoneNumber);
        request.setPaymentTime(LocalDateTime.now());
        request.setStatus("UPLOADED");
        request.setAccountNumber(phoneNumber);
        paymentRequestRepo.save(request);
        //initiate stkpush
    }

    @Async
    public void processLoanPayment(Payments paymentRes){
        paymentRef=paymentRes.getOtherRef();
            //find customer
            Optional<Customer> leadCustomer = customerService.findByPhone(paymentRes.getAccountNumber());
            if (leadCustomer.isPresent()) {
                Customer customer = leadCustomer.get();

                //save payment
                paymentRes.setStatus("PROCESSED");
                paymentRes.setDestinationAccount(paymentRes.getAccountNumber());
                paymentRes.setLoanNumber("");
                paymentRepo.save(paymentRes);
                //getCurrentLoan
                String currentLoan="";
                Optional<LoanAccount> CurrentLoanAccount=findByCustomerIdAndStatus2(customer.getId().toString(), "CURRENT");
                if (CurrentLoanAccount.isPresent()){
                    currentLoan = CurrentLoanAccount.get().getAccountId().toString();
                }
                //getDefaultLoan
                String defaultLoan="";

                Optional<LoanAccount> DefaultLoanAccount =findByCustomerIdAndStatus2(customer.getId().toString(), "DEFAULT");
                 if (DefaultLoanAccount.isPresent()) {
                     defaultLoan = DefaultLoanAccount.get().getAccountId().toString();
                 }
                 String accountNumber = "";
                if (!currentLoan.isBlank()) {
                    accountNumber = currentLoan;
                } else {
                    accountNumber = defaultLoan;
                }
                if (!accountNumber.isBlank()){
                //offset amount from loan account
                    PayLoan(accountNumber, paymentRes.getAmount(),paymentRes.getAccountNumber());

                }else {
                     SuspensePayments suspensePayment = new SuspensePayments();
                     suspensePayment.setPaymentTime(LocalDateTime.now());
                     suspensePayment.setOtherRef(paymentRes.getOtherRef());
                     suspensePayment.setAmount(paymentRes.getAmount());
                     suspensePayment.setAccountNumber(paymentRes.getAccountNumber());
                     suspensePayment.setStatus("SUSPENSE");
                     suspensePayment.setDestinationAccount(paymentRes.getDestinationAccount());
                     suspensePayment.setExceptionType("OVERPAYMENT");
                     //save suspense Payment
                     saveSuspensePayment(suspensePayment);
                 }

            } else {
                //save payment as Suspense
                SuspensePayments payment = new SuspensePayments();
                payment.setPaymentTime(LocalDateTime.now());
                payment.setOtherRef(paymentRes.getOtherRef());
                payment.setAmount(paymentRes.getAmount());
                payment.setAccountNumber(paymentRes.getAccountNumber());
                payment.setStatus("SUSPENSE");
                payment.setDestinationAccount(paymentRes.getDestinationAccount());
                payment.setExceptionType("MISSING CUSTOMER");
                //save suspense Payment
                saveSuspensePayment(payment);

            }

    }

public void saveSuspensePayment(SuspensePayments payment){suspensePaymentRepo.save(payment);}
    public void PayLoan(String accountNumber, String amount,String phoneNumber) {
        Customer customer=customerService.findByPhone(phoneNumber).get();
        log.info("Processing payment");
        email.setRecipient(customer.getEmail());
        email.setMessageType("Payment Confirmation");

        //find loanAccount
       Optional<LoanAccount> transactionalAccount=findById(Long.valueOf(accountNumber));
       LoanAccount loanAccount=null;
       Float accountBalance=Float.valueOf(0);
       if (transactionalAccount.isPresent()){
          loanAccount= transactionalAccount.get();
           accountBalance=loanAccount.getAccountBalance();
       }
       Float paidAmount=Float.valueOf(0);
       Float suspenseAmount=Float.valueOf(0);
       if (Float.valueOf(amount)>accountBalance){
           paidAmount=accountBalance;
           suspenseAmount=Float.valueOf(amount)-accountBalance;
           email.setMessage("Thank you "+customer.getFirstName()+"  We have have Received your payment of Ksh "+amount+" You have over Paid by Ksh "+suspenseAmount);

           //update loan status
           updateStatus(loanAccount.getAccountId().toString(),"PAID");
       }else  {
           paidAmount=Float.valueOf(amount);
       }
       Float finalAmount=accountBalance-paidAmount;
       loanAccount.setAccountBalance(finalAmount);

       if (finalAmount.equals(Float.valueOf(0))){
          //update loan status
           updateStatus(loanAccount.getAccountId().toString(),"PAID");
           email.setMessage("Thank you "+customer.getFirstName()+"  We have have Received your payment of Ksh "+amount+" Your balance is Ksh 0");
       }else {
           email.setMessage("Thank you "+customer.getFirstName()+"  We have have Received your payment of Ksh "+amount+" Your balance is Ksh"+finalAmount);

       }
        communicationService.sendCustomEmail(email);
        //save loan account
      save(loanAccount);
        //saving transactions
        String[] transactionData=new String[]{
                phoneNumber,applicationRepo.findById(loanAccount.getApplicationId()).get().getLoanNumber().toString(),"REPAYMENT",
                accountBalance.toString(),finalAmount.toString(),paymentRef,""
        };
        backbone.saveTransaction(transactionData);
       if (suspenseAmount>0){
           SuspensePayments payments=new SuspensePayments();
           payments.setPaymentTime(LocalDateTime.now());
           payments.setAmount(suspenseAmount.toString());
           payments.setAccountNumber(phoneNumber);
           payments.setStatus("SUSPENSE");
           payments.setExceptionType("OVERPAYMENT");
           //save suspenseAmount
           saveSuspensePayment(payments);


       }
    }


    @Async
    public void processCallBack(JSONObject jsonObject) {
        //
        if (jsonObject.has("Body")){
        if (jsonObject.getJSONObject("Body").has("stkCallback")){
            log.info("Processing stk callback. ..");
            String stkDescription=jsonObject.getJSONObject("Body").getJSONObject("stkCallback").get("ResultDesc").toString();
            int stkCode=jsonObject.getJSONObject("Body").getJSONObject("stkCallback").getInt("ResultCode");

            //checking for success callbacks
            if (stkCode == 0){
                log.info("success stk.. code: {}, Description: {}",stkCode,stkDescription);
                //map data models

                JSONArray items=jsonObject.getJSONObject("Body").getJSONObject("stkCallback").getJSONObject("CallbackMetadata").getJSONArray("Item");

                int i;
                Payments payment = new Payments();
                for (i=0; i<items.length(); i++){


                    log.info(items.getJSONObject(i).get("Name"));
                    if (items.getJSONObject(i).get("Name").toString().equals("PhoneNumber")){
                        payment.setAccountNumber(items.getJSONObject(i).get("Value").toString());
                    }
                    if (items.getJSONObject(i).get("Name").toString().equals("Amount")){
                        payment.setAmount(items.getJSONObject(i).get("Value").toString());
                    }
                    if (items.getJSONObject(i).get("Name").toString().equals("MpesaReceiptNumber")){
                        payment.setOtherRef(items.getJSONObject(i).get("Value").toString());
                    }
                }
                payment.setPaymentTime(LocalDateTime.now());
                log.info(payment);
                processLoanPayment(payment);
                //
                // save model
                //
                // Verify transaction

                // update model
                //
                // process

            }else {
                log.info("failed stk.. code: {}, Description: {}",stkCode,stkDescription);
                //map data models
                //
                // save
            }


        }
         }
         //
        if (jsonObject.has("Result")){
            if (jsonObject.getJSONObject("Result").has("ResultParameters")){
                log.info("processing other callback ....");

            }
        }

    }
}
