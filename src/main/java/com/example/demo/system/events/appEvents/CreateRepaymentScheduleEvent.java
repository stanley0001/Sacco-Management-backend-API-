package com.example.demo.system.events.appEvents;

import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
public class CreateRepaymentScheduleEvent extends ApplicationEvent {
    private final LoanAccount data;

    public CreateRepaymentScheduleEvent(Object source,LoanAccount loanAccount) {
        super(source);
        this.data = loanAccount;
    }
}
