package com.example.demo.finance.loanManagement.services;

import com.example.demo.finance.banking.parsitence.repositories.PaymentRepo;
import com.example.demo.erp.communication.parsitence.models.Email;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.dto.LoanAccountResponseDto;
import com.example.demo.finance.loanManagement.parsistence.entities.*;
import com.example.demo.finance.loanManagement.parsistence.models.*;
import com.example.demo.finance.loanManagement.parsistence.repositories.*;
import com.example.demo.erp.communication.services.CommunicationService;
import com.example.demo.erp.customerManagement.serviceImplimentations.CustomerService;
import com.example.demo.system.parsitence.models.Schedule.Schedule;
import com.example.demo.erp.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.system.parsitence.repositories.ScheduleRepo;
import com.example.demo.system.services.Backbone;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
public class LoanAccountService {
    public final LoanAccountRepo loanAccountRepo;
    public final ApplicationRepo applicationRepo;
    public final ProductService productService;
    public final Backbone backbone;
    public final LoanStatesRepo loanStatesRepo;
    public final ChargeServiceImpl chargeServiceImpl;
    public final SuspensePaymentRepo suspensePaymentRepo;
    public final SubscriptionService subscriptionService;
    public final PaymentRepo paymentRepo;
    public final PaymentRequestRepo paymentRequestRepo;
    public final CustomerService customerService;
    public   final ScheduleRepo scheduleRepo;
    public final TransactionsRepo transactionsRepo;

    @Autowired
    CommunicationService communicationService;


    public LoanAccountService(LoanAccountRepo loanAccountRepo, ApplicationRepo applicationRepo, ProductService productService, Backbone backbone, LoanStatesRepo loanStatesRepo, ChargeServiceImpl chargeServiceImpl, SuspensePaymentRepo suspensePaymentRepo, SubscriptionService subscriptionService, PaymentRepo paymentRepo, PaymentRequestRepo paymentRequestRepo, CustomerService customerService, ScheduleRepo scheduleRepo, TransactionsRepo transactionsRepo) {
        this.loanAccountRepo = loanAccountRepo;
        this.applicationRepo = applicationRepo;
        this.productService = productService;
        this.backbone = backbone;
        this.loanStatesRepo = loanStatesRepo;
        this.chargeServiceImpl = chargeServiceImpl;
        this.suspensePaymentRepo = suspensePaymentRepo;
        this.subscriptionService = subscriptionService;
        this.paymentRepo = paymentRepo;
        this.paymentRequestRepo = paymentRequestRepo;
        this.customerService = customerService;
        this.scheduleRepo = scheduleRepo;
        this.transactionsRepo = transactionsRepo;

    }
 Email email=new Email();
    public LoanAccount save(LoanAccount loanAccount){
        return loanAccountRepo.saveAndFlush(loanAccount);
    }
    public List<LoanAccount> findAll(){
        return loanAccountRepo.findAll();
    }
    public Optional<LoanAccount> findById(Long id){
        return loanAccountRepo.findById(id);
    }
    public Optional<LoanAccount> findByApplicationId(Long id){
        return loanAccountRepo.findByApplicationId(id);
    }
    public Optional<LoanAccount> findByPhone(String phone){
          Long applicationId=applicationRepo.findByCustomerMobileNumber(phone).get().getApplicationId();
        return findByApplicationId(applicationId);

    }
    @SneakyThrows
    public void updateStatus(String accountId, String status){
        //update other states
        List<LoanStates> loanStates=loanStatesRepo.findByAccountNumber(accountId);
        for (LoanStates loanStates1:loanStates
             ) {
            loanStates1.setActive(Boolean.FALSE);
            loanStatesRepo.save(loanStates1);
        }
        LoanAccount account=findById(Long.valueOf(accountId)).get();
        log.info("Updating status for account {} to {}",account,status);
        if(status=="CURRENT"){
            account.setStartDate(LocalDateTime.now());
            account.setDueDate(getDueDate(Long.valueOf(accountId)));
        }
        //save status
        log.info("saving status");
        LoanStates state=new LoanStates();
        state.setStatus(status);
        account.setStatus(status);
        state.setAccountNumber(accountId);
        state.setActive(Boolean.TRUE);
        state.setStartDate(LocalDateTime.now());
        loanStatesRepo.save(state);
        LoanAccount savedAccount=this.save(account);
        log.info("Account updated with : {}",savedAccount);
        /*Thread.sleep(2000);
        this.updateLoanStatus(account);

         */
    }
    void updateLoanStatus(LoanAccount account){
        List<LoanStates> loanStates=loanStatesRepo.findByAccountNumber(String.valueOf(account.getAccountId()));
        for (LoanStates loanStates1:loanStates
        ) {
            if (loanStates1.getActive()){
                account.setStatus(loanStates1.getStatus());
                LoanAccount account1=loanAccountRepo.save(account);
                log.info("Status updated {}",account1);
            }
        }
    }
    public LocalDateTime getDueDate(Long accountId){
        LoanAccount account=findById(accountId).get();
        Long applicationId=account.getApplicationId();
        LoanApplication application =applicationRepo.findById(applicationId).get();
        Long loanDuration=Long.valueOf(application.getLoanTerm());
        LocalDateTime dueDate=LocalDateTime.now().plusDays(loanDuration);
        return dueDate;
    }

