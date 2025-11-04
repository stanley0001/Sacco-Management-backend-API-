package com.example.demo.loanManagement.services;

import com.example.demo.communication.parsitence.models.Email;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.enums.Statuses;
import com.example.demo.loanManagement.parsistence.entities.*;
import com.example.demo.loanManagement.parsistence.entities.LoanAccount;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    public LoanApplication loanApplication(Long customerId, String phoneNumber, String productCode, String amount){
        //get customer details - try by ID first, then by phone
        Customer customer;
        if (customerId != null && customerId > 0) {
            log.info("Fetching customer by ID: {}", customerId);
            customer = customerService.findCustomerById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
        } else if (phoneNumber != null && !phoneNumber.isEmpty()) {
            log.info("Fetching customer by phone: {}", phoneNumber);
            customer = customerService.findByPhone(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found with phone number: " + phoneNumber));
        } else {
            throw new RuntimeException("Either customerId or phoneNumber must be provided");
        }
        
        email.setRecipient(customer.getEmail());
        email.setMessageType("Loan Application");
        
        //get subscriptions
        log.info("Fetching subscription for customer {} and product {}", customer.getId(), productCode);
        Subscriptions subscription = subscriptionService.findCustomerIdandproductCode(customer.getId().toString(), productCode)
            .orElseThrow(() -> new RuntimeException("Subscription not found for customer " + customer.getId() + " and product " + productCode));

        LoanApplication loanApplication=new LoanApplication();
        //save loan application
        loanApplication.setApplicationTime(LocalDateTime.now());
        
        // Handle null credit limit and term with fallback to 0
        Integer creditLimit = subscription.getCreditLimit() != null ? subscription.getCreditLimit() : 0;
        Integer term = subscription.getTerm() != null ? subscription.getTerm() : 0;
        
        loanApplication.setCreditLimit(creditLimit.toString());
        loanApplication.setCustomerId(customer.getId().toString());
        loanApplication.setLoanAmount(amount);
        loanApplication.setProductCode(subscription.getProductCode());
        loanApplication.setLoanTerm(term.toString());
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
            LoanApplication application=applicationRepo.save(loanApplication);
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
            if(disbursementData.getStatus().equals("PROCESSED")){
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
            loanApplication.setApplicationStatus("NEW"); // Set to NEW for manual approval instead of error
         log.warn(errorMessage);
            applicationRepo.save(loanApplication);
            email.setMessage("Hello "+customer.getFirstName()+" your application of Ksh "+loanApplication.getLoanAmount()+" Failed withe the below error :: "+errorMessage);
            communicationService.sendCustomEmail(email);
        }
        //update application status

        return loanApplication;
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
                LoanApplication loanApplication=new LoanApplication();
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

    public loanTransactions interestCalculator(LoanApplication loan){
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

    public Optional<LoanApplication> findApplicationByPhone(String phone){
        return applicationRepo.findByCustomerMobileNumber(phone);

    }

    public Optional<LoanApplication> findApplicationById(Long applicationId) {
        return applicationRepo.findById(applicationId);
    }


    public LoanCalculatorResponse loanCalculator(LoanCalculator data) {
        LoanCalculatorResponse calculatorResponse = new LoanCalculatorResponse();
        Products loanProduct = productService.findById(Long.valueOf(data.getProductId())).orElse(null);
        if (loanProduct == null) return null;
        LoanAccountModel account = new LoanAccountModel();
        account.setInstallments(data.getNumberOfInstallments());
        account.setAmount(Float.valueOf(data.getAmount()));
        account.setStartDate(LocalDateTime.now());
        //get this from loan product
        String interestType="simple";
        float totalInterest = interestCalculator(account.getAmount(), loanProduct.getInterest(), account.getInstallments(), interestType);
        account.setInterest(totalInterest);

        float totalRepayment = account.getAmount() + totalInterest;
        account.setTotalRepayment(totalRepayment);

        List<RepaymentSchedules> schedules = getInstallments(account);
        calculatorResponse.setSchedules(schedules);
        calculatorResponse.setLoanAccount(account);
        calculatorResponse.setTotalRepayment(totalRepayment);
        calculatorResponse.setTotalInterest(totalInterest);

        return calculatorResponse;
    }

    private float interestCalculator(float principal, float annualInterestRate, int installments, String interestType) {
        float totalInterest = 0;

        switch (interestType.toLowerCase()) {
            case "simple":
                totalInterest = simpleInterest(principal, annualInterestRate, installments);
                break;
            case "compound":
                totalInterest = compoundInterest(principal, annualInterestRate, installments);
                break;
            case "reducing balance":
                totalInterest = reducingBalanceInterest(principal, annualInterestRate, installments);
                break;
            default:
                throw new IllegalArgumentException("Invalid interest type");
        }

        return totalInterest;
    }

    private float simpleInterest(float principal, float annualInterestRate, int installments) {
        return (principal * annualInterestRate * installments) / 100;
    }

    private float compoundInterest(float principal, float annualInterestRate, int installments) {
        double monthlyInterestRate = (annualInterestRate / 100) / 12;
        double totalRepayment = principal * Math.pow((1 + monthlyInterestRate), installments);
        return (float)(totalRepayment - principal);
    }

    private float reducingBalanceInterest(float principal, float annualInterestRate, int installments) {
        float totalInterest = 0;
        double monthlyInterestRate = (annualInterestRate / 100) / 12;
        float balance = principal;

        for (int i = 0; i < installments; i++) {
            float interest = (float)(balance * monthlyInterestRate);
            totalInterest += interest;
            float monthlyPrincipalRepayment = principal / installments;
            balance -= monthlyPrincipalRepayment;
        }

        return totalInterest;
    }

    public List<RepaymentSchedules> getInstallments(LoanAccountModel accountModel) {
        List<RepaymentSchedules> schedules = new ArrayList<>();
        float monthlyRepayment = accountModel.getTotalRepayment() / accountModel.getInstallments();

        for (int i = 0; i < accountModel.getInstallments(); i++) {
            RepaymentSchedules schedule = new RepaymentSchedules();
            schedule.setInstallmentNumber(i + 1);
            schedule.setAmount(monthlyRepayment);
            schedule.setBalance((double) monthlyRepayment);
            schedule.setAmountPaid(0.0);
            //check allow decimals
            if (true){
                schedule.setAmount((float) Math.ceil(monthlyRepayment));
                schedule.setBalance(Math.ceil(monthlyRepayment));
            }
            schedule.setStatus(Statuses.CURRENT);
            schedule.setDueDate(LocalDate.from(accountModel.getStartDate().plusMonths(i)));
            schedule.setCommencementDate(schedule.getDueDate(),"MONTHLY");

            schedules.add(schedule);
        }

        return schedules;
    }

    public LoanAccount loadAccount(LoanBookUpload upload,Customer customer) {
        LoanApplication loanApplication= new LoanApplication(upload,customer);
        loanApplication=applicationRepo.save(loanApplication);
        LoanAccount loanAccount =new LoanAccount(upload,loanApplication,customer);
        loanAccount=loanAccountRepo.save(loanAccount);
        return  loanAccount;
    }
    
    /**
     * Create loan account from approved application
     */
    public Map<String, Object> createLoanAccountFromApplication(Long applicationId) {
        log.info("Creating loan account for application ID: {}", applicationId);
        
        // Get application
        LoanApplication application = applicationRepo.findById(applicationId)
            .orElseThrow(() -> new IllegalStateException("Application not found"));
        
        // Verify application is approved
        if (!"APPROVED".equals(application.getApplicationStatus()) && 
            !"AUTHORISED".equals(application.getApplicationStatus())) {
            throw new IllegalStateException("Only APPROVED or AUTHORISED applications can be used to create loan accounts. Current status: " + application.getApplicationStatus());
        }
        
        // Check if loan account already exists for this application
        Optional<LoanAccount> existingAccount = loanAccountRepo.findByApplicationId(application.getApplicationId());
        if (existingAccount.isPresent()) {
            throw new IllegalStateException("Loan account already exists for this application");
        }
        
        // Get customer
        Customer customer = customerService.findById(Long.valueOf(application.getCustomerId())).getClient();
        
        // Get product
        Products product = productService.findByCode(application.getProductCode())
            .orElseThrow(() -> new IllegalStateException("Product not found: " + application.getProductCode()));
        
        // Calculate interest
        loanTransactions transaction = interestCalculator(application);
        
        // Create loan account
        LoanAccount loanAccount = new LoanAccount();
        loanAccount.setApplicationId(application.getApplicationId());
        loanAccount.setAmount(Float.valueOf(application.getLoanAmount()));
        loanAccount.setPayableAmount(Float.valueOf(transaction.getFinalBalance()));
        loanAccount.setAccountBalance(Float.valueOf(transaction.getFinalBalance()));
        loanAccount.setCustomerId(customer.getId().toString());
        loanAccount.setStatus("ACTIVE");
        loanAccount.setStartDate(LocalDateTime.now());
        loanAccount.setInstallments(Integer.parseInt(application.getLoanTerm()));
        
        // Calculate due date
        LocalDateTime dueDate = LocalDateTime.now().plusMonths(Integer.parseInt(application.getLoanTerm()));
        loanAccount.setDueDate(dueDate);
        
        // Generate loan reference
        loanAccount.setLoanref(base64encode(application.getLoanNumber().toString()).toUpperCase());
        loanAccount.setOtherRef("Application: " + application.getApplicationId());
        
        // Save loan account
        LoanAccount savedAccount = loanAccountRepo.save(loanAccount);
        
        // Generate repayment schedule
        try {
            LoanAccountModel accountModel = new LoanAccountModel();
            accountModel.setAmount(savedAccount.getAmount());
            accountModel.setAccountBalance(savedAccount.getAccountBalance());
            accountModel.setInstallments(savedAccount.getInstallments());
            accountModel.setStartDate(savedAccount.getStartDate());
            accountModel.setInterest(Float.valueOf(application.getLoanInterest()));
            
            List<RepaymentSchedules> schedules = getInstallments(accountModel);
            // Note: You may want to save schedules here
            
            log.info("Loan account created successfully: {}", savedAccount.getLoanref());
            
            return Map.of(
                "loanAccountId", savedAccount.getAccountId(),
                "loanReference", savedAccount.getLoanref(),
                "amount", savedAccount.getAmount(),
                "payableAmount", savedAccount.getPayableAmount(),
                "status", savedAccount.getStatus(),
                "schedulesGenerated", schedules.size()
            );
        } catch (Exception e) {
            log.error("Error generating repayment schedule", e);
            return Map.of(
                "loanAccountId", savedAccount.getAccountId(),
                "loanReference", savedAccount.getLoanref(),
                "amount", savedAccount.getAmount(),
                "payableAmount", savedAccount.getPayableAmount(),
                "status", savedAccount.getStatus(),
                "warning", "Schedule generation failed: " + e.getMessage()
            );
        }
    }
}
