package com.example.demo.erp.communication.services;


import com.example.demo.erp.communication.parsitence.repositories.ContactBookRepo;
import com.example.demo.erp.communication.parsitence.repositories.ContactListRepo;
import com.example.demo.erp.communication.parsitence.repositories.TemplateRepo;
import com.example.demo.erp.communication.parsitence.models.*;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepo;
import com.infobip.ApiException;
import com.infobip.model.SmsResponse;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
@Service
@Log4j2
@EnableAsync
public class CommunicationService {
    public final com.example.demo.erp.communication.parsitence.repositories.emailRepo emailRepo;
    public final TemplateRepo templateRepo;
    public final InfoBidApiService sms;
    public final ContactBookRepo contactBookRepo;
    public final ContactListRepo contactListRepo;
    public final CustomerRepo customerRepo;

    public CommunicationService(com.example.demo.erp.communication.parsitence.repositories.emailRepo emailRepo, TemplateRepo templateRepo, InfoBidApiService sms, ContactBookRepo contactBookRepo, ContactListRepo contactListRepo, CustomerRepo customerRepo) {
        this.emailRepo = emailRepo;
        this.templateRepo = templateRepo;
        this.sms = sms;
        this.contactBookRepo = contactBookRepo;
        this.contactListRepo = contactListRepo;
        this.customerRepo = customerRepo;
    }
    public void sendEmail(String[] data){
        String variable[] = new String[]{
                data[0],data[1],data[2]
        };

        theEmail(variable);
    }
    @Async
    public void getData(String[] data){
     String variable[] = new String[]{
             data[0],"Creation Email","Hi "+data[1]+"\r\n" +
             " Welcome to Alpha \r\nPlease use Username:<b>"+data[2]+"</b> and Password:<b>"+data[3]+"</b> to access the system"
     };

     theEmail(variable);
 }

