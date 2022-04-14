package com.example.demo.services;

import com.example.demo.model.LoanAccount;
import com.example.demo.model.SuspensePayments;
import com.example.demo.persistence.repository.LoanAccountRepo;
import com.example.demo.persistence.repository.SuspensePaymentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Test {
    @Autowired
    public final Backbone backbone;
    public final LoanAccountRepo loanAccountRepo;
    public final SuspensePaymentRepo suspensePaymentRepo;


    public Test(Backbone backbone,LoanAccountRepo loanAccountRepo, SuspensePaymentRepo suspensePaymentRepo) {
        this.backbone = backbone;
        this.loanAccountRepo = loanAccountRepo;
        this.suspensePaymentRepo = suspensePaymentRepo;
    }


    public void settleSuspense(SuspensePayments suspensePayment, String cusPhone, Long accountId, String productCode) {
        LoanAccount loanAccount=loanAccountRepo.findById(accountId).get();
        Float accountBalance=loanAccount.getAccountBalance();
        Float suspenseAmount=Float.valueOf(suspensePayment.getAmount());
        if (!accountBalance.equals(Float.valueOf(0))){
            //paymentService.paymentRequest(cusPhone,productCode,suspensePayment.getAmount());
            if (suspenseAmount>accountBalance){
                accountBalance=Float.valueOf(0);
                suspenseAmount=suspenseAmount-accountBalance;
            }else {
                accountBalance=suspenseAmount-accountBalance;
                suspensePayment.setStatus("PROCESSED");
                suspensePayment.setUtilisedBy(cusPhone);
            }
            loanAccountRepo.save(loanAccount);
            suspensePayment.setAmount(suspenseAmount.toString());
            suspensePayment.setUtilisedBy(cusPhone);
            suspensePaymentRepo.save(suspensePayment);

        }

    }
}
