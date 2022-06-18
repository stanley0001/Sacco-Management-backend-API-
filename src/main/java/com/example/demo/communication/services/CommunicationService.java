package com.example.demo.services;


import com.example.demo.communication.parsitence.models.ContactBook;
import com.example.demo.communication.parsitence.models.ContactList;
import com.example.demo.communication.parsitence.models.Email;
import com.example.demo.communication.parsitence.models.messageTemplates;
import com.example.demo.communication.parsitence.models.ContactListUpload;
import com.example.demo.communication.parsitence.models.bulkSmsModel;
import com.example.demo.communication.parsitence.models.singleSmsModel;
import com.example.demo.communication.parsitence.repositories.ContactBookRepo;
import com.example.demo.communication.parsitence.repositories.ContactListRepo;
import com.example.demo.communication.parsitence.repositories.TemplateRepo;
import com.example.demo.services.communication.InfoBidApiService;
import com.infobip.ApiException;
import com.infobip.model.SmsResponse;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@Log4j2
public class CommunicationService {
    public final com.example.demo.communication.parsitence.repositories.emailRepo emailRepo;
    public final TemplateRepo templateRepo;
    public final InfoBidApiService sms;
    public final ContactBookRepo contactBookRepo;
    public final ContactListRepo contactListRepo;

    public CommunicationService(com.example.demo.communication.parsitence.repositories.emailRepo emailRepo, TemplateRepo templateRepo, InfoBidApiService sms, ContactBookRepo contactBookRepo, ContactListRepo contactListRepo) {
        this.emailRepo = emailRepo;
        this.templateRepo = templateRepo;
        this.sms = sms;
        this.contactBookRepo = contactBookRepo;
        this.contactListRepo = contactListRepo;
    }
    public void sendEmail(String[] data){
        String variable[] = new String[]{
                data[0],data[1],data[2]
        };

        theEmail(variable);
    }
    public void getData(String[] data){
     String variable[] = new String[]{
             data[0],"Creation Email","Hi "+data[1]+"\r\n" +
             " Welcome to Alpha \r\nPlease use Username:<b>"+data[2]+"</b> and Password:<b>"+data[3]+"</b> to access the system"
     };

     theEmail(variable);
 }

    //send email
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
    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("mungaistanley001@gmail.com", "rploampijtiyfbhz");
        }
    });
    //email test
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

    public void sendCustomEmail(Email mail) {
        String variable[] = new String[]{
                mail.getRecipient(),mail.getMessageType(),mail.getMessage()
        };

        theEmail(variable);

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
                String smsIdR=APIResponse.getMessages().get(0).getMessageId();
                String bulkIdR=APIResponse.getBulkId();
                if (1==1) {
                    sms.getStatus(bulkIdR, smsIdR, 10);
                    //draftSms.setStatus(deliveryReports.getResults().get(0).getStatus().getGroupName());
                    //emailRepo.save(draftSms);
                }
            }else {
                log.warn("Api Response is null");
            }


        }
       log.info("return data {}");
        return responseList;
    }

    public ContactList  createContactList(ContactList contactList){
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
}
