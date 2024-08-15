package com.example.demo.events;

import com.example.demo.events.appEvents.CreateAccountEvent;
import com.example.demo.events.appEvents.CreateRepaymentScheduleEvent;
import com.example.demo.events.appEvents.LoanBookUploadEvent;
import com.example.demo.events.appEvents.SubscriptionEvent;
import com.example.demo.system.services.EventProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ApplicationEventListener {
    @Autowired
    EventProcessor eventProcessor;
    @EventListener
    @Async
    public void processLoanBookUpload(LoanBookUploadEvent event){
        log.info("Loan book upload event Received {}",event.getData().size());
        eventProcessor.uploadManualLoanBook(event.getData());
    }
    @EventListener
    @Async
    public void processCreateRepaymentSchedules(CreateRepaymentScheduleEvent event){
        log.info("CreateRepaymentSchedule event Received ");
        eventProcessor.createRepaymentSchedules(event.getData());
    }
    @EventListener
    @Async
    public void processSubscription(SubscriptionEvent event){
        log.info("SubscriptionEvent Received ");
        eventProcessor.subscribeCustomer(event.getCustomer(),event.getProduct());
    }
    @EventListener
    @Async
    public void processCreateAccount(CreateAccountEvent event){
        log.info("CreateAccountEvent Received ");
        eventProcessor.createLoanAccount(event.getUpload(),event.getCustomer());
    }
}
