package com.example.demo.customerManagement.serviceImplimentations;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.banking.parsitence.enitities.Payments;
import com.example.demo.banking.parsitence.enitities.Transactions;
import com.example.demo.banking.parsitence.repositories.BankAccountRepo;
import com.example.demo.banking.parsitence.repositories.PaymentRepo;
import com.example.demo.banking.parsitence.repositories.PaymentTransactionRepo;
import com.example.demo.communication.parsitence.models.Email;
import com.example.demo.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.repositories.CustomerRepo;
import com.example.demo.customerManagement.services.CustomerS;
import com.example.demo.loanManagement.parsistence.models.Subscriptions;
import com.example.demo.loanManagement.parsistence.models.loanApplication;
import com.example.demo.loanManagement.parsistence.repositories.ApplicationRepo;
import com.example.demo.loanManagement.services.SubscriptionService;
import com.example.demo.system.parsitence.models.ResponseModel;
import com.example.demo.userManagements.parsitence.enitities.Users;
import com.example.demo.userManagements.serviceImplementation.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class CustomerService implements CustomerS {

   public final CustomerRepo customerRepo;
   public final UserService userService;
   public final SubscriptionService subscriptionService;
   public final ApplicationRepo applicationRepo;
   public final BankAccountRepo bankAccountRepo;
   public final PaymentTransactionRepo transactionsRepo;
   public final PaymentRepo paymentRepo;

    public CustomerService(CustomerRepo customerRepo, UserService userService, SubscriptionService subscriptionService, ApplicationRepo applicationRepo, BankAccountRepo bankAccountRepo, PaymentTransactionRepo transactionsRepo, PaymentRepo paymentRepo) {
        this.customerRepo = customerRepo;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.applicationRepo = applicationRepo;
        this.bankAccountRepo = bankAccountRepo;
        this.transactionsRepo = transactionsRepo;
        this.paymentRepo = paymentRepo;
    }

    public Customer saveCustomer(Customer customer) {
        //validations
        //ie iprs,external perties,crb
        //field validation
        Customer client= customerRepo.save(customer);
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

    public List<Customer> findAll() {
        return customerRepo.findAll();
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
        List<loanApplication> applications=applicationRepo.findByCustomerIdNumber(idNumber);
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
}
