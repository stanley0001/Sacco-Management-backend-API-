package com.example.demo.finance.banking.serviceImplementation;

import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import com.example.demo.finance.banking.parsitence.enitities.Payments;
import com.example.demo.finance.banking.parsitence.enitities.Transactions;
import com.example.demo.finance.banking.parsitence.repositories.BankAccountRepo;
import com.example.demo.finance.banking.parsitence.repositories.PaymentRepo;
import com.example.demo.finance.banking.parsitence.repositories.PaymentTransactionRepo;
import com.example.demo.finance.banking.services.BankingService;
import com.example.demo.erp.communication.parsitence.repositories.emailRepo;
import com.example.demo.erp.communication.services.CommunicationService;
import com.example.demo.erp.communication.services.InfoBidApiService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.serviceImplimentations.CustomerService;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.Products;
import com.example.demo.finance.loanManagement.parsistence.entities.SuspensePayments;
import com.example.demo.finance.loanManagement.services.LoanAccountService;
import com.example.demo.finance.loanManagement.services.PaymentService;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class BankingServiceImplementation implements BankingService {

    public final PaymentRepo paymentRepo;
    public  final CustomerService customerService;
    private final LoanAccountService loanAccountService;
    private final PaymentService paymentService;
    private final BankAccountRepo bankAccountRepo;
    public final PaymentTransactionRepo transactionsRepo;
    public final CommunicationService communicationService;
    public final emailRepo emailRepo;
    public final InfoBidApiService smsService;

    @Autowired
    public BankingServiceImplementation(
            PaymentRepo paymentRepo, 
            CustomerService customerService, 
            @Qualifier("loanAccountService") LoanAccountService loanAccountService, 
            PaymentService paymentService, 
            BankAccountRepo bankAccountRepo, 
            PaymentTransactionRepo transactionsRepo, 
            CommunicationService communicationService, 
            com.example.demo.erp.communication.parsitence.repositories.emailRepo emailRepo,
            InfoBidApiService smsService
    ) {
        this.paymentRepo = paymentRepo;
        this.customerService = customerService;
        this.loanAccountService = loanAccountService;
        this.paymentService = paymentService;
        this.bankAccountRepo = bankAccountRepo;
        this.transactionsRepo = transactionsRepo;
        this.communicationService = communicationService;
        this.emailRepo = emailRepo;
        this.smsService = smsService;
    }
    //saving a payment
   /*public ResponseModel savePayment(Payments payments){
        ResponseModel response=new ResponseModel();
        try {
            Payments payments1=paymentRepo.save(payments);
            response.setStatus(HttpStatus.CREATED);
            response.setMessage("Payment saved..");
            response.setBody(payments1.toString());
        }catch (Exception e){
            String error="Failed to save payment with error: "+e.getMessage();
            response.setErrors(error);
            response.setStatus(HttpStatus.BAD_REQUEST);
            log.warn("error saving payment: {}",e.getMessage());
        }
        return response;
   }

    */
    public Payments savePayment(Payments payments){
       return this.paymentRepo.save(payments);
    }
   //process payment
    @Async
    public void processSinglePayment(Payments payment) throws InterruptedException {
        //find customer
        Optional<Customer> customerByDestinationAccount=customerService.findByPhone(payment.getDestinationAccount());
        Optional<Customer> customerByPaymentAccount=customerService.findByPhone(payment.getAccountNumber());
        Customer workingAccountHolder;
        Optional<LoanAccount> workingLoanAccounts=loanAccountService.findLoanAccountByLoanNumber(payment.getDestinationAccount());
        if (customerByDestinationAccount.isPresent()){
             //set working account holder
            workingAccountHolder=customerByDestinationAccount.get();
            log.info("Saving customer found..{}",workingAccountHolder.getPhoneNumber());
            // process savings
            this.handleSavings(workingAccountHolder,payment);
           }else {
            //find loan to repay
            if (workingLoanAccounts.isPresent()){
                //repay loan
                try {
                    log.info("Repaying a loan");
                    paymentService.processLoanPayment(payment);
                    payment.setStatus("PROCESSED FOR LOAN REPAYMENT");
                    this.paymentRepo.save(payment);
                }catch (Exception e){
                    log.warn("Error repaying loan: {}",e.getMessage());
                }

            }else {
                if (customerByPaymentAccount.isPresent()){
                    //set working account holder
                    workingAccountHolder=customerByPaymentAccount.get();
                    log.info("Saving customer found..{}",workingAccountHolder.getPhoneNumber());
                    // process savings
                    this.handleSavings(workingAccountHolder,payment);
                }else{
                    //else save as suspense amount
                    log.info("No Saving or loan found.., saving suspense payments");
                    // process suspense amounts
                    try {
                        SuspensePayments suspensePayments=new SuspensePayments();
                        suspensePayments.setPaymentTime(LocalDateTime.now());
                        suspensePayments.setDestinationAccount(payment.getDestinationAccount());
                        suspensePayments.setAmount(payment.getAmount());
                        suspensePayments.setStatus("NEW");
                        suspensePayments.setOtherRef(payment.getOtherRef());
                        suspensePayments.setAccountNumber(payment.getAccountNumber());
                        suspensePayments.setExceptionType("MISSING CUSTOMER");
                        paymentService.saveSuspensePayment(suspensePayments);
                    }catch (Exception e){
                        log.warn("Error saving suspense payment: {}",e.getMessage());
                    }

                }
            }


        }

    }

    //handle saving request
    @Async
    public void handleSavings(Customer customer,Payments payment) throws InterruptedException {


        //check overlapping penalties


        //get welfare balance
        Double savingsAccountBalance=null;
       Double workingAmount=Double.valueOf(payment.getAmount());
       List<BankAccounts> bankAccounts=this.getBankAccountsByCustomer(customer).get();
       log.info("bank accounts found {}",bankAccounts.size());
        if (bankAccounts.size() > 0){
            //variables initialisation
            BankAccounts welfareAccount = null;
            BankAccounts savingsAccount = null;
            for (BankAccounts bankAccount:
                 bankAccounts) {
                //proposed working model
                if (bankAccount.getAccountType().equals("ALPHA")){
                    welfareAccount=bankAccount;
                }
                if (bankAccount.getAccountType().equals("SAVINGS")){
                     savingsAccount=bankAccount;
                }

            }
            //check and save welfare should return amount after welfare saving
            Double transactionAmount=saveWelfareTransaction(welfareAccount,payment.getOtherRef(),workingAmount);
            if (transactionAmount > 0.0) {
                this.saveSavingsTransaction(savingsAccount,payment.getOtherRef(),transactionAmount);
            }
        }else {
            log.info("No bank accounts found..");
            this.createBankAccounts(customer);
            Thread.sleep(9000);
            this.handleSavings(customer,payment);
        }
        //send communication
        log.info("sending transaction sms");
        customerService.sendTransactionalSMS(customer.getPhoneNumber(),"Dear "+customer.getFirstName()+", "+customer.getExternalId()+" we have received your contribution of Ksh "+payment.getAmount()+" and Ref "+payment.getOtherRef()+" at "+payment.getPaymentTime()+" Your Nyanathi Saving account \n your balance is ksh "+getBankAccountsByCustomer(customer).get().get(2).getAccountBalance());
        payment.setStatus("PROCESSED AS CONTRIBUTION");
        log.info("saving payment");
         this.paymentRepo.save(payment);
    }


    public Double saveWelfareTransaction(BankAccounts welfareAccount,String paymentRef,Double workingAmount){
        Double welfareAmount=this.getAmountByAccount(welfareAccount);
        Double transactionAmount;
        log.info("working amount before welfare check {}",workingAmount);
        log.info("today s welfare account {}",welfareAmount);
        if (welfareAmount<=20.0){
            if (welfareAmount==0.0){
                welfareAmount=20.0;
            }else {
                welfareAmount=20.0-welfareAmount;
            }

            if (workingAmount>welfareAmount){
                transactionAmount=welfareAmount;
            }else {
                transactionAmount=workingAmount;
            }
            //Save transactions
            Transactions transaction=new Transactions();
            transaction.setTransactionTime(LocalDateTime.now());
            transaction.setTransactionType("WELFARE");
            transaction.setAmount(transactionAmount);
            transaction.setBankAccount(welfareAccount);
            transaction.setOtherRef(paymentRef);
            transaction.setOpeningBalance(welfareAccount.getAccountBalance());
            transaction.setClosingBalance(welfareAccount.getAccountBalance()+transactionAmount);
            this.saveTransaction(transaction);
            welfareAccount.setAccountBalance(transaction.getClosingBalance());
            this.saveBankAccount(welfareAccount);
            workingAmount=workingAmount-transactionAmount;
            log.info("working amount balance {}",workingAmount);
        }
        log.info("working amount after welfare check {}",workingAmount);
        return workingAmount;
    }
    @Async
    public void saveSavingsTransaction(BankAccounts savingsAccount,String paymentRef,Double transactionAmount){
        log.info("working savings account {}",savingsAccount.getBankAccount());
        Transactions transaction = new Transactions();
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setTransactionType("SAVINGS");
        transaction.setAmount(transactionAmount);
        transaction.setOpeningBalance(savingsAccount.getAccountBalance());
        transaction.setClosingBalance(savingsAccount.getAccountBalance() + transactionAmount);
        transaction.setOtherRef(paymentRef);
        transaction.setBankAccount(savingsAccount);
        //create transactions
        log.info("saving transaction");
        this.saveTransaction(transaction);
        //update account balance
        log.info("updating balance");
        savingsAccount.setAccountBalance(transaction.getClosingBalance());
        //save amount
        this.saveBankAccount(savingsAccount);
    }

    //creating BankAccounts
    public List<BankAccounts> createBankAccounts(Customer customer){
        log.info("creating bank accounts for : {}",customer.getPhoneNumber());
       //BankAccounts[] createdAccounts =new BankAccounts[3];
        List<BankAccounts> list = new ArrayList<>();
        //check existing bank accounts
        Optional<List<BankAccounts>> bankAccounts=this.getBankAccountsByCustomer(customer);
        if (!bankAccounts.get().isEmpty()){
            list=bankAccounts.get();
             log.info("Existing bank accounts ..{}",list);

        }else {
            //create accounts "ALPHA","INVESTA","SHARES"
            for (int i = 1; i < 4; i++) {
                String accountType;
                String accountDescription;
                BankAccounts bankAccount=new BankAccounts();
                bankAccount.setAccountBalance(0.0);
                bankAccount.setBankAccount("20"+i+customer.getDocumentNumber()+"00");
                bankAccount.setCreatedAt(LocalDateTime.now());
                bankAccount.setCustomer(customer);
                accountType="";
                accountDescription="";

                if (i==1){
                    accountType="SAVINGS";
                    accountDescription="This is a savings account where one can use it to get a loan";

                }
                   if (i==2) {
                       accountType="SHARES";
                       accountDescription="This is a shares account";

                   }
                       if (i==3){
                           accountType="ALPHA";
                           accountDescription="This is a transactional bank account";

                       }

                bankAccount.setAccountType(accountType);
                bankAccount.setAccountDescription(accountDescription);
                try {
                    list.add(this.saveBankAccount(bankAccount));
                }catch (Exception e){
                    log.error("error saving Bank Account : {}",e.getMessage());
                }

            }

        }
        return list;
    }

    //save bank account
    private BankAccounts saveBankAccount(BankAccounts bankAccount){
        return bankAccountRepo.save(bankAccount);
    }
    //find bank accounts by customer
    public Optional<List<BankAccounts>> getBankAccountsByCustomer(Customer customer){
        return bankAccountRepo.findByCustomer(customer);
    }
    //find bank account by account number
    public BankAccounts findAccountByAccountNumber(String accountNumber){
        return bankAccountRepo.findByBankAccount(accountNumber);
    }
    //REFRESH ALL ACCOUNTS
    public void refreshAllAccounts(){
        List<Customer> customers= (List<Customer>) customerService.findAll(0,10).getBody();
        for (Customer customer:
                customers ) {
        try {
            this.createBankAccounts(customer);
        }catch (Exception e){
            log.warn("error creating accounts for customer: {}",customer.getPhoneNumber());
        }

        }
    }

    @Override
    public void processInitialDepositIfPresent(Customer customer) {
        Double initialDeposit = customer.getInitialDepositAmount();
        if (initialDeposit == null || initialDeposit <= 0) {
            return;
        }

        Optional<List<BankAccounts>> accountsOptional = this.getBankAccountsByCustomer(customer);
        if (accountsOptional.isEmpty() || accountsOptional.get().isEmpty()) {
            log.warn("No bank accounts available for customer {} when processing initial deposit", customer.getPhoneNumber());
            return;
        }

        BankAccounts savingsAccount = accountsOptional.get().stream()
                .filter(acc -> "SAVINGS".equalsIgnoreCase(acc.getAccountType()))
                .findFirst()
                .orElse(null);

        if (savingsAccount == null) {
            log.warn("Savings account missing for customer {} when processing initial deposit", customer.getPhoneNumber());
            return;
        }

        String reference = String.format("INIT-%s", customer.getId());
        Transactions transaction = new Transactions();
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setTransactionType("INITIAL_DEPOSIT");
        transaction.setAmount(initialDeposit);
        transaction.setOpeningBalance(savingsAccount.getAccountBalance());
        transaction.setClosingBalance(savingsAccount.getAccountBalance() + initialDeposit);
        transaction.setOtherRef(reference);
        transaction.setBankAccount(savingsAccount);

        this.saveTransaction(transaction);
        savingsAccount.setAccountBalance(transaction.getClosingBalance());
        this.saveBankAccount(savingsAccount);

        Payments paymentRecord = new Payments();
        paymentRecord.setAccountNumber(customer.getPhoneNumber());
        paymentRecord.setDestinationAccount(savingsAccount.getBankAccount());
        paymentRecord.setAmount(String.valueOf(initialDeposit));
        paymentRecord.setStatus("INITIAL_DEPOSIT");
        paymentRecord.setOtherRef(reference);
        paymentRecord.setPaymentTime(LocalDateTime.now());
        paymentRecord.setCustomer(customer);
        this.savePayment(paymentRecord);
    }

    @Override
    public BankAccounts createAccountForProduct(Customer customer, Products product, String customDescription) {
        if (customer == null || product == null) {
            throw new IllegalArgumentException("Customer and product are required to create an account");
        }

        String productCode = product.getCode() != null ? product.getCode() : "GEN";
        String baseAccountNumber = String.format("%s%s%02d", 30, customer.getDocumentNumber(), product.getId());

        BankAccounts existing = bankAccountRepo.findByBankAccount(baseAccountNumber);
        if (existing != null) {
            log.info("Account already exists for customer {} and product {}", customer.getId(), productCode);
            return existing;
        }

        BankAccounts account = new BankAccounts();
        account.setCustomer(customer);
        account.setBankAccount(baseAccountNumber);
        account.setAccountType(product.getTransactionType() != null ? product.getTransactionType() : "SAVINGS");
        account.setAccountDescription(customDescription != null ? customDescription : product.getName());
        account.setAccountBalance(0.0);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        BankAccounts saved = saveBankAccount(account);
        log.info("Created new bank account {} for customer {} and product {}", saved.getBankAccount(), customer.getId(), productCode);
        return saved;
    }

    //Transactions
    //save transactions
    public Transactions saveTransaction(Transactions transaction){
        return transactionsRepo.save(transaction);
    }
    public List<Transactions> findTransactionsByAccount(BankAccounts account){
       return transactionsRepo.findAllByBankAccount(account);
    }
    //get amount contributed today by account
    public Double getAmountByAccount(BankAccounts account){
        LocalDateTime from= LocalDate.now().atStartOfDay();
        LocalDateTime to=LocalDateTime.now();
        Double amount=0.0;
        Double fetchedAmount=transactionsRepo.findAmountByAccount(from,to,account);
        if (fetchedAmount!=null){
            amount=fetchedAmount;
        }
        return amount;
    }
    //handling mpesa callback
    @Async
    public void handleCallBack(JSONObject jsonObject) throws InterruptedException {
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
                    payment.setDestinationAccount(payment.getAccountNumber());
                    payment.setStatus("NEW");
                    payment.setPaymentTime(LocalDateTime.now());

                    //

                    // Verify transaction

                    // update model
                    //
                    //Save and process
                    try {
                        Payments savedPayment=this.savePayment(payment);
                        if (!savedPayment.getOtherRef().equals(null)){
                            this.processSinglePayment(savedPayment);
                        }
                    }catch (Exception e){
                        log.warn("Error {}",e.getMessage());
                    }
                      /* if (response.getStatus().equals(HttpStatus.CREATED)){
                           this.processSinglePayment(payment);
                       }else {

                       }*/


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
