package com.example.demo.channels.mobile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
    private String lastName;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^254[0-9]{9}$", message = "Phone number must be in format 254XXXXXXXXX")
    private String phoneNumber;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "ID number is required")
    @Size(min = 7, max = 20, message = "ID number must be 7-20 characters")
    private String idNumber;
    
    @NotBlank(message = "PIN is required")
    @Size(min = 4, max = 6, message = "PIN must be 4-6 digits")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "PIN must contain only digits")
    private String pin;
    
    private String dateOfBirth;
    private String gender;
}
