package com.example.demo.ussd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UssdResponse {
    private String message;
    private boolean isEnd;
    
    public static UssdResponse cont(String message) {
        UssdResponse response = new UssdResponse();
        response.setMessage("CON " + message);
        response.setEnd(false);
        return response;
    }
    
    public static UssdResponse end(String message) {
        UssdResponse response = new UssdResponse();
        response.setMessage("END " + message);
        response.setEnd(true);
        return response;
    }
}
