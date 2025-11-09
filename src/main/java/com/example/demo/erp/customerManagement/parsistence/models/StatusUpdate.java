package com.example.demo.erp.customerManagement.parsistence.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdate {
    private Boolean status;
    private Integer userId;

    public StatusUpdate(Boolean status, Integer userId) {
        this.status = status;
        this.userId = userId;
    }

    public StatusUpdate() {
    }
}
