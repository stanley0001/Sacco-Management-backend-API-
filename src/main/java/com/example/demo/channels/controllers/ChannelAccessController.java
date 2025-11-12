package com.example.demo.channels.controllers;

import com.example.demo.erp.customerManagement.parsistence.entities.Customer;
import com.example.demo.erp.customerManagement.services.CustomerS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for managing customer channel access (Web, Mobile, USSD)
 */
@RestController
@RequestMapping("/api/channels")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
@Tag(name = "Channel Access Management", description = "Manage customer access to different channels")
@Slf4j
public class ChannelAccessController {

    private final CustomerS customerService;
    private final PasswordEncoder passwordEncoder;

    public ChannelAccessController(CustomerS customerService, PasswordEncoder passwordEncoder) {
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get channel access status for a customer
     */
    @GetMapping("/customer/{customerId}/status")
    @Operation(summary = "Get customer channel access status")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ', 'CHANNEL_MANAGE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> getChannelStatus(@PathVariable Long customerId) {
        try {
            Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found"));
            }

            Customer customer = customerOpt.get();
            
            Map<String, Object> channelStatus = new HashMap<>();
            
            // Web Channel
            Map<String, Object> webChannel = new HashMap<>();
            webChannel.put("enabled", customer.getWebChannelEnabled() != null && customer.getWebChannelEnabled());
            webChannel.put("login", customer.getWebLogin() != null ? customer.getWebLogin() : "");
            webChannel.put("lastLogin", customer.getWebLastLogin() != null ? customer.getWebLastLogin().toString() : "");
            webChannel.put("failedAttempts", customer.getWebFailedAttempts() != null ? customer.getWebFailedAttempts() : 0);
            channelStatus.put("web", webChannel);
            
            // Mobile Channel
            Map<String, Object> mobileChannel = new HashMap<>();
            mobileChannel.put("enabled", customer.getMobileChannelEnabled() != null && customer.getMobileChannelEnabled());
            mobileChannel.put("login", customer.getMobileLogin() != null ? customer.getMobileLogin() : "");
            mobileChannel.put("lastLogin", customer.getMobileLastLogin() != null ? customer.getMobileLastLogin().toString() : "");
            mobileChannel.put("failedAttempts", customer.getMobileFailedAttempts() != null ? customer.getMobileFailedAttempts() : 0);
            channelStatus.put("mobile", mobileChannel);
            
            // USSD Channel
            Map<String, Object> ussdChannel = new HashMap<>();
            ussdChannel.put("enabled", customer.getUssdChannelEnabled() != null && customer.getUssdChannelEnabled());
            ussdChannel.put("login", customer.getUssdLogin() != null ? customer.getUssdLogin() : "");
            ussdChannel.put("lastLogin", customer.getUssdLastLogin() != null ? customer.getUssdLastLogin().toString() : "");
            ussdChannel.put("failedAttempts", customer.getUssdFailedAttempts() != null ? customer.getUssdFailedAttempts() : 0);
            channelStatus.put("ussd", ussdChannel);

            return ResponseEntity.ok(Map.of(
                    "customerId", customerId,
                    "memberNumber", customer.getMemberNumber() != null ? customer.getMemberNumber() : "",
                    "channels", channelStatus
            ));
        } catch (Exception e) {
            log.error("Error fetching channel status for customer {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch channel status"));
        }
    }

    /**
     * Enable or disable a specific channel for a customer
     */
    @PostMapping("/customer/{customerId}/toggle")
    @Operation(summary = "Enable or disable a channel for customer")
    @PreAuthorize("hasAnyAuthority('CHANNEL_MANAGE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> toggleChannel(
            @PathVariable Long customerId,
            @RequestBody Map<String, Object> request
    ) {
        try {
            String channel = (String) request.get("channel");
            Boolean enable = (Boolean) request.get("enable");
            String pin = (String) request.get("pin");

            if (channel == null || enable == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "channel and enable fields are required"));
            }

            Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found"));
            }

            Customer customer = customerOpt.get();
            
            // Generate PIN if enabling and no PIN provided
            if (enable && pin == null) {
                pin = generateTemporaryPin();
            }

            String hashedPin = pin != null ? passwordEncoder.encode(pin) : null;

            switch (channel.toLowerCase()) {
                case "web":
                    customer.setWebChannelEnabled(enable);
                    if (enable) {
                        customer.setWebLogin(customer.getEmail() != null ? customer.getEmail() : customer.getMemberNumber());
                        customer.setWebPinHash(hashedPin);
                        customer.setWebFailedAttempts(0);
                    }
                    break;
                    
                case "mobile":
                    customer.setMobileChannelEnabled(enable);
                    if (enable) {
                        customer.setMobileLogin(customer.getPhoneNumber());
                        customer.setMobilePinHash(hashedPin);
                        customer.setMobileFailedAttempts(0);
                    }
                    break;
                    
                case "ussd":
                    customer.setUssdChannelEnabled(enable);
                    if (enable) {
                        customer.setUssdLogin(customer.getPhoneNumber());
                        customer.setUssdPinHash(hashedPin);
                        customer.setUssdFailedAttempts(0);
                    }
                    break;
                    
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid channel. Must be: web, mobile, or ussd"));
            }

            customerService.update(customer);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", channel + " channel " + (enable ? "enabled" : "disabled") + " successfully");
            response.put("channel", channel);
            response.put("enabled", enable);
            
            if (enable && pin != null && request.get("pin") == null) {
                response.put("temporaryPin", pin);
                response.put("note", "Temporary PIN generated. Customer should change it on first login.");
            }

            log.info("Channel {} {} for customer {}", channel, enable ? "enabled" : "disabled", customerId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling channel for customer {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update channel access"));
        }
    }

    /**
     * Update PIN for a specific channel
     */
    @PutMapping("/customer/{customerId}/pin")
    @Operation(summary = "Update channel PIN for customer")
    @PreAuthorize("hasAnyAuthority('CHANNEL_MANAGE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> updateChannelPin(
            @PathVariable Long customerId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String channel = request.get("channel");
            String newPin = request.get("newPin");

            if (channel == null || newPin == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "channel and newPin are required"));
            }

            if (newPin.length() < 4) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "PIN must be at least 4 characters"));
            }

            Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found"));
            }

            Customer customer = customerOpt.get();
            String hashedPin = passwordEncoder.encode(newPin);

            switch (channel.toLowerCase()) {
                case "web":
                    if (customer.getWebChannelEnabled() != null && customer.getWebChannelEnabled()) {
                        customer.setWebPinHash(hashedPin);
                        customer.setWebFailedAttempts(0);
                    } else {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Web channel is not enabled"));
                    }
                    break;
                    
                case "mobile":
                    if (customer.getMobileChannelEnabled() != null && customer.getMobileChannelEnabled()) {
                        customer.setMobilePinHash(hashedPin);
                        customer.setMobileFailedAttempts(0);
                    } else {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Mobile channel is not enabled"));
                    }
                    break;
                    
                case "ussd":
                    if (customer.getUssdChannelEnabled() != null && customer.getUssdChannelEnabled()) {
                        customer.setUssdPinHash(hashedPin);
                        customer.setUssdFailedAttempts(0);
                    } else {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "USSD channel is not enabled"));
                    }
                    break;
                    
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid channel. Must be: web, mobile, or ussd"));
            }

            customerService.update(customer);

            log.info("PIN updated for channel {} for customer {}", channel, customerId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "PIN updated successfully for " + channel + " channel"
            ));
        } catch (Exception e) {
            log.error("Error updating PIN for customer {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update PIN"));
        }
    }

    /**
     * Reset failed attempts for a channel
     */
    @PostMapping("/customer/{customerId}/reset-attempts")
    @Operation(summary = "Reset failed login attempts for a channel")
    @PreAuthorize("hasAnyAuthority('CHANNEL_MANAGE', 'ADMIN_ACCESS')")
    public ResponseEntity<?> resetFailedAttempts(
            @PathVariable Long customerId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String channel = request.get("channel");

            if (channel == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "channel is required"));
            }

            Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
            if (customerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Customer not found"));
            }

            Customer customer = customerOpt.get();

            switch (channel.toLowerCase()) {
                case "web":
                    customer.setWebFailedAttempts(0);
                    break;
                case "mobile":
                    customer.setMobileFailedAttempts(0);
                    break;
                case "ussd":
                    customer.setUssdFailedAttempts(0);
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid channel. Must be: web, mobile, or ussd"));
            }

            customerService.update(customer);

            log.info("Failed attempts reset for channel {} for customer {}", channel, customerId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Failed attempts reset for " + channel + " channel"
            ));
        } catch (Exception e) {
            log.error("Error resetting failed attempts for customer {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reset attempts"));
        }
    }

    /**
     * Generate a temporary 6-digit PIN
     */
    private String generateTemporaryPin() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