    public void bookLoan(Long accountId,String otherREf){

        LoanAccount account=findById(Long.valueOf(accountId)).get();
        account.setOtherRef(otherREf);
        save(account);
        //send a notification to client
        ClientInfo customer=customerService.findById(Long.valueOf(account.getCustomerId()));
        email.setRecipient(customer.getClient().getEmail());
        email.setMessageType("Reminder");
        updateStatus(accountId.toString(),"CURRENT");
        email.setMessage("Hello "+customer.getClient().getFirstName()+" your loan of ksh "+account.getAccountBalance()+" Will be due by "+findById(Long.valueOf(accountId)).get().getDueDate());
        communicationService.sendCustomEmail(email);
        //schedules
        Schedule schedule=new Schedule();
        schedule.setScheduleTime(account.getDueDate());
        schedule.setAccountNumber(account.getAccountId().toString());
        schedule.setTopic("Default");
        log.info("creating a schedule");
       backbone.createSchedule(schedule);
       //CHeck customer overpayments and offset the current account
        LoanApplication application=applicationRepo.findById(account.getApplicationId()).get();
        String cusPhone=application.getCustomerMobileNumber();
        Optional<List<SuspensePayments>> suspensePayments=findAllOverPayment(cusPhone);
    if (!suspensePayments.get().isEmpty()){
        log.warn("suspense payments found");
        for (SuspensePayments suspensePayment:
                suspensePayments.get()) {
            log.warn(suspensePayment);
            //backbone.test.settleSuspense(suspensePayment,cusPhone,account.getAccountId(),application.getProductCode());
        }
    }
    }

