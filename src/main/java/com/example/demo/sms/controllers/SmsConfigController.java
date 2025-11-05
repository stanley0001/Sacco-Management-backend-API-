package com.example.demo.sms.controllers;

import com.example.demo.sms.dto.SmsConfigDTO;
import com.example.demo.sms.dto.SmsSendRequest;
import com.example.demo.sms.dto.SmsSendResponse;
import com.example.demo.sms.entities.SmsConfig;
import com.example.demo.sms.services.SmsConfigService;
import com.example.demo.sms.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sms/config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SMS Configuration", description = "Manage SMS provider configurations")
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class SmsConfigController {

    private final SmsConfigService smsConfigService;
    private final SmsService smsService;

    @GetMapping
    @Operation(summary = "Get all SMS configurations")
    @PreAuthorize("hasAnyAuthority('canViewBps', 'ADMIN_ACCESS')")
    public ResponseEntity<List<SmsConfigDTO>> getAllConfigurations(
        @RequestParam(name = "maskSensitive", defaultValue = "true") boolean maskSensitive
    ) {
        try {
            return ResponseEntity.ok(smsConfigService.getAllConfigurations(maskSensitive));
        } catch (Exception e) {
            log.error("Error fetching SMS configurations", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get SMS configuration by ID")
    @PreAuthorize("hasAnyAuthority('canViewBps', 'ADMIN_ACCESS')")
    public ResponseEntity<SmsConfigDTO> getConfigurationById(
        @PathVariable @Parameter(description = "Configuration ID") Long id,
        @RequestParam(name = "maskSensitive", defaultValue = "true") boolean maskSensitive
    ) {
        try {
            return ResponseEntity.ok(smsConfigService.getConfigurationById(id, maskSensitive));
        } catch (Exception e) {
            log.error("Error fetching SMS configuration", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create new SMS configuration")
    @PreAuthorize("hasAnyAuthority('canManageBps', 'ADMIN_ACCESS')")
    public ResponseEntity<SmsConfigDTO> createConfiguration(
        @RequestBody @Parameter(description = "Configuration details") SmsConfigDTO dto,
        Authentication authentication
    ) {
        try {
            String username = authentication != null ? authentication.getName() : "system";
            return ResponseEntity.ok(smsConfigService.createConfiguration(dto, username));
        } catch (Exception e) {
            log.error("Error creating SMS configuration", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update SMS configuration")
    @PreAuthorize("hasAnyAuthority('canManageBps', 'ADMIN_ACCESS')")
    public ResponseEntity<SmsConfigDTO> updateConfiguration(
        @PathVariable @Parameter(description = "Configuration ID") Long id,
        @RequestBody @Parameter(description = "Updated configuration") SmsConfigDTO dto,
        Authentication authentication
    ) {
        try {
            String username = authentication != null ? authentication.getName() : "system";
            return ResponseEntity.ok(smsConfigService.updateConfiguration(id, dto, username));
        } catch (Exception e) {
            log.error("Error updating SMS configuration", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete SMS configuration")
    @PreAuthorize("hasAnyAuthority('canViewBps', 'ADMIN_ACCESS')")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable Long id) {
        try {
            smsConfigService.deleteConfiguration(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting SMS configuration", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get SMS history")
    @PreAuthorize("hasAnyAuthority('canViewCommunication', 'canViewBps', 'ADMIN_ACCESS')")
    public ResponseEntity<?> getSmsHistory(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        try {
            // Get real SMS history from the service
            List<Map<String, Object>> history = smsService.getSmsHistory(page, size);
            
            log.info("Retrieved {} SMS history records (page: {}, size: {})", history.size(), page, size);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching SMS history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle configuration active status")
    @PreAuthorize("hasAnyAuthority('canManageBps', 'ADMIN_ACCESS')")
    public ResponseEntity<SmsConfigDTO> toggleActiveStatus(
        @PathVariable @Parameter(description = "Configuration ID") Long id
    ) {
        try {
            return ResponseEntity.ok(smsConfigService.toggleActiveStatus(id));
        } catch (Exception e) {
            log.error("Error toggling SMS configuration status", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/set-default")
    @Operation(summary = "Set configuration as default")
    @PreAuthorize("hasAnyAuthority('canManageBps', 'ADMIN_ACCESS')")
    public ResponseEntity<SmsConfigDTO> setAsDefault(
        @PathVariable @Parameter(description = "Configuration ID") Long id
    ) {
        try {
            return ResponseEntity.ok(smsConfigService.setAsDefault(id));
        } catch (Exception e) {
            log.error("Error setting SMS configuration as default", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/send")
    @Operation(summary = "Send SMS message")
    @PreAuthorize("hasAnyAuthority('canViewBps', 'ADMIN_ACCESS')")
    public ResponseEntity<?> sendSms(
        @RequestBody SmsSendRequest request,
        Authentication authentication
    ) {
        try {
            String phoneNumber = request.getEffectivePhoneNumber();
            String message = request.getEffectiveMessage();
            
            log.info("Sending SMS - Original phoneNumber: '{}', recipient: '{}', effective: '{}', message: '{}'", 
                request.getPhoneNumber(), request.getRecipient(), phoneNumber, message);
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseEntity.ok(SmsSendResponse.builder()
                    .success(false)
                    .message("Phone number is required")
                    .errorCode("MISSING_PHONE")
                    .build());
            }
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.ok(SmsSendResponse.builder()
                    .success(false)
                    .message("Message is required")
                    .errorCode("MISSING_MESSAGE")
                    .build());
            }
            
            // Use the improved SMS service method
            SmsSendResponse response = smsService.sendSms(request);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending SMS", e);
            return ResponseEntity.ok(SmsSendResponse.builder()
                .success(false)
                .message("Failed to send SMS: " + e.getMessage())
                .errorCode("SMS_SEND_ERROR")
                .build());
        }
    }

    @PostMapping("/bulk-send")
    @Operation(summary = "Send bulk SMS messages")
    @PreAuthorize("hasAnyAuthority('canViewBps', 'ADMIN_ACCESS')")
    public ResponseEntity<?> sendBulkSms(
        @RequestBody Map<String, Object> request,
        Authentication authentication
    ) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> messages = 
                (List<Map<String, Object>>) request.get("messages");
            
            if (messages == null || messages.isEmpty()) {
                return ResponseEntity.ok(SmsSendResponse.builder()
                    .success(false)
                    .message("No messages provided")
                    .build());
            }
            
            log.info("Sending bulk SMS - {} messages", messages.size());
            
            int sent = 0;
            int failed = 0;
            
            for (Map<String, Object> message : messages) {
                try {
                    String phoneNumber = (String) message.get("phoneNumber");
                    String messageText = (String) message.get("message");
                    
                    if (phoneNumber != null && messageText != null) {
                        smsService.sendSms(phoneNumber, messageText);
                        sent++;
                    } else {
                        failed++;
                    }
                } catch (Exception e) {
                    log.error("Error sending SMS to {}", message.get("phoneNumber"), e);
                    failed++;
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Bulk SMS completed. Sent: %d, Failed: %d", sent, failed),
                "sent", sent,
                "failed", failed,
                "total", messages.size()
            ));
        } catch (Exception e) {
            log.error("Error sending bulk SMS", e);
            return ResponseEntity.ok(SmsSendResponse.builder()
                .success(false)
                .message("Failed to send bulk SMS: " + e.getMessage())
                .build());
        }
    }

    /**
     * Simple SMS sending endpoint for frontend use
     */
    @PostMapping("/send-simple")
    @Operation(summary = "Send SMS message - Simple endpoint")
    public ResponseEntity<?> sendSimpleSms(
        @RequestBody Map<String, Object> request,
        Authentication authentication
    ) {
        try {
            String phoneNumber = (String) request.get("phoneNumber");
            String recipient = (String) request.get("recipient");
            String message = (String) request.get("message");
            
            // Use phoneNumber if available, otherwise use recipient
            String effectivePhone = phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber : recipient;
            
            log.info("Simple SMS send - phoneNumber: '{}', recipient: '{}', effective: '{}', message: '{}'", 
                phoneNumber, recipient, effectivePhone, message);
            
            if (effectivePhone == null || effectivePhone.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Phone number is required"
                ));
            }
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Message is required"
                ));
            }
            
            // Create SMS request
            SmsSendRequest smsRequest = SmsSendRequest.builder()
                .phoneNumber(effectivePhone)
                .message(message)
                .build();
            
            SmsSendResponse response = smsService.sendSms(smsRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", response.isSuccess(),
                "message", response.getMessage(),
                "errorCode", response.getErrorCode() != null ? response.getErrorCode() : ""
            ));
        } catch (Exception e) {
            log.error("Error sending simple SMS", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Failed to send SMS: " + e.getMessage()
            ));
        }
    }
}
