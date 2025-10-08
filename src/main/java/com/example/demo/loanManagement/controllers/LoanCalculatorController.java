package com.example.demo.loanManagement.controllers;

import com.example.demo.loanManagement.parsistence.entities.InterestStrategy;
import com.example.demo.loanManagement.parsistence.entities.Products;
import com.example.demo.loanManagement.services.LoanCalculatorService;
import com.example.demo.loanManagement.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/loan-calculator")
@RequiredArgsConstructor
@Tag(name = "Loan Calculator", description = "Calculate loan repayments with different interest strategies")
public class LoanCalculatorController {

    private final LoanCalculatorService calculatorService;
    private final ProductService productService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate loan details based on principal, product, and interest strategy")
    public ResponseEntity<LoanCalculatorService.LoanCalculation> calculateLoan(
            @Parameter(description = "Loan principal amount") @RequestParam double principal,
            @Parameter(description = "Product ID") @RequestParam Long productId,
            @Parameter(description = "Interest calculation strategy") @RequestParam(defaultValue = "FLAT_RATE") InterestStrategy strategy
    ) {
        Optional<Products> productOpt = productService.findById(productId);
        
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Products product = productOpt.get();
        
        // Validate loan amount against product limits
        if (principal < product.getMinLimit() || principal > product.getMaxLimit()) {
            return ResponseEntity.badRequest().build();
        }
        
        LoanCalculatorService.LoanCalculation calculation = calculatorService.calculateLoan(principal, product, strategy);
        return ResponseEntity.ok(calculation);
    }

    @PostMapping("/calculate-custom")
    @Operation(summary = "Calculate loan with custom parameters")
    public ResponseEntity<LoanCalculatorService.LoanCalculation> calculateCustomLoan(
            @Parameter(description = "Loan principal amount") @RequestParam double principal,
            @Parameter(description = "Interest rate (percentage)") @RequestParam double interestRate,
            @Parameter(description = "Loan term (number of periods)") @RequestParam int term,
            @Parameter(description = "Interest calculation strategy") @RequestParam(defaultValue = "FLAT_RATE") InterestStrategy strategy
    ) {
        // Create a temporary product for calculation
        Products tempProduct = new Products();
        tempProduct.setInterest((int) interestRate);
        tempProduct.setTerm(term);
        
        LoanCalculatorService.LoanCalculation calculation = calculatorService.calculateLoan(principal, tempProduct, strategy);
        return ResponseEntity.ok(calculation);
    }

    @GetMapping("/strategies")
    @Operation(summary = "Get all available interest calculation strategies")
    public ResponseEntity<InterestStrategy[]> getStrategies() {
        return ResponseEntity.ok(InterestStrategy.values());
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare loan calculations across all interest strategies")
    public ResponseEntity<?> compareLoanStrategies(
            @Parameter(description = "Loan principal amount") @RequestParam double principal,
            @Parameter(description = "Product ID") @RequestParam Long productId
    ) {
        Optional<Products> productOpt = productService.findById(productId);
        
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Products product = productOpt.get();
        
        // Calculate for all strategies
        var results = new java.util.HashMap<String, LoanCalculatorService.LoanCalculation>();
        for (InterestStrategy strategy : InterestStrategy.values()) {
            LoanCalculatorService.LoanCalculation calc = calculatorService.calculateLoan(principal, product, strategy);
            results.put(strategy.name(), calc);
        }
        
        return ResponseEntity.ok(results);
    }
}
