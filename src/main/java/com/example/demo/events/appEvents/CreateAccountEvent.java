package com.example.demo.events.appEvents;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.loanManagement.parsistence.entities.Products;
import com.example.demo.loanManagement.parsistence.models.LoanBookUpload;
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
