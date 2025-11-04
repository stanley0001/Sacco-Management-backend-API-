# Mobile & USSD API Implementation Guide

## Overview
This document provides the complete implementation structure for Mobile App and USSD APIs for the SACCO Management System.

---

## Mobile APIs Implementation

### 1. Authentication Module

**Controllers:**
- `MobileAuthController.java` - Created ✅
  - POST `/api/mobile/auth/login` - Login with phone + PIN
  - POST `/api/mobile/auth/register` - New member registration
  - POST `/api/mobile/auth/verify-otp` - OTP verification
  - POST `/api/mobile/auth/forgot-pin` - Request PIN reset
  - POST `/api/mobile/auth/reset-pin` - Reset PIN with OTP
  - POST `/api/mobile/auth/change-pin` - Change PIN (authenticated)
  - POST `/api/mobile/auth/refresh-token` - Refresh JWT token
  - POST `/api/mobile/auth/logout` - Invalidate tokens

**DTOs Required:**
```java
// LoginRequest.java
@Data
@Validated
public class LoginRequest {
    @NotBlank @Pattern(regexp = "^254[0-9]{9}$")
    private String phoneNumber;
    
    @NotBlank @Size(min = 4, max = 6)
    private String pin;
    
    private String deviceId;
    private String fcmToken; // For push notifications
}

// AuthResponse.java
@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private MemberDto member;
    private List<String> permissions;
}

// RegisterRequest.java
@Data
@Validated
public class RegisterRequest {
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    @NotBlank @Pattern(regexp = "^254[0-9]{9}$")
    private String phoneNumber;
    
    @NotBlank @Email
    private String email;
    
    @NotBlank
    private String idNumber;
    
    @NotBlank @Size(min = 4, max = 6)
    private String pin;
    
    private String dateOfBirth;
}

// OtpVerificationRequest.java
@Data
@Validated
public class OtpVerificationRequest {
    @NotBlank
    private String phoneNumber;
    
    @NotBlank @Size(min = 4, max = 6)
    private String otp;
    
    private String verificationType; // REGISTRATION, PIN_RESET, TRANSACTION
}
```

---

### 2. Account Management Module

**Controllers:**
- `MobileAccountController.java` - Created ✅
  - GET `/api/mobile/accounts` - Get all member accounts
  - GET `/api/mobile/accounts/{id}/balance` - Get balance
  - GET `/api/mobile/accounts/{id}/statement` - Full statement (paginated)
  - GET `/api/mobile/accounts/{id}/mini-statement` - Last 5 transactions
  - POST `/api/mobile/accounts/{id}/deposit` - Make deposit
  - POST `/api/mobile/accounts/{id}/withdraw` - Make withdrawal
  - POST `/api/mobile/accounts/transfer` - Transfer between accounts

**DTOs Required:**
```java
// AccountSummaryDto.java
@Data
public class AccountSummaryDto {
    private String accountId;
    private String accountNumber;
    private String accountType; // SAVINGS, FOSA, SHARES
    private String accountName;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private String currency;
    private String status;
}

// BalanceDto.java
@Data
public class BalanceDto {
    private String accountId;
    private String accountNumber;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private BigDecimal pendingDebits;
    private BigDecimal pendingCredits;
    private String asOfDate;
}

// TransactionDto.java
@Data
public class TransactionDto {
    private String transactionId;
    private String transactionDate;
    private String transactionType;
    private String description;
    private BigDecimal amount;
    private String debitCredit; // DR or CR
    private BigDecimal balance;
    private String reference;
    private String status;
}

// DepositRequest.java
@Data
@Validated
public class DepositRequest {
    @NotNull @Positive
    private BigDecimal amount;
    
    private String paymentMethod; // MPESA, BANK, CASH
    private String reference;
    private String narration;
    
    @NotBlank
    private String pin; // For verification
}

// WithdrawalRequest.java
@Data
@Validated
public class WithdrawalRequest {
    @NotNull @Positive
    private BigDecimal amount;
    
    private String withdrawalMethod; // MPESA, BANK
    private String destinationAccount;
    private String narration;
    
    @NotBlank
    private String pin;
}

// TransferRequest.java
@Data
@Validated
public class TransferRequest {
    @NotBlank
    private String fromAccountId;
    
    @NotBlank
    private String toAccountId;
    
    @NotNull @Positive
    private BigDecimal amount;
    
    private String narration;
    
    @NotBlank
    private String pin;
}

// TransactionResponseDto.java
@Data
public class TransactionResponseDto {
    private boolean success;
    private String transactionId;
    private String reference;
    private String message;
    private BigDecimal newBalance;
    private String transactionDate;
}
```

