package com.example.demo.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class messageTemplates  {
    @Id
    @Column(nullable = false,unique = true,updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String templateName;
    private String messageType;
    private String subject;
    private String body;
    private String attachment;
    private String createdBy;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public messageTemplates() {
    }

    public messageTemplates(Long id) {
        this.id = id;
    }

    public messageTemplates(String templateName, String messageType, String subject, String body, String attachment, String createdBy, LocalDate createdAt, LocalDate updatedAt) {
        this.templateName = templateName;
        this.messageType = messageType;
        this.subject = subject;
        this.body = body;
        this.attachment = attachment;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public messageTemplates(Long id, String templateName, String messageType, String subject, String body, String attachment, String createdBy, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.templateName = templateName;
        this.messageType = messageType;
        this.subject = subject;
        this.body = body;
        this.attachment = attachment;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "messageTemplates{" +
                "id=" + id +
                ", templateName='" + templateName + '\'' +
                ", messageType='" + messageType + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", attachment='" + attachment + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
