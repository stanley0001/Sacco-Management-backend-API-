package com.example.demo.loanManagement.parsistence.models;

public class subscriptionR {
        private String phone;
        private Long productId;

        public subscriptionR(String phone, Long productId) {
            this.phone = phone;
            this.productId = productId;
        }

    public subscriptionR() {
    }

    public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

}
