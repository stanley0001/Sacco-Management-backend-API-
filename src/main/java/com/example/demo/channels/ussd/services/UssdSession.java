package com.example.demo.channels.ussd.services;

import com.example.demo.channels.ussd.enums.UssdMenuState;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class UssdSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sessionId;
    private String phoneNumber;
    private String memberId;
    private UssdMenuState currentState;
    private String lastInput;
    private Map<String, Object> sessionData = new HashMap<>();
    
    public void storeData(String key, Object value) {
        sessionData.put(key, value);
    }
    
    public Object getData(String key) {
        return sessionData.get(key);
    }
    
    public String getDataAsString(String key) {
        Object value = sessionData.get(key);
        return value != null ? value.toString() : null;
    }
    
    public void clearData() {
        sessionData.clear();
    }
}
