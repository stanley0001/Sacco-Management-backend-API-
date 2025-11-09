package com.example.demo.erp.communication.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SmsStatus status = SmsStatus.PENDING;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    private Double cost;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "template_code")
    private String templateCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    public enum SmsStatus {
        PENDING,
        SENT,
        DELIVERED,
        FAILED,
        REJECTED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = SmsStatus.PENDING;
        }
    }
}
