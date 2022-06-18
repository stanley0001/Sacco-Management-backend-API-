package com.example.demo.loanManagement.parsistence.models;

public class newApplication {
    String amount;
    String phone;
   String productCode;

    public


    newApplication(String amount, String phone, String productCode) {
        this.amount = amount;
        this.phone = phone;
        this.productCode = productCode;
    }

    public newApplication() {
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }
}
