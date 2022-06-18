package com.example.demo.loanManagement.parsistence.models;

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
