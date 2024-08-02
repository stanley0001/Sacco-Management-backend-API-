package com.example.demo.communication.serviceImplementation;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.banking.parsitence.repositories.BankAccountRepo;
import com.example.demo.communication.parsitence.enitities.WhatsAppSession;
import com.example.demo.communication.parsitence.models.Email;
import com.example.demo.communication.parsitence.models.WhatsAppMessage;
import com.example.demo.communication.parsitence.repositories.SessionRepository;
import com.example.demo.communication.parsitence.repositories.emailRepo;
import com.example.demo.communication.services.CommunicationService;
import com.example.demo.communication.services.WhatsAppService;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.repositories.CustomerRepo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WhatsAppImpl implements WhatsAppService {
    private final CustomerRepo customerRepo;
    private final CommunicationService communicationService;
    private final SessionRepository sessionRepository;
    private final BankAccountRepo accountRepo;
    private final emailRepo emailRepo;

    public WhatsAppImpl(CustomerRepo customerRepo, CommunicationService communicationService, SessionRepository sessionRepository, BankAccountRepo accountRepo, com.example.demo.communication.parsitence.repositories.emailRepo emailRepo) {
        this.customerRepo = customerRepo;
        this.communicationService = communicationService;
        this.sessionRepository = sessionRepository;
        this.accountRepo = accountRepo;
        this.emailRepo = emailRepo;
    }

    public String processWhatsAppRequest(WhatsAppMessage message){
        String phone=message.getMessageFrom();
        String command=message.getMessage();
        String response=null;
       Optional<Customer> customer=customerRepo.findByphoneNumber(phone);
        if (customer.isPresent()) {
            WhatsAppSession session=this.getSession(phone);
            response= this.showMenu(session,command,customer.get());
        }else {
            response= "Thank you for contacting us, kindly visit the nearest branch for registration";
        }
        this.sendComm(phone,response);
        return response;
    }
     private WhatsAppSession getSession(String phone){
         Optional<WhatsAppSession> session=sessionRepository.findByPhone(phone);
         if (session.isPresent()){
             if (session.get().getExpiresAt().isBefore(LocalDateTime.now())){
                 return  session.get();
             }
         }
         return this.createSession(phone);
     }
     private WhatsAppSession createSession(String phone){
         WhatsAppSession session=new WhatsAppSession();
         session.setSessionLog("00");
         session.setCreatedAt(LocalDateTime.now());
         session.setExpiresAt(LocalDateTime.now().plusMinutes(20));
         session.setPhone(phone);
         return sessionRepository.save(session);
     }
     public String showMenu(WhatsAppSession session,String input,Customer customer){
        session.setPreviousMenu(session.getSessionLog());
        String command=session+"*"+input;
          switch (command){
              case "00*1":
                  session.setSessionLog(command);
                  sessionRepository.save(session);
                  return this.showOptions("BALANCE",customer);
              case "00*1*1":
                  session.setSessionLog(command);
                  sessionRepository.save(session);
                  return this.showOptions("LOAN_BALANCE",customer);
                  case "00*1*2":
                  session.setSessionLog(command);
                  sessionRepository.save(session);
                  return this.showOptions("SAVINGS_BALANCE",customer);
              case "00*2":
                  session.setSessionLog(command);
                  sessionRepository.save(session);
                  return this.showOptions("LOAN",customer);
                case "00*2*1":
                  session.setSessionLog(command);
                  sessionRepository.save(session);
                  return this.showOptions("LIMIT",customer);
                case "00*2*2":
                  session.setSessionLog(command);
                  sessionRepository.save(session);
                  return this.showOptions("LOAN_APPLICATION",customer);
                case "00*2*3":
                  session.setSessionLog(command);
                  sessionRepository.save(session);
                  return this.showOptions("LOAN_REPAYMENT",customer);

          }
        return null;
     }
    public String showOptions(String command,Customer customer){
        switch (command.toUpperCase()){
            case "BALANCE":
                return "1. loan balance \n 2.savings balance";
            case "LOAN":
                return "1.loan limit \n 2.loan application \n 3.loan repayment";
            case "LOAN_APPLICATION":
                return "please enter the loan amount";
            case "LOAN_REPAYMENT":
                return "1.full repayment \n 2. partial repayment";
            case "LOAN_BALANCE":
                return "your loan balance is";
            case "SAVINGS_BALANCE":
                BankAccounts account=accountRepo.findByCustomer(customer).get().get(3);
                return "your savings balance is Ksh "+account.getAccountBalance();
            case "ENQUIRY":
                return "How may I help you";
            case "LIMIT":
                return "your loan limit is";
            default:
                return "Please select a valid menu";

        }
    }
    @Async
    void sendComm(String phone,String comm){
        Email message=new Email();
        message.setMessage(comm);
        message.setRecipient(phone);
        message.setMessageType("WHATSAPP");
        message.setStatus("NEW");
        message.setDate(LocalDate.now());
        //save
        emailRepo.save(message);
        this.communicationService.sendCustomEmail(message);
    }
}
