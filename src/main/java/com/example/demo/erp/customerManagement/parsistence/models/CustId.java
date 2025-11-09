package com.example.demo.erp.customerManagement.parsistence.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustId {
    private Long cus_id;
    private String initiator;

    public CustId(Long cus_id, String initiator) {
        this.cus_id = cus_id;
        this.initiator = initiator;
    }

}
