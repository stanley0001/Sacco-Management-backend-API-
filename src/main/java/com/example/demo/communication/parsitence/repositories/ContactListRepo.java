package com.example.demo.communication.parsitence.repositories;

import com.example.demo.communication.parsitence.models.ContactList;
import org.springframework.data.repository.CrudRepository;

public interface ContactListRepo extends CrudRepository<ContactList, Long> {
}
