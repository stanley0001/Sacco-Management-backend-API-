package com.example.demo.channels.mobile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePinRequest {
    
    @NotBlank(message = "Current PIN is required")
    @Size(min = 4, max = 6)
    private String currentPin;
    
    @NotBlank(message = "New PIN is required")
    @Size(min = 4, max = 6)
    @Pattern(regexp = "^[0-9]{4,6}$")
    private String newPin;
}
