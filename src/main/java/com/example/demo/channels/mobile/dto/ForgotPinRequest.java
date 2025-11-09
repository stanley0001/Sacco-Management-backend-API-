package com.example.demo.channels.mobile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ForgotPinRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^254[0-9]{9}$")
    private String phoneNumber;
}
