package com.example.demo.model.models;

import java.util.List;

public class bulkSmsModel {
    private String message;
    private List<String> contactList;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getContactList() {
        return contactList;
    }

    public void setContactList(List<String> contactList) {
        this.contactList = contactList;
    }
}
