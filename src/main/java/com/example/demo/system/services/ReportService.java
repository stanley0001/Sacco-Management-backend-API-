package com.example.demo.system.services;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.banking.parsitence.enitities.Payments;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanApplication;
import com.example.demo.finance.loanManagement.parsistence.repositories.ApplicationRepo;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepo;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.finance.banking.parsitence.repositories.PaymentRepo;
import com.example.demo.system.parsitence.models.*;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class ReportService {

    public  final LoanAccountRepo loanAccountRepo;
    public  final PaymentRepo paymentRepo;
    public  final CustomerRepo customerRepo;
    public  final ApplicationRepo applicationRepo;

    public ReportService(LoanAccountRepo loanAccountRepo, PaymentRepo paymentRepo, CustomerRepo customerRepo, ApplicationRepo applicationRepo) {
        this.loanAccountRepo = loanAccountRepo;
        this.paymentRepo = paymentRepo;
        this.customerRepo = customerRepo;
        this.applicationRepo = applicationRepo;
    }


    public DashBoardData getData() {
        LocalDateTime localDate1 = LocalDateTime.now().minusHours(24);
        LocalDate localDate = LocalDate.now();
        DashBoardData dashBoardData = new DashBoardData();
       List<LoanAccount> disbursement = loanAccountRepo.findAllByStartDateGreaterThan(localDate1);
        List<LoanAccount> totalDefaults = loanAccountRepo.findByStatus("DEFAULT");
        Integer totalDefaultsCount = totalDefaults.size();
        dashBoardData.setTotalDefaults(String.valueOf(totalDefaultsCount));
        Integer amountDisbursed = loanAccountRepo.findAmountByStartDateGreaterThan(localDate1);
        if (amountDisbursed == null) {
            amountDisbursed = 0;
        }
        List<Payments> paymentsList = paymentRepo.findAllByPaymentTimeBefore(localDate1);
        double amountPayedIn = 0.0;
        for (Payments payment : paymentsList
        ) {
            amountPayedIn += Double.parseDouble(payment.getAmount());

        }
        List<LoanApplication> applicationList = applicationRepo.findTop10ByApplicationTimeAfter(localDate1);
        Integer disbursementCount = disbursement.size();
        Integer paymentsCount = paymentsList.size();
        Long memberCount=customerRepo.count();
        dashBoardData.setMembersCount(memberCount);
        Integer leads = 0;
        dashBoardData.setTotalLeads(String.valueOf(leads));
        dashBoardData.setAmountCollectedToday(amountPayedIn);
        dashBoardData.setAmountDisbursedToday(amountDisbursed);
        dashBoardData.setDisbursementToday(String.valueOf(disbursementCount));
        dashBoardData.setCollectionToday(String.valueOf(paymentsCount));
        dashBoardData.setApplicationsToday(applicationList);

        return dashBoardData;
    }

    public ReportComponent getReportComponentData() {
        String label = "month";
        ReportComponent response = new ReportComponent();
        ReportData application = new ReportData();
        ReportData payment = new ReportData();
        ReportData authorised = new ReportData();
        List<LoanAccount> accounts = loanAccountRepo.findAll();

        return response;
    }


    public SearchReportResponse getLoanReportByStatus(String status) {
        log.info("Fetching Loan accounts by status {}", status);
        SearchReportResponse response = new SearchReportResponse();
        List<SReportData> modifiedAccounts = new ArrayList<>();
        List<LoanAccount> loanAccounts = loanAccountRepo.findByStatus(status);
        return getSearchReportResponse(response, modifiedAccounts, loanAccounts);
    }

    public SearchReportResponse getLoanReportByDateAndStatus(SearchBody body) {
        log.info("Fetching Loan accounts by body {}", body);
        LocalDateTime from= body.getFromDate().atStartOfDay();
        LocalDateTime to= body.getToDate().atTime(23,59,59);
        SearchReportResponse response = new SearchReportResponse();
        List<SReportData> modifiedAccounts = new ArrayList<>();
        List<LoanAccount> loanAccounts;
        if (body.getStatus().equals("STATUS") || body.getStatus().equals("ALL") ){
            log.info("fetching all loan accounts....");
            loanAccounts=loanAccountRepo.findAll();
        }else {
            log.info("fetching loan accounts by status {}....",body.getStatus());
            loanAccounts = loanAccountRepo.findAmountByStartDateAndStatus(from,to,body.getStatus());

        }

        return getSearchReportResponse(response, modifiedAccounts, loanAccounts);

    }

    @NotNull
    private SearchReportResponse getSearchReportResponse(SearchReportResponse response, List<SReportData> modifiedAccounts, List<LoanAccount> loanAccounts) {
        Double totalLoanAmount = 0.0;
        Double totalLoanBalance = 0.0;
        Double totalPaid = 0.0;
        Double expectedPayment = 0.0;
        Double totalRevenue = 0.0;
        Double expectedRevenue = 0.0;
        Double loss = 0.0;
        for (LoanAccount account :
                loanAccounts) {
            SReportData data = new SReportData();
            Optional<Customer> client = customerRepo.findById(Long.valueOf(account.getCustomerId()));
            data.setClient(client.get());
            data.setLoanAccount(account);
            modifiedAccounts.add(data);
            totalLoanBalance += account.getAccountBalance();
            totalLoanAmount += account.getAmount();
            expectedPayment+=account.getPayableAmount();
        }

        log.info("Preparing Response data");
        response.setCount(modifiedAccounts.size());
        response.setTotalAmount(totalLoanAmount.toString());
        response.setTotalBalance(totalLoanBalance.toString());
        response.setTotalPayable(expectedPayment.toString());
        response.setRevenue(totalRevenue.toString());
        response.setTotalPaid(totalPaid.toString());
        response.setLoss(loss.toString());
        totalPaid=expectedPayment-totalLoanBalance;
        totalRevenue=totalPaid-totalLoanAmount;
        expectedRevenue=expectedPayment-totalLoanAmount;
        if (totalRevenue<0){
            response.setLoss(totalRevenue.toString());
        }else {
            response.setRevenue(totalRevenue.toString());
        }
        if (totalPaid>0){
            response.setTotalPaid(totalPaid.toString());
        }
        response.setExpectedRevenue(expectedRevenue.toString());
        response.getLoanAccounts().addAll(modifiedAccounts);
        log.info(modifiedAccounts);

        return response;
    }

}
