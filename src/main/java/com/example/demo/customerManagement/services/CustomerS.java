package com.example.demo.customerManagement.services;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.models.ClientInfo;
import com.example.demo.system.parsitence.models.ResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;
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

    ResponseModel enableClientLogin(Long id);
    
    void deleteCustomer(Long id);
}
