package com.example.demo.communication.parsitence.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhatsAppMessage {
    private Long id;
    private String messageFrom;
    private String messageTo;
    private String message;
    private String status;
    private String whatsAppId;
    private String messageType;
    private String time;
    private String instanceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @ManyToOne
    private Object consumer;
}
