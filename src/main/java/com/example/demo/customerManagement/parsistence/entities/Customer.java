package com.example.demo.customerManagement.parsistence.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Serializable {

    private static final Long serialVersionUID = 1L;

    @Id
    @Column(unique = true,updatable = false,nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private Float accountBalance;
    private String documentType;
    private String documentNumber;
    private LocalDate dob;
    private String  employmentType;
    private String maritalStatus;
    private String occupation;
    private Float salary;
    @Column(unique = true)
    private String externalId;
    private LocalDate externalStartDate;
    private String address;
    private String AccountStatus;
    private Boolean status;
    private String nextOfKin;
    private String nextOfKinRelationship;
    private String nextOfKinDocumentNumber;
    private String nextOfKinPhone;
    @Column(unique = true,nullable = false)
    private String phoneNumber;
    private String email;
    private String altPhoneNumber;
    private String createdBy;
    private String referredBy;
    private String branchCode;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    /*@OneToMany
    List<BankAccounts> bankAccounts;

   */
}
