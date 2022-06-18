package com.example.demo.system.parsitence.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class AuthResponse {
    private int httpStatusCode;
    private HttpStatus httpStatus;
    private String reason;
    private String message;
    public AuthResponse() {
    }

    public AuthResponse(int httpStatusCode, HttpStatus httpStatus, String reason, String message) {
        this.httpStatusCode = httpStatusCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
        this.message = message;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "httpStatusCode=" + httpStatusCode +
                ", httpStatus=" + httpStatus +
                ", reason='" + reason + '\'' +
                ", accessToken='" + message + '\'' +
                '}';
    }
}
