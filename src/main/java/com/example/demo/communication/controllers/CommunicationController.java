package com.example.demo.communication.controllers;

import com.example.demo.communication.parsitence.models.ContactBook;
import com.example.demo.communication.parsitence.models.ContactList;
import com.example.demo.communication.parsitence.models.Email;
import com.example.demo.communication.parsitence.models.messageTemplates;
import com.example.demo.communication.parsitence.models.ContactListUpload;
import com.example.demo.communication.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communication")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
@Slf4j
public class CommunicationController {
    @Autowired
    CommunicationService communicationService;
    @PostMapping("/createTemplate")
    public ResponseEntity<messageTemplates> createTemplate(@RequestBody messageTemplates template){
        messageTemplates template1=communicationService.createTemplate(template);
        return new ResponseEntity<>(template1, HttpStatus.CREATED);
    }
    @PutMapping("/updateTemplate")
    public ResponseEntity<messageTemplates> updateTemplate(@RequestBody messageTemplates template){
        messageTemplates template1=communicationService.updateTemplate(template);
        return new ResponseEntity<>(template1, HttpStatus.CREATED);
    }
    @GetMapping("/getAllTemplates")
    public ResponseEntity<List<messageTemplates>> getAllTemplate(){
        List<messageTemplates> template1=communicationService.getTemplates();
        return new ResponseEntity<>(template1, HttpStatus.OK);
    }
    @GetMapping("/getTemplate{id}")
    public ResponseEntity<messageTemplates> getTemplateById(@PathVariable Long id){
     messageTemplates template1=communicationService.getTemplateById(id);
        return new ResponseEntity<>(template1, HttpStatus.OK);
    }
    @PostMapping("/sendCustomEmail")
    public ResponseEntity<Email> createTemplate(@RequestBody Email mail){
        communicationService.sendCustomEmail(mail);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/createContactBook")
    public ResponseEntity<ContactBook> createContactBook(@RequestBody ContactBook contactBook){
        communicationService.createContactBook(contactBook);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/createContactList")
    public ResponseEntity<ContactList> createTemplate(@RequestBody ContactList contactList){
        communicationService.createContactList(contactList);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/contactListUpload")
    public ResponseEntity<ResponseEntity> uploadContacts(@RequestBody ContactListUpload contactList){
        communicationService.uploadContact(contactList);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/contactBook")
    public ResponseEntity<Iterable<ContactBook>> getContactBook(){
        Iterable<ContactBook> contactBook =communicationService.getContactBook();
        return new ResponseEntity<>(contactBook,HttpStatus.OK);
    }
    @GetMapping("/contactList")
    public ResponseEntity<Iterable<ContactList>> getContactList(){
        Iterable<ContactList> contactList =communicationService.getContactList();
        return new ResponseEntity<>(contactList,HttpStatus.OK);
    }
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Email>> getCommunicationsByCustomerId(@PathVariable Long customerId){
        // For now, return all communications - in a real implementation, 
        // you'd filter by customer ID
        List<Email> communications = communicationService.getOutbox();
        return new ResponseEntity<>(communications, HttpStatus.OK);
    }
    
    /**
     * Get all communication outbox (emails/SMS sent)
     */
    @GetMapping("/Outbox")
    @PreAuthorize("hasAnyAuthority('canViewCommunication', 'ADMIN_ACCESS')")
    public ResponseEntity<List<Email>> getOutbox(){
        try {
            List<Email> communications = communicationService.getOutbox();
            log.info("Retrieved {} communication records from outbox", communications.size());
            return new ResponseEntity<>(communications, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching outbox communications", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
