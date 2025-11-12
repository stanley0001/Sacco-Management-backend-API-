package com.example.demo.erp.customerManagement.services;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.system.parsitence.models.ResponseModel;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface CustomerS {
    Customer saveCustomer(Customer customer);

    ResponseModel findAll(int page,int size);

    ResponseModel findAll(int page,int size, String status, String searchTerm);

    ClientInfo findById(Long id);
    
    Optional<Customer> findCustomerById(Long id);

    Optional<Customer> findByPhone(String phone);

    Customer update(Customer customer);

    void changeStatus(Long id, String status);

    /**
     * Enable channel-based authentication for a customer
     * @param id Customer ID
     * @param channel Channel to enable (web, mobile, ussd)
     * @param pin PIN to set (will be hashed)
     * @return ResponseModel with status
     */
    ResponseModel enableClientLogin(Long id, String channel, String pin);
    
    /**
     * Legacy method - enables mobile channel by default
     * @param id Customer ID
     * @return ResponseModel with status
     */
    ResponseModel enableClientLogin(Long id);
    
    void deleteCustomer(Long id);
}
