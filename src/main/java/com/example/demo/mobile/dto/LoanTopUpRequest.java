package com.example.demo.mobile.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanTopUpRequest {
    
    @NotNull(message = "Top-up amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal topUpAmount;
    
    @NotBlank(message = "Purpose is required")
    @Size(min = 10, max = 500)
    private String purpose;
    
    @NotBlank(message = "PIN is required")
    @Size(min = 4, max = 6)
    @Pattern(regexp = "^[0-9]{4,6}$")
    private String pin;
}
