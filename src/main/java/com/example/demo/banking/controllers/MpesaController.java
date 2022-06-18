package com.example.demo.banking.controllers;

import com.example.demo.banking.parsitence.enitities.BankAccounts;
import com.example.demo.banking.services.BankingService;
import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("mpesa")
public class MpesaController {
    public final BankingService bankingService;

    public MpesaController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    //processing mpesa callbacks stk,BBG,BPB
    @JsonIgnoreProperties(ignoreUnknown = true)
    @PostMapping("/callbackListener")
    public ResponseEntity<?> mpesaListener(@RequestBody String callback) throws InterruptedException {
        log.info("Mpesa Call Back received.. {}",callback);
        JSONObject jsonObject=new JSONObject(callback);

        bankingService.handleCallBack(jsonObject);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("create bank accounts")
    public ResponseEntity<List<BankAccounts>> createAccounts(@RequestBody Customer customer){
       List<BankAccounts> accounts=bankingService.createBankAccounts(customer);
        return new ResponseEntity<>(accounts,HttpStatus.OK);
    }
    @GetMapping("refresh accounts")
    public ResponseEntity<?> refreshAccounts(){
        bankingService.refreshAllAccounts();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
