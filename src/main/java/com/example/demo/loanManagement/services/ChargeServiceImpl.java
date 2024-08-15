package com.example.demo.loanManagement.services;

import com.example.demo.loanManagement.parsistence.entities.Charges;
import com.example.demo.loanManagement.parsistence.repositories.ChargesRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class ChargeServiceImpl implements ChargeService {
    public ChargesRepo chargesRepo;

    public ChargeServiceImpl(ChargesRepo chargesRepo) {
        this.chargesRepo = chargesRepo;
    }

    @Override
    public Charges createCharge(Charges charge){
        return chargesRepo.save(charge);
    }
    public Charges updateCharge(Charges charge){
        return chargesRepo.save(charge);
    }

    public List<Charges> getCharges(){
        return chargesRepo.findAll();
    }
    public Optional<Charges> getChargeById(Long id){
        return chargesRepo.findById(id);
    }

    @Override
    public Optional<Charges> getChargeByProductIdAndName(String productId,String name){
        log.info("Getting Charge by product id {} and name {}",productId,name);
        Charges newCharge=new Charges();
        Optional<Charges> charge =chargesRepo.findByProductIdAndName(productId,name);
        if (charge.isPresent()) {
            log.info("Charge in Charge service {}", charge);
            newCharge=charge.get();
        }else {
            log.info("no charge action found");
        }
        return charge;
        //return newCharge;
    }

}
