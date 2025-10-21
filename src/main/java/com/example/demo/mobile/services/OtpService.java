package com.example.demo.mobile.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_EXPIRY_MINUTES = 5;

    public void generateAndSendOtp(String phoneNumber, String type) {
        String otp = generateOTP();
        String key = OTP_PREFIX + phoneNumber + ":" + type;
        
        // Store OTP in Redis
        redisTemplate.opsForValue().set(key, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        
        // Send SMS (implementation depends on SMS provider)
        sendOtpSms(phoneNumber, otp);
        
        log.info("OTP generated and sent to: {}", phoneNumber);
    }

    public boolean verifyOtp(String phoneNumber, String otp) {
        String key = OTP_PREFIX + phoneNumber + ":*";
        
        // In production, search for the specific type
        String storedOtp = (String) redisTemplate.opsForValue().get(OTP_PREFIX + phoneNumber + ":REGISTRATION");
        if (storedOtp == null) {
            storedOtp = (String) redisTemplate.opsForValue().get(OTP_PREFIX + phoneNumber + ":PIN_RESET");
        }
        
        if (storedOtp != null && storedOtp.equals(otp)) {
            // Delete OTP after successful verification
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }

    private String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    private void sendOtpSms(String phoneNumber, String otp) {
        // Integration with SMS provider (Africa's Talking, Twilio, etc.)
        log.info("Sending OTP {} to {}", otp, phoneNumber);
        // smsService.send(phoneNumber, "Your OTP is: " + otp);
    }
}