---

### 3. Loan Management Module

**Controllers:**
- `MobileLoanController.java` - Created ✅
  - GET `/api/mobile/loans` - Get all member loans
  - GET `/api/mobile/loans/{id}` - Get loan details
  - GET `/api/mobile/loans/{id}/schedule` - Repayment schedule
  - GET `/api/mobile/loans/products` - Available loan products
  - GET `/api/mobile/loans/eligibility/{productId}` - Check eligibility
  - POST `/api/mobile/loans/apply` - Submit loan application
  - POST `/api/mobile/loans/{id}/repay` - Make loan payment
  - GET `/api/mobile/loans/{id}/transactions` - Loan transaction history
  - POST `/api/mobile/loans/{id}/top-up` - Request loan top-up

**DTOs Required:**
```java
// LoanSummaryDto.java
@Data
public class LoanSummaryDto {
    private String loanId;
    private String loanNumber;
    private String productName;
    private BigDecimal principalAmount;
    private BigDecimal outstandingBalance;
    private BigDecimal interestRate;
    private String status;
    private String disbursementDate;
    private String maturityDate;
    private Integer installmentsPaid;
    private Integer totalInstallments;
}

// LoanDetailDto.java
@Data
public class LoanDetailDto {
    private String loanId;
    private String loanNumber;
    private String productCode;
    private String productName;
    private BigDecimal principalAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalRepayable;
    private BigDecimal paidAmount;
    private BigDecimal outstandingBalance;
    private BigDecimal interestRate;
    private String interestType;
    private Integer loanTerm;
    private String termUnit;
    private String status;
    private String applicationDate;
    private String approvalDate;
    private String disbursementDate;
    private String maturityDate;
    private BigDecimal penaltyAmount;
    private Integer daysOverdue;
    private String nextPaymentDate;
    private BigDecimal nextPaymentAmount;
}

// LoanProductDto.java
@Data
public class LoanProductDto {
    private String productId;
    private String productCode;
    private String productName;
    private String description;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private String interestType;
    private Integer minTerm;
    private Integer maxTerm;
    private String termUnit;
    private List<String> eligibilityCriteria;
    private BigDecimal processingFee;
    private boolean topUpAllowed;
}

// EligibilityResponseDto.java
@Data
public class EligibilityResponseDto {
    private boolean eligible;
    private BigDecimal maxLoanAmount;
    private String reason;
    private List<String> requirements;
    private BigDecimal recommendedAmount;
}

// LoanApplicationRequest.java
@Data
@Validated
public class LoanApplicationRequest {
    @NotBlank
    private String productId;
    
    @NotNull @Positive
    private BigDecimal amount;
    
    @NotNull @Positive
    private Integer term;
    
    private String purpose;
    private String guarantorId; // Optional
    private String collateralDescription;
}

// LoanRepaymentRequest.java
@Data
@Validated
public class LoanRepaymentRequest {
    @NotNull @Positive
    private BigDecimal amount;
    
    private String paymentMethod; // MPESA, BANK, DEDUCTION
    private String reference;
    
    @NotBlank
    private String pin;
}
```

---

## USSD APIs Implementation

### USSD Flow Structure

**Entry Point:** `*384*123#` (example USSD code)

**Main Menu:**
```
Welcome to SACCO Services
1. Check Balance
2. Mini Statement
3. Apply for Loan
4. Make Deposit
5. Loan Products
6. Change PIN
0. Exit
```

