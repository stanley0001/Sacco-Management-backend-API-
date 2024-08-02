package com.example.demo.loanManagement.controllers;

import com.example.demo.events.appEvents.LoanBookUploadEvent;
import com.example.demo.loanManagement.parsistence.models.LoanBookUpload;
import com.example.demo.system.parsitence.models.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/loan-book")
public class LoanBookController {
    @Autowired
    ApplicationEventPublisher eventPublisher;
    @PostMapping("/upload")
    public ResponseEntity<ResponseModel> createProductModified(@RequestBody List<LoanBookUpload> data){
        ResponseModel responseModel=new ResponseModel();
        eventPublisher.publishEvent(new LoanBookUploadEvent(this,data));
        responseModel.setMessage("Data received for processing");
        responseModel.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseModel, HttpStatus.OK);
    }
}
