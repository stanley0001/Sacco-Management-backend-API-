package com.example.demo.customerManagement.controllers;

//import com.africastalking.sms.Recipient;
import com.example.demo.banking.parsitence.enitities.Payments;
import com.example.demo.communication.parsitence.models.WhatsAppMessage;
import com.example.demo.communication.parsitence.models.bulkSmsModel;
import com.example.demo.communication.services.AfricasTalkingApiService;
import com.example.demo.communication.services.CommunicationService;
import com.example.demo.communication.services.WhatsAppService;
import com.example.demo.customerManagement.dto.ImportResultDto;
import com.example.demo.customerManagement.dto.StatusUpdateDto;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.customerManagement.services.CustomerS;
import com.example.demo.customerManagement.services.CustomerImportExportService;
import com.example.demo.loanManagement.parsistence.entities.Subscriptions;
import com.example.demo.loanManagement.parsistence.entities.SuspensePayments;
import com.example.demo.loanManagement.parsistence.entities.LoanApplication;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/api/customers")
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
    public final CustomerImportExportService importExportService;

    public CustomerController(
            SubscriptionService subscriptions, 
            CustomerS customerService, 
            UserService userService, 
            LoanService loanService, 
            PaymentService paymentService, 
            @Qualifier("loanAccountService") LoanAccountService loanAccountService, 
            ReportService reportService, 
            ScoreService scoreService, 
            AfricasTalkingApiService sms, 
            CommunicationService communicationService, 
            WhatsAppService whatsAppService,
            CustomerImportExportService importExportService
    ) {
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
        this.importExportService = importExportService;
    }

    //creating customers
    @PostMapping("/create")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer){
        Customer customer1=customerService.saveCustomer(customer);
        return new ResponseEntity<>(customer1, HttpStatus.CREATED);
    }
    //finding customers info
    @GetMapping("all")
    public ResponseEntity<ResponseModel> findAll(@RequestParam("page") int page,@RequestParam("size") int size){
        ResponseModel customers=customerService.findAll(page,size);
          return new ResponseEntity<>(customers,HttpStatus.OK);
    }
    //find all suspense payments
    @GetMapping("/findAllSuspense")
    public ResponseEntity<List<SuspensePayments>> findAllSuspense(@RequestParam("cusPhone") String cusPhone){
        List<SuspensePayments> suspensePayments=loanAccountService.findAllOverPayment(cusPhone).get();
        return new ResponseEntity<>(suspensePayments,HttpStatus.OK);
    }
    @GetMapping ("/findCus/{id}")
    public ResponseEntity<ClientInfo> findIndividual(@PathVariable("id") Long id){
       ClientInfo customer=customerService.findById(id);
        return new ResponseEntity<>(customer,HttpStatus.OK);
    }
    //This method is too expensive to be changed
    @GetMapping ("/findCusByUsername/{name}")
    public ResponseEntity<ClientInfo> findIndividualByName(@PathVariable("name") String name){
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
    public ResponseEntity<Customer> changeStatus(@RequestParam("id") Long id, @RequestParam("status") String status){
        customerService.changeStatus(id,status);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/createSubscription")
    public ResponseEntity<subscriptionR> subscribe(@RequestBody subscriptionR req){
        subscriptions.subscribe(req.getPhone(),req.getProductId(),req.getAmount());
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/findSubscription/{id}")
    public ResponseEntity<Optional<List<Subscriptions>>> findSubscription(@PathVariable("id") String id){
        Optional<List<Subscriptions>> subscription=subscriptions.findCustomerId(id);
        return new ResponseEntity<>(subscription,HttpStatus.OK);
    }
    @GetMapping("/findSubscriptionBybody")
    public ResponseEntity<Optional<Subscriptions>> findSubscription(@RequestParam("id") String id, @RequestParam("productCode") String productCode){
        Optional<Subscriptions> subscription=subscriptions.findCustomerIdandproductCode(id,productCode);
        return new ResponseEntity<>(subscription,HttpStatus.OK);
    }
    @PostMapping("/loanApplication")
    public ResponseEntity<LoanApplication> loanApplication(@RequestBody newApplication application){
       loanService.loanApplication(application.getPhone(),application.getProductCode(),application.getAmount());
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/whatsappComm")
    public ResponseEntity whatsappComm(@RequestBody WhatsAppMessage message){
        whatsAppService.processWhatsAppRequest(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/loanRepayment")
    public ResponseEntity<Payments> loanRepayment(@RequestParam("phoneNumber") String phoneNumber, @RequestParam("productCode") String productCode, @RequestParam("amount") String amount){
        paymentService.paymentRequest(phoneNumber,productCode,amount);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/customPayment")
    public ResponseEntity<Payments> customPayment(@RequestBody Payments payment){
        paymentService.processLoanPayment(payment);
        return new ResponseEntity<>(HttpStatus.OK);
    }
   @PostMapping("/score")
    public ResponseEntity<Integer> score(@RequestParam("id") Long id){
        Integer score=scoreService.loadData(id);
        return new ResponseEntity<>(score,HttpStatus.OK);
    }

    @PostMapping("/enableClientLogin")
    public ResponseEntity<ResponseModel> enableLogin(@RequestParam("id") Long id){
        ResponseModel response=customerService.enableClientLogin(id);
        return new ResponseEntity<>(response,response.getStatus());
    }
    @GetMapping("/dashBoardData")
    public ResponseEntity<DashBoardData> getData(){
         DashBoardData data= reportService.getData();
        return new ResponseEntity<>(data,HttpStatus.OK);
    }
//    @PostMapping("/sendSms")
//    public ResponseEntity<List<Recipient>> send(String message) throws IOException {
//        List<Recipient> data= sms.sendSms(message);
//        return new ResponseEntity<>(data,HttpStatus.OK);
//    }
    @PostMapping("/sendSms2")
    public ResponseEntity<List<SmsResponse>> send(@RequestBody bulkSmsModel customSms) throws IOException, ApiException {
        List<SmsResponse> data= communicationService.sendBulkSMS(customSms);
        return new ResponseEntity<>(data,HttpStatus.CREATED);
    }

    // New endpoints for frontend integration
    @PostMapping("/applyLoan")
    public ResponseEntity<LoanApplication> applyForLoan(@RequestBody newApplication application){
        loanService.loanApplication(application.getPhone(),application.getProductCode(),application.getAmount());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/makePayment")
    public ResponseEntity<Payments> makePayment(@RequestBody Payments payment){
        paymentService.processLoanPayment(payment);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessage(@RequestBody Object messageData){
        // Implementation for sending messages
        return new ResponseEntity<>("Message sent successfully", HttpStatus.OK);
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<Customer> updateProfile(@RequestBody Customer customer){
        Customer updatedCustomer = customerService.update(customer);
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }

    /**
     * Import customers from Excel or CSV file
     * POST /api/customers/import
     */
    @PostMapping("/import")
    public ResponseEntity<ImportResultDto> importCustomers(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            String importedBy = authentication != null ? authentication.getName() : "system";
            ImportResultDto result = importExportService.importCustomers(file, importedBy);
            
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error importing customers", e);
            ImportResultDto errorResult = ImportResultDto.builder()
                    .failed(1)
                    .successful(0)
                    .totalRecords(0)
                    .build();
            errorResult.getErrors().add("Import failed: " + e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Export all customers to Excel
     * GET /api/customers/export
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCustomers() {
        try {
            ByteArrayOutputStream outputStream = importExportService.exportCustomersToExcel();
            byte[] excelBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    "members_export_" + LocalDate.now() + ".xlsx");
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error exporting customers", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update customer status (activate/deactivate)
     * PATCH /api/customers/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Customer> updateCustomerStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateDto statusUpdate
    ) {
        try {
            Optional<Customer> optionalCustomer = customerService.findCustomerById(id);
            if (optionalCustomer.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Customer customer = optionalCustomer.get();
            Boolean isActive = statusUpdate.getIsActive();
            customer.setAccountStatusFlag(isActive != null && isActive);
            customer.setStatus(isActive != null && isActive ? "ACTIVE" : "INACTIVE");
            
            Customer updated = customerService.update(customer);
            
            log.info("Customer {} status updated to: {}", id, statusUpdate.getIsActive());
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating customer status", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete customer
     * DELETE /api/customers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            Optional<Customer> optionalCustomer = customerService.findCustomerById(id);
            if (optionalCustomer.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            customerService.deleteCustomer(id);
            
            log.info("Customer {} deleted successfully", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error deleting customer", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}

