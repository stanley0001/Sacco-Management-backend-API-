package com.example.demo.customerManagement.serviceImplimentations;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.banking.parsitence.enitities.Payments;
import com.example.demo.banking.parsitence.enitities.Transactions;
import com.example.demo.banking.parsitence.repositories.BankAccountRepo;
import com.example.demo.banking.parsitence.repositories.PaymentRepo;
import com.example.demo.banking.parsitence.repositories.PaymentTransactionRepo;
import com.example.demo.communication.parsitence.models.Email;
import com.example.demo.communication.parsitence.models.singleSmsModel;
import com.example.demo.communication.parsitence.repositories.emailRepo;
import com.example.demo.communication.services.InfoBidApiService;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.customerManagement.parsistence.repositories.CustomerRepo;
import com.example.demo.customerManagement.services.CustomerS;
import com.example.demo.loanManagement.parsistence.entities.Subscriptions;
import com.example.demo.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.loanManagement.parsistence.repositories.ApplicationRepo;
import com.example.demo.loanManagement.services.SubscriptionService;
import com.example.demo.system.parsitence.models.ResponseModel;
import com.example.demo.userManagements.parsitence.enitities.Users;
import com.example.demo.userManagements.serviceImplementation.UserService;
import com.infobip.model.SmsResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@EnableAsync
public class CustomerService implements CustomerS {

   public final CustomerRepo customerRepo;
   public final UserService userService;
   public final SubscriptionService subscriptionService;
   public final ApplicationRepo applicationRepo;
   public final BankAccountRepo bankAccountRepo;
   public final PaymentTransactionRepo transactionsRepo;
   public final PaymentRepo paymentRepo;
   public final emailRepo emailRepo;
   public final InfoBidApiService communicationService;

    public CustomerService(CustomerRepo customerRepo, UserService userService, SubscriptionService subscriptionService, ApplicationRepo applicationRepo, BankAccountRepo bankAccountRepo, PaymentTransactionRepo transactionsRepo, PaymentRepo paymentRepo, com.example.demo.communication.parsitence.repositories.emailRepo emailRepo, InfoBidApiService communicationService) {
        this.customerRepo = customerRepo;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.applicationRepo = applicationRepo;
        this.bankAccountRepo = bankAccountRepo;
        this.transactionsRepo = transactionsRepo;
        this.paymentRepo = paymentRepo;
        this.emailRepo = emailRepo;
        this.communicationService = communicationService;
    }

    public Customer saveCustomer(Customer customer) {
        //validations
        //ie iprs,external perties,crb
        //field validation
        Customer client= customerRepo.save(customer);
          //fire communication event
//        this.sendTransactionalSMS(client.getPhoneNumber(),"Dear "+client.getFirstName()+" "+client.getLastName()+" You have been successfully registered to Nyanathi sacco with member number "+client.getExternalId());
        return client;
    }

    public ResponseModel enableClientLogin(Long clientId){
        Customer client=customerRepo.findById(clientId).get();
        ResponseModel response=new ResponseModel();
            Users user=new Users();
            user.setActive(Boolean.TRUE);
            user.setUserName(client.getEmail());
            user.setEmail(client.getEmail());
            user.setCreatedAt(LocalDate.now());
            user.setFirstName(client.getFirstName());
            user.setLastName(client.getLastName());
            user.setDocumentNumber(client.getDocumentNumber());
            user.setPhone(client.getPhoneNumber());
            user.setRoleId("5");
            try {
                userService.saveUser(user);
                response.setStatus(HttpStatus.OK);
                response.setMessage("Client enabled please proceed");
            }catch (Exception e){
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setErrors("Error encountered: "+e.getMessage());
                log.warn("Error Enabling client login: {}",e.getMessage());
            }

            return response;

    }

    public ResponseModel findAll(int page,int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> allCustomers = customerRepo.findAll(pageable);
        ResponseModel responseModel=new ResponseModel();
        responseModel.setBody(allCustomers.get());
        responseModel.setSize(allCustomers.getSize());
        responseModel.setPage(page);
        responseModel.setTotalElements((int) allCustomers.getTotalElements());
        responseModel.setTotalPages(allCustomers.getTotalPages());
        responseModel.setStatus(HttpStatus.OK);
        responseModel.setMessage(allCustomers.getTotalElements()+" records found");
        return responseModel;
    }

    public ClientInfo findById(Long id) {
        ClientInfo clientInfo=new ClientInfo();
        Customer client=customerRepo.findById(id).get();
        String email=client.getEmail();
        String name=client.getPhoneNumber();
        Optional<List<Subscriptions>> subscriptions=subscriptionService.findCustomerId(id.toString());
        List<Email> communication=userService.communication.getOutboxByEmailOrderByIdDesc(email);
        Optional<Users> user=userService.findByName(name);
        String idNumber=client.getDocumentNumber();
        log.info("Fetching application by {}",idNumber);
        List<BankAccounts> bankAccounts=bankAccountRepo.findByCustomer(client).get();
        List<LoanApplication> applications=applicationRepo.findByCustomerIdNumber(idNumber);
        List<Payments> payments=paymentRepo.findAllByCustomer(client);
        clientInfo.setBankAccounts(bankAccounts);
         clientInfo.setClient(client);
         clientInfo.setCommunications(communication);
         clientInfo.setLoanApplications(applications);
         if (user.isPresent()){
             clientInfo.setUser(user.get());
         }
         if (subscriptions.isPresent()){
             clientInfo.setSubscriptions(subscriptions.get());
         }

        return clientInfo;
    }
 public List<Transactions> findTransactionsByBank(BankAccounts bankA){
        return transactionsRepo.findAllByBankAccount(bankA);
 }
    public Customer update(Customer customer) {
        return customerRepo.save(customer);
    }

    public void changeStatus(Long id, String status) {
        Customer customer=customerRepo.findById(id).get();
        customer.setAccountStatus(status);
        update(customer);

    }

    public Optional<Customer> findByPhone(String customerPhone) {
        return customerRepo.findByphoneNumber(customerPhone);
    }

    public void sendTransactionalSMS(String phone,String message){
        singleSmsModel sms=new singleSmsModel();
        Email SMS=new Email();
        SMS.setMessage(message);
        SMS.setRecipient(phone);
        SMS.setMessageType("SMS");
        SMS.setStatus("NEW");
        SMS.setDate(LocalDate.now());
        emailRepo.save(SMS);
        sms.setContact(SMS.getRecipient());
        sms.setMessage(SMS.getMessage());
        SmsResponse response=communicationService.send1(sms);
        log.info("sending sms");
        if (response!=null) {
            log.info("receiced response");
            if (!response.getBulkId().isEmpty()) {
                SMS.setStatus("PROCESSED " + response.getMessages().get(0).getMessageId());
                emailRepo.save(SMS);
                log.info("Sms response {}", response.getMessages());
            }
        }else {
            log.info("No response from sms API");
        }

    }
}
