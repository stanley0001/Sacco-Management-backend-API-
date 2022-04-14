package com.example.demo.services;

import com.example.demo.model.*;
import com.example.demo.persistence.repository.*;
import com.example.demo.persistence.repository.Schedule.ScheduleRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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
        String[] response = dispatcher.stkPush(phoneNumber, amount, productCode);

        if (!response[0].isBlank() && Integer.valueOf(amount)>0) {
            paymentRef=response[0];
            //find customer
            Optional<Customer> leadCustomer = customerService.findByPhone(phoneNumber);
            if (leadCustomer.isPresent()) {
                Customer customer = leadCustomer.get();

                //save payment
                Payments payment = new Payments();
                payment.setPaymentTime(LocalDateTime.now());
                payment.setOtherRef(paymentRef);
                payment.setAmount(amount);
                payment.setAccountNumber(phoneNumber);
                payment.setStatus("PROCESSED");
                payment.setDestinationAccount(phoneNumber);
                payment.setLoanNumber("");
                paymentRepo.save(payment);
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
                    PayLoan(accountNumber, amount,phoneNumber);

                }else {
                     SuspensePayments suspensePayment = new SuspensePayments();
                     suspensePayment.setPaymentTime(LocalDateTime.now());
                     suspensePayment.setOtherRef(response[0]);
                     suspensePayment.setAmount(amount);
                     suspensePayment.setAccountNumber(phoneNumber);
                     suspensePayment.setStatus("SUSPENSE");
                     suspensePayment.setDestinationAccount(phoneNumber);
                     suspensePayment.setExceptionType("OVERPAYMENT");
                     //save suspense Payment
                     saveSuspensePayment(suspensePayment);
                 }

            } else {
                //save payment as Suspense
                SuspensePayments payment = new SuspensePayments();
                payment.setPaymentTime(LocalDateTime.now());
                payment.setOtherRef(response[0]);
                payment.setAmount(amount);
                payment.setAccountNumber(phoneNumber);
                payment.setStatus("SUSPENSE");
                payment.setDestinationAccount(phoneNumber);
                payment.setExceptionType("MISSING CUSTOMER");
                //save suspense Payment
                saveSuspensePayment(payment);

            }
        }else {
            log.info("AMOUNT TOO LOW");
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
}
