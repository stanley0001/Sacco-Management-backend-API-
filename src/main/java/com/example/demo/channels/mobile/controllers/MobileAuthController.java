package com.example.demo.channels.mobile.controllers;

import com.example.demo.channels.mobile.dto.*;
import com.example.demo.channels.mobile.services.MobileAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mobile Authentication", description = "Mobile app authentication endpoints")
public class MobileAuthController {

    private final MobileAuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Mobile user login", description = "Authenticate mobile user with phone and PIN")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Mobile login attempt for phone: {}", request.getPhoneNumber());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register new mobile user", description = "Register new member for mobile banking")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Mobile registration attempt for: {}", request.getPhoneNumber());
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify OTP sent to phone number")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        log.info("OTP verification for: {}", request.getPhoneNumber());
        authService.verifyOtp(request);
        return ResponseEntity.ok(new ApiResponse(true, "OTP verified successfully"));
    }

    @PostMapping("/forgot-pin")
    @Operation(summary = "Forgot PIN", description = "Request PIN reset via OTP")
    public ResponseEntity<ApiResponse> forgotPin(@Valid @RequestBody ForgotPinRequest request) {
        log.info("Forgot PIN request for: {}", request.getPhoneNumber());
        authService.forgotPin(request);
        return ResponseEntity.ok(new ApiResponse(true, "OTP sent to your phone"));
    }

    @PostMapping("/reset-pin")
    @Operation(summary = "Reset PIN", description = "Reset PIN using OTP")
    public ResponseEntity<ApiResponse> resetPin(@Valid @RequestBody ResetPinRequest request) {
        log.info("Reset PIN for: {}", request.getPhoneNumber());
        authService.resetPin(request);
        return ResponseEntity.ok(new ApiResponse(true, "PIN reset successfully"));
    }

    @PostMapping("/change-pin")
    @Operation(summary = "Change PIN", description = "Change PIN for authenticated user")
    public ResponseEntity<ApiResponse> changePin(
            @Valid @RequestBody ChangePinRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("Change PIN request");
        authService.changePin(request, token);
        return ResponseEntity.ok(new ApiResponse(true, "PIN changed successfully"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout and invalidate tokens")
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout request");
        authService.logout(token);
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }
}
