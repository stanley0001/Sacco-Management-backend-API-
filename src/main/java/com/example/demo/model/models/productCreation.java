package com.example.demo.model.models;

import com.example.demo.model.Charges;
import com.example.demo.model.Products;

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