    //send email
    @Async
    public void theEmail(String[] variable){
        log.info("saving email....");
        //save Email
        Email email= new Email();
        email.setDate(LocalDate.now());
        email.setMessage(variable[2]);
        email.setRecipient(variable[0]);
        email.setStatus("PROCESSED");
        email.setMessageType("EMAIL");
        emailRepo.save(email);

        //send message
        try {
            log.info("sending email to "+email.getRecipient()+"..");

            sendmail(variable);
            log.info("email sent.");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //email auth
    Properties props = new Properties();
    Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("mungaistanley001@gmail.com", "rploampijtiyfbhz");
        }
    });
    //email test
    @Async
    public void sendmail(String[] variables) throws AddressException, MessagingException, IOException {

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("STAN", false));

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(variables[0]));
        msg.setSubject(variables[1]);
        msg.setContent(variables[2], "text/html");
        msg.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(variables[2], "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        MimeBodyPart attachPart = new MimeBodyPart();

        //attachPart.attachFile(customPath);
        // multipart.addBodyPart(attachPart);
        msg.setContent(multipart);

      Transport.send(msg);
    }

    @Async
    public void resetPassword(String[] data) {
        String variable[] = new String[]{
                data[0],"Password Reset","Hi "+data[1]+"\r\n" +
                " Someone tried to reset your Alpha account password,\r\nPlease use Username:<b>"+data[2]+"</b> and Password:<b>"+data[3]+"</b> to access the system"
        };

        theEmail(variable);
    }

    public messageTemplates createTemplate(messageTemplates template) {
        return templateRepo.save(template);
    }

    public messageTemplates updateTemplate(messageTemplates template) {
        return templateRepo.save(template);
    }

    public List<messageTemplates> getTemplates() {
        return templateRepo.findAll();
    }

    public messageTemplates getTemplateById(Long id) {
        return templateRepo.getById(id);
    }

    @Async
    public void sendCustomEmail(Email mail) {
        String variable[] = new String[]{
                mail.getRecipient(),mail.getMessageType(),mail.getMessage()
        };

        try {
           // theEmail(variable);
            //changing email communication to whatsapp communication
            Customer customer=customerRepo.findByEmail(mail.getRecipient());
            if (customer != null && customer.getPhoneNumber() != null) {
                String requestParams="?instanceId=109266945127952&to="+customer.getPhoneNumber()+"&message="+mail.getMessage();
                UriComponents components = UriComponentsBuilder.fromHttpUrl("http://192.168.43.63:30001/communication/sendWhatsAppMessage"+requestParams).pathSegment(null).build();
                ResponseEntity responseEntity = postEntity(components, null, null, String.class);
                log.debug("WhatsApp message sent to {}", customer.getPhoneNumber());
            } else {
                log.debug("Customer not found or phone number missing for email: {}", mail.getRecipient());
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Connection timeout - WhatsApp service unavailable (suppress noisy logs)
            log.debug("WhatsApp service unavailable: {}", e.getMessage());
        } catch (Exception e){
            log.warn("Error sending communication to {}: {}", mail.getRecipient(), e.getMessage());
        }


    }

    public List<Email> getOutbox() {
        return emailRepo.findAllOrderByIdDesc();
    }

    public List<Email> getOutboxByEmail(String email) {
        return emailRepo.findByRecipient(email);
    }

    public List<Email> getOutboxByEmailOrderByIdDesc(String email) {
        return emailRepo.findByRecipientOrderByIdDesc(email);
    }

    /**
     * Get communications for a specific customer
     * @param customerId Customer ID
     * @param limit Maximum number of records to return
     * @return List of recent communications for the customer
     */
    public List<Email> getOutboxByCustomerId(Long customerId, int limit) {
        try {
            // Find customer by ID
            Customer customer = customerRepo.findById(customerId).orElse(null);
            if (customer == null) {
                log.warn("Customer not found: {}", customerId);
                return new java.util.ArrayList<>();
            }
            
            // Get communications by customer's email, ordered by most recent first
            List<Email> communications = emailRepo.findByRecipientOrderByIdDesc(customer.getEmail());
            
            // Limit results to specified number
            if (communications.size() > limit) {
                return communications.subList(0, limit);
            }
            
            return communications;
        } catch (Exception e) {
            log.error("Error fetching communications for customer {}: {}", customerId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public List<SmsResponse> sendBulkSMS(bulkSmsModel customBulkSms) throws ApiException, IOException {
        singleSmsModel singleSms=new singleSmsModel();

        List<SmsResponse> responseList = new List<SmsResponse>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @NotNull
            @Override
            public Iterator<SmsResponse> iterator() {
                return null;
            }

            @NotNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NotNull
            @Override
            public <T> T[] toArray(@NotNull T[] a) {
                return null;
            }

            @Override
            public boolean add(SmsResponse smsResponse) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NotNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(@NotNull Collection<? extends SmsResponse> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, @NotNull Collection<? extends SmsResponse> c) {
                return false;
            }

            @Override
            public boolean removeAll(@NotNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(@NotNull Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public boolean equals(Object o) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public SmsResponse get(int index) {
                return null;
            }

            @Override
            public SmsResponse set(int index, SmsResponse element) {
                return null;
            }

            @Override
            public void add(int index, SmsResponse element) {

            }

            @Override
            public SmsResponse remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @NotNull
            @Override
            public ListIterator<SmsResponse> listIterator() {
                return null;
            }

            @NotNull
            @Override
            public ListIterator<SmsResponse> listIterator(int index) {
                return null;
            }

            @NotNull
            @Override
            public List<SmsResponse> subList(int fromIndex, int toIndex) {
                return null;
            }
        };
        for (String contact:customBulkSms.getContactList()
             ) {
            Email draftSms= new Email();
              singleSms.setMessage(customBulkSms.getMessage());
              singleSms.setContact(contact);
            draftSms.setDate(LocalDate.now());
            draftSms.setMessage(singleSms.getMessage());
            draftSms.setRecipient(singleSms.getContact());
            SmsResponse APIResponse = null;
            try {
                 APIResponse= sms.send(singleSms);

            }catch (Exception e){
                log.info("Error encountered .. {}",e.getMessage());
            }
            draftSms.setStatus("NEW");
          draftSms.setMessageType("SMS");
            emailRepo.save(draftSms);
            if (APIResponse!=null){
                draftSms.setStatus(APIResponse.getMessages().get(0).getStatus().getGroupName());
                emailRepo.save(draftSms);
                log.info("Saving sms");
                responseList.add(APIResponse);
                /*String smsIdR=APIResponse.getMessages().get(0).getMessageId();
                String bulkIdR=APIResponse.getMessages().get(0).getMessageId();
                if (1==1) {
                    sms.getStatus(bulkIdR, smsIdR, 10);
                    draftSms.setStatus(deliveryReports.getResults().get(0).getStatus().getGroupName());
                    emailRepo.save(draftSms);
                }

                */
            }else {
                log.warn("Api Response is null");
            }


        }
       log.info("return data {}");
        return responseList;
    }

    public ContactList createContactList(ContactList contactList){
        return contactListRepo.save(contactList);
    }
    public ContactBook  createContactBook(ContactBook contactBook){
        return contactBookRepo.save(contactBook);
    }
    public void uploadContact(ContactListUpload list){
         ContactBook book=new ContactBook();
         book.setCreatedBy(list.getCreatedBy());
         book.setCreatedAt(LocalDate.now());
         book.setName(list.getContactbookName());
         ContactBook createdBook=this.createContactBook(book);
        for (ContactList singleContact:list.getContacts()
             ) {
                singleContact.setContactBook(createdBook);
                singleContact.setCreatedAt(LocalDate.now());
                this.createContactList(singleContact);
        }
    }
    public Iterable<ContactBook> getContactBook(){
        return contactBookRepo.findAll();
    }
    public Iterable<ContactList> getContactList(){
        return contactListRepo.findAll();
    }

    //New communication sending functionality
    public void NewCommunicationService(){
        //schedule and send all sms/emails
        //change statuses.
    }
    //
    public UriComponents getUriComponent(String resourceURL, MultiValueMap<String, String> requestParams, String... pathUrl) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(resourceURL).pathSegment(pathUrl);
        return requestParams == null || requestParams.isEmpty()
                ? builder.build()
                : builder.queryParams(requestParams).build();
    }
//

    //post http request
    public ResponseEntity postEntity(UriComponents components, Object token, HttpHeaders userHeaders, Class<?> responseType) {
        try {
            URI urlb = components.toUri();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            if (userHeaders != null && !userHeaders.isEmpty()) {
                headers.addAll(userHeaders);
            }
            HttpEntity<Object> entity = new HttpEntity<>(token, headers);
            RestTemplate template = new RestTemplate();
            return template.exchange(urlb, HttpMethod.POST, entity, responseType);
        } catch (HttpClientErrorException ex) {
            log.error("HttpClientErrorException=[statusCode={} responseBody={}]", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            return null;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new ResponseEntity(ex,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
