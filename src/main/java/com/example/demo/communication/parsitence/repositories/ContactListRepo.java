package com.example.demo.persistence.repository;

import com.example.demo.model.ContactList;
import org.springframework.data.repository.CrudRepository;

public interface ContactListRepo extends CrudRepository<ContactList, Long> {
}
