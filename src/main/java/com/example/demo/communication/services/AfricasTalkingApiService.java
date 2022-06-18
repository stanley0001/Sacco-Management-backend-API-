package com.example.demo.communication.services;

import com.africastalking.AfricasTalking;
import com.africastalking.SmsService;
import com.africastalking.sms.Recipient;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Log4j2
public class AfricasTalkingApiService {
    // Initialize a service e.g. SMS
    SmsService sms = AfricasTalking.getService(AfricasTalking.SERVICE_SMS);
    // Initialize
    public void initialize(){
        String username = "sandbox";    // use 'sandbox' for development in the test environment
        String apiKey = "5c190787fb2fa4846f42a02e04a1561eb0ab4fcb4d42f3e52b653193533b41b6";       // use your sandbox app API key for development in the test environment
        log.info("initializing {}");
        AfricasTalking.initialize(username, apiKey);


    }
     public List<Recipient> sendSms(String message) throws IOException {
        this.initialize();
         // Use the service
         log.info("sending.. {}",message);
         sms.send(message, new String[] {"+254723721407"}, true);

         List<Recipient>  response = sms.send(message,"stan", new String[] {"+254723721407"}, true);

         return response;
     }

}
