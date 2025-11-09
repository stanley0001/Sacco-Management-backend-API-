package com.example.demo.channels.ussd.services;

import com.example.demo.channels.ussd.dto.UssdResponse;
import com.example.demo.channels.ussd.enums.UssdMenuState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UssdMenuService {

    public UssdResponse showMainMenu(UssdSession session) {
        session.setCurrentState(UssdMenuState.MAIN_MENU);
        
        StringBuilder menu = new StringBuilder();
        menu.append("Welcome to SACCO Services\n");
        menu.append("1. Check Balance\n");
        menu.append("2. Mini Statement\n");
        menu.append("3. Apply for Loan\n");
        menu.append("4. Make Deposit\n");
        menu.append("5. Loan Products\n");
        menu.append("6. Change PIN\n");
        menu.append("0. Exit");
        
        return UssdResponse.cont(menu.toString());
    }

    public UssdResponse handleMainMenu(UssdSession session, String input) {
        switch (input) {
            case "1":
                session.setCurrentState(UssdMenuState.BALANCE_INQUIRY);
                return UssdResponse.cont("Select Account:\n1. Savings\n2. Loan\n0. Back");
            
            case "2":
                session.setCurrentState(UssdMenuState.MINI_STATEMENT);
                return UssdResponse.cont("Select Account:\n1. Savings\n2. Loan\n0. Back");
            
            case "3":
                session.setCurrentState(UssdMenuState.LOAN_APPLICATION);
                return UssdResponse.cont("Apply for Loan:\n1. Quick Loan\n2. Emergency Loan\n3. Development Loan\n0. Back");
            
            case "4":
                session.setCurrentState(UssdMenuState.DEPOSIT_MENU);
                return UssdResponse.cont("Deposit Options:\n1. M-Pesa\n2. Bank Transfer\n0. Back");
            
            case "5":
                session.setCurrentState(UssdMenuState.LOAN_PRODUCTS);
                return showLoanProducts(session);
            
            case "6":
                session.setCurrentState(UssdMenuState.CHANGE_PIN_MENU);
                return UssdResponse.cont("Enter current PIN:");
            
            case "0":
                return UssdResponse.end("Thank you for using SACCO services.");
            
            default:
                return UssdResponse.cont("Invalid option. Please try again.\n" + getMainMenuText());
        }
    }

    private UssdResponse showLoanProducts(UssdSession session) {
        StringBuilder menu = new StringBuilder();
        menu.append("Available Loan Products:\n\n");
        menu.append("1. Quick Loan\n");
        menu.append("   - Max: KES 50,000\n");
        menu.append("   - Rate: 10% p.a.\n");
        menu.append("   - Term: 1-6 months\n\n");
        menu.append("2. Emergency Loan\n");
        menu.append("   - Max: KES 100,000\n");
        menu.append("   - Rate: 12% p.a.\n");
        menu.append("   - Term: 1-12 months\n\n");
        menu.append("3. Development Loan\n");
        menu.append("   - Max: KES 500,000\n");
        menu.append("   - Rate: 15% p.a.\n");
        menu.append("   - Term: 6-24 months\n\n");
        menu.append("0. Main Menu");
        
        return UssdResponse.cont(menu.toString());
    }

    private String getMainMenuText() {
        return "1. Balance\n2. Statement\n3. Loan\n4. Deposit\n5. Products\n6. Change PIN\n0. Exit";
    }
}
