package com.example.demo.services;

import com.example.demo.model.*;
import com.example.demo.model.models.ClientInfo;
import com.example.demo.persistence.repository.ApplicationRepo;
import com.example.demo.persistence.repository.CustomerRepo;
import com.example.demo.persistence.entities.Users;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class CustomerService {

   public final CustomerRepo customerRepo;
   public final userService userService;
   public final SubscriptionService subscriptionService;
   public final ApplicationRepo applicationRepo;

    public CustomerService(CustomerRepo customerRepo, com.example.demo.services.userService userService, SubscriptionService subscriptionService, ApplicationRepo applicationRepo) {
        this.customerRepo = customerRepo;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.applicationRepo = applicationRepo;
    }

    public Customer saveCustomer(Customer customer) {
        //validations
        //ie iprs,external perties,crb
        //field validation
        Customer client= customerRepo.save(customer);
           if (client!=null){
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
               userService.saveUser(user);
           }

        return client;
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
        List<loanApplication> applications=applicationRepo.findByCustomerIdNumber(idNumber);
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

    public Customer update(Customer customer) {
        return customerRepo.save(customer);
    }

    public void changeStatus(Long id, Boolean status) {
        Customer customer=customerRepo.findById(id).get();
        customer.setStatus(status);
        update(customer);

    }

    public Optional<Customer> findByPhone(String customerPhone) {
        return customerRepo.findByphoneNumber(customerPhone);
    }
}
