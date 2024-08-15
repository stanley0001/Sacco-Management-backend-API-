package com.example.demo.system.services;

import com.example.demo.customerManagement.serviceImplimentations.CustomerService;
import com.example.demo.loanManagement.parsistence.entities.Disbursements;
import com.example.demo.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.loanManagement.parsistence.entities.Products;
import com.example.demo.loanManagement.parsistence.entities.Subscriptions;
import com.example.demo.loanManagement.services.LoanAccountService;
import com.example.demo.loanManagement.services.ProductService;
import com.example.demo.loanManagement.services.SubscriptionService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InternalChecks {
public final SubscriptionService subscriptionService;
public final ProductService productService;
public final CustomerService customerService;
public final LoanAccountService loanAccountService;

    public InternalChecks(SubscriptionService subscriptionService, ProductService productService, CustomerService customerService, LoanAccountService loanAccountService) {
        this.subscriptionService = subscriptionService;
        this.productService = productService;
        this.customerService = customerService;
        this.loanAccountService = loanAccountService;
    }

    //loan validation
    public String Productchecks(String[] data){
        //Received data=subscriptionId,amount,customerId
        //get subscription
        Subscriptions subscription=subscriptionService.subscriptionsRepo.findById(Long.valueOf(data[0])).get();
        Products product=productService.findByProductCode(subscription.getProductCode());
        String feedback="";
        //amount check
        Integer amount=Integer.valueOf(data[1]);
        String currentStatus=statusCheck(data[2]);
        if (amount>product.getMaxLimit() || amount<product.getMinLimit()){
            feedback="NOT IN PRODUCT LIMIT";
        }else if (amount>subscription.getCreditLimit()){
            feedback="EXCEED LIMIT";
        }else if (subscription.getStatus()==Boolean.FALSE){
            feedback="INACTIVE SUBSCRIPTION";
        }else if (!currentStatus.isBlank()){
            feedback="EXISTING LOAN::"+currentStatus;
        }


        //existing loan check

        return feedback;
    }
    //Checking existing loan
    public String statusCheck(String customerId){
        String status="";
        Optional<LoanAccount> account=loanAccountService.findByCustomerIdAndStatus(customerId,"PAID");
       if (account.isPresent()){
           status=account.get().getStatus();
       }
        return  status;
    }

    public void bookLoan(Disbursements disbursementData) {
        //book loan
        loanAccountService.bookLoan(disbursementData.getAccountId(), disbursementData.getOtherRef());

    }
}
