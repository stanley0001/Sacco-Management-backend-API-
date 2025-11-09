package com.example.demo.channels.mobile.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    
    @NotBlank(message = "Source account is required")
    private String fromAccountId;
    
    @NotBlank(message = "Destination account is required")
    private String toAccountId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "10.0", message = "Minimum transfer is KES 10")
    private BigDecimal amount;
    
    private String narration;
    
    @NotBlank(message = "PIN is required")
    @Size(min = 4, max = 6)
    @Pattern(regexp = "^[0-9]{4,6}$")
    private String pin;
}
