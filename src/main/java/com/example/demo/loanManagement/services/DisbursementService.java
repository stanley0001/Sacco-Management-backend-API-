package com.example.demo.loanManagement.services;

import com.example.demo.loanManagement.parsistence.models.Disbursements;
import com.example.demo.loanManagement.parsistence.repositories.DisbursementsRepo;
import org.springframework.stereotype.Service;

@Service
public class DisbursementService {
    public final DisbursementsRepo disbursementsRepo;

    public DisbursementService(DisbursementsRepo disbursementsRepo) {
        this.disbursementsRepo = disbursementsRepo;

    }
    public Disbursements saveDisbursement(Disbursements disbursement){
        return disbursementsRepo.save(disbursement);
    }

}
