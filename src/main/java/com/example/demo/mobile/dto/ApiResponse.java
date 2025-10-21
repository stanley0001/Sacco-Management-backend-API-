package com.example.demo.mobile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
    
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
