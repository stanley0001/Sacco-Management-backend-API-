package com.example.demo.finance.loanManagement.parsistence.models;

import com.example.demo.system.enums.Statuses;
import lombok.Data;

import java.time.LocalDate;
@Data
public class RepaymentSchedules {
    private float amount;
    private Double amountPaid;
    private Integer loanAccount;
    private Integer installmentNumber;
    private Double balance;
    private Statuses status;
    private LocalDate commencementDate;
    private LocalDate dueDate;

    public void setCommencementDate(LocalDate dueDate,String cal) {
        switch (cal){
            case "WEEKLY":
                this.commencementDate=dueDate.minusWeeks(1);
                break;
            case "DAILY":
                this.commencementDate=dueDate.minusDays(1);
                break;
            default:
                this.commencementDate=dueDate.minusMonths(1);
        }
    }
}
