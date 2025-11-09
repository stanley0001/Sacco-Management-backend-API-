package com.example.demo.channels.ussd.services;

import com.example.demo.channels.ussd.dto.UssdRequest;
import com.example.demo.channels.ussd.dto.UssdResponse;
import com.example.demo.channels.ussd.enums.UssdMenuState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UssdService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UssdMenuService menuService;
    private final UssdTransactionService transactionService;

    private static final String SESSION_PREFIX = "ussd:session:";
    private static final int SESSION_TIMEOUT = 300; // 5 minutes

    public UssdResponse handleUssdRequest(UssdRequest request) {
        String sessionKey = SESSION_PREFIX + request.getSessionId();
        
        try {
            // Get or create session
            UssdSession session = getSession(sessionKey);
            if (session == null) {
                session = createNewSession(request);
            }

            // Update session with current input
            session.setLastInput(request.getText());
            session.setPhoneNumber(request.getPhoneNumber());

            // Process input based on current state
            UssdResponse response = processUssdInput(session, request);

            // Save session state
            saveSession(sessionKey, session);

            // If response is terminal, clear session
            if (response.isEnd()) {
                clearSession(sessionKey);
            }

            return response;

        } catch (Exception e) {
            log.error("Error processing USSD request", e);
            clearSession(sessionKey);
            return UssdResponse.end("An error occurred. Please try again later.");
        }
    }

    private UssdResponse processUssdInput(UssdSession session, UssdRequest request) {
        String input = extractLastInput(request.getText());
        
        switch (session.getCurrentState()) {
            case MAIN_MENU:
                return menuService.handleMainMenu(session, input);
            
            case BALANCE_INQUIRY:
                return transactionService.handleBalanceInquiry(session, input);
            
            case MINI_STATEMENT:
                return transactionService.handleMiniStatement(session, input);
            
            case LOAN_APPLICATION:
                return transactionService.handleLoanApplication(session, input);
            
            case LOAN_PRODUCTS:
                return transactionService.handleLoanProducts(session, input);
            
            case LOAN_AMOUNT_INPUT:
                return transactionService.handleLoanAmountInput(session, input);
            
            case DEPOSIT_MENU:
                return transactionService.handleDepositMenu(session, input);
            
            case DEPOSIT_AMOUNT_INPUT:
                return transactionService.handleDepositAmountInput(session, input);
            
            case MPESA_DEPOSIT:
                return transactionService.handleMpesaDeposit(session, input);
            
            case PIN_VERIFICATION:
                return transactionService.handlePinVerification(session, input);
            
            case CHANGE_PIN_MENU:
                return transactionService.handleChangePinMenu(session, input);
            
            case NEW_PIN_INPUT:
                return transactionService.handleNewPinInput(session, input);
            
            case CONFIRM_PIN_INPUT:
                return transactionService.handleConfirmPinInput(session, input);
            
            default:
                return menuService.showMainMenu(session);
        }
    }

    private String extractLastInput(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String[] parts = text.split("\\*");
        return parts[parts.length - 1];
    }

    private UssdSession getSession(String sessionKey) {
        return (UssdSession) redisTemplate.opsForValue().get(sessionKey);
    }

    private UssdSession createNewSession(UssdRequest request) {
        UssdSession session = new UssdSession();
        session.setSessionId(request.getSessionId());
        session.setPhoneNumber(request.getPhoneNumber());
        session.setCurrentState(UssdMenuState.MAIN_MENU);
        return session;
    }

    private void saveSession(String sessionKey, UssdSession session) {
        redisTemplate.opsForValue().set(sessionKey, session, SESSION_TIMEOUT, TimeUnit.SECONDS);
    }

    private void clearSession(String sessionKey) {
        redisTemplate.delete(sessionKey);
    }
}
