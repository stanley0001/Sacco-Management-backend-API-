package com.example.demo.finance.loanManagement.controllers;

import com.example.demo.finance.loanManagement.services.LoanService;
import com.example.demo.system.events.appEvents.LoanBookUploadEvent;
import com.example.demo.finance.loanManagement.parsistence.models.LoanBookUpload;
import com.example.demo.finance.loanManagement.parsistence.models.LoanCalculator;
import com.example.demo.finance.loanManagement.parsistence.models.LoanCalculatorResponse;
import com.example.demo.finance.loanManagement.services.LoanService;
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
@RequestMapping("/api/loan-actions")
public class LoanBookController {
    @Autowired
    ApplicationEventPublisher eventPublisher;
    @Autowired
    LoanService loanService;
    @PostMapping("/upload")
    public ResponseEntity<ResponseModel> createProductModified(@RequestBody List<LoanBookUpload> data){
        ResponseModel responseModel=new ResponseModel();
        eventPublisher.publishEvent(new LoanBookUploadEvent(this,data));
        int size=data.size();
        responseModel.setMessage(size+" Record(s) received for processing");
        responseModel.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseModel, HttpStatus.OK);
    }
    @PostMapping("calculator")
    public ResponseEntity<ResponseModel> loanCalculator(@RequestBody LoanCalculator data){
        ResponseModel responseModel=new ResponseModel();
//        eventPublisher.publishEvent(new LoanBookUploadEvent(this,data));
        LoanCalculatorResponse response=loanService.loanCalculator(data);
        responseModel.setBody(response);
        responseModel.setMessage("Success");
        responseModel.setStatus(HttpStatus.OK);
        if (response==null) {
            responseModel.setMessage("FAILED");
            responseModel.setStatus(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(responseModel, responseModel.getStatus());
    }

}
