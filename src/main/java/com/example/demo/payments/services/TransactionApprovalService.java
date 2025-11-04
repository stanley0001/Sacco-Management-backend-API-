package com.example.demo.payments.services;

import com.example.demo.loanManagement.services.LoanPaymentService;
import com.example.demo.loanManagement.parsistence.entities.loanTransactions;
import com.example.demo.payments.entities.MpesaTransaction;
import com.example.demo.payments.entities.TransactionRequest;
import com.example.demo.payments.repositories.TransactionRequestRepository;
import com.example.demo.savingsManagement.persistence.entities.SavingsTransaction;
import com.example.demo.savingsManagement.services.SavingsAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionApprovalService {

    private final TransactionRequestRepository transactionRequestRepository;
    private final SavingsAccountService savingsAccountService;
    private final LoanPaymentService loanPaymentService;

    @Transactional
    public TransactionRequest approveTransaction(Long requestId, String approvedBy, String referenceNumber) {
        TransactionRequest request = transactionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + requestId));

        if (request.getStatus() == TransactionRequest.RequestStatus.FAILED
            || request.getStatus() == TransactionRequest.RequestStatus.CANCELLED) {
            throw new IllegalStateException("Cannot approve a failed or cancelled request");
        }

        if (request.getStatus() == TransactionRequest.RequestStatus.POSTED_TO_ACCOUNT) {
            log.info("Transaction {} already posted, skipping approval.", requestId);
            return request;
        }

        if (request.getTransactionCategory() == TransactionRequest.TransactionCategory.LOAN_REPAYMENT) {
            processLoanRepayment(request, referenceNumber, approvedBy);
        } else {
            processSavingsDeposit(request, referenceNumber, approvedBy);
        }

        request.setStatus(TransactionRequest.RequestStatus.POSTED_TO_ACCOUNT);
        request.setPostedToAccount(true);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(approvedBy);
        if (referenceNumber != null && !referenceNumber.isBlank()) {
            request.setReferenceNumber(referenceNumber);
        }

        return transactionRequestRepository.save(request);
    }

    @Transactional
    public TransactionRequest rejectTransaction(Long requestId, String rejectedBy, String reason) {
        TransactionRequest request = transactionRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + requestId));

        if (request.getStatus() == TransactionRequest.RequestStatus.POSTED_TO_ACCOUNT) {
            throw new IllegalStateException("Cannot reject a transaction that has already been posted");
        }

        request.setStatus(TransactionRequest.RequestStatus.FAILED);
        request.setFailureReason(reason);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(rejectedBy);

        return transactionRequestRepository.save(request);
    }

    @Transactional
    public TransactionRequest autoPostSuccessfulMpesa(MpesaTransaction mpesaTransaction) {
        if (mpesaTransaction.getTransactionRequestId() == null) {
            log.warn("M-PESA transaction {} has no linked transaction request", mpesaTransaction.getId());
            return null;
        }

        TransactionRequest request = transactionRequestRepository.findById(mpesaTransaction.getTransactionRequestId())
            .orElseThrow(() -> new RuntimeException("Transaction request not found: " + mpesaTransaction.getTransactionRequestId()));

        if (Boolean.TRUE.equals(request.getPostedToAccount())) {
            log.info("Transaction request {} already posted", request.getId());
            return request;
        }

        approveTransaction(request.getId(), "MPESA_CALLBACK", mpesaTransaction.getMpesaReceiptNumber());
        request.setStatus(TransactionRequest.RequestStatus.SUCCESS);
        request.setServiceProviderResponse(mpesaTransaction.getResultDesc());

        return transactionRequestRepository.save(request);
    }

    private void processSavingsDeposit(TransactionRequest request, String referenceNumber, String approvedBy) {
        if (request.getSavingsAccountId() == null) {
            throw new IllegalStateException("Savings deposit requires savings account ID");
        }

        SavingsTransaction transaction = savingsAccountService.deposit(
            request.getSavingsAccountId(),
            request.getAmount(),
            request.getPaymentMethod() != null ? request.getPaymentMethod().name() : "MANUAL",
            referenceNumber != null ? referenceNumber : request.getReferenceNumber(),
            request.getDescription(),
            approvedBy
        );

        request.setReferenceNumber(transaction.getPaymentReference());
        request.setPostedAt(LocalDateTime.now());
    }

    private void processLoanRepayment(TransactionRequest request, String referenceNumber, String approvedBy) {
        if (request.getLoanId() == null) {
            throw new IllegalStateException("Loan repayment requires loan ID");
        }

        loanTransactions loanTxn = loanPaymentService.processLoanPayment(
            request.getLoanId(),
            request.getAmount(),
            request.getPaymentMethod() != null ? request.getPaymentMethod().name() : "MANUAL",
            referenceNumber != null ? referenceNumber : request.getReferenceNumber()
        );

        request.setUtilizedForLoan(true);
        request.setUtilizedLoanId(request.getLoanId());
        request.setReferenceNumber(loanTxn.getOtherRef());
        request.setPostedAt(LocalDateTime.now());
    }
}
