package com.example.demo.communication.parsitence.repositories;

import com.example.demo.communication.parsitence.models.ContactBook;
import org.springframework.data.repository.CrudRepository;

public interface ContactBookRepo extends CrudRepository<ContactBook, Long> {
}