    public Optional<LoanAccount> findByCustomerIdAndStatus(String customerId, String status) {
        return loanAccountRepo.findByCustomerIdAndStatusNot(customerId,status);
    }
    public Optional<LoanAccount> findLoanAccountByLoanNumber(String loanNumber){
        return loanAccountRepo.findByLoanref(loanNumber);
    }
    public Optional<LoanAccount> findByCustomerIdAndStatus2(String customerId, String status) {
        return loanAccountRepo.findByCustomerIdAndStatus(customerId,status);
    }
     public LoanAccount getAccountById(Long accountNumber){
         LoanAccount account=findById(accountNumber).get();

         return account;
     }
     //find by customer id
     public List<AccountModified> findByCustomerId(String customerId) {
         List<AccountModified> modifiedList = new ArrayList<>();
         List<LoanAccount> accounts= loanAccountRepo.findByCustomerIdOrderByStartDateDesc(customerId);
         for(LoanAccount account:accounts){
             AccountModified accountModified= new AccountModified();
             accountModified.setAccount(account);
             accountModified.setTransactions(transactionsRepo.findByLoanRefOrderByTransactionIdAsc(account.getLoanref()));
            modifiedList.add(accountModified);
         }
          return modifiedList;
     }
    /*find by customer phone  */
    public Optional<LoanAccount> findByCustomerPhone(String customerPhone) {
        List<LoanAccount> la=loanAccountRepo.findByStatus("DEFAULT");
        Customer cs=customerService.findByPhone(customerPhone).orElse(null);
        if (cs==null)
            return Optional.empty();
       return loanAccountRepo.findByCustomerIdAndStatusNot(String.valueOf(cs.getId()),"PAID");
    }



     //default action
    public Boolean defaultAccount(String accountNumber) {
       log.info("Processing payment");
        String loanStatus="DEFAULT";
        Boolean response=Boolean.FALSE;
        //get transactional account
        LoanAccount account=getAccountById(Long.valueOf(accountNumber));
        log.info("account Found {}",account);
        //check account status
        if (!account.getStatus().equals("PAID")){
            log.info("WorkingAccount Found");
        //update loanStatus
        updateStatus(accountNumber,loanStatus);
        //apply defaultCharge
        String accountStatus=account.getStatus();
        if (accountStatus.equals(loanStatus)){
            log.info("DefaultAccount Found");
            response=Boolean.TRUE;
            ClientInfo customer=customerService.findById(Long.valueOf(account.getCustomerId()));
            email.setRecipient(customer.getClient().getEmail());
            email.setMessageType("DEFAULT STATE");
            email.setMessage("Hello "+customer.getClient().getFirstName()+"You have been charged an additional penalty for delaying in payment, please make your payment to avoid more penalties");
            communicationService.sendCustomEmail(email);
            ChargeCalculator(account,"DEFAULT_PENALTY");
        }}else {
            response=Boolean.TRUE;
        }
        return response;
    }
    //suspense payments offset overpayment
    public Optional<List<SuspensePayments>> findAllOverPayment(String phone){
        return suspensePaymentRepo.findByAccountNumberAndStatus(phone,"SUSPENSE");
    }

 //calculation of charges
    public void ChargeCalculator(LoanAccount TransactionalAccount,String chargeType){
        log.info("Calculating charges");
        LoanApplication application=applicationRepo.findById(Long.valueOf(TransactionalAccount.getApplicationId())).get();
        log.info("Loan application Found {}",application);
        Charges charge= chargeServiceImpl.getChargeByProductIdAndName(productService.findByProductCode(application.getProductCode()).getId().toString(),chargeType).orElse(null);
        log.info("Charge1 {}",charge);
        double rate=0.002;
        if (charge!=null)
            rate=charge.getRate();
         Float chargeRate=Float.valueOf((float) rate)/100;
        Float accountBalance=TransactionalAccount.getAccountBalance();
        Float loanAmount=TransactionalAccount.getAmount();
        Float TransactionalAmount=loanAmount*chargeRate;
         Float finalAmount=TransactionalAmount+accountBalance;
         TransactionalAccount.setAccountBalance(finalAmount);
         log.info("TransactionData {}",TransactionalAccount);
         //communication to client
        Customer customer=customerService.findByPhone(applicationRepo.findById(TransactionalAccount.getApplicationId()).get().getCustomerMobileNumber()).get();

        email.setRecipient(customer.getEmail());
        email.setMessageType(chargeType+" Alert");
        email.setRecipient(customer.getEmail());
        email.setMessage("Hello "+customer.getFirstName()+"You have been charged an extra Amount as "+chargeType+" Your balance is now Ksh "+finalAmount);
        communicationService.sendCustomEmail(email);
         //save transaction
        String[] transactionData=new String[]{
                applicationRepo.findById(TransactionalAccount.getApplicationId()).get().getCustomerMobileNumber(),application.getLoanNumber().toString(),chargeType,
                accountBalance.toString(),finalAmount.toString(),"",""
        };
        log.info(charge);
        backbone.saveTransaction(transactionData);
        //save loan account
        save(TransactionalAccount);
    }

