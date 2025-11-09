package com.example.demo.finance.loanManagement.parsistence.models;

import com.example.demo.finance.loanManagement.parsistence.entities.Charges;
import com.example.demo.finance.loanManagement.parsistence.entities.Products;
import com.example.demo.finance.loanManagement.parsistence.entities.Charges;
import com.example.demo.finance.loanManagement.parsistence.entities.Products;

import java.util.List;

public class productCreation {
    private Products product;
    private List<Charges> charges;

    public Products getProduct() {
        return product;
    }

    public void setProduct(Products product) {
        this.product = product;
    }

    public List<Charges> getCharges() {
        return charges;
    }

    public void setCharges(List<Charges> charges) {
        this.charges = charges;
    }
}
