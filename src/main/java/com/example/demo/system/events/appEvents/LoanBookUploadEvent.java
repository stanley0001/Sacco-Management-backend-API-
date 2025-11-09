package com.example.demo.system.events.appEvents;

import com.example.demo.finance.loanManagement.parsistence.models.LoanBookUpload;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class LoanBookUploadEvent extends ApplicationEvent {
    private final List<LoanBookUpload> data;

    public LoanBookUploadEvent(Object source,List<LoanBookUpload> data) {
        super(source);
        this.data=data;
    }
}
