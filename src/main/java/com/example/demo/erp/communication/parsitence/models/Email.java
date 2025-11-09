package com.example.demo.erp.communication.parsitence.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Email {
    @Id
    @Column(nullable = false,unique = true,updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String messageType;
    private String recipient;
    private String message;
    private String status;
    private LocalDate Date;

    public Email() {
    }

    public Email(Long id) {
        this.id = id;
    }

    public Email(String messageType, String recipient, String message, String status, LocalDate date) {
        this.messageType = messageType;
        this.recipient = recipient;
        this.message = message;
        this.status = status;
        Date = date;
    }

    public Email(Long id, String messageType, String recipient, String message, String status, LocalDate date) {
        this.id = id;
        this.messageType = messageType;
        this.recipient = recipient;
        this.message = message;
        this.status = status;
        Date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return Date;
    }

    public void setDate(LocalDate date) {
        Date = date;
    }

    @Override
    public String toString() {
        return "Email{" +
                "id=" + id +
                ", messageType='" + messageType + '\'' +
                ", recipient='" + recipient + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", Date=" + Date +
                '}';
    }
}
