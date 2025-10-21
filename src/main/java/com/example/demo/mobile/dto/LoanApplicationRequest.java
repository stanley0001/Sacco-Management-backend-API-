package com.example.demo.mobile.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {
    
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Loan term is required")
    @Positive(message = "Loan term must be positive")
    @Min(value = 1, message = "Minimum term is 1")
    @Max(value = 240, message = "Maximum term is 240 months")
    private Integer term;
    
    @NotBlank(message = "Purpose is required")
    @Size(min = 10, max = 500, message = "Purpose must be 10-500 characters")
    private String purpose;
    
    private String guarantorId;
    private String collateralDescription;
    private String repaymentSource;
}
