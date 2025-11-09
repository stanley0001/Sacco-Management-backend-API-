package com.example.demo.channels.ussd.controllers;

import com.example.demo.channels.ussd.dto.UssdRequest;
import com.example.demo.channels.ussd.dto.UssdResponse;
import com.example.demo.channels.ussd.services.UssdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ussd")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "USSD", description = "USSD callback endpoints")
public class UssdController {

    private final UssdService ussdService;

    @PostMapping(value = "/callback", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "USSD callback", description = "Main USSD callback endpoint for Africa's Talking")
    public ResponseEntity<String> handleUssdCallback(@RequestBody UssdRequest request) {
        log.info("USSD request from: {} sessionId: {} text: {}", 
                request.getPhoneNumber(), request.getSessionId(), request.getText());
        
        UssdResponse response = ussdService.handleUssdRequest(request);
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(response.getMessage());
    }

    @PostMapping(value = "/callback/safaricom", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Safaricom USSD callback", description = "USSD callback for Safaricom format")
    public ResponseEntity<UssdResponse> handleSafaricomUssdCallback(@RequestBody UssdRequest request) {
        log.info("Safaricom USSD request from: {} sessionId: {}", 
                request.getPhoneNumber(), request.getSessionId());
        
        UssdResponse response = ussdService.handleUssdRequest(request);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    @Operation(summary = "Test USSD", description = "Test USSD functionality")
    public ResponseEntity<String> testUssd(
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String input) {
        log.info("USSD test for: {} input: {}", phoneNumber, input);
        
        UssdRequest testRequest = new UssdRequest();
        testRequest.setPhoneNumber(phoneNumber);
        testRequest.setSessionId("test-session-" + System.currentTimeMillis());
        testRequest.setText(input != null ? input : "");
        
        UssdResponse response = ussdService.handleUssdRequest(testRequest);
        
        return ResponseEntity.ok(response.getMessage());
    }
}
