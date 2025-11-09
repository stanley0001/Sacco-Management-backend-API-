package com.example.demo.erp.communication.parsitence.repositories;

import com.example.demo.erp.communication.parsitence.models.ContactBook;
import org.springframework.data.repository.CrudRepository;

public interface ContactBookRepo extends CrudRepository<ContactBook, Long> {
}
