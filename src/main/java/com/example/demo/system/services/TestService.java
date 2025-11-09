package com.example.demo.system.services;

import com.example.demo.finance.loanManagement.parsistence.entities.SuspensePayments;

public interface TestService {
    void settleSuspense(SuspensePayments suspensePayment, String cusPhone, Long accountId, String productCode);
}
