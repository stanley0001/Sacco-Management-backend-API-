package com.example.demo.loanManagement.parsistence.repositories;

import com.example.demo.loanManagement.parsistence.entities.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepo extends JpaRepository<Subscriptions, Long> {

    Optional<List<Subscriptions>> findBycustomerId(String id);

    Optional<Subscriptions> findByCustomerIdAndProductCode(String cusId, String productCode);


    //  Optional<Subscriptions> findBycustomerIdAndproductCode(String cusId, String productCode);
}
