package com.example.demo.channels.mobile.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRepaymentRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "10.0", message = "Minimum repayment is KES 10")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(MPESA|BANK|DEDUCTION|CASH)$", message = "Invalid payment method")
    private String paymentMethod;
    
    private String reference;
    
    @NotBlank(message = "PIN is required")
    @Size(min = 4, max = 6)
    @Pattern(regexp = "^[0-9]{4,6}$")
    private String pin;
}
