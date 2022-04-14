package com.example.demo.controllers;

import com.example.demo.model.ContactBook;
import com.example.demo.model.ContactList;
import com.example.demo.model.Email;
import com.example.demo.model.messageTemplates;
import com.example.demo.model.models.ContactListUpload;
import com.example.demo.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/communication")
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
    @GetMapping("/Outbox")
    public ResponseEntity<List<Email>> getOutbox(){
        List<Email> outbox =communicationService.getOutbox();
        return new ResponseEntity<>(outbox,HttpStatus.OK);
    }
}
