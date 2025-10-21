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

    @GetMapping("/calculate")
    @Operation(summary = "Calculate loan details based on principal, product, and interest strategy")
    public ResponseEntity<?> calculateLoan(
            @Parameter(description = "Loan principal amount") @RequestParam double principal,
            @Parameter(description = "Product ID") @RequestParam Long productId,
            @Parameter(description = "Interest calculation strategy") @RequestParam(defaultValue = "REDUCING_BALANCE") String strategyName
    ) {
        try {
            // Log incoming request
            System.out.println("Calculate request - Principal: " + principal + ", ProductId: " + productId + ", Strategy: " + strategyName);
            
            InterestStrategy strategy = InterestStrategy.valueOf(strategyName.toUpperCase());
            Optional<Products> productOpt = productService.findById(productId);

            if (productOpt.isEmpty()) {
                System.out.println("Product not found with ID: " + productId);
                return ResponseEntity.status(404).body("Product not found with ID: " + productId);
            }

            Products product = productOpt.get();
            System.out.println("Product found: " + product.getName() + ", MinLimit: " + product.getMinLimit() + ", MaxLimit: " + product.getMaxLimit());

            // Validate loan amount against product limits
            if (principal < product.getMinLimit() || principal > product.getMaxLimit()) {
                String errorMsg = String.format("Loan amount %.2f is outside product limits (%.2f - %.2f)", 
                    principal, product.getMinLimit(), product.getMaxLimit());
                System.out.println(errorMsg);
                return ResponseEntity.status(400).body(errorMsg);
            }

            LoanCalculatorService.LoanCalculation calculation = calculatorService.calculateLoan(principal, product, strategy);
            System.out.println("Calculation successful");
            return ResponseEntity.ok(calculation);
        } catch (IllegalArgumentException e) {
            String errorMsg = "Invalid strategy: " + strategyName + ". Valid strategies: FLAT_RATE, REDUCING_BALANCE, DECLINING_BALANCE, SIMPLE_INTEREST, COMPOUND_INTEREST, ADD_ON_INTEREST";
            System.out.println(errorMsg);
            return ResponseEntity.status(400).body(errorMsg);
        } catch (Exception e) {
            String errorMsg = "Error calculating loan: " + e.getMessage();
            System.out.println(errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(500).body(errorMsg);
        }
    }

    @GetMapping("/calculate-custom")
    @Operation(summary = "Calculate loan with custom parameters")
    public ResponseEntity<LoanCalculatorService.LoanCalculation> calculateCustomLoan(
            @Parameter(description = "Loan principal amount") @RequestParam double principal,
            @Parameter(description = "Interest rate (percentage)") @RequestParam double interestRate,
            @Parameter(description = "Loan term (number of periods)") @RequestParam int term,
            @Parameter(description = "Interest calculation strategy") @RequestParam(defaultValue = "REDUCING_BALANCE") String strategyName
    ) {
        try {
            InterestStrategy strategy = InterestStrategy.valueOf(strategyName.toUpperCase());

            // Create a temporary product for calculation
            Products tempProduct = new Products();
            tempProduct.setInterest((int) interestRate);
            tempProduct.setTerm(term);

            LoanCalculatorService.LoanCalculation calculation = calculatorService.calculateLoan(principal, tempProduct, strategy);
            return ResponseEntity.ok(calculation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
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
