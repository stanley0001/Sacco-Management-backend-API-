package com.example.demo.banking.services;

import com.example.demo.loanManagement.parsistence.models.Disbursements;
import com.example.demo.loanManagement.services.DisbursementService;
import com.example.demo.system.services.InternalChecks;
import com.example.demo.userManagements.serviceImplementation.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class Dispatcher {
public final DisbursementService disbursementService;
public final UserService userService;
public final InternalChecks internalChecks;


    public Dispatcher(DisbursementService disbursementService, UserService userService, InternalChecks internalChecks) {
        this.disbursementService = disbursementService;
        this.userService = userService;
        this.internalChecks = internalChecks;
    }


    public Disbursements Disburse(String[] requestData) {
        //requestData[0]=customerNumber,requestData[1]=destinationAccountNumber,requestData[2]=loanAccountId,requestData[3]=amount,
        String customerPhone = requestData[0];
        String destinationAccount = requestData[1];
        String loanAccount = requestData[2];
        String amount = requestData[3];
        //saving disbursements
        Disbursements disbursement = new Disbursements();
        disbursement.setAmount(amount);
        disbursement.setDisbursementStart(LocalDateTime.now());
        disbursement.setAccountId(Long.valueOf(loanAccount));
        disbursement.setDestinationAccount(destinationAccount);
        disbursement.setStatus("UPLOADED");
        Disbursements disbursementRequest = disbursementService.saveDisbursement(disbursement);
        //disbursement checks


        //sending fund transfer request
        //receiving response
        String response="";
         response="\"JSON RESPONSE FROM MPESA\"";
        if (!response.isBlank()) {
            // saving response and updating disbursement status
            disbursementRequest.setStatus("PROCESSED");
            disbursementRequest.setResponse(response);
           // disbursementRequest.setOtherRef("PS658878TW");
            disbursementRequest.setOtherRef(random());
            disbursementRequest.setDisbursementEnd(LocalDateTime.now());
            internalChecks.bookLoan(disbursementRequest);
     }
        return disbursementRequest;
    }

    public void repayment(){
        //payment checks
        //sending payment request
        //saving payment request
        //initiate stkpush
        //receiving response


    }

    public void collections(){
        //receive all payment requests;
        //check customer account
        //save payments
        //offset loan accounts
        //save suspense payments
        //notify customer
    }

    public String[] stkPush(String phoneNumber, String amount, String accountNumber) {
        String shortCode="1234";
        String stkPhoneNumber=phoneNumber;
        Integer paymentAmount=Integer.valueOf(amount);
        String[] response=new String[]{""};
        //actual mpesa stk push
        Boolean stkPush=Boolean.TRUE;
        if (stkPush==Boolean.TRUE) {
            //save data
             //response =new String[]{ "PW109877e5ST","JSON RESPONSE FROM MPESA"};
            response =new String[]{ random(),"JSON RESPONSE FROM MPESA"};
        }
        return response;
    }
    public String random(){

        String randomMpesaRef="P1"+userService.randomString()+"4Z";
        return randomMpesaRef;
    }
}