### Controllers

**`UssdController.java`** - Created ✅
- POST `/api/ussd/callback` - Africa's Talking format
- POST `/api/ussd/callback/safaricom` - Safaricom format
- GET `/api/ussd/test` - Testing endpoint

### Services Architecture

**`UssdService.java`** - Main orchestrator
- Session management with Redis
- State machine implementation
- Input routing based on current state

**`UssdMenuService.java`** - Menu generation
- Main menu display
- Sub-menu navigation
- Product listings

**`UssdTransactionService.java`** - Transaction handling
```java
@Service
public class UssdTransactionService {
    
    public UssdResponse handleBalanceInquiry(UssdSession session, String input) {
        // 1. Verify member
        // 2. Fetch balances
        // 3. Format and return
    }
    
    public UssdResponse handleMiniStatement(UssdSession session, String input) {
        // 1. Get last 5 transactions
        // 2. Format as USSD response
    }
    
    public UssdResponse handleLoanApplication(UssdSession session, String input) {
        // State machine for loan application flow:
        // Select Product -> Enter Amount -> Enter Term -> Confirm -> Submit
    }
    
    public UssdResponse handleMpesaDeposit(UssdSession session, String input) {
        // 1. Collect amount
        // 2. Trigger STK Push
        // 3. Wait for callback
        // 4. Confirm transaction
    }
    
    public UssdResponse handlePinVerification(UssdSession session, String input) {
        // Verify PIN before sensitive operations
    }
}
```

### USSD Session Management

**Session Storage (Redis):**
```java
// UssdSession.java - Created ✅
@Data
public class UssdSession implements Serializable {
    private String sessionId;
    private String phoneNumber;
    private String memberId;
    private UssdMenuState currentState;
    private String lastInput;
    private Map<String, Object> sessionData; // Store temp data
    
    // Methods for data management
    public void storeData(String key, Object value);
    public Object getData(String key);
}
```

**Session States (Enum):**
```java
// UssdMenuState.java - Created ✅
public enum UssdMenuState {
    MAIN_MENU,
    BALANCE_INQUIRY,
    MINI_STATEMENT,
    LOAN_APPLICATION,
    LOAN_PRODUCTS,
    LOAN_AMOUNT_INPUT,
    LOAN_TERM_INPUT,
    DEPOSIT_MENU,
    DEPOSIT_AMOUNT_INPUT,
    MPESA_DEPOSIT,
    PIN_VERIFICATION,
    CHANGE_PIN_MENU,
    NEW_PIN_INPUT,
    CONFIRM_PIN_INPUT
}
```

### USSD Response Format

```java
// UssdResponse.java
@Data
public class UssdResponse {
    private String message;
    private boolean isEnd; // CON for continue, END for terminal
    
    public static UssdResponse cont(String message) {
        UssdResponse response = new UssdResponse();
        response.setMessage("CON " + message);
        response.setEnd(false);
        return response;
    }
    
    public static UssdResponse end(String message) {
        UssdResponse response = new UssdResponse();
        response.setMessage("END " + message);
        response.setEnd(true);
        return response;
    }
}
```

---

## Integration Requirements

### 1. M-Pesa Integration

**Daraja API Endpoints:**
```java
@Service
public class MpesaService {
    
    // STK Push for deposits
    public MpesaResponse initiateSTKPush(String phoneNumber, BigDecimal amount, String reference) {
        // Call Safaricom Daraja API
        // Store transaction for callback matching
    }
    
    // Callback handler
    public void handleSTKCallback(MpesaCallback callback) {
        // Match transaction
        // Update account balance
        // Send confirmation SMS
    }
    
    // B2C for withdrawals
    public MpesaResponse initiateB2C(String phoneNumber, BigDecimal amount, String reference) {
        // Disburse funds to member
    }
    
    // Query transaction status
    public TransactionStatus queryTransaction(String checkoutRequestId) {
        // Check transaction status
    }
}
```

