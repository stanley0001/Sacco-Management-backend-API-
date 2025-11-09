package com.example.demo.finance.loanManagement.services;

import com.example.demo.erp.communication.sms.SmsService;
import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.finance.banking.parsitence.enitities.BankAccounts;
import com.example.demo.finance.banking.parsitence.repositories.BankAccountRepo;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanAccount;
import com.example.demo.finance.loanManagement.parsistence.entities.LoanStandingOrder;
import com.example.demo.finance.loanManagement.parsistence.entities.loanTransactions;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanAccountRepo;
import com.example.demo.finance.loanManagement.parsistence.repositories.LoanStandingOrderRepository;
import com.example.demo.system.entities.GlobalConfig;
import com.example.demo.system.repositories.GlobalConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Auto Loan Deduction Service
 * Automatically deducts loan payments from account deposits
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutoLoanDeductionService {

    private final GlobalConfigRepository globalConfigRepository;
    private final LoanStandingOrderRepository standingOrderRepository;
    private final LoanAccountRepo loanAccountRepo;
    private final BankAccountRepo bankAccountRepo;
    private final CustomerRepository customerRepository;
    private final LoanPaymentService loanPaymentService;
    private final SmsService smsService;

    // Config keys
    private static final String CONFIG_AUTO_DEDUCTION_ENABLED = "AUTO_LOAN_DEDUCTION_ENABLED";
    private static final String CONFIG_DEFAULT_DEDUCTION_PERCENTAGE = "DEFAULT_DEDUCTION_PERCENTAGE";
    private static final String CONFIG_MINIMUM_BALANCE = "MINIMUM_BALANCE_AFTER_DEDUCTION";

    /**
     * Check if auto loan deduction is globally enabled
     */
    public boolean isAutoDeductionEnabled() {
        return globalConfigRepository.findByConfigKey(CONFIG_AUTO_DEDUCTION_ENABLED)
            .map(GlobalConfig::getBooleanValue)
            .orElse(false);
    }

    /**
     * Process auto loan deduction on account deposit
     */
    @Transactional
    public void processAutoDeduction(
        Long customerId, 
        Long savingsAccountId, 
        BigDecimal depositAmount,
        BigDecimal currentBalance,
        String transactionType,
        String transactionReference
    ) {
        try {
            // Check if auto deduction is globally enabled
            if (!isAutoDeductionEnabled()) {
                log.debug("Auto loan deduction is disabled globally");
                return;
            }

            log.info("Processing auto loan deduction for customer: {}, account: {}, deposit: {}", 
                customerId, savingsAccountId, depositAmount);

            // Find active standing orders for this customer and account
            List<LoanStandingOrder> standingOrders = standingOrderRepository
                .findByCustomerIdAndIsActiveTrue(customerId);

            if (standingOrders.isEmpty()) {
                log.debug("No active standing orders found for customer: {}", customerId);
                return;
            }

            // Filter by savings account if specified
            standingOrders = standingOrders.stream()
                .filter(so -> so.getSavingsAccountId().equals(savingsAccountId))
                .filter(so -> shouldTriggerDeduction(so, transactionType))
                .toList();

            if (standingOrders.isEmpty()) {
                log.debug("No matching standing orders for account: {} and transaction type: {}", 
                    savingsAccountId, transactionType);
                return;
            }

            // Process each standing order
            BigDecimal remainingBalance = currentBalance;
            for (LoanStandingOrder standingOrder : standingOrders) {
                remainingBalance = processStandingOrder(
                    standingOrder, 
                    depositAmount, 
                    remainingBalance,
                    transactionReference
                );

                // Stop if balance is too low
                if (remainingBalance.compareTo(getMinimumBalance()) <= 0) {
                    log.info("Minimum balance reached, stopping further deductions");
                    break;
                }
            }

        } catch (Exception e) {
            log.error("Error processing auto loan deduction for customer: {}", customerId, e);
            // Don't throw exception - allow deposit to succeed even if deduction fails
        }
    }

    /**
     * Process a single standing order
     */
    private BigDecimal processStandingOrder(
        LoanStandingOrder standingOrder,
        BigDecimal depositAmount,
        BigDecimal currentBalance,
        String transactionReference
    ) {
        try {
            // Get loan account
            Optional<LoanAccount> loanAccountOpt = loanAccountRepo.findById(standingOrder.getLoanAccountId());
            if (loanAccountOpt.isEmpty()) {
                log.warn("Loan account not found: {}", standingOrder.getLoanAccountId());
                return currentBalance;
            }

            LoanAccount loanAccount = loanAccountOpt.get();

            // Check if loan is still active
            if (!"ACTIVE".equalsIgnoreCase(loanAccount.getStatus())) {
                log.debug("Loan account is not active: {}", loanAccount.getLoanref());
                return currentBalance;
            }

            // Get outstanding balance
            BigDecimal outstanding = loanAccount.getAccountBalance() != null 
                ? BigDecimal.valueOf(loanAccount.getAccountBalance())
                : BigDecimal.ZERO;

            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
                log.info("Loan fully paid, deactivating standing order: {}", standingOrder.getId());
                standingOrder.setIsActive(false);
                standingOrderRepository.save(standingOrder);
                return currentBalance;
            }

            // Calculate deduction amount
            BigDecimal deductionAmount = standingOrder.calculateDeductionAmount(
                depositAmount,
                currentBalance,
                outstanding
            );

            if (deductionAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("No deduction required for standing order: {}", standingOrder.getId());
                return currentBalance;
            }

            log.info("Deducting {} from loan account: {}", deductionAmount, loanAccount.getLoanref());

            // Process loan payment
            loanTransactions transaction = loanPaymentService.processLoanPayment(
                loanAccount.getAccountId(),
                deductionAmount,
                "AUTO_DEDUCTION",
                transactionReference
            );
            
            boolean paymentSuccess = (transaction != null);

            if (paymentSuccess) {
                // Record successful deduction
                standingOrder.recordDeduction(deductionAmount);
                standingOrderRepository.save(standingOrder);

                // Update balance
                currentBalance = currentBalance.subtract(deductionAmount);

                // Send SMS notification
                if (standingOrder.getSendSmsNotification()) {
                    sendDeductionNotification(
                        standingOrder.getCustomerId(),
                        loanAccount,
                        deductionAmount,
                        currentBalance
                    );
                }

                log.info("✅ Auto deduction successful: {} for loan: {}", 
                    deductionAmount, loanAccount.getLoanref());
            } else {
                log.warn("❌ Loan payment failed for auto deduction");
            }

            return currentBalance;

        } catch (Exception e) {
            log.error("Error processing standing order: {}", standingOrder.getId(), e);
            return currentBalance;
        }
    }

    /**
     * Check if deduction should be triggered based on transaction type
     */
    private boolean shouldTriggerDeduction(LoanStandingOrder standingOrder, String transactionType) {
        if (transactionType == null) return false;

        return switch (standingOrder.getTriggerType()) {
            case ON_DEPOSIT -> transactionType.contains("DEPOSIT");
            case ON_SALARY -> transactionType.contains("SALARY");
            case ON_MPESA -> transactionType.contains("MPESA") || transactionType.contains("M-PESA");
            case ON_ANY_CREDIT -> true;
        };
    }

    /**
     * Get minimum balance configuration
     */
    private BigDecimal getMinimumBalance() {
        return globalConfigRepository.findByConfigKey(CONFIG_MINIMUM_BALANCE)
            .map(GlobalConfig::getDecimalValue)
            .orElse(BigDecimal.valueOf(100)); // Default KES 100
    }

    /**
     * Send SMS notification for deduction
     */
    private void sendDeductionNotification(
        Long customerId,
        LoanAccount loanAccount,
        BigDecimal deductedAmount,
        BigDecimal remainingBalance
    ) {
        try {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) return;

            Customer customer = customerOpt.get();
            String phoneNumber = customer.getPhoneNumber();

            if (phoneNumber == null || phoneNumber.isBlank()) return;

            String message = String.format(
                "Auto Loan Payment: KES %,.2f deducted from your account for loan %s. " +
                "Remaining loan balance: KES %,.2f. Account balance: KES %,.2f. Thank you.",
                deductedAmount,
                loanAccount.getLoanref(),
                loanAccount.getAccountBalance(),
                remainingBalance
            );

            smsService.sendSms(phoneNumber, message);
            log.info("✅ Deduction notification sent to: {}", phoneNumber);

        } catch (Exception e) {
            log.error("Failed to send deduction notification", e);
        }
    }

    /**
     * Create standing order for customer
     */
    @Transactional
    public LoanStandingOrder createStandingOrder(
        Long customerId,
        Long loanAccountId,
        Long savingsAccountId,
        LoanStandingOrder.DeductionType deductionType,
        BigDecimal amount,
        BigDecimal percentage,
        String createdBy
    ) {
        // Check if standing order already exists
        Optional<LoanStandingOrder> existing = standingOrderRepository
            .findByCustomerIdAndLoanAccountIdAndIsActiveTrue(customerId, loanAccountId);

        if (existing.isPresent()) {
            log.info("Standing order already exists, updating: {}", existing.get().getId());
            LoanStandingOrder order = existing.get();
            order.setDeductionType(deductionType);
            order.setDeductionAmount(amount);
            order.setDeductionPercentage(percentage);
            order.setUpdatedBy(createdBy);
            return standingOrderRepository.save(order);
        }

        // Get loan reference
        String loanReference = loanAccountRepo.findById(loanAccountId)
            .map(LoanAccount::getLoanref)
            .orElse(null);

        // Create new standing order
        LoanStandingOrder standingOrder = LoanStandingOrder.builder()
            .customerId(customerId)
            .loanAccountId(loanAccountId)
            .loanReference(loanReference)
            .savingsAccountId(savingsAccountId)
            .isActive(true)
            .deductionType(deductionType)
            .deductionAmount(amount)
            .deductionPercentage(percentage)
            .minimumBalance(getMinimumBalance())
            .triggerType(LoanStandingOrder.TriggerType.ON_DEPOSIT)
            .sendSmsNotification(true)
            .totalDeducted(BigDecimal.ZERO)
            .deductionCount(0)
            .createdBy(createdBy)
            .build();

        standingOrder = standingOrderRepository.save(standingOrder);
        log.info("✅ Standing order created: {} for loan: {}", standingOrder.getId(), loanReference);

        return standingOrder;
    }

    /**
     * Deactivate standing order
     */
    @Transactional
    public void deactivateStandingOrder(Long standingOrderId, String updatedBy) {
        Optional<LoanStandingOrder> orderOpt = standingOrderRepository.findById(standingOrderId);
        if (orderOpt.isPresent()) {
            LoanStandingOrder order = orderOpt.get();
            order.setIsActive(false);
            order.setUpdatedBy(updatedBy);
            standingOrderRepository.save(order);
            log.info("✅ Standing order deactivated: {}", standingOrderId);
        }
    }

    /**
     * Get customer's active standing orders
     */
    public List<LoanStandingOrder> getCustomerStandingOrders(Long customerId) {
        return standingOrderRepository.findByCustomerIdAndIsActiveTrue(customerId);
    }
}
