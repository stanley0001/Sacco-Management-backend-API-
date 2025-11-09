package com.example.demo.finance.accounting.repositories;

import com.example.demo.finance.accounting.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmployeeCode(String employeeCode);
    
    Optional<Employee> findByNationalId(String nationalId);
    
    Optional<Employee> findByPhoneNumber(String phoneNumber);
    
    Optional<Employee> findByEmail(String email);
    
    List<Employee> findByStatusOrderByEmployeeCodeAsc(Employee.EmployeeStatus status);
    
    List<Employee> findByDepartmentOrderByEmployeeCodeAsc(String department);
    
    boolean existsByEmployeeCode(String employeeCode);
    
    boolean existsByNationalId(String nationalId);
}