    /**
     * Get all loan accounts enriched with customer and product information
     */
    public List<LoanAccountResponseDto> findAllEnriched() {
        List<LoanAccount> accounts = loanAccountRepo.findAll();
        return accounts.stream()
                .map(this::enrichLoanAccount)
                .collect(Collectors.toList());
    }

    /**
     * Get loan accounts by customer ID enriched with information
     */
    public List<LoanAccountResponseDto> findByCustomerIdEnriched(String customerId) {
        List<LoanAccount> accounts = loanAccountRepo.findByCustomerId(customerId);
        return accounts.stream()
                .map(this::enrichLoanAccount)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to enrich a single loan account with customer and product info
     */
    private LoanAccountResponseDto enrichLoanAccount(LoanAccount account) {
        LoanAccountResponseDto dto = new LoanAccountResponseDto();
        
        // Set loan account fields
        dto.setId(account.getAccountId());
        dto.setAccountNumber(account.getLoanref());
        dto.setApplicationId(account.getApplicationId());
        dto.setPrincipalAmount(account.getAmount());
        dto.setPayableAmount(account.getPayableAmount());
        dto.setAmountPaid(account.getAmountPaid());
        dto.setBalance(account.getAccountBalance());
        dto.setStartDate(account.getStartDate());
        dto.setDueDate(account.getDueDate());
        dto.setStatus(account.getStatus());
        dto.setTerm(account.getInstallments());
        dto.setCustomerId(account.getCustomerId());
        
        // Enrich with customer information
        try {
            ClientInfo clientInfo = customerService.findById(Long.valueOf(account.getCustomerId()));
            if (clientInfo==null){
                clientInfo = customerService.findByDocumentNumber(account.getCustomerId());
            }
            if (clientInfo==null){
                clientInfo = customerService.findByExternalId(account.getCustomerId());
            }
            if (clientInfo != null && clientInfo.getClient() != null) {
                Customer customer = clientInfo.getClient();
                dto.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
                dto.setPhoneNumber(customer.getPhoneNumber());
            } else {
                dto.setCustomerName("Unknown");
                dto.setPhoneNumber("N/A");
            }
        } catch (Exception e) {
            log.warn("error: {}", e.getMessage());
            log.warn("Could not fetch customer info for customer ID: {}", account.getCustomerId());
            dto.setCustomerName("Unknown");
            dto.setPhoneNumber("N/A");
        }
        
        // Enrich with product information from application
        try {
            Optional<LoanApplication> applicationOpt = applicationRepo.findById(account.getApplicationId());
            if (applicationOpt.isPresent()) {
                LoanApplication application = applicationOpt.get();
                dto.setProductCode(application.getProductCode());
                // Try to get product name from Products table
                try {
                    Optional<Products> productOpt = productService.findById(Long.valueOf(application.getProductCode()));
                    if (productOpt.isPresent()) {
                        Products product = productOpt.get();
                        dto.setProductName(product.getName());
                        dto.setInterestRate(product.getInterest() != null ? product.getInterest().doubleValue() : 0.0);
                    } else {
                        dto.setProductName(application.getProductCode());
                        dto.setInterestRate(0.0);
                    }
                } catch (Exception ex) {
                    dto.setProductName(application.getProductCode());
                    dto.setInterestRate(0.0);
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch product info for application ID: {}", account.getApplicationId());
            dto.setProductCode("N/A");
            dto.setProductName("N/A");
            dto.setInterestRate(0.0);
        }
        
        return dto;
    }

}
