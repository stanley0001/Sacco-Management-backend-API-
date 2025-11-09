package com.example.demo.finance.loanManagement.service.interest;

import com.example.demo.finance.loanManagement.parsistence.entities.InterestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Factory class to provide the appropriate InterestCalculator based on InterestType
 */
@Service
public class InterestCalculatorFactory {
    
    private final PerMonthInterestCalculator perMonthCalculator;
    private final OnceTotalInterestCalculator onceTotalCalculator;
    
    @Autowired
    public InterestCalculatorFactory(PerMonthInterestCalculator perMonthCalculator, 
                                    OnceTotalInterestCalculator onceTotalCalculator) {
        this.perMonthCalculator = perMonthCalculator;
        this.onceTotalCalculator = onceTotalCalculator;
    }
    
    /**
     * Get the appropriate interest calculator based on interest type
     * 
     * @param interestType The type of interest calculation (PER_MONTH or ONCE_TOTAL)
     * @return The corresponding InterestCalculator implementation
     */
    public InterestCalculator getCalculator(InterestType interestType) {
        if (interestType == null) {
            // Default to PER_MONTH if not specified
            return perMonthCalculator;
        }
        
        switch (interestType) {
            case PER_MONTH:
                return perMonthCalculator;
            case ONCE_TOTAL:
                return onceTotalCalculator;
            default:
                throw new IllegalArgumentException("Unsupported interest type: " + interestType);
        }
    }
}
