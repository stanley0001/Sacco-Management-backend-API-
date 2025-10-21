package com.example.demo.mobile.services;

import com.example.demo.customerManagement.parsistence.entities.Customer;
import com.example.demo.customerManagement.parsistence.repositories.CustomerRepository;
import com.example.demo.mobile.dto.*;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileAuthService {

    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for phone: {}", request.getPhoneNumber());
        
        // Find customer by phone
        Customer customer = customerRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("Invalid phone number or PIN"));
        
        // Verify PIN
        if (!verifyPin(request.getPin(), customer.getPinHash())) {
            customer.setFailedPinAttempts(customer.getFailedPinAttempts() + 1);
            customerRepository.save(customer);
            
            if (customer.getFailedPinAttempts() >= 3) {
                throw new RuntimeException("Account locked due to multiple failed attempts");
            }
            throw new RuntimeException("Invalid phone number or PIN");
        }
        
        // Reset failed attempts on successful login
        customer.setFailedPinAttempts(0);
        customer.setLastLogin(LocalDateTime.now());
        customerRepository.save(customer);
        
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(customer.getId().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(customer.getId().toString());
        
        // Build response
        MemberDto memberDto = buildMemberDto(customer);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L) // 1 hour
                .member(memberDto)
                .permissions(Arrays.asList("MOBILE_ACCESS"))
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for phone: {}", request.getPhoneNumber());
        
        // Check if phone exists
        if (customerRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("Phone number already registered");
        }
        
        // Check if ID number exists
        if (customerRepository.findByDocumentNumber(request.getIdNumber()).isPresent()) {
            throw new RuntimeException("ID number already registered");
        }
        
        // Create new customer
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setDocumentNumber(request.getIdNumber());
        customer.setPinHash(hashPin(request.getPin()));
        customer.setStatus("PENDING_VERIFICATION");
        customer.setFailedPinAttempts(0);
        customer.setCreatedAt(LocalDateTime.now());
        
        customer = customerRepository.save(customer);
        
        // Send OTP for verification
        otpService.generateAndSendOtp(request.getPhoneNumber(), "REGISTRATION");
        
        // Build response
        MemberDto memberDto = buildMemberDto(customer);
        
        return AuthResponse.builder()
                .accessToken(null) // No token until verified
                .refreshToken(null)
                .expiresIn(0L)
                .member(memberDto)
                .permissions(Arrays.asList())
                .build();
    }

    @Transactional
    public void verifyOtp(OtpVerificationRequest request) {
        log.info("Verifying OTP for phone: {}", request.getPhoneNumber());
        
        boolean verified = otpService.verifyOtp(request.getPhoneNumber(), request.getOtp());
        
        if (!verified) {
            throw new RuntimeException("Invalid or expired OTP");
        }
        
        // Update customer status if registration verification
        if ("REGISTRATION".equals(request.getVerificationType())) {
            Customer customer = customerRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            customer.setStatus("ACTIVE");
            customerRepository.save(customer);
        }
    }

    @Transactional
    public void forgotPin(ForgotPinRequest request) {
        log.info("Processing forgot PIN for: {}", request.getPhoneNumber());
        
        Customer customer = customerRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("Phone number not found"));
        
        // Send OTP
        otpService.generateAndSendOtp(request.getPhoneNumber(), "PIN_RESET");
    }

    @Transactional
    public void resetPin(ResetPinRequest request) {
        log.info("Resetting PIN for: {}", request.getPhoneNumber());
        
        // Verify OTP
        boolean verified = otpService.verifyOtp(request.getPhoneNumber(), request.getOtp());
        if (!verified) {
            throw new RuntimeException("Invalid or expired OTP");
        }
        
        // Update PIN
        Customer customer = customerRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        customer.setPinHash(hashPin(request.getNewPin()));
        customer.setFailedPinAttempts(0);
        customerRepository.save(customer);
    }

    @Transactional
    public void changePin(ChangePinRequest request, String token) {
        log.info("Processing change PIN request");
        
        String memberId = jwtTokenProvider.getMemberIdFromToken(token.replace("Bearer ", ""));
        
        Customer customer = customerRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Verify current PIN
        if (!verifyPin(request.getCurrentPin(), customer.getPinHash())) {
            throw new RuntimeException("Current PIN is incorrect");
        }
        
        // Update to new PIN
        customer.setPinHash(hashPin(request.getNewPin()));
        customerRepository.save(customer);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Processing token refresh");
        
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        String memberId = jwtTokenProvider.getMemberIdFromToken(request.getRefreshToken());
        Customer customer = customerRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        String newAccessToken = jwtTokenProvider.generateAccessToken(memberId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(3600L)
                .member(buildMemberDto(customer))
                .permissions(Arrays.asList("MOBILE_ACCESS"))
                .build();
    }

    public void logout(String token) {
        log.info("Processing logout");
        // In production, add token to blacklist in Redis
    }

    private String hashPin(String pin) {
        return BCrypt.hashpw(pin, BCrypt.gensalt(12));
    }

    private boolean verifyPin(String pin, String hashedPin) {
        if (hashedPin == null) return false;
        return BCrypt.checkpw(pin, hashedPin);
    }

    private MemberDto buildMemberDto(Customer customer) {
        return MemberDto.builder()
                .memberId(customer.getId().toString())
                .memberNumber(customer.getMemberNumber())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .idNumber(customer.getDocumentNumber())
                .status(customer.getStatus())
                .build();
    }
}
