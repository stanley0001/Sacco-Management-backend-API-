package com.example.demo.model.models;

public class CustId {
    private Long cus_id;
    private String initiator;

    public CustId(Long cus_id, String initiator) {
        this.cus_id = cus_id;
        this.initiator = initiator;
    }

    public Long getCus_id() {
        return cus_id;
    }

    public void setCus_id(Long cus_id) {
        this.cus_id = cus_id;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }
}
