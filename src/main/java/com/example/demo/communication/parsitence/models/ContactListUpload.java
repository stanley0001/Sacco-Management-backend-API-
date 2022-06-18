package com.example.demo.communication.parsitence.models;

import java.util.List;

public class ContactListUpload {
    private String contactbookName;
    private String createdBy;

    private List<ContactList> contacts;

    public ContactListUpload(String contactbookName, String createdBy, List<ContactList> contacts) {
        this.contactbookName = contactbookName;
        this.createdBy = createdBy;
        this.contacts = contacts;
    }

    public String getContactbookName() {
        return contactbookName;
    }

    public void setContactbookName(String contactbookName) {
        this.contactbookName = contactbookName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<ContactList> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactList> contacts) {
        this.contacts = contacts;
    }
}
