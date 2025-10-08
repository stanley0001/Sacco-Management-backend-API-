package com.example.demo.communication.parsitence.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class ContactList {
    @Id
    @Column(nullable = false,unique = true,updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    @ManyToOne
    @JoinColumn(name="contact_book_id")
    private ContactBook contactBook;

    public ContactList() {
    }

    public ContactList(Long id) {
        this.id = id;
    }

    public ContactList(Long id, String name, String email, String mobile, LocalDate createdAt, LocalDate updatedAt, ContactBook contactBook) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.contactBook = contactBook;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
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

    public ContactBook getContactBook() {
        return contactBook;
    }

    public void setContactBook(ContactBook contactBook) {
        this.contactBook = contactBook;
    }

    @Override
    public String toString() {
        return "ContactList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", contactBook=" + contactBook +
                '}';
    }
}

