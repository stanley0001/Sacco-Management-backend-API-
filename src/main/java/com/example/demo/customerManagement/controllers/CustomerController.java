package com.example.demo.customerManagement.controllers;

import com.africastalking.sms.Recipient;
import com.example.demo.banking.parsitence.enitities.Payments;
import com.example.demo.communication.parsitence.models.WhatsAppMessage;
import com.example.demo.communication.parsitence.models.bulkSmsModel;
import com.example.demo.communication.services.AfricasTalkingApiService;
import com.example.demo.communication.services.CommunicationService;
import com.example.demo.communication.services.WhatsAppService;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.customerManagement.services.CustomerS;
import com.example.demo.loanManagement.parsistence.models.*;
import com.example.demo.loanManagement.services.LoanAccountService;
import com.example.demo.loanManagement.services.LoanService;
import com.example.demo.loanManagement.services.PaymentService;
import com.example.demo.loanManagement.services.SubscriptionService;
import com.example.demo.system.parsitence.models.DashBoardData;
import com.example.demo.system.parsitence.models.ResponseModel;
import com.example.demo.system.services.ReportService;
import com.example.demo.system.services.ScoreService;
import com.example.demo.userManagements.parsitence.enitities.Users;
import com.example.demo.userManagements.serviceImplementation.UserService;
import com.infobip.ApiException;
import com.infobip.model.SmsResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/customers")
public class CustomerController {

    public  final SubscriptionService subscriptions;
    public final CustomerS customerService;
    public  final UserService userService;
    public final LoanService loanService;
    public final PaymentService paymentService;
    public final LoanAccountService loanAccountService;
    public final ReportService reportService;
    public final ScoreService scoreService;
    public final AfricasTalkingApiService sms;
    public final CommunicationService communicationService;
    public final WhatsAppService whatsAppService;

    public CustomerController(SubscriptionService subscriptions, CustomerS customerService, UserService userService, LoanService loanService, PaymentService paymentService, LoanAccountService loanAccountService, ReportService reportService, ScoreService scoreService, AfricasTalkingApiService sms, CommunicationService communicationService, WhatsAppService whatsAppService) {
        this.subscriptions = subscriptions;
        this.customerService = customerService;
        this.userService = userService;
        this.loanService = loanService;
        this.paymentService = paymentService;
        this.loanAccountService = loanAccountService;
        this.reportService = reportService;
        this.scoreService = scoreService;
        this.sms = sms;
        this.communicationService = communicationService;
        this.whatsAppService = whatsAppService;
    }

    //creating customers
    @PostMapping("/create")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer){
        Customer customer1=customerService.saveCustomer(customer);
        return new ResponseEntity<>(customer1, HttpStatus.CREATED);
    }
    //finding customers info
    @GetMapping("/findall")
    public ResponseEntity<List<Customer>> findAll(){
          List<Customer> customers=customerService.findAll();
          return new ResponseEntity<>(customers,HttpStatus.OK);
    }
    //find all suspense payments
    @GetMapping("/findAllSuspense")
    public ResponseEntity<List<SuspensePayments>> findAllSuspense(String cusPhone){
        List<SuspensePayments> suspensePayments=loanAccountService.findAllOverPayment(cusPhone).get();
        return new ResponseEntity<>(suspensePayments,HttpStatus.OK);
    }
    @GetMapping ("/findCus{id}")
    public ResponseEntity<ClientInfo> findIndividual(@PathVariable Long id){
       ClientInfo customer=customerService.findById(id);
        return new ResponseEntity<>(customer,HttpStatus.OK);
    }
    //This method is too expensive to be changed
    @GetMapping ("/findCusByUsername{name}")
    public ResponseEntity<ClientInfo> findIndividualByName(@PathVariable String name){
        Users user=userService.findByName(name).get();
        Customer customerS=customerService.findByPhone(user.getPhone()).get();
        ClientInfo customer=customerService.findById(customerS.getId());
        return new ResponseEntity<>(customer,HttpStatus.OK);
    }
    //updating customers info
    @PutMapping("/update")
    public ResponseEntity<Customer> updateIndividual(@RequestBody Customer customer){
        Customer customer1=customerService.update(customer);
        return new ResponseEntity<>(customer1,HttpStatus.CREATED);
    }
    //deactivating customer
    @PutMapping("/changeStatus")
    public ResponseEntity<Customer> changeStatus(Long id,String status){
        customerService.changeStatus(id,status);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/createSubscription")
    public ResponseEntity<subscriptionR> subscribe(@RequestBody subscriptionR req){
        subscriptions.subscribe(req.getPhone(),req.getProductId());
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/findSubscription{id}")
    public ResponseEntity<Optional<List<Subscriptions>>> findSubscription(@PathVariable String id){
        Optional<List<Subscriptions>> subscription=subscriptions.findCustomerId(id);
        return new ResponseEntity<>(subscription,HttpStatus.OK);
    }
    @GetMapping("/findSubscriptionBybody")
    public ResponseEntity<Optional<Subscriptions>> findSubscription(String id,String productCode){
        Optional<Subscriptions> subscription=subscriptions.findCustomerIdandproductCode(id,productCode);
        return new ResponseEntity<>(subscription,HttpStatus.OK);
    }
    @PostMapping("/loanApplication")
    public ResponseEntity<loanApplication> loanApplication(@RequestBody newApplication application){
       loanService.loanApplication(application.getPhone(),application.getProductCode(),application.getAmount());
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/whatsappComm")
    public ResponseEntity whatsappComm(@RequestBody WhatsAppMessage message){
        whatsAppService.processWhatsAppRequest(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/loanRepayment")
    public ResponseEntity<Payments> loanRepayment(String phoneNumber, String productCode, String amount){
        paymentService.paymentRequest(phoneNumber,productCode,amount);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/customPayment")
    public ResponseEntity<Payments> customPayment(@RequestBody Payments payment){
        paymentService.processLoanPayment(payment);
        return new ResponseEntity<>(HttpStatus.OK);
    }
   @PostMapping("/score")
    public ResponseEntity score(Long id){
        Integer score=scoreService.loadData(id);
        return new ResponseEntity<>(score,HttpStatus.OK);
    }

    @PostMapping("/enableClientLogin")
    public ResponseEntity<ResponseModel> enableLogin(Long id){
        ResponseModel response=customerService.enableClientLogin(id);
        return new ResponseEntity<>(response,response.getStatus());
    }
    @GetMapping("/dashBoardData")
    public ResponseEntity<DashBoardData> getData(){
         DashBoardData data= reportService.getData();
        return new ResponseEntity<>(data,HttpStatus.OK);
    }
    @PostMapping("/sendSms")
    public ResponseEntity<List<Recipient>> send(String message) throws IOException {
        List<Recipient> data= sms.sendSms(message);
        return new ResponseEntity<>(data,HttpStatus.OK);
    }
    @PostMapping("/sendSms2")
    public ResponseEntity<List<SmsResponse>> send(@RequestBody bulkSmsModel customSms) throws IOException, ApiException {
        List<SmsResponse> data= communicationService.sendBulkSMS(customSms);
        return new ResponseEntity<>(data,HttpStatus.CREATED);
    }


}

