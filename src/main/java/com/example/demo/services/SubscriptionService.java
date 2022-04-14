package com.example.demo.services;

import com.example.demo.model.Customer;
import com.example.demo.model.Products;
import com.example.demo.model.Subscriptions;
import com.example.demo.persistence.repository.CustomerRepo;
import com.example.demo.persistence.repository.SubscriptionRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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


    public void subscribe(String customerPhone, Long productId) {
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
                subscription.setCreditLimit(12000);
                subscription.setCustomerDocumentNumber(customer.get().getDocumentNumber());
                subscription.setInterestRate(product.get().getInterest());
                subscription.setProductCode(product.get().getCode());
                subscription.setTerm(product.get().getTerm());
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
}
