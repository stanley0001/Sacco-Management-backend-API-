package com.example.demo.system.parsitence.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ResponseModel {
    private HttpStatus status;
    private String message;
    private String errors;
    private String body;

}
