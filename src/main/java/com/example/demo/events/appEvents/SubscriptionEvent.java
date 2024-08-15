package com.example.demo.events.appEvents;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.loanManagement.parsistence.entities.Products;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class SubscriptionEvent extends ApplicationEvent {
    private final Products product;
    private final Customer customer;

    public SubscriptionEvent(Object source,Products product, Customer customer) {
        super(source);
        this.product = product;
        this.customer = customer;
    }
}
