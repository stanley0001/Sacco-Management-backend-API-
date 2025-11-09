package com.example.demo.finance.loanManagement.config;

import com.example.demo.finance.loanManagement.services.ProductService;
import com.example.demo.finance.loanManagement.parsistence.entities.InterestStrategy;
import com.example.demo.finance.loanManagement.parsistence.entities.InterestType;
import com.example.demo.finance.loanManagement.parsistence.entities.Products;
import com.example.demo.finance.loanManagement.services.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeds WAHUDUMU SACCO Products on application startup
 * Only runs if products don't already exist
 */
@Configuration
public class WahudumuProductSeeder {

    private static final Logger logger = LoggerFactory.getLogger(WahudumuProductSeeder.class);

    @Bean
    CommandLineRunner seedWahudumuProducts(ProductService productService) {
        return args -> {
            // Check if products already exist
            if (!productService.findAllProducts().isEmpty()) {
                logger.info("Products already exist. Skipping WAHUDUMU seed data.");
                return;
            }

            logger.info("Seeding WAHUDUMU SACCO Products...");

            // ================================================
            // SAVING PRODUCTS
            // ================================================

            // 1. Share Capital
            Products shareCapital = new Products();
            shareCapital.setName("Share Capital");
            shareCapital.setCode("SHARE_CAP");
            shareCapital.setTransactionType("SAVINGS");
            shareCapital.setTerm(12);
            shareCapital.setTimeSpan("MONTHS");
            shareCapital.setInterest(0);
            shareCapital.setShareValue(100.0); // KES 100 per share
            shareCapital.setMinShares(500); // Minimum 500 shares
            shareCapital.setMaxShares(1000); // Maximum 1000 shares
            shareCapital.setMinLimit(500 * 100); // 500 shares × 100
            shareCapital.setMaxLimit(1000 * 100); // 1000 shares × 100
            shareCapital.setActive(true);
            shareCapital.setInterestStrategy(InterestStrategy.SIMPLE_INTEREST);
            shareCapital.setInterestType(InterestType.PER_YEAR);
            shareCapital.setMpesaEnabled(true);
            shareCapital.setAllowEarlyRepayment(true);
            productService.saveProduct(shareCapital);

            // 2. Normal Deposit
            Products normalDeposit = new Products();
            normalDeposit.setName("Normal Deposit");
            normalDeposit.setCode("NORM_DEP");
            normalDeposit.setTransactionType("SAVINGS");
            normalDeposit.setTerm(12);
            normalDeposit.setTimeSpan("MONTHS");
            normalDeposit.setInterest(0);
            normalDeposit.setMinLimit(500); // Min monthly KES 500
            normalDeposit.setMaxLimit(999999999);
            normalDeposit.setActive(true);
            normalDeposit.setInterestStrategy(InterestStrategy.SIMPLE_INTEREST);
            normalDeposit.setInterestType(InterestType.PER_YEAR);
            normalDeposit.setMpesaEnabled(true);
            normalDeposit.setAllowEarlyRepayment(true);
            productService.saveProduct(normalDeposit);

            // 3. Junior Saving
            Products juniorSaving = new Products();
            juniorSaving.setName("Junior Saving");
            juniorSaving.setCode("JUN_SAV");
            juniorSaving.setTransactionType("SAVINGS");
            juniorSaving.setTerm(12);
            juniorSaving.setTimeSpan("MONTHS");
            juniorSaving.setInterest(0);
            juniorSaving.setMinLimit(0);
            juniorSaving.setMaxLimit(999999999);
            juniorSaving.setMaxAge(17); // Below 18 years
            juniorSaving.setActive(true);
            juniorSaving.setInterestStrategy(InterestStrategy.SIMPLE_INTEREST);
            juniorSaving.setInterestType(InterestType.PER_YEAR);
            juniorSaving.setMpesaEnabled(true);
            juniorSaving.setAllowEarlyRepayment(true);
            productService.saveProduct(juniorSaving);

            // ================================================
            // LOAN PRODUCTS
            // ================================================

            // 1. Normal Loan
            Products normalLoan = new Products();
            normalLoan.setName("Normal Loan");
            normalLoan.setCode("NORM_LOAN");
            normalLoan.setTransactionType("LOAN");
            normalLoan.setTerm(108);
            normalLoan.setTimeSpan("MONTHS");
            normalLoan.setInterest((int)(14.4 * 10)); // Store as 144 (14.4%)
            normalLoan.setMinLimit(1000);
            normalLoan.setMaxLimit(999999999);
            normalLoan.setEligibilityMultiplier(3.0); // 3x savings
            normalLoan.setActive(true);
            normalLoan.setInterestStrategy(InterestStrategy.REDUCING_BALANCE);
            normalLoan.setInterestType(InterestType.PER_MONTH);
            normalLoan.setMpesaEnabled(true);
            normalLoan.setAllowEarlyRepayment(true);
            productService.saveProduct(normalLoan);

            // 2. Emergency Loan
            Products emergencyLoan = new Products();
            emergencyLoan.setName("Emergency Loan");
            emergencyLoan.setCode("EMER_LOAN");
            emergencyLoan.setTransactionType("LOAN");
            emergencyLoan.setTerm(18);
            emergencyLoan.setTimeSpan("MONTHS");
            emergencyLoan.setInterest(140);
            emergencyLoan.setMinLimit(1000);
            emergencyLoan.setMaxLimit(999999999);
            emergencyLoan.setEligibilityMultiplier(3.0); // 3x savings
            emergencyLoan.setActive(true);
            emergencyLoan.setInterestStrategy(InterestStrategy.REDUCING_BALANCE);
            emergencyLoan.setInterestType(InterestType.PER_MONTH);
            emergencyLoan.setMpesaEnabled(true);
            emergencyLoan.setAllowEarlyRepayment(true);
            productService.saveProduct(emergencyLoan);

            // 3. Development Loan
            Products developmentLoan = new Products();
            developmentLoan.setName("Development Loan");
            developmentLoan.setCode("DEV_LOAN");
            developmentLoan.setTransactionType("LOAN");
            developmentLoan.setTerm(108);
            developmentLoan.setTimeSpan("MONTHS");
            developmentLoan.setInterest(140);
            developmentLoan.setMinLimit(1000);
            developmentLoan.setMaxLimit(999999999);
            developmentLoan.setEligibilityMultiplier(3.0); // 3x savings
            developmentLoan.setActive(true);
            developmentLoan.setInterestStrategy(InterestStrategy.REDUCING_BALANCE);
            developmentLoan.setInterestType(InterestType.PER_MONTH);
            developmentLoan.setMpesaEnabled(true);
            developmentLoan.setAllowEarlyRepayment(true);
            productService.saveProduct(developmentLoan);

            // 4. Top-Up Loan
            Products topUpLoan = new Products();
            topUpLoan.setName("Top Up Loan");
            topUpLoan.setCode("TOPUP_LOAN");
            topUpLoan.setTransactionType("LOAN");
            topUpLoan.setTerm(108);
            topUpLoan.setTimeSpan("MONTHS");
            topUpLoan.setInterest(140);
            topUpLoan.setMinLimit(1000);
            topUpLoan.setMaxLimit(999999999);
            topUpLoan.setEligibilityMultiplier(3.0); // 3x savings
            topUpLoan.setTopUp(true); // Top-up enabled
            topUpLoan.setActive(true);
            topUpLoan.setInterestStrategy(InterestStrategy.REDUCING_BALANCE);
            topUpLoan.setInterestType(InterestType.PER_MONTH);
            topUpLoan.setMpesaEnabled(true);
            topUpLoan.setAllowEarlyRepayment(true);
            productService.saveProduct(topUpLoan);

            // 5. Education Loan
            Products educationLoan = new Products();
            educationLoan.setName("Education Loan");
            educationLoan.setCode("EDU_LOAN");
            educationLoan.setTransactionType("LOAN");
            educationLoan.setTerm(18);
            educationLoan.setTimeSpan("MONTHS");
            educationLoan.setInterest(140);
            educationLoan.setMinLimit(1000);
            educationLoan.setMaxLimit(999999999);
            educationLoan.setEligibilityMultiplier(3.0); // 3x savings
            educationLoan.setActive(true);
            educationLoan.setInterestStrategy(InterestStrategy.REDUCING_BALANCE);
            educationLoan.setInterestType(InterestType.PER_MONTH);
            educationLoan.setMpesaEnabled(true);
            educationLoan.setAllowEarlyRepayment(true);
            productService.saveProduct(educationLoan);

            // 6. Asset Based Loan
            Products assetLoan = new Products();
            assetLoan.setName("Asset Based Loan");
            assetLoan.setCode("ASSET_LOAN");
            assetLoan.setTransactionType("LOAN");
            assetLoan.setTerm(24);
            assetLoan.setTimeSpan("MONTHS");
            assetLoan.setInterest(150);
            assetLoan.setMinLimit(1000);
            assetLoan.setMaxLimit(150000); // Max KES 150,000
            assetLoan.setEligibilityMultiplier(3.0); // 3x savings
            assetLoan.setActive(true);
            assetLoan.setInterestStrategy(InterestStrategy.FLAT_RATE);
            assetLoan.setInterestType(InterestType.ONCE_TOTAL); // Straight line
            assetLoan.setMpesaEnabled(true);
            assetLoan.setAllowEarlyRepayment(true);
            productService.saveProduct(assetLoan);

            // 7. Boresha Maisha Loan
            Products boreshaLoan = new Products();
            boreshaLoan.setName("Boresha Maisha Loan");
            boreshaLoan.setCode("BORESHA_LOAN");
            boreshaLoan.setTransactionType("LOAN");
            boreshaLoan.setTerm(18);
            boreshaLoan.setTimeSpan("MONTHS");
            boreshaLoan.setInterest(150);
            boreshaLoan.setMinLimit(1000);
            boreshaLoan.setMaxLimit(100000); // Max KES 100,000
            boreshaLoan.setEligibilityMultiplier(3.0); // 3x savings
            boreshaLoan.setActive(true);
            boreshaLoan.setInterestStrategy(InterestStrategy.FLAT_RATE);
            boreshaLoan.setInterestType(InterestType.ONCE_TOTAL); // Straight line
            boreshaLoan.setMpesaEnabled(true);
            boreshaLoan.setAllowEarlyRepayment(true);
            productService.saveProduct(boreshaLoan);

            // 8. Salary Advance Loan
            Products salaryAdvance = new Products();
            salaryAdvance.setName("Salary Advance Loan");
            salaryAdvance.setCode("SAL_ADV");
            salaryAdvance.setTransactionType("LOAN");
            salaryAdvance.setTerm(6);
            salaryAdvance.setTimeSpan("MONTHS");
            salaryAdvance.setInterest(100);
            salaryAdvance.setMinLimit(1000);
            salaryAdvance.setMaxLimit(20000); // Max KES 20,000
            salaryAdvance.setRequireCheckOff(true); // Check-off members only
            salaryAdvance.setActive(true);
            salaryAdvance.setInterestStrategy(InterestStrategy.FLAT_RATE);
            salaryAdvance.setInterestType(InterestType.ONCE_TOTAL); // Straight line
            salaryAdvance.setMpesaEnabled(true);
            salaryAdvance.setAutoPaymentEnabled(true); // Auto-deduction for check-off
            salaryAdvance.setAllowEarlyRepayment(true);
            productService.saveProduct(salaryAdvance);

            // 9. Quickfix Loan
            Products quickfixLoan = new Products();
            quickfixLoan.setName("Quickfix Loan");
            quickfixLoan.setCode("QUICK_FIX");
            quickfixLoan.setTransactionType("LOAN");
            quickfixLoan.setTerm(2);
            quickfixLoan.setTimeSpan("MONTHS");
            quickfixLoan.setInterest(140);
            quickfixLoan.setMinLimit(500);
            quickfixLoan.setMaxLimit(5000); // Max KES 5,000
            quickfixLoan.setEligibilityMultiplier(3.0); // 3x savings
            quickfixLoan.setActive(true);
            quickfixLoan.setInterestStrategy(InterestStrategy.FLAT_RATE);
            quickfixLoan.setInterestType(InterestType.ONCE_TOTAL);
            quickfixLoan.setMpesaEnabled(true);
            quickfixLoan.setAllowEarlyRepayment(true);
            productService.saveProduct(quickfixLoan);

            logger.info("Successfully seeded {} WAHUDUMU SACCO products", productService.findAllProducts().size());
        };
    }
}
