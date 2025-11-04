package com.example.demo.loanManagement.services;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.loanManagement.parsistence.entities.Products;
import com.example.demo.loanManagement.parsistence.entities.Subscriptions;
import com.example.demo.customerManagement.parsistence.repositories.CustomerRepo;
import com.example.demo.loanManagement.parsistence.repositories.SubscriptionRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class SubscriptionService {

    public final ProductService products;
    public final CustomerRepo customers;
    public final SubscriptionRepo subscriptionsRepo;

    public SubscriptionService(ProductService products, CustomerRepo customers, SubscriptionRepo subscriptionsRepo) {
        this.products = products;
        this.customers = customers;
        this.subscriptionsRepo = subscriptionsRepo;
    }


    public void subscribe(String customerPhone, Long productId,Integer subscriptionAmount) {
        Optional<Customer> customer=customers.findByphoneNumber(customerPhone);
                //findByPhone(customerPhone);
        if (customer.isPresent()){
            log.info("Found customer: "+customer);
        Optional<Products> product=products.findById(productId);
        if (product.isPresent()){
            //find subscription
            Optional<Subscriptions> subscription1=findCustomerIdandproductCode(customer.get().getId().toString(),product.get().getCode());
            if (subscription1.isPresent()){
                  log.warn("Similar Subscription Exists for customer: "+customer.get().getFirstName());
            }else {
                Subscriptions subscription = new Subscriptions();
                subscription.setCreatedAt(LocalDate.now());
                subscription.setStatus(Boolean.TRUE);
                subscription.setCreditStatusDate(LocalDate.now());
                subscription.setCustomerId(customer.get().getId().toString());
                
                // Ensure credit limit is never null - use provided amount or calculate based on rules
                if (subscriptionAmount != null && subscriptionAmount > 0) {
                    subscription.setCreditLimit(subscriptionAmount);
                    subscription.setCreditLimitOverridden(true);
                } else {
                    // Calculate credit limit based on product and customer data
                    Integer calculatedLimit = this.calculateCreditLimit(
                        customer.get().getId().toString(), 
                        product.get().getCode(), 
                        "PRODUCT_ELIGIBILITY" // Default calculation rule
                    );
                    subscription.setCreditLimit(calculatedLimit);
                    subscription.setCreditLimitOverridden(false);
                    subscription.setCreditLimitCalculationRule("PRODUCT_ELIGIBILITY");
                }
                subscription.setInterestRate(product.get().getInterest());
                subscription.setProductCode(product.get().getCode());
                subscription.setTerm(product.get().getTerm() != null ? product.get().getTerm() : 0);
                subscription.setTimeSpan(product.get().getTimeSpan());
                subscription.setCustomerPhoneNumber(customerPhone);
                log.info("subscribing " + customer.get().getFirstName() + "to " + product.get().getName());
                subscriptionsRepo.save(subscription);
                log.info("Subscription created");
            }
        }else {
            log.warn("No product found with phone "+productId);
        }

        }else {
            log.warn("No customer found with phone "+customerPhone);
        }
    }

    public Optional<List<Subscriptions>> findCustomerId(String id) {
        Customer customer=customers.findById(Long.parseLong(id)).get();
        log.info("fetch subscriptions for "+customer.getFirstName());

        return subscriptionsRepo.findBycustomerId(id);

    }

   public Optional<Subscriptions> findCustomerIdandproductCode(String cusId, String productCode) {
        log.info("fetch subscription by "+cusId,productCode);
        return subscriptionsRepo.findByCustomerIdAndProductCode(cusId,productCode);
    }

    /**
     * Update credit limit for a subscription
     * @param subscriptionId Subscription ID
     * @param newLimit New credit limit
     * @param override Mark as manually overridden
     * @param calculationRule Rule used for calculation
     * @return Updated subscription
     */
    public Subscriptions updateCreditLimit(Long subscriptionId, Integer newLimit, Boolean override, String calculationRule) {
        log.info("Updating credit limit for subscription {} to {}", subscriptionId, newLimit);
        
        Subscriptions subscription = subscriptionsRepo.findById(subscriptionId)
            .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));
        
        subscription.setCreditLimit(newLimit != null ? newLimit : 0);
        subscription.setCreditLimitOverridden(override != null ? override : false);
        subscription.setCreditLimitCalculationRule(calculationRule);
        subscription.setUpdatedAt(LocalDate.now());
        
        return subscriptionsRepo.save(subscription);
    }

    /**
     * Calculate credit limit based on rules and customer data
     * @param customerId Customer ID
     * @param productCode Product code
     * @param rule Calculation rule
     * @return Calculated credit limit
     */
    public Integer calculateCreditLimit(String customerId, String productCode, String rule) {
        log.info("Calculating credit limit for customer {} with product {} using rule: {}", customerId, productCode, rule);
        
        try {
            // Get customer data
            Optional<Customer> customerOpt = customers.findById(Long.parseLong(customerId));
            if (customerOpt.isEmpty()) {
                log.warn("Customer not found: {}", customerId);
                return 10000; // Default fallback
            }
            Customer customer = customerOpt.get();
            
            // Get product data
            Optional<Products> productOpt = products.findByCode(productCode);
            if (productOpt.isEmpty()) {
                log.warn("Product not found: {}", productCode);
                return 10000; // Default fallback
            }
            Products product = productOpt.get();
            
            Integer calculatedLimit = 0;
            
            // Apply calculation rules
            if ("3X_SAVINGS".equals(rule)) {
                // 3 times customer savings balance
                Float accountBalance = customer.getAccountBalance();
                if (accountBalance != null && accountBalance > 0) {
                    calculatedLimit = (int) (accountBalance * 3);
                    log.info("3X_SAVINGS: Account balance {} * 3 = {}", accountBalance, calculatedLimit);
                } else {
                    calculatedLimit = 0;
                    log.warn("No account balance found for customer {}", customerId);
                }
            } else if ("5X_SAVINGS".equals(rule)) {
                // 5 times customer savings balance
                Float accountBalance = customer.getAccountBalance();
                if (accountBalance != null && accountBalance > 0) {
                    calculatedLimit = (int) (accountBalance * 5);
                    log.info("5X_SAVINGS: Account balance {} * 5 = {}", accountBalance, calculatedLimit);
                } else {
                    calculatedLimit = 0;
                    log.warn("No account balance found for customer {}", customerId);
                }
            } else if ("FIXED_50K".equals(rule)) {
                // Fixed 50,000
                calculatedLimit = 50000;
                log.info("FIXED_50K: Fixed limit = {}", calculatedLimit);
            } else if ("FIXED_100K".equals(rule)) {
                // Fixed 100,000
                calculatedLimit = 100000;
                log.info("FIXED_100K: Fixed limit = {}", calculatedLimit);
            } else if ("BASED_ON_HISTORY".equals(rule)) {
                // Calculate based on loan repayment history
                calculatedLimit = calculateBasedOnHistory(customerId);
                log.info("BASED_ON_HISTORY: Calculated limit = {}", calculatedLimit);
            } else if ("PRODUCT_ELIGIBILITY".equals(rule)) {
                // Use product's eligibility multiplier
                Float accountBalance = customer.getAccountBalance();
                Double multiplier = product.getEligibilityMultiplier() != null ? product.getEligibilityMultiplier() : 3.0;
                if (accountBalance != null && accountBalance > 0) {
                    calculatedLimit = (int) (accountBalance * multiplier);
                    log.info("PRODUCT_ELIGIBILITY: Account balance {} * {} = {}", accountBalance, multiplier, calculatedLimit);
                } else {
                    calculatedLimit = 0;
                }
            } else {
                // Default rule - use eligibility multiplier from product
                Float accountBalance = customer.getAccountBalance();
                Double multiplier = product.getEligibilityMultiplier() != null ? product.getEligibilityMultiplier() : 3.0;
                if (accountBalance != null && accountBalance > 0) {
                    calculatedLimit = (int) (accountBalance * multiplier);
                    log.info("DEFAULT: Account balance {} * {} = {}", accountBalance, multiplier, calculatedLimit);
                } else {
                    calculatedLimit = 0;
                }
            }
            
            // Apply product limits (min/max)
            if (calculatedLimit > 0) {
                if (product.getMaxLimit() != null && calculatedLimit > product.getMaxLimit()) {
                    calculatedLimit = product.getMaxLimit();
                    log.info("Applied product max limit: {}", calculatedLimit);
                }
                
                // Ensure minimum limit is respected
                Integer minLimit = product.getMinLimit() != null ? product.getMinLimit() : 1000;
                if (calculatedLimit < minLimit) {
                    calculatedLimit = minLimit;
                    log.info("Applied product min limit: {}", calculatedLimit);
                }
            } else {
                // Fallback to product minimum limit
                Integer minLimit = product.getMinLimit() != null ? product.getMinLimit() : 1000;
                calculatedLimit = minLimit;
                log.warn("No calculated limit, using product minimum: {}", calculatedLimit);
            }
            
            log.info("Final calculated credit limit for customer {}: {}", customerId, calculatedLimit);
            return calculatedLimit;
            
        } catch (Exception e) {
            log.error("Error calculating credit limit for customer {}: {}", customerId, e.getMessage());
            return 10000; // Safe fallback
        }
    }
    
    /**
     * Calculate credit limit based on loan repayment history
     * @param customerId Customer ID
     * @return Calculated limit based on history
     */
    private Integer calculateBasedOnHistory(String customerId) {
        try {
            // Get customer's loan accounts
            List<Subscriptions> subscriptions = subscriptionsRepo.findByCustomerId(customerId)
                .orElse(new ArrayList<>());
            
            if (subscriptions.isEmpty()) {
                log.info("No subscriptions found for customer {}, using default", customerId);
                return 30000; // Default for new customers
            }
            
            // Calculate average credit limit from existing subscriptions
            int totalLimits = 0;
            int activeSubscriptions = 0;
            
            for (Subscriptions sub : subscriptions) {
                if (sub.getStatus() != null && sub.getStatus()) { // Active subscription
                    totalLimits += sub.getCreditLimit() != null ? sub.getCreditLimit() : 0;
                    activeSubscriptions++;
                }
            }
            
            if (activeSubscriptions > 0) {
                int averageLimit = totalLimits / activeSubscriptions;
                // Increase by 25% for good history
                int increasedLimit = (int) (averageLimit * 1.25);
                log.info("Based on history: average limit {} increased to {}", averageLimit, increasedLimit);
                return increasedLimit;
            } else {
                return 30000; // Default for customers with subscriptions but no active ones
            }
            
        } catch (Exception e) {
            log.error("Error calculating limit based on history: {}", e.getMessage());
            return 30000; // Safe fallback
        }
    }
}
