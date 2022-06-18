package com.example.demo.communication.parsitence.models;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
public class ContactBook implements Serializable {
    private static final Long serialVersionUID = 1L;

    @Id
    @Column(nullable = false,unique = true,updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String createdBy;
    private String updatedBy;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    @OneToMany(mappedBy = "contactBook")
    private List<ContactList> contactLists;

    public ContactBook() {
    }

    public ContactBook(Long id) {
        this.id = id;
    }

    public ContactBook(Long id, String name, String createdBy, String updatedBy, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
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
        return "ContactBook{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
