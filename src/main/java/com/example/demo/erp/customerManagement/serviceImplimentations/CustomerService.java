package com.example.demo.erp.customerManagement.serviceImplimentations;

import com.example.demo.erp.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.erp.customerManagement.services.CustomerS;
import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import com.example.demo.finance.banking.parsitence.enitities.Payments;
import com.example.demo.finance.banking.parsitence.enitities.Transactions;
import com.example.demo.finance.banking.parsitence.repositories.BankAccountRepo;
import com.example.demo.finance.banking.parsitence.repositories.PaymentRepo;
import com.example.demo.finance.banking.parsitence.repositories.PaymentTransactionRepo;
import com.example.demo.erp.communication.parsitence.models.Email;
import com.example.demo.erp.communication.parsitence.models.singleSmsModel;
import com.example.demo.erp.communication.parsitence.repositories.emailRepo;
import com.example.demo.erp.communication.services.InfoBidApiService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepo;
import com.example.demo.finance.loanManagement.parsistence.entities.Subscriptions;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.repositories.ApplicationRepo;
import com.example.demo.finance.loanManagement.services.SubscriptionService;
import com.example.demo.system.parsitence.models.ResponseModel;
import com.example.demo.system.userManagements.parsitence.enitities.Users;
import com.example.demo.system.userManagements.serviceImplementation.UserService;
import com.infobip.model.SmsResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
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

    public CustomerService(CustomerRepo customerRepo, UserService userService, SubscriptionService subscriptionService, ApplicationRepo applicationRepo, BankAccountRepo bankAccountRepo, PaymentTransactionRepo transactionsRepo, PaymentRepo paymentRepo, com.example.demo.erp.communication.parsitence.repositories.emailRepo emailRepo, InfoBidApiService communicationService) {
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
        return findAll(page, size, null, null);
    }

    @Override
    public ResponseModel findAll(int page, int size, String status, String searchTerm) {
        Pageable pageable = PageRequest.of(page, size);

        String query = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;
        Boolean statusFlag = null;
        if (status != null && !status.trim().isEmpty()) {
            if ("active".equalsIgnoreCase(status) || "true".equalsIgnoreCase(status)) {
                statusFlag = Boolean.TRUE;
            } else if ("inactive".equalsIgnoreCase(status) || "false".equalsIgnoreCase(status)) {
                statusFlag = Boolean.FALSE;
            }
        }

        Page<Customer> customerPage=findAll(statusFlag, query, pageable);

        ResponseModel responseModel = new ResponseModel();
        responseModel.setBody(customerPage.getContent());
        responseModel.setSize(customerPage.getSize());
        responseModel.setPage(page);
        responseModel.setTotalElements((int) customerPage.getTotalElements());
        responseModel.setTotalPages(customerPage.getTotalPages());
        responseModel.setStatus(HttpStatus.OK);
        responseModel.setMessage(customerPage.getTotalElements() + " records found");
        return responseModel;
    }

    public Page<Customer> findAll(Boolean statusFlag, String query, Pageable pageable) {
        Specification<Customer> spec = Specification
                .where(hasStatus(statusFlag))
                .and(matchesQuery(query));

        return customerRepo.findAll(spec, pageable);
    }

    public static Specification<Customer> hasStatus(Boolean status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() :
                        cb.equal(root.get("accountStatusFlag"), status);
    }

    public static Specification<Customer> matchesQuery(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + search.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), likePattern),
                    cb.like(cb.lower(root.get("lastName")), likePattern),
                    cb.like(cb.lower(root.get("documentNumber")), likePattern),
                    cb.like(cb.lower(root.get("phoneNumber")), likePattern),
                    cb.like(cb.lower(root.get("email")), likePattern)
            );
        };
    }

    public ClientInfo findById(Long id) {
        return customerRepo.findById(id)
                .map(this::buildClientInfo)
                .orElse(null);
    }

    public ClientInfo findByDocumentNumber(String documentNumber) {
        return customerRepo.findByDocumentNumber(documentNumber)
                .map(this::buildClientInfo)
                .orElse(null);
    }

    public ClientInfo findByExternalId(String externalId) {
        return customerRepo.findByExternalId(externalId)
                .map(this::buildClientInfo)
                .orElse(null);
    }

    /**
     * Helper method to build ClientInfo from a Customer entity.
     */
    private ClientInfo buildClientInfo(Customer client) {
        ClientInfo clientInfo = new ClientInfo();
        if (client.getDocumentNumber()==null && client.getExternalId()!=null){
            client=this.populateDocumentNumber(client,client.getExternalId());
        }
        String email = client.getEmail();
        String name = client.getPhoneNumber();
        String idNumber = client.getDocumentNumber();

        log.info("Building client info for customer with ID Number: {}", idNumber);

        // Defensive optional handling
        List<BankAccounts> bankAccounts = bankAccountRepo.findByCustomer(client).orElse(Collections.emptyList());
        List<LoanApplication> applications = applicationRepo.findByCustomerIdNumber(idNumber);
        List<Payments> payments = paymentRepo.findAllByCustomer(client);
        List<Email> communication = userService.communication.getOutboxByEmailOrderByIdDesc(email);

        Optional<List<Subscriptions>> subscriptions = subscriptionService.findCustomerId(client.getId().toString());
        Optional<Users> user = userService.findByName(name);

        clientInfo.setClient(client);
        clientInfo.setBankAccounts(bankAccounts);
        clientInfo.setLoanApplications(applications);
        clientInfo.setCommunications(communication);
        clientInfo.setCustomerPayments(payments);

        subscriptions.ifPresent(clientInfo::setSubscriptions);
        user.ifPresent(clientInfo::setUser);

        return clientInfo;
    }

    private Customer populateDocumentNumber(Customer customer,String documentNumber){
       log.info("populating document number for {}",documentNumber);
        customer.setDocumentNumber(documentNumber);
        return customerRepo.save(customer);
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

    @Override
    public Optional<Customer> findCustomerById(Long id) {
        return customerRepo.findById(id);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepo.deleteById(id);
        log.info("Customer with ID {} has been deleted", id);
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
