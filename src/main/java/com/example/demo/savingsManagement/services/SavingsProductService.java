package com.example.demo.savingsManagement.services;

import com.example.demo.savingsManagement.persistence.entities.SavingsProduct;
import com.example.demo.savingsManagement.persistence.repositories.SavingsProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsProductService {

    private final SavingsProductRepository productRepository;

    @Transactional
    public SavingsProduct createProduct(SavingsProduct product) {
        log.info("Creating savings product: {}", product.getName());
        
        if (productRepository.existsByCode(product.getCode())) {
            throw new RuntimeException("Product code already exists: " + product.getCode());
        }
        
        if (productRepository.existsByName(product.getName())) {
            throw new RuntimeException("Product name already exists: " + product.getName());
        }
        
        return productRepository.save(product);
    }

    @Transactional
    public SavingsProduct updateProduct(Long id, SavingsProduct product) {
        log.info("Updating savings product: {}", id);
        
        SavingsProduct existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setInterestRate(product.getInterestRate());
        existing.setInterestCalculationMethod(product.getInterestCalculationMethod());
        existing.setInterestPostingFrequency(product.getInterestPostingFrequency());
        existing.setMinimumBalance(product.getMinimumBalance());
        existing.setMinimumOpeningBalance(product.getMinimumOpeningBalance());
        existing.setMaximumBalance(product.getMaximumBalance());
        existing.setWithdrawalFee(product.getWithdrawalFee());
        existing.setMaxWithdrawalsPerMonth(product.getMaxWithdrawalsPerMonth());
        existing.setMonthlyMaintenanceFee(product.getMonthlyMaintenanceFee());
        existing.setAllowsWithdrawals(product.getAllowsWithdrawals());
        existing.setAllowsDeposits(product.getAllowsDeposits());
        existing.setAllowsOverdraft(product.getAllowsOverdraft());
        existing.setOverdraftLimit(product.getOverdraftLimit());
        
        return productRepository.save(existing);
    }

    public SavingsProduct getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    public SavingsProduct getProductByCode(String code) {
        return productRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Product not found: " + code));
    }

    public List<SavingsProduct> getAllProducts() {
        return productRepository.findAll();
    }

    public List<SavingsProduct> getActiveProducts() {
        return productRepository.findByIsActive(true);
    }

    @Transactional
    public SavingsProduct toggleProductStatus(Long id) {
        SavingsProduct product = getProductById(id);
        product.setIsActive(!product.getIsActive());
        return productRepository.save(product);
    }
}
