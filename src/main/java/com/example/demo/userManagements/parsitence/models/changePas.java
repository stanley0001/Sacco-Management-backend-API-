package com.example.demo.userManagements.parsitence.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class changePas {
    private Integer userId;
    private String newPassword;

    public changePas() {
    }

    public changePas(Integer userId, String newPassword) {
        this.userId = userId;
        this.newPassword = newPassword;
    }


}
