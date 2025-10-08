package com.example.demo.loanManagement.parsistence.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Subscriptions  {
        @Id
        @Column(nullable = false,updatable = false,unique = true)
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String customerPhoneNumber;
        private String customerId;
        private String customerDocumentNumber;
        private String productCode;
        private LocalDate creditStatusDate;
        private Boolean status;
        private Integer term;
        private Integer interestRate;
        private Integer creditLimit;
        private String timeSpan;
        private LocalDate createdAt;
        private LocalDate updatedAt;

        public Subscriptions() {
        }

        public Subscriptions(Long id) {
            this.id = id;
        }

        public Subscriptions(String customerPhoneNumber, String customerId, String customerDocumentNumber, String productCode, LocalDate creditStatusDate, Boolean status, Integer term, Integer interestRate, Integer creditLimit, String timeSpan, LocalDate createdAt, LocalDate updatedAt) {
            this.customerPhoneNumber = customerPhoneNumber;
            this.customerId = customerId;
            this.customerDocumentNumber = customerDocumentNumber;
            this.productCode = productCode;
            this.creditStatusDate = creditStatusDate;
            this.status = status;
            this.term = term;
            this.interestRate = interestRate;
            this.creditLimit = creditLimit;
            this.timeSpan = timeSpan;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public Subscriptions(Long id, String customerPhoneNumber, String customerId, String customerDocumentNumber, String productCode, LocalDate creditStatusDate, Boolean status, Integer term, Integer interestRate, Integer creditLimit, String timeSpan, LocalDate createdAt, LocalDate updatedAt) {
            this.id = id;
            this.customerPhoneNumber = customerPhoneNumber;
            this.customerId = customerId;
            this.customerDocumentNumber = customerDocumentNumber;
            this.productCode = productCode;
            this.creditStatusDate = creditStatusDate;
            this.status = status;
            this.term = term;
            this.interestRate = interestRate;
            this.creditLimit = creditLimit;
            this.timeSpan = timeSpan;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCustomerPhoneNumber() {
            return customerPhoneNumber;
        }

        public void setCustomerPhoneNumber(String customerPhoneNumber) {
            this.customerPhoneNumber = customerPhoneNumber;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getCustomerDocumentNumber() {
            return customerDocumentNumber;
        }

        public void setCustomerDocumentNumber(String customerDocumentNumber) {
            this.customerDocumentNumber = customerDocumentNumber;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public LocalDate getCreditStatusDate() {
            return creditStatusDate;
        }

        public void setCreditStatusDate(LocalDate creditStatusDate) {
            this.creditStatusDate = creditStatusDate;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }

        public Integer getTerm() {
            return term;
        }

        public void setTerm(Integer term) {
            this.term = term;
        }

        public Integer getInterestRate() {
            return interestRate;
        }

        public void setInterestRate(Integer interestRate) {
            this.interestRate = interestRate;
        }

    public Integer getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Integer creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getTimeSpan() {
            return timeSpan;
        }

        public void setTimeSpan(String timeSpan) {
            this.timeSpan = timeSpan;
        }

        public LocalDate getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDate createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDate getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDate updatedAt) {
            this.updatedAt = updatedAt;
        }

    @Override
    public String toString() {
        return "Subscriptions{" +
                "id=" + id +
                ", customerPhoneNumber='" + customerPhoneNumber + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customerDocumentNumber='" + customerDocumentNumber + '\'' +
                ", productCode='" + productCode + '\'' +
                ", creditStatusDate=" + creditStatusDate +
                ", status=" + status +
                ", term=" + term +
                ", interestRate=" + interestRate +
                ", creditLimit=" + creditLimit +
                ", timeSpan='" + timeSpan + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