### 2. SMS Integration (Africa's Talking)

```java
@Service
public class SmsService {
    
    public void sendOTP(String phoneNumber, String otp) {
        // Send OTP via SMS
    }
    
    public void sendTransactionAlert(String phoneNumber, TransactionDto transaction) {
        // Send transaction notification
    }
    
    public void sendLoanApprovalNotification(String phoneNumber, LoanDetailDto loan) {
        // Loan approval SMS
    }
    
    public void sendBalanceAlert(String phoneNumber, BigDecimal balance) {
        // Balance inquiry response
    }
}
```

---

## Security Implementation

### JWT Token Configuration

```java
@Configuration
public class JwtConfig {
    private String secret = "your-secret-key-minimum-256-bits";
    private long accessTokenExpiration = 3600000; // 1 hour
    private long refreshTokenExpiration = 604800000; // 7 days
}

@Component
public class JwtTokenProvider {
    
    public String generateAccessToken(String memberId, List<String> roles) {
        return Jwts.builder()
            .setSubject(memberId)
            .claim("roles", roles)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public String generateRefreshToken(String memberId) {
        return Jwts.builder()
            .setSubject(memberId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getMemberIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
}
```

### PIN Encryption

```java
@Service
public class PinEncryptionService {
    
    public String hashPin(String pin) {
        return BCrypt.hashpw(pin, BCrypt.gensalt(12));
    }
    
    public boolean verifyPin(String pin, String hashedPin) {
        return BCrypt.checkpw(pin, hashedPin);
    }
}
```

---

## Error Handling

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }
    
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INSUFFICIENT_BALANCE", ex.getMessage()));
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("UNAUTHORIZED", ex.getMessage()));
    }
}

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String errorCode;
    private String message;
    private String timestamp = LocalDateTime.now().toString();
}
```

---

## Redis Configuration

```java
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues();
        
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}
```

---

## Testing

### Unit Tests Example

```java
@SpringBootTest
class MobileLoanServiceTest {
    
    @Autowired
    private MobileLoanService loanService;
    
    @MockBean
    private LoanAccountRepo loanRepo;
    
    @Test
    void testGetMemberLoans() {
        // Given
        String memberId = "12345";
        List<LoanAccount> mockLoans = createMockLoans();
        when(loanRepo.findByCustomerId(memberId)).thenReturn(mockLoans);
        
        // When
        List<LoanSummaryDto> result = loanService.getMemberLoans(memberId);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
```

### API Test Collection (Postman)

Create collections for:
1. **Mobile Auth** - Login, Register, OTP, PIN management
2. **Mobile Accounts** - Balance, Statement, Transactions
3. **Mobile Loans** - Application, Repayment, History
4. **USSD** - Session flow testing

---

## Deployment

### Docker Compose

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: sacco_db
      POSTGRES_USER: sacco_user
      POSTGRES_PASSWORD: secure_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/sacco_db
      SPRING_REDIS_HOST: redis
      MPESA_CONSUMER_KEY: ${MPESA_CONSUMER_KEY}
      MPESA_CONSUMER_SECRET: ${MPESA_CONSUMER_SECRET}
      AFRICASTALKING_API_KEY: ${AFRICASTALKING_API_KEY}
    depends_on:
      - postgres
      - redis

volumes:
  postgres_data:
  redis_data:
```

---

## Next Steps

1. ✅ Create all DTO classes
2. ✅ Implement service layer logic
3. ⬜ Add comprehensive validation
4. ⬜ Implement caching
5. ⬜ Add pagination to all list endpoints
6. ⬜ Complete M-Pesa integration
7. ⬜ Complete SMS integration
8. ⬜ Write unit tests
9. ⬜ Write integration tests
10. ⬜ Load testing
11. ⬜ Security audit
12. ⬜ Documentation completion

---

## API Documentation (Swagger)

Access at: `http://localhost:8080/swagger-ui.html`

All endpoints documented with:
- Request/Response schemas
- Authentication requirements
- Example payloads
- Error responses
