package com.example.demo.erp.communication.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sms_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private String category; // LOAN, PAYMENT, REMINDER, NOTIFICATION, GENERAL

    @ElementCollection
    @CollectionTable(name = "sms_template_variables", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "variable_name")
    private List<String> variables = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "description")
    private String description;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Replace variables in message with actual values
     */
    public String populateMessage(java.util.Map<String, String> values) {
        String result = this.message;
        for (java.util.Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /**
     * Extract variables from message text
     */
    public static List<String> extractVariables(String message) {
        List<String> variables = new ArrayList<>();
        int start = 0;
        while ((start = message.indexOf("{", start)) != -1) {
            int end = message.indexOf("}", start);
            if (end != -1) {
                variables.add(message.substring(start + 1, end));
                start = end + 1;
            } else {
                break;
            }
        }
        return variables;
    }
}
