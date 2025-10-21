package com.example.demo.ussd.services;

import com.example.demo.ussd.dto.UssdResponse;
import com.example.demo.ussd.enums.UssdMenuState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UssdTransactionService {

    public UssdResponse handleBalanceInquiry(UssdSession session, String input) {
        if (input.equals("0")) {
            session.setCurrentState(UssdMenuState.MAIN_MENU);
            return UssdResponse.cont("Back to main menu\n1. Balance\n2. Statement\n3. Loan\n0. Exit");
        }
        
        // Mock balance data
        String balance = "KES 45,230.50";
        return UssdResponse.end("Your account balance is: " + balance + "\nThank you for using SACCO services.");
    }

    public UssdResponse handleMiniStatement(UssdSession session, String input) {
        if (input.equals("0")) {
            session.setCurrentState(UssdMenuState.MAIN_MENU);
            return UssdResponse.cont("Back to main menu");
        }
        
        // Mock mini statement
        StringBuilder statement = new StringBuilder();
        statement.append("Last 3 Transactions:\n");
        statement.append("1. Deposit KES 5,000\n");
        statement.append("2. Withdrawal KES 2,000\n");
        statement.append("3. Loan Repay KES 3,500\n");
        statement.append("Balance: KES 45,230.50");
        
        return UssdResponse.end(statement.toString());
    }

    public UssdResponse handleLoanApplication(UssdSession session, String input) {
        if (input.equals("0")) {
            session.setCurrentState(UssdMenuState.MAIN_MENU);
            return UssdResponse.cont("Back to main menu");
        }
        
        session.storeData("selectedProduct", input);
        session.setCurrentState(UssdMenuState.LOAN_AMOUNT_INPUT);
        return UssdResponse.cont("Enter loan amount (KES):");
    }

    public UssdResponse handleLoanProducts(UssdSession session, String input) {
        if (input.equals("0")) {
            session.setCurrentState(UssdMenuState.MAIN_MENU);
            return UssdResponse.cont("Back to main menu");
        }
        return UssdResponse.end("Product details sent to your phone.");
    }

    public UssdResponse handleLoanAmountInput(UssdSession session, String input) {
        try {
            double amount = Double.parseDouble(input);
            session.storeData("loanAmount", amount);
            session.setCurrentState(UssdMenuState.PIN_VERIFICATION);
            return UssdResponse.cont("Enter your PIN to confirm:");
        } catch (NumberFormatException e) {
            return UssdResponse.cont("Invalid amount. Please enter a valid number:");
        }
    }

    public UssdResponse handleDepositMenu(UssdSession session, String input) {
        if (input.equals("1")) {
            session.setCurrentState(UssdMenuState.DEPOSIT_AMOUNT_INPUT);
            return UssdResponse.cont("Enter amount to deposit:");
        }
        return UssdResponse.end("Feature coming soon!");
    }

    public UssdResponse handleDepositAmountInput(UssdSession session, String input) {
        try {
            double amount = Double.parseDouble(input);
            session.storeData("depositAmount", amount);
            session.setCurrentState(UssdMenuState.MPESA_DEPOSIT);
            return UssdResponse.end("STK Push sent to " + session.getPhoneNumber() + " for KES " + amount);
        } catch (NumberFormatException e) {
            return UssdResponse.cont("Invalid amount. Please enter a valid number:");
        }
    }

    public UssdResponse handleMpesaDeposit(UssdSession session, String input) {
        return UssdResponse.end("Deposit processed successfully!");
    }

    public UssdResponse handlePinVerification(UssdSession session, String input) {
        // In production, verify PIN against database
        if (input.length() >= 4) {
            String amount = session.getDataAsString("loanAmount");
            return UssdResponse.end("Loan application for KES " + amount + " submitted successfully!");
        }
        return UssdResponse.end("Invalid PIN. Transaction cancelled.");
    }

    public UssdResponse handleChangePinMenu(UssdSession session, String input) {
        session.storeData("oldPin", input);
        session.setCurrentState(UssdMenuState.NEW_PIN_INPUT);
        return UssdResponse.cont("Enter new PIN:");
    }

    public UssdResponse handleNewPinInput(UssdSession session, String input) {
        session.storeData("newPin", input);
        session.setCurrentState(UssdMenuState.CONFIRM_PIN_INPUT);
        return UssdResponse.cont("Confirm new PIN:");
    }

    public UssdResponse handleConfirmPinInput(UssdSession session, String input) {
        String newPin = session.getDataAsString("newPin");
        if (input.equals(newPin)) {
            return UssdResponse.end("PIN changed successfully!");
        }
        return UssdResponse.end("PINs do not match. Please try again.");
    }
}
