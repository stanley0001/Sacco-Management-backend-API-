package com.example.demo.mobile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPinRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^254[0-9]{9}$")
    private String phoneNumber;
    
    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 6)
    private String otp;
    
    @NotBlank(message = "New PIN is required")
    @Size(min = 4, max = 6)
    @Pattern(regexp = "^[0-9]{4,6}$")
    private String newPin;
}
