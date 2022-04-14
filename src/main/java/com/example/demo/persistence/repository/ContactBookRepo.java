package com.example.demo.persistence.repository;

import com.example.demo.model.ContactBook;
import org.springframework.data.repository.CrudRepository;

public interface ContactBookRepo extends CrudRepository<ContactBook, Long> {
}
