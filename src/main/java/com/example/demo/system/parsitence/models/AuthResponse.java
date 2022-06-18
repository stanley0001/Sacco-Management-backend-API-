package com.example.demo.system.parsitence.models;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;

public class ResponseModel {
    private int httpStatusCode;
    private HttpStatus httpStatus;
    private String reason;
    private String message;
    private UserDetails user;
    public ResponseModel() {
    }

    public ResponseModel(int httpStatusCode, HttpStatus httpStatus, String reason, String message, UserDetails user) {
        this.httpStatusCode = httpStatusCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
        this.message = message;
        this.user = user;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResponseModel{" +
                "httpStatusCode=" + httpStatusCode +
                ", httpStatus=" + httpStatus +
                ", reason='" + reason + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
