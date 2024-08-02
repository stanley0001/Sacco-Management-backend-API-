package com.example.demo.events;

import com.example.demo.events.appEvents.LoanBookUploadEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ApplicationEventListener {
    @EventListener
    @Async
    public void processLoanBookUpload(LoanBookUploadEvent event){
        log.info("Loan book upload event Received {}",event.getData());
    }
}
