package com.example.demo.services;

import com.example.demo.loanManagement.parsistence.models.Products;
import com.example.demo.loanManagement.parsistence.repositories.ProductRepo;
import org.springframework.stereotype.Service;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class ProductService {

   public ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public Products saveProduct(Products product){
        //validations

        Optional<Products> product1=productRepo.getByCode(product.getCode());

       if (product1.isPresent()){
           log.warn("product with code "+product.getCode()+" already Exists");
           return product;
       }else {
           log.info("product with code created");
           return productRepo.save(product);
       }

    }

    public List<Products> findAllProducts() {
        log.info("Fetching all products...");
        return productRepo.findAll();
    }

    public Optional<Products> findById(Long id) {
        return productRepo.findById(id);
    }

    public Products findByProductCode(String productCode) {
        return productRepo.findByCode(productCode);
    }
}
