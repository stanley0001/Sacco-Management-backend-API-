package com.example.demo.channels.mobile.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "10.0", message = "Minimum withdrawal is KES 10")
    @DecimalMax(value = "500000.0", message = "Maximum withdrawal is KES 500,000")
    private BigDecimal amount;
    
    @NotBlank(message = "Withdrawal method is required")
    @Pattern(regexp = "^(MPESA|BANK)$", message = "Invalid withdrawal method")
    private String withdrawalMethod;
    
    private String destinationAccount;
    private String narration;
    
    @NotBlank(message = "PIN is required")
    @Size(min = 4, max = 6)
    @Pattern(regexp = "^[0-9]{4,6}$")
    private String pin;
}
