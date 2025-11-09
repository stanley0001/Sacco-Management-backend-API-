package com.example.demo.system.events.appEvents;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.finance.loanManagement.parsistence.models.LoanBookUpload;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CreateAccountEvent extends ApplicationEvent {
    private final LoanBookUpload upload;
    private final Customer customer;

    public CreateAccountEvent(Object source, LoanBookUpload upload, Customer customer) {
        super(source);
        this.upload = upload;
        this.customer = customer;
    }
}
