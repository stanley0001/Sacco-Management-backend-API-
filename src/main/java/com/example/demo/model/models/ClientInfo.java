package com.example.demo.model.models;

import com.example.demo.model.*;
import com.example.demo.persistence.entities.Users;

import java.util.List;

public class ClientInfo {
    private Customer client;
    private Users user;
    private List<Subscriptions> subscriptions;
    private List<Email> communications;
    private List<loanApplication> loanApplications;

    public List<loanApplication> getLoanApplications() {
        return loanApplications;
    }

    public void setLoanApplications(List<loanApplication> loanApplications) {
        this.loanApplications = loanApplications;
    }

    public Customer getClient() {
        return client;
    }

    public void setClient(Customer client) {
        this.client = client;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<Subscriptions> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscriptions> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<Email> getCommunications() {
        return communications;
    }

    public void setCommunications(List<Email> communications) {
        this.communications = communications;
    }
}
