package com.example.demo.banking.parsitence.enitities;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payments  {
    @Id
    @Column(updatable = false,unique = true,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    @Column(nullable = false)
    private String accountNumber;
    @Column(nullable = false)
    private String status;
    @Column(unique = true)
    private String OtherRef;
    private String OtherResponse;
    private String amount;
    private String destinationAccount;
    private LocalDateTime paymentTime;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public Payments() {
    }

    public Payments(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Payments(String accountNumber, String status, String otherRef, String amount,String destinationAccount, LocalDateTime paymentTime) {
        this.accountNumber = accountNumber;
        this.status = status;
        OtherRef = otherRef;
        this.amount = amount;
        this.destinationAccount = destinationAccount;
        this.paymentTime = paymentTime;
    }

    public Payments(Long paymentId, String accountNumber, String status, String otherRef, String amount, String destinationAccount, LocalDateTime paymentTime) {
        this.paymentId = paymentId;
        this.accountNumber = accountNumber;
        this.status = status;
        OtherRef = otherRef;
        this.amount = amount;
        this.destinationAccount = destinationAccount;
        this.paymentTime = paymentTime;
    }
    @Override
    public String toString() {
        return "Payments{" +
                "paymentId=" + paymentId +
                ", accountNumber='" + accountNumber + '\'' +
                ", status='" + status + '\'' +
                ", OtherRef='" + OtherRef + '\'' +
                ", amount='" + amount + '\'' +
                ", destinationAccount='" + destinationAccount + '\'' +
                ", paymentTime=" + paymentTime +
                '}';
    }
}
